package npc.model.residences.clanhall;

import l2.commons.collections.CollectionUtils;
import l2.gameserver.dao.SiegeClanDAO;
import l2.gameserver.dao.SiegePlayerDAO;
import l2.gameserver.model.Player;
import l2.gameserver.model.entity.events.impl.ClanHallTeamBattleEvent;
import l2.gameserver.model.entity.events.objects.CTBSiegeClanObject;
import l2.gameserver.model.entity.events.objects.SiegeClanObject;
import l2.gameserver.model.entity.residence.ClanHall;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.pledge.Clan;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.network.l2.components.NpcString;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2.gameserver.templates.npc.NpcTemplate;
import l2.gameserver.utils.TimeUtils;
import quests._655_AGrandPlanForTamingWildBeasts;

import java.util.List;
import java.util.StringTokenizer;

public class FarmMessengerInstance extends NpcInstance
{
	public FarmMessengerInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		ClanHall clanHall = getClanHall();
		ClanHallTeamBattleEvent siegeEvent = clanHall.getSiegeEvent();
		Clan clan = player.getClan();
		if(command.equalsIgnoreCase("registrationMenu"))
		{
			if(!checkCond(player, true))
			{
				return;
			}
			showChatWindow(player, "residence2/clanhall/farm_kel_mahum_messenger_1.htm");
		}
		else if(command.equalsIgnoreCase("registerAsClan"))
		{
			if(!checkCond(player, false))
			{
				return;
			}
			List siegeClans = siegeEvent.getObjects("attackers");
			CTBSiegeClanObject siegeClan = siegeEvent.getSiegeClan("attackers", clan);
			if(siegeClan != null)
			{
				showFlagInfo(player, siegeClans.indexOf(siegeClan));
				return;
			}
			QuestState questState = player.getQuestState(_655_AGrandPlanForTamingWildBeasts.class);
			if(questState == null || questState.getQuestItemsCount(8293) != 1)
			{
				showChatWindow(player, "residence2/clanhall/farm_kel_mahum_messenger_27.htm");
				return;
			}
			questState.exitCurrentQuest(true);
			register(player);
		}
		else if(command.equalsIgnoreCase("registerAsMember"))
		{
			CTBSiegeClanObject siegeClan = siegeEvent.getSiegeClan("attackers", player.getClan());
			if(siegeClan == null)
			{
				showChatWindow(player, "residence2/clanhall/farm_kel_mahum_messenger_7.htm");
				return;
			}
			if(siegeClan.getClan().getLeaderId() == player.getObjectId())
			{
				showChatWindow(player, "residence2/clanhall/farm_kel_mahum_messenger_5.htm");
				return;
			}
			if(siegeClan.getPlayers().contains(player.getObjectId()))
			{
				showChatWindow(player, "residence2/clanhall/farm_kel_mahum_messenger_9.htm");
			}
			else
			{
				if(siegeClan.getPlayers().size() >= 18)
				{
					showChatWindow(player, "residence2/clanhall/farm_kel_mahum_messenger_8.htm");
					return;
				}
				siegeClan.getPlayers().add(player.getObjectId());
				SiegePlayerDAO.getInstance().insert(clanHall, clan.getClanId(), player.getObjectId());
				showChatWindow(player, "residence2/clanhall/farm_kel_mahum_messenger_9.htm");
			}
		}
		else if(command.startsWith("formAlliance"))
		{
			CTBSiegeClanObject siegeClan = siegeEvent.getSiegeClan("attackers", player.getClan());
			if(siegeClan == null)
			{
				showChatWindow(player, "residence2/clanhall/farm_kel_mahum_messenger_7.htm");
				return;
			}
			if(siegeClan.getClan().getLeaderId() != player.getObjectId())
			{
				showChatWindow(player, "residence2/clanhall/agit_oel_mahum_messeger_10.htm");
				return;
			}
			if(siegeClan.getParam() > 0)
			{
				return;
			}
			StringTokenizer t = new StringTokenizer(command);
			t.nextToken();
			int npcId = Integer.parseInt(t.nextToken());
			siegeClan.setParam(npcId);
			SiegeClanDAO.getInstance().update(clanHall, siegeClan);
			showChatWindow(player, "residence2/clanhall/agit_oel_mahum_messeger_9.htm");
		}
		else if(command.equalsIgnoreCase("setNpc"))
		{
			CTBSiegeClanObject siegeClan = siegeEvent.getSiegeClan("attackers", player.getClan());
			if(siegeClan == null)
			{
				showChatWindow(player, "residence2/clanhall/farm_kel_mahum_messenger_7.htm");
				return;
			}
			if(siegeClan.getClan().getLeaderId() != player.getObjectId())
			{
				showChatWindow(player, "residence2/clanhall/agit_oel_mahum_messeger_10.htm");
				return;
			}
			showChatWindow(player, npcDialog(siegeClan));
		}
		else if(command.equalsIgnoreCase("viewNpc"))
		{
			CTBSiegeClanObject siegeClan = siegeEvent.getSiegeClan("attackers", player.getClan());
			if(siegeClan == null)
			{
				showChatWindow(player, "residence2/clanhall/farm_kel_mahum_messenger_7.htm");
				return;
			}
			String file = siegeClan.getParam() == 0 ? "residence2/clanhall/agit_oel_mahum_messeger_10.htm" : npcDialog(siegeClan);
			showChatWindow(player, file);
		}
		else if(command.equalsIgnoreCase("listClans"))
		{
			NpcHtmlMessage msg = new NpcHtmlMessage(player, this);
			msg.setFile("residence2/clanhall/farm_messenger003.htm");
			List siegeClans = siegeEvent.getObjects("attackers");
			for(int i = 0;i < 5;++i)
			{
				CTBSiegeClanObject siegeClan = (CTBSiegeClanObject) CollectionUtils.safeGet(siegeClans, i);
				if(siegeClan != null)
				{
					msg.replace("%clan_" + i + "%", siegeClan.getClan().getName());
				}
				else
				{
					msg.replaceNpcString("%clan_" + i + "%", NpcString.__UNREGISTERED__);
				}
				msg.replace("%clan_count_" + i + "%", siegeClan == null ? "" : String.valueOf(siegeClan.getPlayers().size()));
			}
			player.sendPacket(msg);
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
	
	private void register(Player player)
	{
		Clan clan = player.getClan();
		ClanHall clanHall = getClanHall();
		ClanHallTeamBattleEvent siegeEvent = clanHall.getSiegeEvent();
		CTBSiegeClanObject siegeClan = new CTBSiegeClanObject("attackers", clan, 0);
		siegeClan.getPlayers().add(player.getObjectId());
		siegeEvent.addObject("attackers", siegeClan);
		SiegeClanDAO.getInstance().insert(clanHall, siegeClan);
		SiegePlayerDAO.getInstance().insert(clanHall, clan.getClanId(), player.getObjectId());
		List siegeClans = siegeEvent.getObjects("attackers");
		showFlagInfo(player, siegeClans.indexOf(siegeClan));
	}
	
	private void showFlagInfo(Player player, int index)
	{
		String file;
		switch(index)
		{
			case 0:
			{
				file = "residence2/clanhall/farm_kel_mahum_messenger_4a.htm";
				break;
			}
			case 1:
			{
				file = "residence2/clanhall/farm_kel_mahum_messenger_4b.htm";
				break;
			}
			case 2:
			{
				file = "residence2/clanhall/farm_kel_mahum_messenger_4c.htm";
				break;
			}
			case 3:
			{
				file = "residence2/clanhall/farm_kel_mahum_messenger_4d.htm";
				break;
			}
			case 4:
			{
				file = "residence2/clanhall/farm_kel_mahum_messenger_4e.htm";
				break;
			}
			default:
			{
				return;
			}
		}
		showChatWindow(player, file);
	}
	
	private String npcDialog(SiegeClanObject siegeClanObject)
	{
		String file = null;
		switch((int) siegeClanObject.getParam())
		{
			case 0:
			{
				file = "residence2/clanhall/farm_kel_mahum_messenger_6.htm";
				break;
			}
			case 35618:
			{
				file = "residence2/clanhall/farm_kel_mahum_messenger_17.htm";
				break;
			}
			case 35619:
			{
				file = "residence2/clanhall/farm_kel_mahum_messenger_18.htm";
				break;
			}
			case 35620:
			{
				file = "residence2/clanhall/farm_kel_mahum_messenger_19.htm";
				break;
			}
			case 35621:
			{
				file = "residence2/clanhall/farm_kel_mahum_messenger_20.htm";
				break;
			}
			case 35622:
			{
				file = "residence2/clanhall/farm_kel_mahum_messenger_23.htm";
			}
		}
		return file;
	}
	
	private boolean checkCond(Player player, boolean regMenu)
	{
		Clan clan = player.getClan();
		ClanHall clanHall = getClanHall();
		ClanHallTeamBattleEvent siegeEvent = clanHall.getSiegeEvent();
		List siegeClans = siegeEvent.getObjects("attackers");
		SiegeClanObject siegeClan = siegeEvent.getSiegeClan("attackers", clan);
		if(siegeEvent.isRegistrationOver())
		{
			showChatWindow(player, "quests/_655_AGrandPlanForTamingWildBeasts/farm_messenger_q0655_11.htm", "%siege_time%", TimeUtils.toSimpleFormat(clanHall.getSiegeDate()));
			return false;
		}
		if(regMenu && siegeClan != null)
		{
			return true;
		}
		if(clan == null || player.getObjectId() != clan.getLeaderId())
		{
			showChatWindow(player, "quests/_655_AGrandPlanForTamingWildBeasts/farm_messenger_q0655_03.htm");
			return false;
		}
		if(player.getObjectId() == clan.getLeaderId() && clan.getLevel() < 4)
		{
			showChatWindow(player, "quests/_655_AGrandPlanForTamingWildBeasts/farm_messenger_q0655_05.htm");
			return false;
		}
		if(clan.getHasHideout() == clanHall.getId())
		{
			showChatWindow(player, "residence2/clanhall/farm_kel_mahum_messenger_22.htm");
			return false;
		}
		if(clan.getHasHideout() > 0)
		{
			showChatWindow(player, "quests/_655_AGrandPlanForTamingWildBeasts/farm_messenger_q0655_04.htm");
			return false;
		}
		if(siegeClans.size() >= 5)
		{
			showChatWindow(player, "residence2/clanhall/farm_kel_mahum_messenger_21.htm");
			return false;
		}
		return true;
	}
	
	@Override
	public void showChatWindow(Player player, int val, Object... arg)
	{
		Clan clan = getClanHall().getOwner();
		if(clan != null)
		{
			showChatWindow(player, "residence2/clanhall/farm_messenger001.htm", "%owner_name%", clan.getName());
		}
		else
		{
			showChatWindow(player, "residence2/clanhall/farm_messenger002.htm");
		}
	}
}