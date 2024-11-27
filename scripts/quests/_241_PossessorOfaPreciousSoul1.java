package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _241_PossessorOfaPreciousSoul1 extends Quest implements ScriptFile
{
	private static final int LEGENG_OF_SEVENTEEN = 7587;
	private static final int MALRUK_SUCCUBUS_CLAW = 7597;
	private static final int ECHO_CRYSTAL = 7589;
	private static final int FADED_POETRY_BOOK = 7588;
	private static final int CRIMSON_MOSS = 7598;
	private static final int MEDICINE = 7599;
	private static final int LUNARGENT = 6029;
	private static final int HELLFIRE_OIL = 6033;
	private static final int VIRGILS_LETTER = 7677;
	
	public _241_PossessorOfaPreciousSoul1()
	{
		super(false);
		addStartNpc(31739);
		addTalkId(30753);
		addTalkId(30754);
		addTalkId(31042);
		addTalkId(30692);
		addTalkId(31742);
		addTalkId(31744);
		addTalkId(31336);
		addTalkId(31743);
		addTalkId(31740);
		addTalkId(31272);
		addKillId(21154);
		addKillId(27113);
		addKillId(20244);
		addKillId(20245);
		addKillId(21511);
		int npcKillId = 21508;
		while(npcKillId <= 21512)
		{
			addKillId(npcKillId++);
		}
		addQuestItem(7587, 7597, 7588, 7589, 7599, 7598);
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
		if(event.equalsIgnoreCase("31739-02.htm"))
		{
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("30753-02.htm"))
		{
			st.setCond(2);
		}
		else if(event.equalsIgnoreCase("30754-02.htm"))
		{
			st.setCond(3);
		}
		else if(event.equalsIgnoreCase("31739-04.htm"))
		{
			st.takeItems(7587, -1);
			st.setCond(5);
		}
		else if(event.equalsIgnoreCase("31042-02.htm"))
		{
			st.setCond(6);
		}
		else if(event.equalsIgnoreCase("31042-04.htm"))
		{
			st.takeItems(7597, -1);
			st.giveItems(7589, 1);
			st.setCond(8);
		}
		else if(event.equalsIgnoreCase("31739-06.htm"))
		{
			st.takeItems(7589, -1);
			st.setCond(9);
		}
		else if(event.equalsIgnoreCase("30692-02.htm"))
		{
			st.giveItems(7588, 1);
			st.setCond(10);
		}
		else if(event.equalsIgnoreCase("31739-08.htm"))
		{
			st.takeItems(7588, -1);
			st.setCond(11);
		}
		else if(event.equalsIgnoreCase("31742-02.htm"))
		{
			st.setCond(12);
		}
		else if(event.equalsIgnoreCase("31744-02.htm"))
		{
			st.setCond(13);
		}
		else if(event.equalsIgnoreCase("31336-02.htm"))
		{
			st.setCond(14);
		}
		else if(event.equalsIgnoreCase("31336-04.htm"))
		{
			st.takeItems(7598, -1);
			st.giveItems(7599, 1);
			st.setCond(16);
		}
		else if(event.equalsIgnoreCase("31743-02.htm"))
		{
			st.takeItems(7599, -1);
			st.setCond(17);
		}
		else if(event.equalsIgnoreCase("31742-04.htm"))
		{
			st.setCond(18);
		}
		else if(event.equalsIgnoreCase("31740-02.htm"))
		{
			st.setCond(19);
		}
		else if(event.equalsIgnoreCase("31272-02.htm"))
		{
			st.setCond(20);
		}
		else if(event.equalsIgnoreCase("31272-04.htm") && st.getQuestItemsCount(6029) >= 5 && st.getQuestItemsCount(6033) != 0)
		{
			st.takeItems(6029, 5);
			st.takeItems(6033, 1);
			st.setCond(21);
		}
		else if(event.equalsIgnoreCase("31740-04.htm"))
		{
			st.giveItems(7677, 1);
			st.unset("cond");
			st.exitCurrentQuest(false);
		}
		return event;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		if(!st.getPlayer().isSubClassActive())
		{
			return "Subclass only!";
		}
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == 31739)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 50)
				{
					htmltext = "31739-01.htm";
				}
				else
				{
					htmltext = "31739-00.htm";
					st.exitCurrentQuest(true);
				}
			}
			else if(cond == 1)
			{
				htmltext = "31739-02r.htm";
			}
			else if(cond == 4 && st.getQuestItemsCount(7587) >= 1)
			{
				htmltext = "31739-03.htm";
			}
			else if(cond < 8 && st.getQuestItemsCount(7589) < 1)
			{
				htmltext = "31739-04r.htm";
			}
			else if(cond == 8 && st.getQuestItemsCount(7589) == 1)
			{
				htmltext = "31739-05.htm";
			}
			else if(cond < 10 && st.getQuestItemsCount(7588) < 1)
			{
				htmltext = "31739-06r.htm";
			}
			else if(cond == 10 && st.getQuestItemsCount(7588) == 1)
			{
				htmltext = "31739-07.htm";
			}
			else if(cond == 11)
			{
				htmltext = "31739-08r.htm";
			}
		}
		else if(npcId == 30753)
		{
			if(cond == 1)
			{
				htmltext = "30753-01.htm";
			}
			else if(cond == 2)
			{
				htmltext = "30753-02r.htm";
			}
		}
		else if(npcId == 30754)
		{
			if(cond == 2)
			{
				htmltext = "30754-01.htm";
			}
			else if(cond == 3 && st.getQuestItemsCount(7587) < 1)
			{
				htmltext = "30754-02r.htm";
			}
		}
		else if(npcId == 31042)
		{
			if(cond == 5)
			{
				htmltext = "31042-01.htm";
			}
			else if(cond == 6 && st.getQuestItemsCount(7597) < 10)
			{
				htmltext = "31042-02r.htm";
			}
			else if(cond == 7 && st.getQuestItemsCount(7597) == 10)
			{
				htmltext = "31042-03.htm";
			}
			else if(cond == 8 && st.getQuestItemsCount(7589) >= 1)
			{
				htmltext = "31042-04r.htm";
			}
			else if(cond == 8 && st.getQuestItemsCount(7589) == 0)
			{
				st.giveItems(7589, 1);
				htmltext = "31042-04r.htm";
			}
		}
		else if(npcId == 30692)
		{
			if(cond == 9)
			{
				htmltext = "30692-01.htm";
			}
			else if(cond == 10)
			{
				htmltext = "30692-02r.htm";
			}
		}
		else if(npcId == 31742)
		{
			if(cond == 11)
			{
				htmltext = "31742-01.htm";
			}
			else if(cond == 12)
			{
				htmltext = "31742-02r.htm";
			}
			else if(cond == 17)
			{
				htmltext = "31742-03.htm";
			}
			else if(cond >= 18)
			{
				htmltext = "31742-04r.htm";
			}
		}
		else if(npcId == 31744)
		{
			if(cond == 12)
			{
				htmltext = "31744-01.htm";
			}
		}
		else if(npcId == 31336)
		{
			if(cond == 13)
			{
				htmltext = "31336-01.htm";
			}
			else if(cond == 14 && st.getQuestItemsCount(7598) < 5)
			{
				htmltext = "31336-02r.htm";
			}
			else if(cond == 15 && st.getQuestItemsCount(7598) >= 5)
			{
				htmltext = "31336-03.htm";
			}
			else if(cond == 16 && st.getQuestItemsCount(7599) >= 1)
			{
				htmltext = "31336-04r.htm";
			}
		}
		else if(npcId == 31743)
		{
			if(cond == 16 && st.getQuestItemsCount(7599) >= 1)
			{
				htmltext = "31743-01.htm";
			}
		}
		else if(npcId == 31740)
		{
			if(cond == 18)
			{
				htmltext = "31740-01.htm";
			}
			else if(cond == 19)
			{
				htmltext = "31740-02r.htm";
			}
			else if(cond == 21)
			{
				htmltext = "31740-03.htm";
			}
		}
		else if(npcId == 31272)
		{
			if(cond == 19)
			{
				htmltext = "31272-01.htm";
			}
			else if(cond == 20 && st.getQuestItemsCount(6029) < 5 && st.getQuestItemsCount(6033) < 1)
			{
				htmltext = "31272-02r.htm";
			}
			else if(cond == 20 && st.getQuestItemsCount(6029) >= 5 && st.getQuestItemsCount(6033) >= 1)
			{
				htmltext = "31272-03.htm";
			}
			else if(cond == 21)
			{
				htmltext = "31272-04r.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(!st.getPlayer().isSubClassActive())
		{
			return null;
		}
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(cond == 3)
		{
			if(npcId == 21154 && Rnd.chance(10))
			{
				st.addSpawn(27113);
				return null;
			}
			else
			{
				if(npcId != 27113 || st.getQuestItemsCount(7587) != 0)
					return null;
				st.giveItems(7587, 1);
				st.setCond(4);
				st.playSound("ItemSound.quest_itemget");
			}
			return null;
		}
		else if(cond == 6)
		{
			if(npcId != 20244 && npcId != 20245 || !Rnd.chance(10))
				return null;
			if(st.getQuestItemsCount(7597) <= 9)
			{
				st.giveItems(7597, 1);
			}
			if(st.getQuestItemsCount(7597) == 10)
			{
				st.playSound("ItemSound.quest_middle");
				st.setCond(7);
				return null;
			}
			else
			{
				st.playSound("ItemSound.quest_itemget");
			}
			return null;
		}
		else
		{
			if(cond != 14)
				return null;
			int i = 21508;
			while(i <= 21512)
			{
				if(npcId == i && Rnd.chance(10))
				{
					if(st.getQuestItemsCount(7598) <= 4)
					{
						st.giveItems(7598, 1);
					}
					if(st.getQuestItemsCount(7598) == 5)
					{
						st.playSound("ItemSound.quest_middle");
						st.setCond(15);
					}
					else
					{
						st.playSound("ItemSound.quest_itemget");
					}
				}
				++i;
			}
			return null;
		}
	}
}