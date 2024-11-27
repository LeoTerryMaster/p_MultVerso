package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _660_AidingtheFloranVillage extends Quest implements ScriptFile
{
	public final int MARIA = 30608;
	public final int ALEX = 30291;
	public final int CARSED_SEER = 21106;
	public final int PLAIN_WATCMAN = 21102;
	public final int ROUGH_HEWN_ROCK_GOLEM = 21103;
	public final int DELU_LIZARDMAN_SHAMAN = 20781;
	public final int DELU_LIZARDMAN_SAPPLIER = 21104;
	public final int DELU_LIZARDMAN_COMMANDER = 21107;
	public final int DELU_LIZARDMAN_SPESIAL_AGENT = 21105;
	public final int WATCHING_EYES = 8074;
	public final int ROUGHLY_HEWN_ROCK_GOLEM_SHARD = 8075;
	public final int DELU_LIZARDMAN_SCALE = 8076;
	public final int SCROLL_ENCANT_ARMOR = 956;
	public final int SCROLL_ENCHANT_WEAPON = 955;
	
	public _660_AidingtheFloranVillage()
	{
		super(false);
		addStartNpc(30608);
		addTalkId(30608);
		addTalkId(30291);
		addKillId(21106);
		addKillId(21102);
		addKillId(21103);
		addKillId(20781);
		addKillId(21104);
		addKillId(21107);
		addKillId(21105);
		addQuestItem(8074);
		addQuestItem(8076);
		addQuestItem(8075);
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
		long EYES = st.getQuestItemsCount(8074);
		long SCALE = st.getQuestItemsCount(8076);
		long SHARD = st.getQuestItemsCount(8075);
		if(event.equalsIgnoreCase("30608-04.htm"))
		{
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("30291-05.htm"))
		{
			if(EYES + SCALE + SHARD >= 45)
			{
				st.giveItems(57, EYES * 100 + SCALE * 100 + SHARD * 100 + 9000);
				st.takeItems(8074, -1);
				st.takeItems(8076, -1);
				st.takeItems(8075, -1);
			}
			else
			{
				st.giveItems(57, EYES * 100 + SCALE * 100 + SHARD * 100);
				st.takeItems(8074, -1);
				st.takeItems(8076, -1);
				st.takeItems(8075, -1);
			}
			st.playSound("ItemSound.quest_itemget");
		}
		else if(event.equalsIgnoreCase("30291-11.htm"))
		{
			if(EYES + SCALE + SHARD >= 99)
			{
				if(EYES >= 100)
				{
					st.takeItems(8074, 100);
				}
				else
				{
					st.takeItems(8074, -1);
					long n = 100 - EYES;
					if(SCALE >= n)
					{
						st.takeItems(8076, n);
					}
					else
					{
						st.takeItems(8076, -1);
						long t = 100 - SCALE - EYES;
						st.takeItems(8075, t);
					}
				}
				if(Rnd.chance(80))
				{
					st.giveItems(57, 13000);
					st.giveItems(956, 1);
				}
				else
				{
					st.giveItems(57, 1000);
				}
				st.playSound("ItemSound.quest_itemget");
			}
			else
			{
				htmltext = "30291-14.htm";
			}
		}
		else if(event.equalsIgnoreCase("30291-12.htm"))
		{
			if(EYES + SCALE + SHARD >= 199)
			{
				int luck = Rnd.get(15);
				if(EYES >= 200)
				{
					st.takeItems(8074, 200);
				}
				else
				{
					st.takeItems(8074, -1);
				}
				long n = 200 - EYES;
				if(SCALE >= n)
				{
					st.takeItems(8076, n);
				}
				else
				{
					st.takeItems(8076, -1);
				}
				long t = 200 - SCALE - EYES;
				st.takeItems(8075, t);
				if(luck < 9)
				{
					st.giveItems(57, 20000);
					st.giveItems(956, 1);
				}
				else if(luck > 8 && luck < 12)
				{
					st.giveItems(955, 1);
				}
				else
				{
					st.giveItems(57, 2000);
				}
				st.playSound("ItemSound.quest_itemget");
			}
			else
			{
				htmltext = "30291-14.htm";
			}
		}
		else if(event.equalsIgnoreCase("30291-13.htm"))
		{
			if(EYES + SCALE + SHARD >= 499)
			{
				if(EYES >= 500)
				{
					st.takeItems(8074, 500);
				}
				else
				{
					st.takeItems(8074, -1);
				}
				long n = 500 - EYES;
				if(SCALE >= n)
				{
					st.takeItems(8076, n);
				}
				else
				{
					st.takeItems(8076, -1);
					long t = 500 - SCALE - EYES;
					st.takeItems(8075, t);
				}
				if(Rnd.chance(80))
				{
					st.giveItems(57, 45000);
					st.giveItems(955, 1);
				}
				else
				{
					st.giveItems(57, 5000);
				}
				st.playSound("ItemSound.quest_itemget");
			}
			else
			{
				htmltext = "30291-14.htm";
			}
		}
		else if(event.equalsIgnoreCase("30291-06.htm"))
		{
			st.exitCurrentQuest(true);
			st.playSound("ItemSound.quest_finish");
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == 30608 && cond < 1)
		{
			if(st.getPlayer().getLevel() < 30)
			{
				htmltext = "30608-01.htm";
				st.exitCurrentQuest(true);
			}
			else
			{
				htmltext = "30608-02.htm";
			}
		}
		else if(npcId == 30608 && cond == 1)
		{
			htmltext = "30608-06.htm";
		}
		else if(npcId == 30291 && cond == 1)
		{
			htmltext = "30291-01.htm";
			st.playSound("ItemSound.quest_middle");
			st.setCond(2);
		}
		else if(npcId == 30291 && cond == 2)
		{
			htmltext = st.getQuestItemsCount(8074) + st.getQuestItemsCount(8076) + st.getQuestItemsCount(8075) == 0 ? "30291-02.htm" : "30291-03.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int chance = Rnd.get(100) + 1;
		if(st.getCond() == 2)
		{
			if(npcId == 21106 | npcId == 21102 && chance < 79)
			{
				st.giveItems(8074, 1);
				st.playSound("ItemSound.quest_itemget");
			}
			else if(npcId == 21103 && chance < 75)
			{
				st.giveItems(8075, 1);
				st.playSound("ItemSound.quest_itemget");
			}
			else if(npcId == 20781 | npcId == 21104 | npcId == 21107 | npcId == 21105 && chance < 67)
			{
				st.giveItems(8076, 1);
				st.playSound("ItemSound.quest_itemget");
			}
		}
		return null;
	}
}