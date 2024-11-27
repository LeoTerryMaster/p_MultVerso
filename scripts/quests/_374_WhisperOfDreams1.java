package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _374_WhisperOfDreams1 extends Quest implements ScriptFile
{
	private static final int seer_manakia = 30515;
	private static final int torai = 30557;
	private static final int cave_beast = 20620;
	private static final int death_wave = 20621;
	private static final int cave_beast_tooth = 5884;
	private static final int death_wave_light = 5885;
	private static final int sealed_mysterious_stone = 5886;
	private static final int mysterious_stone = 5887;
	
	public _374_WhisperOfDreams1()
	{
		super(true);
		addStartNpc(30515);
		addTalkId(30515, 30557);
		addKillId(20620, 20621);
		addQuestItem(5884, 5885, 5886, 5887);
	}
	
	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		int npcId = npc.getNpcId();
		if(npcId == 30515)
		{
			if(event.equalsIgnoreCase("quest_accept"))
			{
				htmltext = "seer_manakia_q0374_03.htm";
				st.setCond(1);
				st.setState(2);
				st.playSound("ItemSound.quest_accept");
			}
			else if(event.equalsIgnoreCase("reply_1"))
			{
				htmltext = "seer_manakia_q0374_06.htm";
			}
			else if(event.equalsIgnoreCase("reply_2"))
			{
				htmltext = "seer_manakia_q0374_07.htm";
				st.exitCurrentQuest(true);
				st.playSound("ItemSound.quest_finish");
			}
			else if(event.equalsIgnoreCase("reply_3") && st.getQuestItemsCount(5884) >= 65 && st.getQuestItemsCount(5885) >= 65)
			{
				st.giveItems(5486, 3);
				st.giveItems(57, 15886, true);
				st.takeItems(5884, -1);
				st.takeItems(5885, -1);
				htmltext = "seer_manakia_q0374_10.htm";
			}
			else if(event.equalsIgnoreCase("reply_4") && st.getQuestItemsCount(5884) >= 65 && st.getQuestItemsCount(5885) >= 65)
			{
				st.giveItems(5487, 2);
				st.giveItems(57, 28458, true);
				st.takeItems(5884, -1);
				st.takeItems(5885, -1);
				htmltext = "seer_manakia_q0374_10.htm";
			}
			else if(event.equalsIgnoreCase("reply_5") && st.getQuestItemsCount(5884) >= 65 && st.getQuestItemsCount(5885) >= 65)
			{
				st.giveItems(5488, 2);
				st.giveItems(57, 28458, true);
				st.takeItems(5884, -1);
				st.takeItems(5885, -1);
				htmltext = "seer_manakia_q0374_10.htm";
			}
			else if(event.equalsIgnoreCase("reply_6") && st.getQuestItemsCount(5884) >= 65 && st.getQuestItemsCount(5885) >= 65)
			{
				st.giveItems(5485, 4);
				st.giveItems(57, 28458, true);
				st.takeItems(5884, -1);
				st.takeItems(5885, -1);
				htmltext = "seer_manakia_q0374_10.htm";
			}
			else if(event.equalsIgnoreCase("reply_7") && st.getQuestItemsCount(5884) >= 65 && st.getQuestItemsCount(5885) >= 65)
			{
				st.giveItems(5489, 6);
				st.giveItems(57, 28458, true);
				st.takeItems(5884, -1);
				st.takeItems(5885, -1);
				htmltext = "seer_manakia_q0374_10.htm";
			}
		}
		if(npcId == 30557 && event.equalsIgnoreCase("reply_1") && st.getQuestItemsCount(5886) > 0)
		{
			htmltext = "torai_q0374_02.htm";
			st.setCond(3);
			st.playSound("ItemSound.quest_middle");
			st.takeItems(5886, -1);
			st.giveItems(5887, 1);
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "no-quest";
		int npcId = npc.getNpcId();
		int state = st.getState();
		int cond = st.getCond();
		switch(state)
		{
			case 1:
			{
				if(npcId != 30515)
					break;
				if(st.getPlayer().getLevel() >= 56)
				{
					htmltext = "seer_manakia_q0374_01.htm";
					break;
				}
				st.exitCurrentQuest(true);
				htmltext = "seer_manakia_q0374_02.htm";
				break;
			}
			case 2:
			{
				if(npcId == 30515)
				{
					if((st.getQuestItemsCount(5884) <= 65 && st.getQuestItemsCount(5885) < 65 || st.getQuestItemsCount(5884) < 65 && st.getQuestItemsCount(5885) <= 65) && st.getQuestItemsCount(5886) == 0)
					{
						htmltext = "seer_manakia_q0374_04.htm";
					}
					else if(st.getQuestItemsCount(5884) >= 65 && st.getQuestItemsCount(5885) >= 65 && st.getQuestItemsCount(5886) == 0)
					{
						htmltext = "seer_manakia_q0374_05.htm";
					}
					else if(cond == 1 && st.getQuestItemsCount(5886) > 0)
					{
						htmltext = "seer_manakia_q0374_08.htm";
						st.setCond(2);
						st.playSound("ItemSound.quest_middle");
					}
					else if(cond == 2 && st.getQuestItemsCount(5886) > 0)
					{
						htmltext = "seer_manakia_q0374_09.htm";
						st.playSound("ItemSound.quest_middle");
					}
				}
				if(npcId != 30557)
					break;
				if(cond == 2 && st.getQuestItemsCount(5886) > 0)
				{
					htmltext = "torai_q0374_01.htm";
				}
				if(cond != 3 || st.getQuestItemsCount(5886) <= 0)
					break;
				htmltext = "torai_q0374_03.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		if(Rnd.chance(1) && st.getState() == 2 && st.getCond() != 3 && st.getQuestItemsCount(5886) < 1)
		{
			st.rollAndGive(5886, 1, 100.0);
			st.playSound("ItemSound.quest_middle");
		}
		else if(npcId == 20620 && st.getState() == 2 && Rnd.chance(60) && st.getQuestItemsCount(5884) < 65)
		{
			st.rollAndGive(5884, 1, 100.0);
			st.playSound("ItemSound.quest_middle");
		}
		else if(npcId == 20621 && st.getState() == 2 && Rnd.chance(60) && st.getQuestItemsCount(5885) < 65)
		{
			st.rollAndGive(5885, 1, 100.0);
			st.playSound("ItemSound.quest_middle");
		}
		return null;
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
}