package services.community.custom;

import l2.commons.dbutils.DbUtils;
import l2.gameserver.Config;
import l2.gameserver.cache.Msg;
import l2.gameserver.data.htm.HtmCache;
import l2.gameserver.database.DatabaseFactory;
import l2.gameserver.handler.bbs.CommunityBoardManager;
import l2.gameserver.handler.bbs.ICommunityBoardHandler;
import l2.gameserver.instancemanager.ReflectionManager;
import l2.gameserver.model.Player;
import l2.gameserver.model.Zone;
import l2.gameserver.network.l2.s2c.ShowBoard;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.scripts.ScriptFile;
import l2.gameserver.utils.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.StringTokenizer;

public class CommunityTeleport implements ScriptFile, ICommunityBoardHandler
{
	public static final Zone.ZoneType[] FORBIDDEN_ZONES = {Zone.ZoneType.RESIDENCE, Zone.ZoneType.ssq_zone, Zone.ZoneType.battle_zone, Zone.ZoneType.SIEGE, Zone.ZoneType.no_restart, Zone.ZoneType.no_summon};
	private static final Logger _log = LoggerFactory.getLogger(CommunityTeleport.class);
	
	public static boolean checkFirstConditions(Player player)
	{
		if(player == null)
		{
			return false;
		}
		if(player.getActiveWeaponFlagAttachment() != null)
		{
			player.sendPacket(Msg.YOU_CANNOT_TELEPORT_WHILE_IN_POSSESSION_OF_A_WARD);
			return false;
		}
		if(player.isOlyParticipant())
		{
			player.sendPacket(Msg.YOU_CANNOT_USE_MY_TELEPORTS_WHILE_PARTICIPATING_IN_AN_OLYMPIAD_MATCH);
			return false;
		}
		if(player.getReflection() != ReflectionManager.DEFAULT)
		{
			player.sendPacket(Msg.YOU_CANNOT_USE_MY_TELEPORTS_IN_AN_INSTANT_ZONE);
			return false;
		}
		if(player.isInDuel())
		{
			player.sendPacket(Msg.YOU_CANNOT_USE_MY_TELEPORTS_DURING_A_DUEL);
			return false;
		}
		if(player.isInCombat() || player.getPvpFlag() != 0)
		{
			player.sendPacket(Msg.YOU_CANNOT_USE_MY_TELEPORTS_DURING_A_BATTLE);
			return false;
		}
		if(player.isOnSiegeField() || player.isInZoneBattle())
		{
			player.sendPacket(Msg.YOU_CANNOT_USE_MY_TELEPORTS_WHILE_PARTICIPATING_A_LARGE_SCALE_BATTLE_SUCH_AS_A_CASTLE_SIEGE);
			return false;
		}
		if(player.isFlying())
		{
			player.sendPacket(Msg.YOU_CANNOT_USE_MY_TELEPORTS_WHILE_FLYING);
			return false;
		}
		if(player.isInWater() || player.isInBoat())
		{
			player.sendPacket(Msg.YOU_CANNOT_USE_MY_TELEPORTS_UNDERWATER);
			return false;
		}
		return true;
	}
	
	public static boolean checkTeleportConditions(Player player)
	{
		if(player == null)
		{
			return false;
		}
		if(player.isAlikeDead())
		{
			player.sendPacket(Msg.YOU_CANNOT_USE_MY_TELEPORTS_WHILE_YOU_ARE_DEAD);
			return false;
		}
		if(player.isInStoreMode() || player.isInTrade())
		{
			player.sendPacket(Msg.YOU_CANNOT_SUMMON_DURING_A_TRADE_OR_WHILE_USING_THE_PRIVATE_SHOPS);
			return false;
		}
		if(player.isInBoat() || player.isParalyzed() || player.isStunned() || player.isSleeping())
		{
			player.sendPacket(Msg.YOU_CANNOT_USE_MY_TELEPORTS_WHILE_YOU_ARE_IN_A_FLINT_OR_PARALYZED_STATE);
			return false;
		}
		return true;
	}
	
	public static boolean checkTeleportLocation(Player player, Location loc)
	{
		return checkTeleportLocation(player, loc.x, loc.y, loc.z);
	}
	
