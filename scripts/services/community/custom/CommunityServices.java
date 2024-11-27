package services.community.custom;

import l2.commons.dbutils.DbUtils;
import l2.gameserver.Config;
import l2.gameserver.dao.CharacterDAO;
import l2.gameserver.data.htm.HtmCache;
import l2.gameserver.database.DatabaseFactory;
import l2.gameserver.handler.bbs.CommunityBoardManager;
import l2.gameserver.handler.bbs.ICommunityBoardHandler;
import l2.gameserver.model.Player;
import l2.gameserver.model.base.Experience;
import l2.gameserver.model.entity.events.impl.SiegeEvent;
import l2.gameserver.model.entity.oly.NoblesController;
import l2.gameserver.model.pledge.Clan;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.BuyList;
import l2.gameserver.network.l2.s2c.ShowBoard;
import l2.gameserver.network.l2.s2c.SkillList;
import l2.gameserver.scripts.Functions;
import l2.gameserver.scripts.ScriptFile;
import l2.gameserver.utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.StringTokenizer;

public class CommunityServices implements ScriptFile, ICommunityBoardHandler
{
	private static final Logger _log = LoggerFactory.getLogger(CommunityServices.class);
	
	@Override
	public void onLoad()
	{
		if(Config.COMMUNITYBOARD_ENABLED)
		{
			_log.info("CommunityBoard: CommunityServices loaded.");
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
		return new String[] {"_cbbsservicesdelvl", "_cbbsservicesbuynoble", "_cbbsserviceschangesex", "_cbbsserviceschangename", "_bbssell", "_bbsclanup", "_bbsclanexpire"};
	}
	
	@Override
	public void onBypassCommand(Player player, String bypass)
	{
		if(!CommunityTools.checkConditions(player))
		{
			String html = HtmCache.getInstance().getNotNull("scripts/services/community/pages/locked.htm", player);
			html = html.replace("%name%", player.getName());
			ShowBoard.separateAndSend(html, player);
			return;
		}
		StringTokenizer st = new StringTokenizer(bypass, "_");
		String cmd = st.nextToken();
		if("_bbssell".equals(cmd))
		{
			if(!player.getPlayerAccess().UseShop)
			{
				player.sendMessage("Вам запрещено использовать магазин!");
				return;
			}
			player.sendPacket(new BuyList(null, player, 0.0));
		}
		else if(bypass.startsWith("_cbbsservicesdelvl"))
		{
			if(Functions.getItemCount(player, ACbConfigManager.ALT_CB_DELVLV_ITEM_ID) < ACbConfigManager.ALT_CB_DELVL_ITEM_COUNT)
			{
				player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
				return;
			}
			if(player.getLevel() < 3 || (long) player.getLevel() > player.getMaxExp())
			{
				return;
			}
			Functions.removeItem(player, ACbConfigManager.ALT_CB_DELVLV_ITEM_ID, ACbConfigManager.ALT_CB_DELVL_ITEM_COUNT);
			player.addExpAndSp(Experience.LEVEL[player.getLevel() - 2] - player.getExp(), 0, false, false);
		}
		else if(bypass.startsWith("_cbbsservicesbuynoble"))
		{
			if(Functions.getItemCount(player, ACbConfigManager.ALT_CB_NOBLES_ITEM_ID) < ACbConfigManager.ALT_CB_NOBLES_ITEM_COUNT)
			{
				player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
				return;
			}
			if(player.isNoble())
			{
				player.sendMessage("You already have a noble status.");
				return;
			}
			if(player.getLevel() < 76)
			{
				player.sendMessage("You must be 76 lvl or greater.");
				return;
			}
			Functions.removeItem(player, ACbConfigManager.ALT_CB_NOBLES_ITEM_ID, ACbConfigManager.ALT_CB_NOBLES_ITEM_COUNT);
			NoblesController.getInstance().addNoble(player);
			player.setNoble(true);
			player.updatePledgeClass();
			player.updateNobleSkills();
			player.sendPacket(new SkillList(player));
			player.broadcastUserInfo(true);
			player.sendMessage("Congratulation! You become a nobles.");
		}
		else if(bypass.startsWith("_cbbsserviceschangesex"))
		{
			if(Functions.getItemCount(player, ACbConfigManager.ALT_CB_CHANGESEX_ITEM_ID) < ACbConfigManager.ALT_CB_CHANGESEX_ITEM_COUNT)
			{
				player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
				return;
			}
			Functions.removeItem(player, ACbConfigManager.ALT_CB_CHANGESEX_ITEM_ID, ACbConfigManager.ALT_CB_CHANGESEX_ITEM_COUNT);
			Connection con = null;
			PreparedStatement offline = null;
			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				offline = con.prepareStatement("UPDATE characters SET sex = ? WHERE obj_Id = ?");
				offline.setInt(1, player.getSex() == 1 ? 0 : 1);
				offline.setInt(2, player.getObjectId());
				offline.executeUpdate();
			}
			catch(Exception e)
			{
				e.printStackTrace();
				return;
			}
			finally
			{
				DbUtils.closeQuietly(con, offline);
			}
			player.setHairColor(0);
			player.setHairStyle(0);
			player.setFace(0);
			player.logout();
		}
		else if(bypass.startsWith("_cbbsserviceschangename"))
		{
			if(player.isHero())
			{
				player.sendMessage("Rename is unavailable for hero character.");
				return;
			}
			if(player.getEvent(SiegeEvent.class) != null)
			{
				player.sendMessage("Rename is unavailable in siege period.");
				return;
			}
			String[] param = null;
			try
			{
				param = bypass.split(" ");
			}
			catch(Exception e)
			{
				
			}
			if(param == null || param.length != 2 || param[1] == null)
			{
				player.sendMessage("Incorrect name.");
				return;
			}
			String newname = param[1];
			if(!Util.isMatchingRegexp(newname, Config.CNAME_TEMPLATE) || newname.length() > Config.CNAME_MAXLEN)
			{
				player.sendMessage("Incorrect name.");
				return;
			}
			if(CharacterDAO.getInstance().getObjectIdByName(newname) > 0)
			{
				player.sendMessage("Name already used.");
				return;
			}
			if(Functions.getItemCount(player, ACbConfigManager.ALT_CB_CHANGENAME_ITEM_ID) < ACbConfigManager.ALT_CB_CHANGENAME_ITEM_COUNT)
			{
				player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
				return;
			}
			Functions.removeItem(player, ACbConfigManager.ALT_CB_CHANGENAME_ITEM_ID, ACbConfigManager.ALT_CB_CHANGENAME_ITEM_COUNT);
			player.reName(newname, true);
			player.logout();
		}
		else if(bypass.startsWith("_bbsclanup"))
		{
			Clan clan = player.getClan();
			if(clan == null)
			{
				player.sendMessage("Get clan first.");
				return;
			}
			if(clan.getLeaderId() != player.getObjectId())
			{
				player.sendMessage("Only clan leader can do that.");
				return;
			}
			if(clan.getLevel() < 1 || clan.getLevel() > 7)
			{
				player.sendMessage("Clan level to high.");
				return;
			}
			if(Functions.getItemCount(player, ACbConfigManager.ALT_CB_CLANUP_ITEM_ID) < ACbConfigManager.ALT_CB_CLANUP_ITEM_COUNT)
			{
				player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
				return;
			}
			Functions.removeItem(player, ACbConfigManager.ALT_CB_CLANUP_ITEM_ID, ACbConfigManager.ALT_CB_CLANUP_ITEM_COUNT);
			clan.setLevel(clan.getLevel() + 1);
			clan.updateClanInDB();
			clan.broadcastClanStatus(true, true, true);
		}
		else if(bypass.startsWith("_bbsclanexpire"))
		{
			Clan clan = player.getClan();
			if(clan == null)
			{
				player.sendMessage("Get clan first.");
				return;
			}
			if(clan.getLeaderId() != player.getObjectId())
			{
				player.sendMessage("Only clan leader can do that.");
				return;
			}
			if(Functions.getItemCount(player, ACbConfigManager.ALT_CB_CLAN_PENALTY_ITEM_ID) < ACbConfigManager.ALT_CB_CLAN_PENALTY_ITEM_COUNT)
			{
				player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
				return;
			}
			Functions.removeItem(player, ACbConfigManager.ALT_CB_CLAN_PENALTY_ITEM_ID, ACbConfigManager.ALT_CB_CLAN_PENALTY_ITEM_COUNT);
			player.getClan().setExpelledMemberTime(0);
			player.sendMessage("The penalty for a clan has been lifted");
		}
		String html = "";
		ShowBoard.separateAndSend(html, player);
	}
	
	@Override
	public void onWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4, String arg5)
	{
	}
}