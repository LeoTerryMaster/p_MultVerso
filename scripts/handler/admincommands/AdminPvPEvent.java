package handler.admincommands;

import events.TvT2.PvPEvent;
import l2.gameserver.instancemanager.ServerVariables;
import l2.gameserver.model.Player;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2.gameserver.scripts.Scripts;

public class AdminPvPEvent extends ScriptAdminCommand
{
	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}
	
	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands c = (Commands) comm;
		if(!activeChar.getPlayerAccess().IsEventGm)
		{
			return false;
		}
		PvPEvent.PvPEventRule rule;
		switch(c)
		{
			case admin_pvpevent:
			{
				if(wordList.length > 1)
				{
					if(wordList[1].equalsIgnoreCase("tvt"))
					{
						showEventMenu(activeChar, "tvt");
						break;
					}
					if(wordList[1].equalsIgnoreCase("ctf"))
					{
						showEventMenu(activeChar, "ctf");
						break;
					}
					if(!wordList[1].equalsIgnoreCase("dm"))
						break;
					showEventMenu(activeChar, "dm");
					break;
				}
				showMainMenu(activeChar);
				break;
			}
			case admin_pvpevent_active:
			{
				if(wordList.length > 1)
				{
					boolean active = Boolean.parseBoolean(wordList[1]);
					ServerVariables.set("PvP_is_active", active);
					PvPEvent.getInstance().LoadVars();
					if(active)
					{
						PvPEvent.getInstance().Activate();
					}
					else
					{
						PvPEvent.getInstance().Deativate();
					}
				}
				showMainMenu(activeChar);
				break;
			}
			case admin_pvpevent_setanntime:
			{
				if(wordList.length > 1)
				{
					ServerVariables.set("PvP_announce_time", wordList[1]);
					PvPEvent.getInstance().LoadVars();
				}
				showMainMenu(activeChar);
				break;
			}
			case admin_pvpevent_setannredu:
			{
				if(wordList.length > 1)
				{
					ServerVariables.set("PvP_announce_reduct", wordList[1]);
					PvPEvent.getInstance().LoadVars();
				}
				showMainMenu(activeChar);
				break;
			}
			case admin_pvpevent_settime:
			{
				if(wordList.length > 1 && !wordList[1].isEmpty())
				{
					ServerVariables.set("PvP_start_time", ServerVariables.getString("PvP_start_time", "") + wordList[1]);
					PvPEvent.getInstance().LoadVars();
				}
				else
				{
					ServerVariables.set("PvP_start_time", "");
					PvPEvent.getInstance().LoadVars();
				}
				showMainMenu(activeChar);
				break;
			}
			case admin_pvpevent_setinstsid:
			{
				if(wordList.length > 1)
				{
					ServerVariables.set("PvP_instances_ids", wordList[1]);
					PvPEvent.getInstance().LoadVars();
				}
				showMainMenu(activeChar);
				break;
			}
			case admin_pvpevent_enable:
			{
				if(wordList.length <= 2)
					break;
				try
				{
					PvPEvent.PvPEventRule rule2 = Enum.valueOf(PvPEvent.PvPEventRule.class, wordList[1].toUpperCase());
					ServerVariables.set("PvP_" + rule2.name() + "_enabled", Boolean.parseBoolean(wordList[2]));
					showEventMenu(activeChar, rule2.name().toLowerCase());
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				break;
			}
			case admin_pvpevent_capcha:
			{
				if(wordList.length <= 2)
					break;
				try
				{
					PvPEvent.PvPEventRule rule3 = Enum.valueOf(PvPEvent.PvPEventRule.class, wordList[1].toUpperCase());
					ServerVariables.set("PvP_" + rule3.name() + "_use_capcha", Boolean.parseBoolean(wordList[2]));
					showEventMenu(activeChar, rule3.name().toLowerCase());
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				break;
			}
			case admin_pvpevent_hwid_restrict:
			{
				if(wordList.length <= 2)
					break;
				try
				{
					PvPEvent.PvPEventRule rule4 = Enum.valueOf(PvPEvent.PvPEventRule.class, wordList[1].toUpperCase());
					ServerVariables.set("PvP_" + rule4.name() + "_use_hwid_restrict", Boolean.parseBoolean(wordList[2]));
					showEventMenu(activeChar, rule4.name().toLowerCase());
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				break;
			}
			case admin_pvpevent_hide_identiti:
			{
				if(wordList.length <= 2)
					break;
				try
				{
					PvPEvent.PvPEventRule rule5 = Enum.valueOf(PvPEvent.PvPEventRule.class, wordList[1].toUpperCase());
					ServerVariables.set("PvP_" + rule5.name() + "_hide_identiti", Boolean.parseBoolean(wordList[2]));
					showEventMenu(activeChar, rule5.name().toLowerCase());
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				break;
			}
			case admin_pvpevent_dispell:
			{
				if(wordList.length > 2)
				{
					try
					{
						rule = Enum.valueOf(PvPEvent.PvPEventRule.class, wordList[1].toUpperCase());
						ServerVariables.set("PvP_" + rule.name() + "_dispell", Boolean.parseBoolean(wordList[2]));
						showEventMenu(activeChar, rule.name().toLowerCase());
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
				}
			}
			case admin_pvpevent_dispell_after:
			{
				if(wordList.length <= 2)
					break;
				try
				{
					rule = Enum.valueOf(PvPEvent.PvPEventRule.class, wordList[1].toUpperCase());
					ServerVariables.set("PvP_" + rule.name() + "_dispell_after", Boolean.parseBoolean(wordList[2]));
					showEventMenu(activeChar, rule.name().toLowerCase());
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				break;
			}
			case admin_pvpevent_reqpart:
			{
				if(wordList.length <= 2)
					break;
				try
				{
					PvPEvent.PvPEventRule rule6 = Enum.valueOf(PvPEvent.PvPEventRule.class, wordList[1].toUpperCase());
					ServerVariables.set("PvP_" + rule6.name() + "_req_parts", wordList[2]);
					showEventMenu(activeChar, rule6.name().toLowerCase());
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				break;
			}
			case admin_pvpevent_maxpart:
			{
				if(wordList.length <= 2)
					break;
				try
				{
					PvPEvent.PvPEventRule rule7 = Enum.valueOf(PvPEvent.PvPEventRule.class, wordList[1].toUpperCase());
					ServerVariables.set("PvP_" + rule7.name() + "_max_parts", wordList[2]);
					showEventMenu(activeChar, rule7.name().toLowerCase());
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				break;
			}
			case admin_pvpevent_minlvl:
			{
				if(wordList.length <= 2)
					break;
				try
				{
					PvPEvent.PvPEventRule rule8 = Enum.valueOf(PvPEvent.PvPEventRule.class, wordList[1].toUpperCase());
					ServerVariables.set("PvP_" + rule8.name() + "_min_lvl", wordList[2]);
					showEventMenu(activeChar, rule8.name().toLowerCase());
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				break;
			}
			case admin_pvpevent_maxlvl:
			{
				if(wordList.length <= 2)
					break;
				try
				{
					PvPEvent.PvPEventRule rule9 = Enum.valueOf(PvPEvent.PvPEventRule.class, wordList[1].toUpperCase());
					ServerVariables.set("PvP_" + rule9.name() + "_max_lvl", wordList[2]);
					showEventMenu(activeChar, rule9.name().toLowerCase());
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				break;
			}
			case admin_pvpevent_evetime:
			{
				if(wordList.length <= 2)
					break;
				try
				{
					PvPEvent.PvPEventRule rule10 = Enum.valueOf(PvPEvent.PvPEventRule.class, wordList[1].toUpperCase());
					ServerVariables.set("PvP_" + rule10.name() + "_time", wordList[2]);
					showEventMenu(activeChar, rule10.name().toLowerCase());
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				break;
			}
			case admin_pvpevent_prohclid:
			{
				if(wordList.length <= 2)
					break;
				try
				{
					PvPEvent.PvPEventRule rule11 = Enum.valueOf(PvPEvent.PvPEventRule.class, wordList[1].toUpperCase());
					ServerVariables.set("PvP_" + rule11.name() + "_prohibited_class_ids", wordList[2]);
					showEventMenu(activeChar, rule11.name().toLowerCase());
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				break;
			}
			case admin_pvpevent_revdel:
			{
				if(wordList.length <= 2)
					break;
				try
				{
					PvPEvent.PvPEventRule rule12 = Enum.valueOf(PvPEvent.PvPEventRule.class, wordList[1].toUpperCase());
					ServerVariables.set("PvP_" + rule12.name() + "_revive_delay", wordList[2]);
					showEventMenu(activeChar, rule12.name().toLowerCase());
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				break;
			}
			case admin_pvpevent_ipkill:
			{
				if(wordList.length <= 2)
					break;
				try
				{
					PvPEvent.PvPEventRule rule13 = Enum.valueOf(PvPEvent.PvPEventRule.class, wordList[1].toUpperCase());
					ServerVariables.set("PvP_" + rule13.name() + "_item_per_kill", wordList[2]);
					showEventMenu(activeChar, rule13.name().toLowerCase());
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				break;
			}
			case admin_pvpevent_herorevhours:
			{
				if(wordList.length <= 2)
					break;
				try
				{
					PvPEvent.PvPEventRule rule14 = Enum.valueOf(PvPEvent.PvPEventRule.class, wordList[1].toUpperCase());
					ServerVariables.set("PvP_" + rule14.name() + "_herorevhours", wordList[2]);
					showEventMenu(activeChar, rule14.name().toLowerCase());
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				break;
			}
			case admin_pvpevent_reward_team:
			{
				if(wordList.length <= 2)
					break;
				try
				{
					PvPEvent.PvPEventRule rule15 = Enum.valueOf(PvPEvent.PvPEventRule.class, wordList[1].toUpperCase());
					ServerVariables.set("PvP_" + rule15.name() + "_rev_team", wordList[2]);
					showEventMenu(activeChar, rule15.name().toLowerCase());
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				break;
			}
			case admin_pvpevent_reward_top:
			{
				if(wordList.length <= 2)
					break;
				try
				{
					PvPEvent.PvPEventRule rule16 = Enum.valueOf(PvPEvent.PvPEventRule.class, wordList[1].toUpperCase());
					ServerVariables.set("PvP_" + rule16.name() + "_rev_top", wordList[2]);
					showEventMenu(activeChar, rule16.name().toLowerCase());
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				break;
			}
			case admin_pvpevent_start:
			{
				if(wordList.length <= 1)
					break;
				try
				{
					PvPEvent.PvPEventRule rule17 = Enum.valueOf(PvPEvent.PvPEventRule.class, wordList[1].toUpperCase());
					PvPEvent.getInstance().LoadVars();
					PvPEvent.getInstance().setRule(rule17);
					PvPEvent.getInstance().goRegistration();
					showEventMenu(activeChar, rule17.name().toLowerCase());
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				break;
			}
			case admin_capt1:
			{
				Scripts.getInstance().callScripts(activeChar, "Util", "RequestCapcha", new Object[] {"handler.admincommands.AdminPvPEvent:CapchaAccespted", activeChar.getStoredId(), 60});
				return true;
			}
			case admin_tvt1:
			{
				PvPEvent.getInstance().LoadVars();
				PvPEvent.getInstance().setRule(PvPEvent.PvPEventRule.TVT);
				PvPEvent.getInstance().goRegistration();
				break;
			}
			case admin_dm1:
			{
				PvPEvent.getInstance().LoadVars();
				PvPEvent.getInstance().setRule(PvPEvent.PvPEventRule.DM);
				PvPEvent.getInstance().goRegistration();
				break;
			}
			case admin_ctf1:
			{
				PvPEvent.getInstance().LoadVars();
				PvPEvent.getInstance().setRule(PvPEvent.PvPEventRule.CTF);
				PvPEvent.getInstance().goRegistration();
			}
		}
		return false;
	}
	
	private void showMainMenu(Player activeChar)
	{
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		StringBuilder sb = new StringBuilder("<html><title>PvPEvent</title><body>");
		appendTopMenu(sb);
		appendLine(sb);
		appendBoolEdit(sb, "Active", ServerVariables.getBool("PvP_is_active", false), "admin_pvpevent_active");
		appendVarEdit(sb, "Announce time", ServerVariables.getString("PvP_announce_time", "5"), "anntime", "admin_pvpevent_setanntime");
		appendVarEdit(sb, "Announce reduct", ServerVariables.getString("PvP_announce_reduct", "1"), "annredu", "admin_pvpevent_setannredu");
		appendLongEditMenu(sb, "Start time (add)", ServerVariables.getString("PvP_start_time", ""), "sttime", "admin_pvpevent_settime");
		appendLongEditMenu(sb, "Instances (delim ';')", ServerVariables.getString("PvP_instances_ids", ""), "insid", "admin_pvpevent_setinstsid");
		appendLine(sb);
		PvPEvent.PvPEventRule nextRule = PvPEvent.getInstance().getNextRule(PvPEvent.getInstance().getRule());
		sb.append("<center>Next event: " + (nextRule != null ? nextRule.name() : "none") + "</center><br>");
		sb.append("</body></html>");
		adminReply.setHtml(sb.toString());
		activeChar.sendPacket(adminReply);
	}
	
	private void showEventMenu(Player activeChar, String event)
	{
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		StringBuilder sb = new StringBuilder("<html><title>PvPEvent - " + event + "</title><body>");
		appendTopMenu(sb);
		appendLine(sb);
		appendDefaultEventMenu(sb, event);
		appendLine(sb);
		if(event.equalsIgnoreCase("tvt"))
		{
			appendRevardEventMenu(sb, "tvt", true, true);
		}
		else if(event.equalsIgnoreCase("ctf"))
		{
			appendRevardEventMenu(sb, "ctf", true, false);
		}
		else if(event.equalsIgnoreCase("dm"))
		{
			appendRevardEventMenu(sb, "dm", false, true);
		}
		appendLine(sb);
		sb.append("<center><button value=\"Start " + event + "\" action=\"bypass -h admin_pvpevent_start " + event + "\" width=100 height=20 back=\"L2UI_CH3.bigbutton2_down\" fore=\"L2UI_CH3.bigbutton2\"></center>");
		sb.append("</body></html>");
		adminReply.setHtml(sb.toString());
		activeChar.sendPacket(adminReply);
	}
	
	private void appendTopMenu(StringBuilder sb)
	{
		sb.append("<center><table width=260><tr>");
		sb.append("<td><button value=\"Main\" action=\"bypass -h admin_pvpevent\" width=60 height=20 back=\"L2UI_CH3.bigbutton2_down\" fore=\"L2UI_CH3.bigbutton2\"></td>");
		sb.append("<td><button value=\"TVT\" action=\"bypass -h admin_pvpevent tvt\" width=60 height=20 back=\"L2UI_CH3.bigbutton2_down\" fore=\"L2UI_CH3.bigbutton2\"></td>");
		sb.append("<td><button value=\"CTF\" action=\"bypass -h admin_pvpevent ctf\" width=60 height=20 back=\"L2UI_CH3.bigbutton2_down\" fore=\"L2UI_CH3.bigbutton2\"></td>");
		sb.append("<td><button value=\"DM\" action=\"bypass -h admin_pvpevent dm\" width=60 height=20 back=\"L2UI_CH3.bigbutton2_down\" fore=\"L2UI_CH3.bigbutton2\"></td>");
		sb.append("<center><table width=260><tr>");
	}
	
	private void appendLongEditMenu(StringBuilder sb, String title, String curr_val, String var_name, String command)
	{
		sb.append("<center>" + title + ":</center><br1>");
		sb.append("<center><table width=260><tr>");
		sb.append("<td width=40>Curr:</td><td width=210>").append(curr_val).append("</td>");
		sb.append("</tr></table></center><br1><center><table width=260><tr>");
		sb.append("<td width=40>New:</td><td width=180><edit var=\"" + var_name + "\" width=180 length=72></td><td width=40>");
		sb.append("<button value=\"Set\" action=\"bypass -h " + command + " $" + var_name + "\" width=40 height=20 back=\"L2UI_CH3.bigbutton2_down\" fore=\"L2UI_CH3.bigbutton2\"></td>");
		sb.append("</tr></table></center><br>");
	}
	
	private void appendLine(StringBuilder sb)
	{
		sb.append("<br><center><img src=\"L2UI.SquareWhite\" width=260 height=1></center><br>");
	}
	
	private void appendVarEdit(StringBuilder sb, String title, String curr_val, String var_name, String command)
	{
		sb.append("<center><table width=260><tr><td with=120>");
		sb.append(title);
		sb.append(":</td><td with=40>");
		sb.append(curr_val);
		sb.append("</td><td with=40><edit var=\"");
		sb.append(var_name);
		sb.append("\" width=40 length=5></td><td with=40><button value=\"set\" action=\"bypass -h ");
		sb.append(command);
		sb.append(" $");
		sb.append(var_name);
		sb.append("\" width=40 height=20 back=\"L2UI_CH3.bigbutton2_down\" fore=\"L2UI_CH3.bigbutton2\"></td></tr></table></center><br>");
	}
	
	private void appendBoolEdit(StringBuilder sb, String title, boolean curr_val, String command)
	{
		sb.append("<center><table width=260><tr><td with=100>");
		sb.append(title);
		sb.append(":</td><td with=70>");
		sb.append(curr_val ? "on" : "off");
		sb.append(":</td><td with=60><td with=30 align=right>");
		sb.append("<button value=\"on\" action=\"bypass -h ");
		sb.append(command);
		sb.append(" true\" width=30 height=20 back=\"L2UI_CH3.bigbutton2_down\" fore=\"L2UI_CH3.bigbutton2\"></td><td with=30 align=left>");
		sb.append("<button value=\"off\" action=\"bypass -h ");
		sb.append(command);
		sb.append(" false\" width=30 height=20 back=\"L2UI_CH3.bigbutton2_down\" fore=\"L2UI_CH3.bigbutton2\"></td></tr></table></center><br>");
	}
	
	private void appendDefaultEventMenu(StringBuilder sb, String event_name)
	{
		appendBoolEdit(sb, "Enabled", ServerVariables.getBool("PvP_" + event_name.toUpperCase() + "_enabled", false), "admin_pvpevent_enable " + event_name.toLowerCase());
		appendBoolEdit(sb, "Captcha", ServerVariables.getBool("PvP_" + event_name.toUpperCase() + "_use_capcha", false), "admin_pvpevent_capcha " + event_name.toLowerCase());
		appendBoolEdit(sb, "HWID Check", ServerVariables.getBool("PvP_" + event_name.toUpperCase() + "_use_hwid_restrict", false), "admin_pvpevent_hwid_restrict " + event_name.toLowerCase());
		appendBoolEdit(sb, "Hide identiti", ServerVariables.getBool("PvP_" + event_name.toUpperCase() + "_hide_identiti", false), "admin_pvpevent_hide_identiti " + event_name.toLowerCase());
		appendBoolEdit(sb, "Remove Buff To", ServerVariables.getBool("PvP_" + event_name.toUpperCase() + "_dispell", false), "admin_pvpevent_dispell " + event_name.toLowerCase());
		appendBoolEdit(sb, "Remove Buff From", ServerVariables.getBool("PvP_" + event_name.toUpperCase() + "_dispell_after", false), "admin_pvpevent_dispell_after " + event_name.toLowerCase());
		appendVarEdit(sb, "Req part", ServerVariables.getString("PvP_" + event_name.toUpperCase() + "_req_parts", "50"), "reqpart", "admin_pvpevent_reqpart " + event_name.toLowerCase());
		appendVarEdit(sb, "Max part", ServerVariables.getString("PvP_" + event_name.toUpperCase() + "_max_parts", "100"), "maxpart", "admin_pvpevent_maxpart " + event_name.toLowerCase());
		appendVarEdit(sb, "Min lvl", ServerVariables.getString("PvP_" + event_name.toUpperCase() + "_min_lvl", "1"), "minlvl", "admin_pvpevent_minlvl " + event_name.toLowerCase());
		appendVarEdit(sb, "Max lvl", ServerVariables.getString("PvP_" + event_name.toUpperCase() + "_max_lvl", "86"), "maxlvl", "admin_pvpevent_maxlvl " + event_name.toLowerCase());
		appendVarEdit(sb, "Time", ServerVariables.getString("PvP_" + event_name.toUpperCase() + "_time", "10"), "evetime", "admin_pvpevent_evetime " + event_name.toLowerCase());
		appendVarEdit(sb, "Perkill item", ServerVariables.getString("PvP_" + event_name.toUpperCase() + "_item_per_kill", "0"), "iperkill", "admin_pvpevent_ipkill " + event_name.toLowerCase());
		appendVarEdit(sb, "Revive delay", ServerVariables.getString("PvP_" + event_name.toUpperCase() + "_revive_delay", "1"), "revdel", "admin_pvpevent_revdel " + event_name.toLowerCase());
		appendLongEditMenu(sb, "Class id restrict", ServerVariables.getString("PvP_" + event_name.toUpperCase() + "_prohibited_class_ids", "0"), "prohclid", "admin_pvpevent_prohclid " + event_name.toLowerCase());
	}
	
	private void appendRevardEventMenu(StringBuilder sb, String event_name, boolean team, boolean top)
	{
		if(team)
		{
			sb.append("<center>Team reward list</center><br>");
			sb.append("<center>Simple: 57:100;4037:100</center><br>");
			appendLongEditMenu(sb, "Field title", ServerVariables.getString("PvP_" + event_name.toUpperCase() + "_rev_team", ""), "trev", "admin_pvpevent_reward_team " + event_name.toLowerCase());
		}
		if(top)
		{
			sb.append("<center>Top reward list</center><br>");
			sb.append("<center>Simple: 57:100;4037:100</center><br>");
			appendLongEditMenu(sb, "Field title", ServerVariables.getString("PvP_" + event_name.toUpperCase() + "_rev_top", ""), "srev", "admin_pvpevent_reward_top " + event_name.toLowerCase());
			appendVarEdit(sb, "Hours of hero status", ServerVariables.getString("PvP_" + event_name.toUpperCase() + "_herorevhours", "0"), "hhrev", "admin_pvpevent_herorevhours " + event_name.toLowerCase());
		}
	}
	
	private enum Commands
	{
		admin_pvpevent,
		admin_pvpevent_active,
		admin_pvpevent_setanntime,
		admin_pvpevent_setannredu,
		admin_pvpevent_settime,
		admin_pvpevent_setinstsid,
		admin_pvpevent_enable,
		admin_pvpevent_capcha,
		admin_pvpevent_hwid_restrict,
		admin_pvpevent_hide_identiti,
		admin_pvpevent_dispell,
		admin_pvpevent_dispell_after,
		admin_pvpevent_reqpart,
		admin_pvpevent_maxpart,
		admin_pvpevent_minlvl,
		admin_pvpevent_maxlvl,
		admin_pvpevent_evetime,
		admin_pvpevent_ipkill,
		admin_pvpevent_herorevhours,
		admin_pvpevent_prohclid,
		admin_pvpevent_revdel,
		admin_pvpevent_reward_team,
		admin_pvpevent_reward_top,
		admin_pvpevent_start,
		admin_capt1,
		admin_tvt1,
		admin_dm1,
		admin_ctf1;
	}
}