	public static boolean checkTeleportLocation(Player player, int x, int y, int z)
	{
		if(player == null)
		{
			return false;
		}
		for(Zone.ZoneType zoneType : FORBIDDEN_ZONES)
		{
			Zone zone = player.getZone(zoneType);
			if(zone == null)
				continue;
			player.sendPacket(Msg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM);
			return false;
		}
		return true;
	}
	
	@Override
	public void onLoad()
	{
		if(Config.COMMUNITYBOARD_ENABLED && ACbConfigManager.ALLOW_PVPCB_TELEPORT)
		{
			_log.info("CommunityBoard: CommunityTeleport loaded.");
			CommunityBoardManager.getInstance().registerHandler(this);
		}
	}
	
	@Override
	public void onReload()
	{
		if(Config.COMMUNITYBOARD_ENABLED)
		{
			CommunityBoardManager.getInstance().removeHandler(this);
		}
	}
	
	@Override
	public void onShutdown()
	{
	}
	
	@Override
	public String[] getBypassCommands()
	{
		return new String[] {"_bbsteleport", "_bbsteleport_delete", "_bbsteleport_save", "_bbsteleport_teleport"};
	}
	
	@Override
	public void onBypassCommand(Player player, String command)
	{
		if(!CommunityTools.checkConditions(player))
		{
			String html = HtmCache.getInstance().getNotNull("scripts/services/community/pages/locked.htm", player);
			html = html.replace("%name%", player.getName());
			ShowBoard.separateAndSend(html, player);
			return;
		}
		StringTokenizer stcmd = new StringTokenizer(command, ";");
		String cmd = stcmd.nextToken();
		if(cmd.equals("_bbsteleport"))
		{
			showTp(player);
		}
		else if(cmd.equals("_bbsteleport_delete"))
		{
			int TpNameDell = Integer.parseInt(stcmd.nextToken());
			delTp(player, TpNameDell);
			showTp(player);
		}
		else if(cmd.equals("_bbsteleport_save"))
		{
			String TpNameAdd = stcmd.nextToken();
			AddTp(player, TpNameAdd);
			showTp(player);
		}
		else if(cmd.equals("_bbsteleport_teleport"))
		{
			StringTokenizer stGoTp = new StringTokenizer(stcmd.nextToken(), " ");
			int xTp = Integer.parseInt(stGoTp.nextToken());
			int yTp = Integer.parseInt(stGoTp.nextToken());
			int zTp = Integer.parseInt(stGoTp.nextToken());
			int priceTp = Integer.parseInt(stGoTp.nextToken());
			goTp(player, xTp, yTp, zTp, priceTp);
			showTp(player);
		}
		else
		{
			ShowBoard.separateAndSend("<html><body><br><br><center>Функция: " + command + " пока не реализована</center><br><br></body></html>", player);
		}
	}
	
	private void goTp(Player player, int xTp, int yTp, int zTp, int priceTp)
	{
		if(!checkFirstConditions(player) || !checkTeleportConditions(player))
		{
			return;
		}
		if(!checkTeleportLocation(player, xTp, yTp, zTp))
		{
			return;
		}
		if(priceTp > 0 && player.getAdena() < (long) priceTp)
		{
			player.sendPacket(new SystemMessage(279));
			return;
		}
		if(priceTp > 0)
		{
			player.reduceAdena((long) priceTp, true);
		}
		player.teleToLocation(xTp, yTp, zTp);
	}
	
	private void showTp(Player player)
	{
		Connection con = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement st = con.prepareStatement("SELECT * FROM bbs_comteleport WHERE charId=?;");
			st.setLong(1, player.getObjectId());
			ResultSet rs = st.executeQuery();
			StringBuilder html = new StringBuilder();
			html.append("<table width=220>");
			while(rs.next())
			{
				CBteleport tp = new CBteleport();
				tp.TpId = rs.getInt("TpId");
				tp.TpName = rs.getString("name");
				tp.PlayerId = rs.getInt("charId");
				tp.xC = rs.getInt("xPos");
				tp.yC = rs.getInt("yPos");
				tp.zC = rs.getInt("zPos");
				html.append("<tr>");
				html.append("<td>");
				html.append("<button value=\"" + tp.TpName + "\" action=\"bypass _bbsteleport_teleport;" + tp.xC + " " + tp.yC + " " + tp.zC + " " + ACbConfigManager.ALT_CB_TELE_POINT_PRICE + "\" width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
				html.append("</td>");
				html.append("<td>");
				html.append("<button value=\"Удалить\" action=\"bypass _bbsteleport_delete;" + tp.TpId + "\" width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
				html.append("</td>");
				html.append("</tr>");
			}
			html.append("</table>");
			DbUtils.closeQuietly(st, rs);
			String content = HtmCache.getInstance().getNotNull("scripts/services/community/pages/teleport.htm", player);
			content = content.replace("%tp%", html.toString());
			ShowBoard.separateAndSend(content, player);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con);
		}
	}
	
