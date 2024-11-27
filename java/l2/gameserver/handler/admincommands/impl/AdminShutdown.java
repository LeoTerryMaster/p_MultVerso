package l2.gameserver.handler.admincommands.impl;

import l2.commons.lang.StatsUtils;
import l2.gameserver.Config;
import l2.gameserver.GameTimeController;
import l2.gameserver.Shutdown;
import l2.gameserver.handler.admincommands.IAdminCommandHandler;
import l2.gameserver.model.GameObjectsStorage;
import l2.gameserver.model.Player;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;
import org.apache.commons.lang3.math.NumberUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AdminShutdown implements IAdminCommandHandler
{
	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;
		if(!activeChar.getPlayerAccess().CanRestart)
		{
			return false;
		}
		try
		{
			switch(command)
			{
				case admin_server_shutdown:
				{
					Shutdown.getInstance().schedule(NumberUtils.toInt(wordList[1], -1), 0);
					break;
				}
				case admin_server_restart:
				{
					Shutdown.getInstance().schedule(NumberUtils.toInt(wordList[1], -1), 2);
					break;
				}
				case admin_server_abort:
				{
					Shutdown.getInstance().cancel();
				}
			}
		}
		catch(Exception e)
		{
			sendHtmlForm(activeChar);
		}
		return true;
	}
	
	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}
	
	private void sendHtmlForm(Player activeChar)
	{
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		int t = GameTimeController.getInstance().getGameTime();
		int h = t / 60;
		SimpleDateFormat format = new SimpleDateFormat("h:mm a");
		Calendar cal = Calendar.getInstance();
		cal.set(11, h);
		int m = t % 60;
		cal.set(12, m);
		StringBuilder replyMSG = new StringBuilder("<html><body>");
		replyMSG.append("<table width=260><tr>");
		replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td>");
		replyMSG.append("<td width=180><center>Server Management Menu</center></td>");
		replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td>");
		replyMSG.append("</tr></table>");
		replyMSG.append("<br><br>");
		replyMSG.append("<table>");
		replyMSG.append("<tr><td>Players Online: " + GameObjectsStorage.getAllPlayersCount() + "</td></tr>");
		replyMSG.append("<tr><td>Used Memory: " + StatsUtils.getMemUsedMb() + "</td></tr>");
		replyMSG.append("<tr><td>Server Rates: " + Config.RATE_XP + "x, " + Config.RATE_SP + "x, " + Config.RATE_DROP_ADENA + "x, " + Config.RATE_DROP_ITEMS + "x</td></tr>");
		replyMSG.append("<tr><td>Game Time: " + format.format(cal.getTime()) + "</td></tr>");
		replyMSG.append("</table><br>");
		replyMSG.append("<table width=270>");
		replyMSG.append("<tr><td>Enter in seconds the time till the server shutdowns bellow:</td></tr>");
		replyMSG.append("<br>");
		replyMSG.append("<tr><td><center>Seconds till: <edit var=\"shutdown_time\" width=60></center></td></tr>");
		replyMSG.append("</table><br>");
		replyMSG.append("<center><table><tr><td>");
		replyMSG.append("<button value=\"Shutdown\" action=\"bypass -h admin_server_shutdown $shutdown_time\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td><td>");
		replyMSG.append("<button value=\"Restart\" action=\"bypass -h admin_server_restart $shutdown_time\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td><td>");
		replyMSG.append("<button value=\"Abort\" action=\"bypass -h admin_server_abort\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td><td>");
		replyMSG.append("<button value=\"Refresh\" action=\"bypass -h admin_server_shutdown\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\">");
		replyMSG.append("</td></tr></table></center>");
		replyMSG.append("</body></html>");
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	
	private enum Commands
	{
		admin_server_shutdown,
		admin_server_restart,
		admin_server_abort;
	}
}