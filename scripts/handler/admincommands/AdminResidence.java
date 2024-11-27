package handler.admincommands;

import l2.commons.dao.JdbcEntityState;
import l2.commons.threading.RunnableImpl;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.data.xml.holder.ResidenceHolder;
import l2.gameserver.handler.admincommands.AdminCommandHandler;
import l2.gameserver.model.Player;
import l2.gameserver.model.entity.events.impl.SiegeEvent;
import l2.gameserver.model.entity.events.objects.SiegeClanObject;
import l2.gameserver.model.entity.residence.Residence;
import l2.gameserver.model.pledge.Clan;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2.gameserver.tables.ClanTable;
import l2.gameserver.utils.HtmlUtils;

import java.io.Serializable;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class AdminResidence extends ScriptAdminCommand
{
	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;
		if(!activeChar.getPlayerAccess().CanEditNPC)
		{
			return false;
		}
		switch(command)
		{
			case admin_residence_list:
			{
				NpcHtmlMessage msg = new NpcHtmlMessage(5);
				msg.setFile("admin/residence/residence_list.htm");
				StringBuilder replyMSG = new StringBuilder(200);
				for(Residence residence : ResidenceHolder.getInstance().getResidences())
				{
					if(residence == null)
						continue;
					replyMSG.append("<tr><td>");
					replyMSG.append("<a action=\"bypass -h admin_residence ").append(residence.getId()).append("\">").append(HtmlUtils.htmlResidenceName(residence.getId())).append("</a>");
					replyMSG.append("</td><td>");
					Clan owner = residence.getOwner();
					if(owner == null)
					{
						replyMSG.append("NPC");
					}
					else
					{
						replyMSG.append(owner.getName());
					}
					replyMSG.append("</td></tr>");
				}
				msg.replace("%residence_list%", replyMSG.toString());
				activeChar.sendPacket(msg);
				break;
			}
			case admin_residence:
			{
				if(wordList.length != 2)
				{
					return false;
				}
				Residence r = ResidenceHolder.getInstance().getResidence(Integer.parseInt(wordList[1]));
				if(r == null)
				{
					return false;
				}
				SiegeEvent event = r.getSiegeEvent();
				NpcHtmlMessage msg = new NpcHtmlMessage(5);
				msg.setFile("admin/residence/siege_info.htm");
				msg.replace("%residence%", HtmlUtils.htmlResidenceName(r.getId()));
				msg.replace("%id%", String.valueOf(r.getId()));
				msg.replace("%owner%", r.getOwner() == null ? "NPC" : r.getOwner().getName());
				msg.replace("%cycle%", String.valueOf(r.getCycle()));
				msg.replace("%paid_cycle%", String.valueOf(r.getPaidCycle()));
				msg.replace("%reward_count%", String.valueOf(r.getRewardCount()));
				msg.replace("%left_time%", String.valueOf(r.getCycleDelay()));
				StringBuilder clans = new StringBuilder(100);
				for(Map.Entry<String, List<Serializable>> entry : event.getObjects().entrySet())
				{
					for(Serializable o : entry.getValue())
					{
						if(!(o instanceof SiegeClanObject))
							continue;
						SiegeClanObject siegeClanObject = (SiegeClanObject) o;
						clans.append("<tr>").append("<td>").append(siegeClanObject.getClan().getName()).append("</td>").append("<td>").append(siegeClanObject.getClan().getLeaderName()).append("</td>").append("<td>").append(siegeClanObject.getType()).append("</td>").append("</tr>");
					}
				}
				msg.replace("%clans%", clans.toString());
				msg.replace("%hour%", String.valueOf(r.getSiegeDate().get(11)));
				msg.replace("%minute%", String.valueOf(r.getSiegeDate().get(12)));
				msg.replace("%day%", String.valueOf(r.getSiegeDate().get(5)));
				msg.replace("%month%", String.valueOf(r.getSiegeDate().get(2) + 1));
				msg.replace("%year%", String.valueOf(r.getSiegeDate().get(1)));
				activeChar.sendPacket(msg);
				break;
			}
			case admin_set_owner:
			{
				if(wordList.length != 3)
				{
					return false;
				}
				Residence r = ResidenceHolder.getInstance().getResidence(Integer.parseInt(wordList[1]));
				if(r == null)
				{
					return false;
				}
				String clanName = wordList[2];
				Clan clan = ClanTable.getInstance().getClanByName(clanName);
				if(!clanName.equalsIgnoreCase("npc") && clan == null)
				{
					activeChar.sendPacket(SystemMsg.INCORRECT_NAME);
					AdminCommandHandler.getInstance().useAdminCommandHandler(activeChar, "admin_residence " + r.getId());
					return false;
				}
				SiegeEvent event = r.getSiegeEvent();
				event.clearActions();
				r.getLastSiegeDate().setTimeInMillis(clan == null ? 0 : System.currentTimeMillis());
				r.getOwnDate().setTimeInMillis(clan == null ? 0 : System.currentTimeMillis());
				r.changeOwner(clan);
				event.reCalcNextTime(false);
				break;
			}
			case admin_set_siege_time:
			{
				Residence r = ResidenceHolder.getInstance().getResidence(Integer.parseInt(wordList[1]));
				if(r == null)
				{
					return false;
				}
				Calendar calendar = (Calendar) r.getSiegeDate().clone();
				block18:
				for(int i = 2;i < wordList.length;++i)
				{
					int type;
					int val = Integer.parseInt(wordList[i]);
					switch(i)
					{
						case 2:
						{
							type = 11;
							break;
						}
						case 3:
						{
							type = 12;
							break;
						}
						case 4:
						{
							type = 5;
							break;
						}
						case 5:
						{
							type = 2;
							--val;
							break;
						}
						case 6:
						{
							type = 1;
							break;
						}
						default:
						{
							continue block18;
						}
					}
					calendar.set(type, val);
				}
				SiegeEvent event = r.getSiegeEvent();
				event.clearActions();
				r.getSiegeDate().setTimeInMillis(calendar.getTimeInMillis());
				event.registerActions();
				r.setJdbcState(JdbcEntityState.UPDATED);
				r.update();
				AdminCommandHandler.getInstance().useAdminCommandHandler(activeChar, "admin_residence " + r.getId());
				break;
			}
			case admin_quick_siege_start:
			{
				Residence r = ResidenceHolder.getInstance().getResidence(Integer.parseInt(wordList[1]));
				if(r == null)
				{
					return false;
				}
				Calendar calendar = Calendar.getInstance();
				if(wordList.length >= 3)
				{
					calendar.set(13, -Integer.parseInt(wordList[2]));
				}
				SiegeEvent event = r.getSiegeEvent();
				event.clearActions();
				r.getSiegeDate().setTimeInMillis(calendar.getTimeInMillis());
				event.registerActions();
				r.setJdbcState(JdbcEntityState.UPDATED);
				r.update();
				AdminCommandHandler.getInstance().useAdminCommandHandler(activeChar, "admin_residence " + r.getId());
				break;
			}
			case admin_quick_siege_stop:
			{
				Residence r = ResidenceHolder.getInstance().getResidence(Integer.parseInt(wordList[1]));
				if(r == null)
				{
					return false;
				}
				SiegeEvent event = r.getSiegeEvent();
				event.clearActions();
				ThreadPoolManager.getInstance().execute(new RunnableImpl()
				{
					
					@Override
					public void runImpl() throws Exception
					{
						event.stopEvent();
					}
				});
				AdminCommandHandler.getInstance().useAdminCommandHandler(activeChar, "admin_residence " + r.getId());
			}
		}
		return true;
	}
	
	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}
	
	private enum Commands
	{
		admin_residence_list,
		admin_residence,
		admin_set_owner,
		admin_set_siege_time,
		admin_quick_siege_start,
		admin_quick_siege_stop;
	}
}