	private void delTp(Player player, int TpNameDell)
	{
		Connection conDel = null;
		try
		{
			conDel = DatabaseFactory.getInstance().getConnection();
			PreparedStatement stDel = conDel.prepareStatement("DELETE FROM bbs_comteleport WHERE charId=? AND TpId=?;");
			stDel.setInt(1, player.getObjectId());
			stDel.setInt(2, TpNameDell);
			stDel.execute();
			DbUtils.closeQuietly(stDel);
		}
		catch(Exception e)
		{
			_log.error("data error on Delete Teleport: " + e);
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(conDel);
		}
	}
	
	private void AddTp(Player player, String TpNameAdd)
	{
		if(!checkFirstConditions(player) || !checkTeleportConditions(player))
		{
			return;
		}
		if(!checkTeleportLocation(player, player.getX(), player.getY(), player.getZ()))
		{
			return;
		}
		if(TpNameAdd.equals("") || TpNameAdd.equals(null))
		{
			player.sendMessage("Вы не ввели Имя закладки");
			return;
		}
		Connection con = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement st = con.prepareStatement("SELECT COUNT(*) FROM bbs_comteleport WHERE charId=?;");
			st.setLong(1, player.getObjectId());
			ResultSet rs = st.executeQuery();
			rs.next();
			if(rs.getInt(1) <= ACbConfigManager.ALT_CB_TELE_POINT_MAX_COUNT - 1)
			{
				PreparedStatement st1 = con.prepareStatement("SELECT COUNT(*) FROM bbs_comteleport WHERE charId=? AND name=?;");
				st1.setLong(1, player.getObjectId());
				st1.setString(2, TpNameAdd);
				ResultSet rs1 = st1.executeQuery();
				rs1.next();
				if(rs1.getInt(1) == 0)
				{
					PreparedStatement stAdd = con.prepareStatement("INSERT INTO bbs_comteleport (charId,xPos,yPos,zPos,name) VALUES(?,?,?,?,?)");
					stAdd.setInt(1, player.getObjectId());
					stAdd.setInt(2, player.getX());
					stAdd.setInt(3, player.getY());
					stAdd.setInt(4, player.getZ());
					stAdd.setString(5, TpNameAdd);
					stAdd.execute();
					DbUtils.closeQuietly(stAdd);
				}
				else
				{
					PreparedStatement stAdd = con.prepareStatement("UPDATE bbs_comteleport SET xPos=?, yPos=?, zPos=? WHERE charId=? AND name=?;");
					stAdd.setInt(1, player.getObjectId());
					stAdd.setInt(2, player.getX());
					stAdd.setInt(3, player.getY());
					stAdd.setInt(4, player.getZ());
					stAdd.setString(5, TpNameAdd);
					stAdd.execute();
					DbUtils.closeQuietly(stAdd);
				}
			}
			else
			{
				player.sendMessage("Вы не можете сохранить более " + ACbConfigManager.ALT_CB_TELE_POINT_MAX_COUNT + " закладок");
				return;
			}
			DbUtils.closeQuietly(st, rs);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public void onWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4, String arg5)
	{
	}
	
	public class CBteleport
	{
		public int TpId;
		public String TpName;
		public int PlayerId;
		public int xC;
		public int yC;
		public int zC;
		
		public CBteleport()
		{
			TpId = 0;
			TpName = "";
			PlayerId = 0;
			xC = 0;
			yC = 0;
			zC = 0;
		}
	}
}