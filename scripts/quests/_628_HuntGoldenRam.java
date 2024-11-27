package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _628_HuntGoldenRam extends Quest implements ScriptFile
{
	private static final int merc_kahmun = 31554;
	private static final int splinter_stakato = 21508;
	private static final int splinter_stakato_worker = 21509;
	private static final int splinter_stakato_soldier = 21510;
	private static final int splinter_stakato_drone = 21511;
	private static final int splinter_stakato_drone_a = 21512;
	private static final int needle_stakato = 21513;
	private static final int needle_stakato_worker = 21514;
	private static final int needle_stakato_soldier = 21515;
	private static final int needle_stakato_drone = 21516;
	private static final int needle_stakato_drone_a = 21517;
	private static final int q_goldenram_badge1 = 7246;
	private static final int q_goldenram_badge2 = 7247;
	private static final int q_splinter_chitin = 7248;
	private static final int q_needle_chitin = 7249;
	
	public _628_HuntGoldenRam()
	{
		super(true);
		addStartNpc(31554);
		addKillId(21508, 21509, 21510, 21511, 21512, 21513, 21514, 21515, 21516, 21517);
		addQuestItem(q_splinter_chitin, q_needle_chitin);
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
			if(st.getQuestItemsCount(q_goldenram_badge1) < 1 && st.getQuestItemsCount(q_goldenram_badge2) < 1)
			{
				st.setCond(1);
				st.setState(2);
				st.playSound("ItemSound.quest_accept");
				htmltext = "merc_kahmun_q0628_03.htm";
			}
			else if(st.getQuestItemsCount(q_goldenram_badge1) >= 1 && st.getQuestItemsCount(q_goldenram_badge2) < 1)
			{
				st.setCond(2);
				st.setState(2);
				st.playSound("ItemSound.quest_accept");
				htmltext = "merc_kahmun_q0628_04.htm";
			}
			else if(st.getQuestItemsCount(q_goldenram_badge2) >= 1)
			{
				st.setCond(3);
				st.setState(2);
				st.playSound("ItemSound.quest_accept");
				htmltext = "merc_kahmun_q0628_05.htm";
			}
		}
		else if(event.equalsIgnoreCase("reply_1"))
		{
			if(st.getQuestItemsCount(q_goldenram_badge1) < 1 && st.getQuestItemsCount(q_goldenram_badge2) < 1 && st.getQuestItemsCount(q_splinter_chitin) >= 100)
			{
				st.setCond(2);
				st.giveItems(q_goldenram_badge1, 1);
				st.takeItems(q_splinter_chitin, -1);
				st.getPlayer().updateRam();
				htmltext = "merc_kahmun_q0628_08.htm";
			}
		}
		else if(event.equalsIgnoreCase("reply_3"))
		{
			st.takeItems(q_goldenram_badge1, -1);
			st.takeItems(q_goldenram_badge2, -1);
			st.takeItems(q_splinter_chitin, -1);
			st.takeItems(q_needle_chitin, -1);
			st.getPlayer().updateRam();
			st.playSound("ItemSound.quest_finish");
			st.exitCurrentQuest(true);
			htmltext = "merc_kahmun_q0628_13.htm";
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
				if(npcId != 31554)
					break;
				if(st.getPlayer().getLevel() >= 66)
				{
					htmltext = "merc_kahmun_q0628_01.htm";
					break;
				}
				htmltext = "merc_kahmun_q0628_02.htm";
				st.exitCurrentQuest(true);
				break;
			}
			case 2:
			{
				if(npcId != 31554)
					break;
				if(st.getQuestItemsCount(q_goldenram_badge1) < 1 && st.getQuestItemsCount(q_goldenram_badge2) < 1 && st.getQuestItemsCount(q_splinter_chitin) < 100)
				{
					htmltext = "merc_kahmun_q0628_06.htm";
					break;
				}
				if(st.getQuestItemsCount(q_goldenram_badge1) < 1 && st.getQuestItemsCount(q_goldenram_badge2) < 1 && st.getQuestItemsCount(q_splinter_chitin) >= 100)
				{
					htmltext = "merc_kahmun_q0628_07.htm";
					break;
				}
				if(st.getQuestItemsCount(q_goldenram_badge1) >= 1 && st.getQuestItemsCount(q_goldenram_badge2) < 1 && (st.getQuestItemsCount(q_splinter_chitin) < 100 || st.getQuestItemsCount(q_needle_chitin) < 100))
				{
					htmltext = "merc_kahmun_q0628_09.htm";
					break;
				}
				if(st.getQuestItemsCount(q_goldenram_badge1) >= 1 && st.getQuestItemsCount(q_goldenram_badge2) < 1 && st.getQuestItemsCount(q_splinter_chitin) >= 100 && st.getQuestItemsCount(q_needle_chitin) >= 100)
				{
					st.setCond(3);
					st.giveItems(q_goldenram_badge2, 1);
					st.takeItems(q_goldenram_badge1, -1);
					st.takeItems(q_splinter_chitin, -1);
					st.takeItems(q_needle_chitin, -1);
					st.playSound("ItemSound.quest_middle");
					htmltext = "merc_kahmun_q0628_10.htm";
					break;
				}
				if(st.getQuestItemsCount(q_goldenram_badge2) < 1)
					break;
				htmltext = "merc_kahmun_q0628_11.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getState() != 2)
		{
			return null;
		}
		int npcId = npc.getNpcId();
		if(npcId == 21508)
		{
			if(st.getQuestItemsCount(q_goldenram_badge2) < 1 && st.getQuestItemsCount(q_splinter_chitin) < 100 && Rnd.get(100) < 50)
			{
				if(st.getQuestItemsCount(q_goldenram_badge1) >= 1)
				{
					if(st.getQuestItemsCount(q_splinter_chitin) >= 99)
					{
						st.rollAndGive(q_splinter_chitin, 1, 100.0);
						st.playSound("ItemSound.quest_middle");
					}
					else
					{
						st.rollAndGive(q_splinter_chitin, 1, 100.0);
						st.playSound("ItemSound.quest_itemget");
					}
				}
				else if(st.getQuestItemsCount(q_splinter_chitin) >= 99)
				{
					st.rollAndGive(q_splinter_chitin, 1, 100.0);
					st.playSound("ItemSound.quest_middle");
				}
				else
				{
					st.rollAndGive(q_splinter_chitin, 1, 100.0);
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		else if(npcId == 21509)
		{
			if(st.getQuestItemsCount(q_goldenram_badge2) < 1 && st.getQuestItemsCount(q_splinter_chitin) < 100 && Rnd.get(100) < 43)
			{
				if(st.getQuestItemsCount(q_goldenram_badge1) >= 1)
				{
					if(st.getQuestItemsCount(q_splinter_chitin) >= 99)
					{
						st.rollAndGive(q_splinter_chitin, 1, 100.0);
						st.playSound("ItemSound.quest_middle");
					}
					else
					{
						st.rollAndGive(q_splinter_chitin, 1, 100.0);
						st.playSound("ItemSound.quest_itemget");
					}
				}
				else if(st.getQuestItemsCount(q_splinter_chitin) >= 99)
				{
					st.rollAndGive(q_splinter_chitin, 1, 100.0);
					st.playSound("ItemSound.quest_middle");
				}
				else
				{
					st.rollAndGive(q_splinter_chitin, 1, 100.0);
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		else if(npcId == 21510)
		{
			if(st.getQuestItemsCount(q_goldenram_badge2) < 1 && st.getQuestItemsCount(q_splinter_chitin) < 100 && Rnd.get(1000) < 521)
			{
				if(st.getQuestItemsCount(q_goldenram_badge1) >= 1)
				{
					if(st.getQuestItemsCount(q_splinter_chitin) >= 99)
					{
						st.rollAndGive(q_splinter_chitin, 1, 100.0);
						st.playSound("ItemSound.quest_middle");
					}
					else
					{
						st.rollAndGive(q_splinter_chitin, 1, 100.0);
						st.playSound("ItemSound.quest_itemget");
					}
				}
				else if(st.getQuestItemsCount(q_splinter_chitin) >= 99)
				{
					st.rollAndGive(q_splinter_chitin, 1, 100.0);
					st.playSound("ItemSound.quest_middle");
				}
				else
				{
					st.rollAndGive(q_splinter_chitin, 1, 100.0);
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		else if(npcId == 21511)
		{
			if(st.getQuestItemsCount(q_goldenram_badge2) < 1 && st.getQuestItemsCount(q_splinter_chitin) < 100 && Rnd.get(1000) < 575)
			{
				if(st.getQuestItemsCount(q_goldenram_badge1) >= 1)
				{
					if(st.getQuestItemsCount(q_splinter_chitin) >= 99)
					{
						st.rollAndGive(q_splinter_chitin, 1, 100.0);
						st.playSound("ItemSound.quest_middle");
					}
					else
					{
						st.rollAndGive(q_splinter_chitin, 1, 100.0);
						st.playSound("ItemSound.quest_itemget");
					}
				}
				else if(st.getQuestItemsCount(q_splinter_chitin) >= 99)
				{
					st.rollAndGive(q_splinter_chitin, 1, 100.0);
					st.playSound("ItemSound.quest_middle");
				}
				else
				{
					st.rollAndGive(q_splinter_chitin, 1, 100.0);
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		else if(npcId == 21512)
		{
			if(st.getQuestItemsCount(q_goldenram_badge2) < 1 && st.getQuestItemsCount(q_splinter_chitin) < 100 && Rnd.get(1000) < 746)
			{
				if(st.getQuestItemsCount(q_goldenram_badge1) >= 1)
				{
					if(st.getQuestItemsCount(q_splinter_chitin) >= 99)
					{
						st.rollAndGive(q_splinter_chitin, 1, 100.0);
						st.playSound("ItemSound.quest_middle");
					}
					else
					{
						st.rollAndGive(q_splinter_chitin, 1, 100.0);
						st.playSound("ItemSound.quest_itemget");
					}
				}
				else if(st.getQuestItemsCount(q_splinter_chitin) >= 99)
				{
					st.rollAndGive(q_splinter_chitin, 1, 100.0);
					st.playSound("ItemSound.quest_middle");
				}
				else
				{
					st.rollAndGive(q_splinter_chitin, 1, 100.0);
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		else if(npcId == 21513)
		{
			if(st.getQuestItemsCount(q_goldenram_badge2) < 1 && st.getQuestItemsCount(q_goldenram_badge1) >= 1 && st.getQuestItemsCount(q_needle_chitin) < 100 && Rnd.get(100) < 50)
			{
				if(st.getQuestItemsCount(q_needle_chitin) >= 99)
				{
					st.rollAndGive(q_needle_chitin, 1, 100.0);
					st.playSound("ItemSound.quest_middle");
				}
				else
				{
					st.rollAndGive(q_needle_chitin, 1, 100.0);
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		else if(npcId == 21514)
		{
			if(st.getQuestItemsCount(q_goldenram_badge2) < 1 && st.getQuestItemsCount(q_goldenram_badge1) >= 1 && st.getQuestItemsCount(q_needle_chitin) < 100 && Rnd.get(100) < 43)
			{
				if(st.getQuestItemsCount(q_needle_chitin) >= 99)
				{
					st.rollAndGive(q_needle_chitin, 1, 100.0);
					st.playSound("ItemSound.quest_middle");
				}
				else
				{
					st.rollAndGive(q_needle_chitin, 1, 100.0);
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		else if(npcId == 21515)
		{
			if(st.getQuestItemsCount(q_goldenram_badge2) < 1 && st.getQuestItemsCount(q_goldenram_badge1) >= 1 && st.getQuestItemsCount(q_needle_chitin) < 100 && Rnd.get(100) < 52)
			{
				if(st.getQuestItemsCount(q_needle_chitin) >= 99)
				{
					st.rollAndGive(q_needle_chitin, 1, 100.0);
					st.playSound("ItemSound.quest_middle");
				}
				else
				{
					st.rollAndGive(q_needle_chitin, 1, 100.0);
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		else if(npcId == 21516)
		{
			if(st.getQuestItemsCount(q_goldenram_badge2) < 1 && st.getQuestItemsCount(q_goldenram_badge1) >= 1 && st.getQuestItemsCount(q_needle_chitin) < 100 && Rnd.get(1000) < 531)
			{
				if(st.getQuestItemsCount(q_needle_chitin) >= 99)
				{
					st.rollAndGive(q_needle_chitin, 1, 100.0);
					st.playSound("ItemSound.quest_middle");
				}
				else
				{
					st.rollAndGive(q_needle_chitin, 1, 100.0);
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		else if(npcId == 21517 && st.getQuestItemsCount(q_goldenram_badge2) < 1 && st.getQuestItemsCount(q_goldenram_badge1) >= 1 && st.getQuestItemsCount(q_needle_chitin) < 100 && Rnd.get(1000) < 744)
		{
			if(st.getQuestItemsCount(q_needle_chitin) >= 99)
			{
				st.rollAndGive(q_needle_chitin, 1, 100.0);
				st.playSound("ItemSound.quest_middle");
			}
			else
			{
				st.rollAndGive(q_needle_chitin, 1, 100.0);
				st.playSound("ItemSound.quest_itemget");
			}
		}
		return null;
	}
}