package l2.gameserver.handler.admincommands.impl;

import l2.gameserver.Announcements;
import l2.gameserver.handler.admincommands.IAdminCommandHandler;
import l2.gameserver.model.GameObjectsStorage;
import l2.gameserver.model.Player;
import l2.gameserver.network.l2.components.ChatType;
import l2.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;

import java.util.List;

public class AdminAnnouncements implements IAdminCommandHandler
{
	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;
		if(!activeChar.getPlayerAccess().CanAnnounce)
		{
			return false;
		}
		switch(command)
		{
			case admin_list_announcements:
			{
				listAnnouncements(activeChar);
				break;
			}
			case admin_announce_menu:
			{
				Announcements.getInstance().announceToAll(fullString.substring(20));
				listAnnouncements(activeChar);
				break;
			}
			case admin_announce_announcements:
			{
				for(Player player : GameObjectsStorage.getAllPlayersForIterate())
				{
					Announcements.getInstance().showAnnouncements(player);
				}
				listAnnouncements(activeChar);
				break;
			}
			case admin_add_announcement:
			{
				if(wordList.length < 3)
				{
					return false;
				}
				try
				{
					int time = Integer.parseInt(wordList[1]);
					StringBuilder builder = new StringBuilder();
					for(int i = 2;i < wordList.length;++i)
					{
						builder.append(" ").append(wordList[i]);
					}
					Announcements.getInstance().addAnnouncement(time, builder.toString(), true);
					listAnnouncements(activeChar);
				}
				catch(Exception e)
				{
				}
				break;
			}
			case admin_del_announcement:
			{
				if(wordList.length != 2)
				{
					return false;
				}
				int val = Integer.parseInt(wordList[1]);
				Announcements.getInstance().delAnnouncement(val);
				listAnnouncements(activeChar);
				break;
			}
			case admin_announce:
			{
				Announcements.getInstance().announceToAll(fullString.substring(15));
				break;
			}
			case admin_a:
			{
				Announcements.getInstance().announceToAll(fullString.substring(8));
				break;
			}
			case admin_crit_announce:
			case admin_c:
			{
				if(wordList.length < 2)
				{
					return false;
				}
				Announcements.getInstance().announceToAll(activeChar.getName() + ": " + fullString.replaceFirst("admin_crit_announce ", "").replaceFirst("admin_c ", ""), ChatType.CRITICAL_ANNOUNCE);
				break;
			}
			case admin_toscreen:
			case admin_s:
			{
				if(wordList.length < 2)
				{
					return false;
				}
				String text = activeChar.getName() + ": " + fullString.replaceFirst("admin_toscreen ", "").replaceFirst("admin_s ", "");
				int time = 3000 + text.length() * 100;
				ExShowScreenMessage sm = new ExShowScreenMessage(text, time, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, text.length() < 64);
				for(Player player : GameObjectsStorage.getAllPlayersForIterate())
				{
					player.sendPacket(sm);
				}
				break;
			}
			case admin_reload_announcements:
			{
				Announcements.getInstance().loadAnnouncements();
				listAnnouncements(activeChar);
				activeChar.sendMessage("Announcements reloaded.");
			}
		}
		return true;
	}
	
	public void listAnnouncements(Player activeChar)
	{
		List<Announcements.Announce> announcements = Announcements.getInstance().getAnnouncements();
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		StringBuilder replyMSG = new StringBuilder("<html><body>");
		replyMSG.append("<table width=260><tr>");
		replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td>");
		replyMSG.append("<td width=180><center>Announcement Menu</center></td>");
		replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td>");
		replyMSG.append("</tr></table>");
		replyMSG.append("<br><br>");
		replyMSG.append("<center>Add or announce a new announcement:</center>");
		replyMSG.append("<center><multiedit var=\"new_announcement\" width=240 height=30></center><br>");
		replyMSG.append("<center>Time(in seconds, 0 - only for start)<edit var=\"time\" width=40 height=20></center><br>");
		replyMSG.append("<center><table><tr><td>");
		replyMSG.append("<button value=\"Add\" action=\"bypass -h admin_add_announcement $time $new_announcement\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td><td>");
		replyMSG.append("<button value=\"Announce\" action=\"bypass -h admin_announce_menu $new_announcement\" width=64 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td><td>");
		replyMSG.append("<button value=\"Reload\" action=\"bypass -h admin_reload_announcements\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td><td>");
		replyMSG.append("<button value=\"Broadcast\" action=\"bypass -h admin_announce_announcements\" width=70 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\">");
		replyMSG.append("</td></tr></table></center>");
		replyMSG.append("<br>");
		for(int i = 0;i < announcements.size();++i)
		{
			Announcements.Announce announce = announcements.get(i);
			replyMSG.append("<table width=260><tr><td width=180>" + announce.getAnnounce() + "</td><td width=40>" + announce.getTime() + "</td><<td width=40>");
			replyMSG.append("<button value=\"Delete\" action=\"bypass -h admin_del_announcement " + i + "\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td></tr></table>");
		}
		replyMSG.append("</body></html>");
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	
	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}
	
	private enum Commands
	{
		admin_list_announcements,
		admin_announce_announcements,
		admin_add_announcement,
		admin_del_announcement,
		admin_announce,
		admin_a,
		admin_announce_menu,
		admin_crit_announce,
		admin_c,
		admin_toscreen,
		admin_s,
		admin_reload_announcements;
	}
}