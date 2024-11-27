package l2.gameserver.model.instances;

import l2.gameserver.Config;
import l2.gameserver.cache.Msg;
import l2.gameserver.data.xml.holder.MultiSellHolder;
import l2.gameserver.model.Player;
import l2.gameserver.model.entity.oly.CompetitionController;
import l2.gameserver.model.entity.oly.CompetitionType;
import l2.gameserver.model.entity.oly.HeroController;
import l2.gameserver.model.entity.oly.NoblesController;
import l2.gameserver.model.entity.oly.OlyController;
import l2.gameserver.model.entity.oly.ParticipantPool;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.ExHeroList;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.network.l2.s2c.SystemMessage2;
import l2.gameserver.templates.npc.NpcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class OlympiadManagerInstance extends NpcInstance
{
	private static final Logger _log = LoggerFactory.getLogger(OlympiadManagerInstance.class);
	private static final int OLYMPIAD_MANAGER_ID = 31688;
	private static final int[] OLYMPIAD_MONUMENT_IDS = {31690, 31769, 31770, 31771, 31772};
	private static final int HERO_CIRCLE = 6842;
	private static final int[] TOP_RANK_CLOAKS;
	
	static
	{
		Arrays.sort(OLYMPIAD_MONUMENT_IDS);
		TOP_RANK_CLOAKS = new int[] {31274, 31275, 31275};
	}
	
	public OlympiadManagerInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	private Player[] createTeam(Player leader)
	{
		if(leader.getParty() == null)
		{
			leader.sendPacket(Msg.THE_REQUEST_CANNOT_BE_MADE_BECAUSE_THE_REQUIREMENTS_HAVE_NOT_BEEN_MET_TO_PARTICIPATE_IN_A_TEAM);
			return null;
		}
		if(!leader.getParty().isLeader(leader))
		{
			leader.sendPacket(Msg.ONLY_A_PARTY_LEADER_CAN_REQUEST_A_TEAM_MATCH);
			return null;
		}
		if(!checkMatchLimit(leader, CompetitionType.TEAM_CLASS_FREE))
		{
			return null;
		}
		Player[] ret = new Player[3];
		ret[0] = leader;
		int i = 0;
		for(Player pm : leader.getParty().getPartyMembers())
		{
			if(!checkMatchLimit(pm, CompetitionType.TEAM_CLASS_FREE))
			{
				return null;
			}
			if(pm == leader)
				continue;
			if(++i < 3)
			{
				ret[i] = pm;
				continue;
			}
			leader.sendPacket(Msg.THE_REQUEST_CANNOT_BE_MADE_BECAUSE_THE_REQUIREMENTS_HAVE_NOT_BEEN_MET_TO_PARTICIPATE_IN_A_TEAM);
			return null;
		}
		return ret;
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
		{
			return;
		}
		if(!Config.OLY_ENABLED)
		{
			return;
		}
		if(command.startsWith("oly hwpn_"))
		{
			boolean already_got = false;
			int item_id = Integer.parseInt(command.substring(9));
			int reward_id = 0;
			for(int hid : HeroController.HERO_WEAPONS)
			{
				if(player.getInventory().getItemByItemId(hid) != null || player.getWarehouse().getCountOf(hid) > 0)
				{
					already_got = true;
				}
				if(hid != item_id)
					continue;
				reward_id = hid;
			}
			if(already_got)
			{
				showChatWindow(player, 51);
			}
			if(!player.isHero() || reward_id <= 0)
				return;
			player.getInventory().addItem(reward_id, 1);
			player.sendPacket(SystemMessage2.obtainItems(reward_id, 1, 0));
			return;
		}
		if(command.startsWith("oly "))
		{
			int cmdID = Integer.parseInt(command.substring(4));
			if(getNpcId() == 31688)
			{
				SystemMessage sm;
				switch(cmdID)
				{
					case 100:
					{
						if(OlyController.getInstance().isRegAllowed())
						{
							NpcHtmlMessage html = new NpcHtmlMessage(player, this);
							if(!ParticipantPool.getInstance().isRegistred(player) && !player.isOlyParticipant())
							{
								html.setFile("oly/olympiad_operator100.htm");
								html.replace("%period%", String.valueOf(OlyController.getInstance().getCurrentPeriod()));
								html.replace("%season%", String.valueOf(OlyController.getInstance().getCurrentSeason()));
								html.replace("%particicnt%", String.valueOf(OlyController.getInstance().getPartCount()));
								html.replace("%currpartcnt%", String.valueOf(ParticipantPool.getInstance().getParticipantCount()));
							}
							else
							{
								html.setFile("oly/olympiad_operator110.htm");
							}
							player.sendPacket(html);
							return;
						}
						else
						{
							player.sendPacket(Msg.THE_OLYMPIAD_GAME_IS_NOT_CURRENTLY_IN_PROGRESS);
						}
						return;
					}
					case 101:
					{
						if(OlyController.getInstance().isRegAllowed())
						{
							if(!checkMatchLimit(player, CompetitionType.CLASS_INDIVIDUAL))
								return;
							player.sendPacket(Msg.THE_OLYMPIAD_GAME_IS_NOT_CURRENTLY_IN_PROGRESS);
							sm = CompetitionController.getInstance().AddParticipationRequest(CompetitionType.CLASS_INDIVIDUAL, new Player[] {player});
							if(sm == null)
								return;
							player.sendPacket(sm);
							return;
						}
						else
						{
							player.sendPacket(Msg.THE_OLYMPIAD_GAME_IS_NOT_CURRENTLY_IN_PROGRESS);
						}
						return;
					}
					case 102:
					{
						if(OlyController.getInstance().isRegAllowed())
						{
							if(!checkMatchLimit(player, CompetitionType.CLASS_FREE) || (sm = CompetitionController.getInstance().AddParticipationRequest(CompetitionType.CLASS_FREE, new Player[] {player})) == null)
								return;
							player.sendPacket(sm);
							return;
						}
						else
						{
							player.sendPacket(Msg.THE_OLYMPIAD_GAME_IS_NOT_CURRENTLY_IN_PROGRESS);
						}
						return;
					}
					case 103:
					{
						if(OlyController.getInstance().isRegAllowed())
						{
							Player[] participants = createTeam(player);
							if(participants == null || (sm = CompetitionController.getInstance().AddParticipationRequest(CompetitionType.TEAM_CLASS_FREE, participants)) == null)
								return;
							player.sendPacket(sm);
							return;
						}
						else
						{
							player.sendPacket(Msg.THE_OLYMPIAD_GAME_IS_NOT_CURRENTLY_IN_PROGRESS);
						}
						return;
					}
					case 104:
					{
						if(OlyController.getInstance().isRegAllowed())
						{
							CompetitionType ctype = ParticipantPool.getInstance().getCompTypeOf(player);
							if(ctype == null)
								return;
							ParticipantPool.getInstance().removeEntryByPlayer(ctype, player);
							player.sendPacket(Msg.YOU_HAVE_BEEN_DELETED_FROM_THE_WAITING_LIST_OF_A_GAME);
							return;
						}
						else
						{
							player.sendPacket(Msg.THE_OLYMPIAD_GAME_IS_NOT_CURRENTLY_IN_PROGRESS);
						}
						return;
					}
					case 200:
					{
						if(OlyController.getInstance().isRegAllowed())
						{
							CompetitionController.getInstance().showCompetitionList(player);
							return;
						}
						else
						{
							player.sendPacket(Msg.THE_OLYMPIAD_GAME_IS_NOT_CURRENTLY_IN_PROGRESS);
						}
						return;
					}
					case 301:
					{
						NpcHtmlMessage phtml = new NpcHtmlMessage(player, this);
						int rcount = NoblesController.getInstance().getNoblessePasses(player);
						if(rcount > 0)
						{
							phtml.setFile(getHtmlPath(getNpcId(), 301, player));
							phtml.replace("%points%", String.valueOf(rcount));
							player.getInventory().addItem(Config.OLY_VICTORY_RITEMID, rcount);
							player.sendPacket(SystemMessage2.obtainItems(Config.OLY_VICTORY_RITEMID, rcount, 0));
						}
						else
						{
							phtml.setFile(getHtmlPath(getNpcId(), 302, player));
						}
						player.sendPacket(phtml);
						return;
					}
					case 1902:
					case 1903:
					{
						MultiSellHolder.getInstance().SeparateAndSend(cmdID, player, 0.0);
						return;
					}
				}
				if(cmdID >= 588 && cmdID <= 634)
				{
					NpcHtmlMessage rhtml = new NpcHtmlMessage(player, this);
					rhtml.setFile("oly/olympiad_operator_rank_class.htm");
					int class_id = cmdID - 500;
					String[] rlist = NoblesController.getInstance().getClassLeaders(class_id);
					for(int i = 0;i < 15;++i)
					{
						String Rank;
						String Name2;
						if(i < rlist.length)
						{
							Name2 = rlist[i];
							Rank = String.valueOf(i + 1);
						}
						else
						{
							Name2 = "";
							Rank = "";
						}
						rhtml.replace("<?Rank" + (i + 1) + "?>", Rank);
						rhtml.replace("<?Name" + (i + 1) + "?>", Name2);
					}
					player.sendPacket(rhtml);
					return;
				}
				else
				{
					showChatWindow(player, cmdID);
				}
				return;
			}
			if(Arrays.binarySearch(OLYMPIAD_MONUMENT_IDS, getNpcId()) < 0)
				return;
			if(cmdID == 1000)
			{
				player.sendPacket(new ExHeroList());
				return;
			}
			else if(cmdID == 2000)
			{
				if(OlyController.getInstance().isCalculationPeriod())
				{
					showChatWindow(player, 11);
					return;
				}
				else if(HeroController.getInstance().isInactiveHero(player))
				{
					HeroController.getInstance().activateHero(player);
					showChatWindow(player, 10);
					return;
				}
				else
				{
					showChatWindow(player, 1);
				}
				return;
			}
			else if(cmdID == 3)
			{
				if(player.isHero())
				{
					if(player.getInventory().getItemByItemId(6842) != null || player.getWarehouse().getCountOf(6842) > 0)
					{
						showChatWindow(player, 55);
						return;
					}
					else
					{
						player.getInventory().addItem(6842, 1);
						player.sendPacket(SystemMessage2.obtainItems(6842, 1, 0));
					}
					return;
				}
				else
				{
					showChatWindow(player, 3);
				}
				return;
			}
			else if(cmdID == 4)
			{
				if(player.isHero())
				{
					boolean already_got = false;
					for(int hid : HeroController.HERO_WEAPONS)
					{
						if(player.getInventory().getItemByItemId(hid) == null && player.getWarehouse().getCountOf(hid) <= 0)
							continue;
						already_got = true;
					}
					showChatWindow(player, already_got ? 51 : 50);
					return;
				}
				else
				{
					showChatWindow(player, 4);
				}
				return;
			}
			else if(cmdID == 5)
			{
				if(player.isNoble())
				{
					int rank = NoblesController.getInstance().getPlayerClassRank(player.getBaseClassId(), player.getObjectId());
					if(rank < 0 || rank >= TOP_RANK_CLOAKS.length)
					{
						showChatWindow(player, 5);
						return;
					}
					else
					{
						int cloakItemId = TOP_RANK_CLOAKS[rank];
						if(player.getInventory().getItemByItemId(cloakItemId) != null || player.getWarehouse().getCountOf(cloakItemId) > 0)
						{
							showChatWindow(player, 5);
							return;
						}
						else
						{
							player.getInventory().addItem(cloakItemId, 1);
							player.sendPacket(SystemMessage2.obtainItems(cloakItemId, 1, 0));
						}
					}
					return;
				}
				else
				{
					showChatWindow(player, 5);
				}
				return;
			}
			else
			{
				showChatWindow(player, cmdID);
			}
			return;
		}
		super.onBypassFeedback(player, command);
	}
	
	@Override
	public void showChatWindow(Player player, int val, Object... replace)
	{
		if(!(val != 0 && val != 100 || player.isNoble()))
		{
			super.showChatWindow(player, 900);
		}
		else
		{
			super.showChatWindow(player, val);
		}
	}
	
	@Override
	public String getHtmlPath(int npcId, int val, Player player)
	{
		return String.format(getNpcId() == 31688 ? "oly/olympiad_operator%03d.htm" : "oly/olympiad_monument%03d.htm", val);
	}
	
	private boolean checkMatchLimit(Player player, CompetitionType type)
	{
		if(player == null)
		{
			return false;
		}
		NoblesController.NobleRecord nr = NoblesController.getInstance().getNobleRecord(player.getObjectId());
		if(nr.class_based_cnt + nr.class_free_cnt + nr.team_cnt > Config.OLY_MAX_TOTAL_MATCHES)
		{
			player.sendPacket(SystemMsg.THE_MAXIMUM_MATCHES_YOU_CAN_PARTICIPATE_IN_1_WEEK_IS_70);
			return false;
		}
		switch(type)
		{
			case CLASS_FREE:
			{
				if(nr.class_free_cnt <= Config.OLY_CF_MATCHES)
					break;
				player.sendPacket(SystemMsg.THE_TOTAL_NUMBER_OF_MATCHES_THAT_CAN_BE_ENTERED_IN_1_WEEK_IS_60_CLASS_IRRELEVANT_INDIVIDUAL_MATCHES_30_SPECIFIC_MATCHES_AND_10_TEAM_MATCHES);
				return false;
			}
			case CLASS_INDIVIDUAL:
			{
				if(nr.class_based_cnt <= Config.OLY_CB_MATCHES)
					break;
				player.sendPacket(SystemMsg.THE_TOTAL_NUMBER_OF_MATCHES_THAT_CAN_BE_ENTERED_IN_1_WEEK_IS_60_CLASS_IRRELEVANT_INDIVIDUAL_MATCHES_30_SPECIFIC_MATCHES_AND_10_TEAM_MATCHES);
				return false;
			}
			case TEAM_CLASS_FREE:
			{
				if(nr.team_cnt <= Config.OLY_TB_MATCHES)
					break;
				player.sendPacket(SystemMsg.THE_TOTAL_NUMBER_OF_MATCHES_THAT_CAN_BE_ENTERED_IN_1_WEEK_IS_60_CLASS_IRRELEVANT_INDIVIDUAL_MATCHES_30_SPECIFIC_MATCHES_AND_10_TEAM_MATCHES);
				return false;
			}
		}
		return true;
	}
}