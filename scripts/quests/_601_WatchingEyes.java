package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _601_WatchingEyes extends Quest implements ScriptFile
{
	private static final int eye_of_argos = 31683;
	private static final int apostle_defender = 21306;
	private static final int apostle_avenger = 21308;
	private static final int apostle_avenger_a = 21309;
	private static final int apostle_magistrate = 21310;
	private static final int apostle_magistrate_a = 21311;
	private static final int sealed_ring_of_aurakyria_gem = 6699;
	private static final int sealed_sanddragons_earing_piece = 6698;
	private static final int sealed_dragon_necklace_wire = 6700;
	private static final int q_proof_of_avenger = 7188;
	
	public _601_WatchingEyes()
	{
		super(false);
		addStartNpc(31683);
		addKillId(21306, 21308, 21309, 21310, 21311);
		addQuestItem(q_proof_of_avenger);
	}
	
	@Override
	public void onLoad()
	{
	}
	
	@Override
	public void onReload()
	{
	}
	
	@Override
	public void onShutdown()
	{
	}
	
	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("quest_accept"))
		{
			st.setCond(1);
			st.set("argoss_favor", String.valueOf(11), true);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
			htmltext = "eye_of_argos_q0601_0104.htm";
		}
		else if(event.equalsIgnoreCase("reply_3"))
		{
			if(st.getQuestItemsCount(q_proof_of_avenger) >= 100)
			{
				int i1 = Rnd.get(1000);
				st.takeItems(q_proof_of_avenger, 100);
				if(i1 < 200)
				{
					st.giveItems(6699, 5);
					st.giveItems(57, 90000);
					st.addExpAndSp(120000, 10000);
				}
				else if(i1 < 400)
				{
					st.giveItems(6698, 5);
					st.giveItems(57, 80000);
					st.addExpAndSp(120000, 10000);
				}
				else if(i1 < 500)
				{
					st.giveItems(6700, 5);
					st.giveItems(57, 40000);
					st.addExpAndSp(120000, 10000);
				}
				else if(i1 < 1000)
				{
					st.giveItems(57, 230000);
				}
				st.unset("argoss_favor");
				st.playSound("ItemSound.quest_finish");
				st.exitCurrentQuest(true);
				htmltext = "eye_of_argos_q0601_0201.htm";
			}
			else
			{
				htmltext = "eye_of_argos_q0601_0202.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "no-quest";
		int GetMemoState = st.getInt("argoss_favor");
		int npcId = npc.getNpcId();
		int id = st.getState();
		switch(id)
		{
			case 1:
			{
				if(npcId != 31683)
					break;
				if(st.getPlayer().getLevel() >= 71)
				{
					htmltext = "eye_of_argos_q0601_0101.htm";
					break;
				}
				htmltext = "eye_of_argos_q0601_0103.htm";
				st.exitCurrentQuest(true);
				break;
			}
			case 2:
			{
				if(npcId != 31683 || GetMemoState < 11 || GetMemoState > 12)
					break;
				if(GetMemoState == 12 && st.getQuestItemsCount(q_proof_of_avenger) >= 100)
				{
					htmltext = "eye_of_argos_q0601_0105.htm";
					break;
				}
				htmltext = "eye_of_argos_q0601_0106.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int GetMemoState = st.getInt("argoss_favor");
		int npcId = npc.getNpcId();
		if(GetMemoState == 11)
		{
			if(npcId == 21306)
			{
				int i42 = Rnd.get(1000);
				if(i42 < 850)
				{
					if(st.getQuestItemsCount(q_proof_of_avenger) + 1 >= 100)
					{
						if(st.getQuestItemsCount(q_proof_of_avenger) < 100)
						{
							st.setCond(2);
							st.set("argoss_favor", String.valueOf(12), true);
							st.giveItems(q_proof_of_avenger, 100 - st.getQuestItemsCount(q_proof_of_avenger));
							st.playSound("ItemSound.quest_middle");
						}
					}
					else
					{
						st.giveItems(q_proof_of_avenger, 1);
						st.playSound("ItemSound.quest_itemget");
					}
				}
			}
			else if(npcId == 21308)
			{
				int i43 = Rnd.get(1000);
				if(i43 < 790)
				{
					if(st.getQuestItemsCount(q_proof_of_avenger) + 1 >= 100)
					{
						if(st.getQuestItemsCount(q_proof_of_avenger) < 100)
						{
							st.setCond(2);
							st.set("argoss_favor", String.valueOf(12), true);
							st.giveItems(q_proof_of_avenger, 100 - st.getQuestItemsCount(q_proof_of_avenger));
							st.playSound("ItemSound.quest_middle");
						}
					}
					else
					{
						st.giveItems(q_proof_of_avenger, 1);
						st.playSound("ItemSound.quest_itemget");
					}
				}
			}
			else if(npcId == 21309)
			{
				int i44 = Rnd.get(1000);
				if(i44 < 820)
				{
					if(st.getQuestItemsCount(q_proof_of_avenger) + 1 >= 100)
					{
						if(st.getQuestItemsCount(q_proof_of_avenger) < 100)
						{
							st.setCond(2);
							st.set("argoss_favor", String.valueOf(12), true);
							st.giveItems(q_proof_of_avenger, 100 - st.getQuestItemsCount(q_proof_of_avenger));
							st.playSound("ItemSound.quest_middle");
						}
					}
					else
					{
						st.giveItems(q_proof_of_avenger, 1);
						st.playSound("ItemSound.quest_itemget");
					}
				}
			}
			else if(npcId == 21310)
			{
				int i45 = Rnd.get(1000);
				if(i45 < 680)
				{
					if(st.getQuestItemsCount(q_proof_of_avenger) + 1 >= 100)
					{
						if(st.getQuestItemsCount(q_proof_of_avenger) < 100)
						{
							st.setCond(2);
							st.set("argoss_favor", String.valueOf(12), true);
							st.giveItems(q_proof_of_avenger, 100 - st.getQuestItemsCount(q_proof_of_avenger));
							st.playSound("ItemSound.quest_middle");
						}
					}
					else
					{
						st.giveItems(q_proof_of_avenger, 1);
						st.playSound("ItemSound.quest_itemget");
					}
				}
			}
			else if(npcId == 21311 && Rnd.get(1000) < 630)
			{
				if(st.getQuestItemsCount(q_proof_of_avenger) + 1 >= 100)
				{
					if(st.getQuestItemsCount(q_proof_of_avenger) < 100)
					{
						st.setCond(2);
						st.set("argoss_favor", String.valueOf(12), true);
						st.giveItems(q_proof_of_avenger, 100 - st.getQuestItemsCount(q_proof_of_avenger));
						st.playSound("ItemSound.quest_middle");
					}
				}
				else
				{
					st.giveItems(q_proof_of_avenger, 1);
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		return null;
	}
}