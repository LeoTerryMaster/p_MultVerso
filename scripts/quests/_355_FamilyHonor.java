package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _355_FamilyHonor extends Quest implements ScriptFile
{
	private static final int galicbredo = 30181;
	private static final int patrin = 30929;
	private static final int timak_orc_troop_leader = 20767;
	private static final int timak_orc_troop_shaman = 20768;
	private static final int timak_orc_troop_warrior = 20769;
	private static final int timak_orc_troop_archer = 20770;
	private static final int q_ancient_portrait = 4252;
	private static final int q_beronas_sculpture_0 = 4350;
	private static final int q_beronas_sculpture_s = 4351;
	private static final int q_beronas_sculpture_a = 4352;
	private static final int q_beronas_sculpture_b = 4353;
	private static final int q_beronas_sculpture_c = 4354;
	
	public _355_FamilyHonor()
	{
		super(false);
		addStartNpc(30181);
		addTalkId(30929);
		addKillId(20767, 20768, 20769, 20770);
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
		int npcId = npc.getNpcId();
		if(npcId == 30181)
		{
			if(event.equalsIgnoreCase("quest_accept"))
			{
				st.setCond(1);
				st.setState(2);
				st.playSound("ItemSound.quest_accept");
				htmltext = "galicbredo_q0355_04.htm";
			}
			else if(event.equalsIgnoreCase("reply_1"))
			{
				htmltext = "galicbredo_q0355_03.htm";
			}
			else if(event.equalsIgnoreCase("reply_2"))
			{
				if(st.getQuestItemsCount(4252) < 1)
				{
					htmltext = "galicbredo_q0355_07.htm";
				}
				else if(st.getQuestItemsCount(4252) >= 100)
				{
					long cnt = 7800 + st.getQuestItemsCount(4252) * 120;
					st.takeItems(4252, -1);
					st.giveItems(57, cnt);
					htmltext = "galicbredo_q0355_07b.htm";
				}
				else if(st.getQuestItemsCount(4252) >= 1 && st.getQuestItemsCount(4252) < 100)
				{
					st.giveItems(57, st.getQuestItemsCount(4252) * 120 + 2800);
					st.takeItems(4252, -1);
					htmltext = "galicbredo_q0355_07a.htm";
				}
			}
			else if(event.equalsIgnoreCase("reply_3"))
			{
				htmltext = "galicbredo_q0355_08.htm";
			}
			else if(event.equalsIgnoreCase("reply_4"))
			{
				if(st.getQuestItemsCount(4252) > 0)
				{
					st.giveItems(57, st.getQuestItemsCount(4252) * 120);
				}
				st.takeItems(4252, -1);
				st.playSound("ItemSound.quest_finish");
				st.exitCurrentQuest(true);
				htmltext = "galicbredo_q0355_09.htm";
			}
		}
		else if(npcId == 30929 && event.equalsIgnoreCase("reply_1"))
		{
			int i0 = Rnd.get(100);
			if(st.getQuestItemsCount(4350) < 1)
			{
				htmltext = "patrin_q0355_02.htm";
			}
			else if(st.getQuestItemsCount(4350) >= 1 && i0 < 2)
			{
				st.giveItems(4351, 1);
				st.takeItems(4350, 1);
				htmltext = "patrin_q0355_03.htm";
			}
			else if(st.getQuestItemsCount(4350) >= 1 && i0 < 32)
			{
				st.giveItems(4352, 1);
				st.takeItems(4350, 1);
				htmltext = "patrin_q0355_04.htm";
			}
			else if(st.getQuestItemsCount(4350) >= 1 && i0 < 62)
			{
				st.giveItems(4353, 1);
				st.takeItems(4350, 1);
				htmltext = "patrin_q0355_05.htm";
			}
			else if(st.getQuestItemsCount(4350) >= 1 && i0 < 77)
			{
				st.giveItems(4354, 1);
				st.takeItems(4350, 1);
				htmltext = "patrin_q0355_06.htm";
			}
			else
			{
				st.takeItems(4350, 1);
				htmltext = "patrin_q0355_07.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "no-quest";
		int npcId = npc.getNpcId();
		int id = st.getState();
		switch(id)
		{
			case 1:
			{
				if(npcId != 30181)
					break;
				if(st.getPlayer().getLevel() < 36)
				{
					st.exitCurrentQuest(true);
					htmltext = "galicbredo_q0355_01.htm";
					break;
				}
				htmltext = "galicbredo_q0355_02.htm";
				break;
			}
			case 2:
			{
				if(npcId == 30181)
				{
					if(st.getQuestItemsCount(4350) < 1)
					{
						htmltext = "galicbredo_q0355_05.htm";
						break;
					}
					if(st.getQuestItemsCount(4350) < 1)
						break;
					htmltext = "galicbredo_q0355_06.htm";
					break;
				}
				if(npcId != 30929 || id != 2)
					break;
				htmltext = "patrin_q0355_01.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int id = st.getState();
		if(id == 2)
		{
			if(npcId == 20767)
			{
				int i0 = Rnd.get(1000);
				if(i0 < 560)
				{
					st.giveItems(4252, 1);
					st.playSound("ItemSound.quest_itemget");
				}
				else if(i0 < 684)
				{
					st.giveItems(4350, 1);
					st.playSound("ItemSound.quest_itemget");
				}
			}
			else if(npcId == 20768)
			{
				int i0 = Rnd.get(100);
				if(i0 < 53)
				{
					st.giveItems(4252, 1);
					st.playSound("ItemSound.quest_itemget");
				}
				else if(i0 < 65)
				{
					st.giveItems(4350, 1);
					st.playSound("ItemSound.quest_itemget");
				}
			}
			else if(npcId == 20769)
			{
				int i0 = Rnd.get(1000);
				if(i0 < 420)
				{
					st.giveItems(4252, 1);
					st.playSound("ItemSound.quest_itemget");
				}
				else if(i0 < 516)
				{
					st.giveItems(4350, 1);
					st.playSound("ItemSound.quest_itemget");
				}
			}
			else if(npcId == 20770)
			{
				int i0 = Rnd.get(100);
				if(i0 < 44)
				{
					st.giveItems(4252, 1);
					st.playSound("ItemSound.quest_itemget");
				}
				else if(i0 < 56)
				{
					st.giveItems(4350, 1);
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		return null;
	}
}