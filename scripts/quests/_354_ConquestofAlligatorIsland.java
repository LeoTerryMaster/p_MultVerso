package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _354_ConquestofAlligatorIsland extends Quest implements ScriptFile
{
	private static final int warehouse_keeper_kluck = 30895;
	private static final int crokian_lad = 20804;
	private static final int dailaon_lad = 20805;
	private static final int crokian_lad_warrior = 20806;
	private static final int farhite_lad = 20807;
	private static final int nos_lad = 20808;
	private static final int tribe_of_swamp = 20991;
	private static final int croc_tooth = 5863;
	private static final int mysterious_map_piece = 5864;
	private static final int pirates_treasure_map = 5915;
	
	public _354_ConquestofAlligatorIsland()
	{
		super(false);
		addStartNpc(30895);
		addKillId(20804, 20805, 20806, 20807, 20808, 20991);
		addQuestItem(5863, 5864);
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
		if(npcId == 30895)
		{
			if(event.equalsIgnoreCase("quest_accept"))
			{
				st.setCond(1);
				st.setState(2);
				st.playSound("ItemSound.quest_accept");
				htmltext = "warehouse_keeper_kluck_q0354_03.htm";
			}
			else if(event.equalsIgnoreCase("reply_1"))
			{
				if(st.getQuestItemsCount(5863) >= 100)
				{
					st.giveItems(57, st.getQuestItemsCount(5863) * 220 + 10700);
					st.takeItems(5863, -1);
					htmltext = "warehouse_keeper_kluck_q0354_06b.htm";
				}
				else if(st.getQuestItemsCount(5863) > 0 && st.getQuestItemsCount(5863) < 100)
				{
					st.giveItems(57, st.getQuestItemsCount(5863) * 220 + 3100);
					st.takeItems(5863, st.getQuestItemsCount(5863));
					htmltext = "warehouse_keeper_kluck_q0354_06a.htm";
				}
				else if(st.getQuestItemsCount(5863) == 0)
				{
					htmltext = "warehouse_keeper_kluck_q0354_06.htm";
				}
			}
			else if(event.equalsIgnoreCase("reply_2"))
			{
				htmltext = "warehouse_keeper_kluck_q0354_07.htm";
			}
			else if(event.equalsIgnoreCase("reply_3"))
			{
				st.playSound("ItemSound.quest_finish");
				st.exitCurrentQuest(true);
				htmltext = "warehouse_keeper_kluck_q0354_08.htm";
			}
			else if(event.equalsIgnoreCase("reply_4") && st.getQuestItemsCount(5864) > 0 && st.getQuestItemsCount(5864) < 10)
			{
				htmltext = "warehouse_keeper_kluck_q0354_09.htm";
			}
			else if(event.equalsIgnoreCase("reply_4") && st.getQuestItemsCount(5864) >= 10)
			{
				st.giveItems(5915, 1);
				st.takeItems(5864, 10);
				htmltext = "warehouse_keeper_kluck_q0354_10.htm";
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
				if(npcId != 30895)
					break;
				if(st.getPlayer().getLevel() < 38)
				{
					htmltext = "warehouse_keeper_kluck_q0354_01.htm";
					st.exitCurrentQuest(true);
					break;
				}
				htmltext = "warehouse_keeper_kluck_q0354_02.htm";
				break;
			}
			case 2:
			{
				if(npcId != 30895)
					break;
				if(st.getQuestItemsCount(5864) == 0)
				{
					htmltext = "warehouse_keeper_kluck_q0354_04.htm";
					break;
				}
				if(st.getQuestItemsCount(5864) < 1)
					break;
				htmltext = "warehouse_keeper_kluck_q0354_05.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		if(npcId == 20804)
		{
			if(Rnd.get(100) < 84)
			{
				st.giveItems(5863, 1);
				st.playSound("ItemSound.quest_itemget");
			}
			if(Rnd.get(10) == 5)
			{
				st.giveItems(5864, 1);
			}
		}
		else if(npcId == 20805)
		{
			if(Rnd.get(100) < 91)
			{
				st.giveItems(5863, 1);
				st.playSound("ItemSound.quest_itemget");
			}
			if(Rnd.get(10) == 5)
			{
				st.giveItems(5864, 1);
			}
		}
		else if(npcId == 20806)
		{
			if(Rnd.get(100) < 88)
			{
				st.giveItems(5863, 1);
				st.playSound("ItemSound.quest_itemget");
			}
			if(Rnd.get(10) == 5)
			{
				st.giveItems(5864, 1);
			}
		}
		else if(npcId == 20807)
		{
			if(Rnd.get(100) < 92)
			{
				st.giveItems(5863, 1);
				st.playSound("ItemSound.quest_itemget");
			}
			if(Rnd.get(10) == 5)
			{
				st.giveItems(5864, 1);
			}
		}
		else if(npcId == 20808)
		{
			if(Rnd.get(100) < 14)
			{
				st.giveItems(5863, 2);
				st.playSound("ItemSound.quest_itemget");
			}
			else
			{
				st.giveItems(5863, 1);
				st.playSound("ItemSound.quest_itemget");
			}
			if(Rnd.get(10) == 5)
			{
				st.giveItems(5864, 1);
			}
		}
		else if(npcId == 20991)
		{
			if(Rnd.get(100) < 69)
			{
				st.giveItems(5863, 2);
				st.playSound("ItemSound.quest_itemget");
			}
			else
			{
				st.giveItems(5863, 1);
				st.playSound("ItemSound.quest_itemget");
			}
			if(Rnd.get(10) == 5)
			{
				st.giveItems(5864, 1);
			}
		}
		return null;
	}
}