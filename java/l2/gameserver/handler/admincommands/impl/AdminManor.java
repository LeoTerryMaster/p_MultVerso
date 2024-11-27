package l2.gameserver.handler.admincommands.impl;

import l2.gameserver.data.xml.holder.ResidenceHolder;
import l2.gameserver.handler.admincommands.IAdminCommandHandler;
import l2.gameserver.instancemanager.CastleManorManager;
import l2.gameserver.instancemanager.ServerVariables;
import l2.gameserver.model.Player;
import l2.gameserver.model.entity.residence.Castle;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class AdminManor implements IAdminCommandHandler
{
	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;
		if(!activeChar.getPlayerAccess().Menu)
		{
			return false;
		}
		StringTokenizer st = new StringTokenizer(fullString);
		fullString = st.nextToken();
		if(fullString.equals("admin_manor"))
		{
			showMainPage(activeChar);
		}
		else if(fullString.equals("admin_manor_reset"))
		{
			int castleId = 0;
			try
			{
				castleId = Integer.parseInt(st.nextToken());
			}
			catch(Exception e)
			{
				
			}
			if(castleId > 0)
			{
				Castle castle = ResidenceHolder.getInstance().getResidence(Castle.class, castleId);
				castle.setCropProcure(new ArrayList<>(), 0);
				castle.setCropProcure(new ArrayList<>(), 1);
				castle.setSeedProduction(new ArrayList<>(), 0);
				castle.setSeedProduction(new ArrayList<>(), 1);
				castle.saveCropData();
				castle.saveSeedData();
				activeChar.sendMessage("Manor data for " + castle.getName() + " was nulled");
			}
			else
			{
				for(Castle castle : ResidenceHolder.getInstance().getResidenceList(Castle.class))
				{
					castle.setCropProcure(new ArrayList<>(), 0);
					castle.setCropProcure(new ArrayList<>(), 1);
					castle.setSeedProduction(new ArrayList<>(), 0);
					castle.setSeedProduction(new ArrayList<>(), 1);
					castle.saveCropData();
					castle.saveSeedData();
				}
				activeChar.sendMessage("Manor data was nulled");
			}
			showMainPage(activeChar);
		}
		else if(fullString.equals("admin_manor_save"))
		{
			CastleManorManager.getInstance().save();
			activeChar.sendMessage("Manor System: all data saved");
			showMainPage(activeChar);
		}
		else if(fullString.equals("admin_manor_disable"))
		{
			boolean mode;
			CastleManorManager.getInstance().setDisabled(!(mode = CastleManorManager.getInstance().isDisabled()));
			if(mode)
			{
				activeChar.sendMessage("Manor System: enabled");
			}
			else
			{
				activeChar.sendMessage("Manor System: disabled");
			}
			showMainPage(activeChar);
		}
		return true;
	}
	
	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}
	
	private void showMainPage(Player activeChar)
	{
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		StringBuilder replyMSG = new StringBuilder("<html><body>");
		replyMSG.append("<center><font color=\"LEVEL\"> [Manor System] </font></center><br>");
		replyMSG.append("<table width=\"100%\">");
		replyMSG.append("<tr><td>Disabled: " + (CastleManorManager.getInstance().isDisabled() ? "yes" : "no") + "</td>");
		replyMSG.append("<td>Under Maintenance: " + (CastleManorManager.getInstance().isUnderMaintenance() ? "yes" : "no") + "</td></tr>");
		replyMSG.append("<tr><td>Approved: " + (ServerVariables.getBool("ManorApproved") ? "yes" : "no") + "</td></tr>");
		replyMSG.append("</table>");
		replyMSG.append("<center><table>");
		replyMSG.append("<tr><td><button value=\"" + (CastleManorManager.getInstance().isDisabled() ? "Enable" : "Disable") + "\" action=\"bypass -h admin_manor_disable\" width=110 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td>");
		replyMSG.append("<td><button value=\"Reset\" action=\"bypass -h admin_manor_reset\" width=110 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td></tr>");
		replyMSG.append("<tr><td><button value=\"Refresh\" action=\"bypass -h admin_manor\" width=110 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td>");
		replyMSG.append("<td><button value=\"Back\" action=\"bypass -h admin_admin\" width=110 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td></tr>");
		replyMSG.append("</table></center>");
		replyMSG.append("<br><center>Castle Information:<table width=\"100%\">");
		replyMSG.append("<tr><td></td><td>Current Period</td><td>Next Period</td></tr>");
		for(Castle c : ResidenceHolder.getInstance().getResidenceList(Castle.class))
		{
			replyMSG.append("<tr><td>" + c.getName() + "</td><td>" + c.getManorCost(0) + "a</td><td>" + c.getManorCost(1) + "a</td></tr>");
		}
		replyMSG.append("</table><br>");
		replyMSG.append("</body></html>");
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	
	private enum Commands
	{
		admin_manor,
		admin_manor_reset,
		admin_manor_save,
		admin_manor_disable;
	}
}