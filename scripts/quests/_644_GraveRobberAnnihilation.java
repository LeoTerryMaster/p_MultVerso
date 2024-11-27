package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _644_GraveRobberAnnihilation extends Quest implements ScriptFile
{
	private static final int karuda = 32017;
	private static final int thief_patroller_archer = 22003;
	private static final int thief_patroller = 22004;
	private static final int thief_guard = 22005;
	private static final int grave_robber_swordscout = 22006;
	private static final int thief_fighter = 22008;
	private static final int q_grave_goods_of_orc = 8088;
	private static final int varnish = 1865;
	private static final int animal_skin = 1867;
	private static final int animal_bone = 1872;
	private static final int charcoal = 1871;
	private static final int coal = 1870;
	private static final int iron_ore = 1869;
	
	public _644_GraveRobberAnnihilation()
	{
		super(false);
		addStartNpc(32017);
		addKillId(22003, 22004, 22005, 22006, 22008);
		addQuestItem(8088);
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
		int GetMemoState = st.getInt("sweep_the_snatcher");
		int GetHTMLCookie = st.getInt("sweep_the_snatcher_cookie");
		int npcId = npc.getNpcId();
		if(npcId == 32017)
		{
			if(event.equalsIgnoreCase("quest_accept"))
			{
				st.setCond(1);
				st.set("sweep_the_snatcher", String.valueOf(11), true);
				st.setState(2);
				st.playSound("ItemSound.quest_accept");
				htmltext = "karuda_q0644_0103.htm";
			}
			else if(event.equalsIgnoreCase("reply_3") && GetHTMLCookie == 1 && GetMemoState >= 11)
			{
				htmltext = "karuda_q0644_0201.htm";
			}
			else if(GetMemoState >= 11 && st.getQuestItemsCount(8088) >= 120)
			{
				st.takeItems(8088, 120);
				if(event.equalsIgnoreCase("reply_11"))
				{
					st.giveItems(1865, 30);
				}
				else if(event.equalsIgnoreCase("reply_12"))
				{
					st.giveItems(1867, 40);
				}
				else if(event.equalsIgnoreCase("reply_13"))
				{
					st.giveItems(1872, 40);
				}
				else if(event.equalsIgnoreCase("reply_14"))
				{
					st.giveItems(1871, 30);
				}
				else if(event.equalsIgnoreCase("reply_15"))
				{
					st.giveItems(1870, 30);
				}
				else if(event.equalsIgnoreCase("reply_16"))
				{
					st.giveItems(1869, 30);
				}
				st.unset("sweep_the_snatcher");
				st.unset("sweep_the_snatcher_cookie");
				st.playSound("ItemSound.quest_finish");
				st.exitCurrentQuest(true);
				htmltext = "karuda_q0644_0202.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "no-quest";
		int GetMemoState = st.getInt("sweep_the_snatcher");
		int npcId = npc.getNpcId();
		int id = st.getState();
		switch(id)
		{
			case 1:
			{
				if(npcId != 32017)
					break;
				if(st.getPlayer().getLevel() >= 20)
				{
					htmltext = "karuda_q0644_0101.htm";
					break;
				}
				htmltext = "karuda_q0644_0102.htm";
				st.exitCurrentQuest(true);
				break;
			}
			case 2:
			{
				if(npcId != 32017 || GetMemoState < 11 || GetMemoState > 12)
					break;
				if(GetMemoState == 12 && st.getQuestItemsCount(8088) >= 120)
				{
					st.set("sweep_the_snatcher_cookie", String.valueOf(1), true);
					htmltext = "karuda_q0644_0105.htm";
					break;
				}
				htmltext = "karuda_q0644_0106.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int GetMemoState = st.getInt("sweep_the_snatcher");
		int npcId = npc.getNpcId();
		if(GetMemoState == 11)
		{
			int i4;
			if(npcId == 22003)
			{
				int i42 = Rnd.get(1000);
				if(i42 < 714)
				{
					if(st.getQuestItemsCount(8088) + 1 >= 120)
					{
						if(st.getQuestItemsCount(8088) < 120)
						{
							st.giveItems(8088, 120 - st.getQuestItemsCount(8088));
							st.playSound("ItemSound.quest_middle");
						}
						st.setCond(2);
						st.set("sweep_the_snatcher", String.valueOf(12), true);
					}
					else
					{
						st.giveItems(8088, 1);
						st.playSound("ItemSound.quest_itemget");
					}
				}
			}
			else if(npcId == 22004)
			{
				int i43 = Rnd.get(1000);
				if(i43 < 841)
				{
					if(st.getQuestItemsCount(8088) + 1 >= 120)
					{
						if(st.getQuestItemsCount(8088) < 120)
						{
							st.giveItems(8088, 120 - st.getQuestItemsCount(8088));
							st.playSound("ItemSound.quest_middle");
						}
						st.setCond(2);
						st.set("sweep_the_snatcher", String.valueOf(12), true);
					}
					else
					{
						st.giveItems(8088, 1);
						st.playSound("ItemSound.quest_itemget");
					}
				}
			}
			else if(npcId == 22005)
			{
				int i44 = Rnd.get(1000);
				if(i44 < 746)
				{
					if(st.getQuestItemsCount(8088) + 1 >= 120)
					{
						if(st.getQuestItemsCount(8088) < 120)
						{
							st.giveItems(8088, 120 - st.getQuestItemsCount(8088));
							st.playSound("ItemSound.quest_middle");
						}
						st.setCond(2);
						st.set("sweep_the_snatcher", String.valueOf(12), true);
					}
					else
					{
						st.giveItems(8088, 1);
						st.playSound("ItemSound.quest_itemget");
					}
				}
			}
			else if(npcId == 22006)
			{
				int i45 = Rnd.get(1000);
				if(i45 < 778)
				{
					if(st.getQuestItemsCount(8088) + 1 >= 120)
					{
						if(st.getQuestItemsCount(8088) < 120)
						{
							st.giveItems(8088, 120 - st.getQuestItemsCount(8088));
							st.playSound("ItemSound.quest_middle");
						}
						st.setCond(2);
						st.set("sweep_the_snatcher", String.valueOf(12), true);
					}
					else
					{
						st.giveItems(8088, 1);
						st.playSound("ItemSound.quest_itemget");
					}
				}
			}
			else if(npcId == 22008 && Rnd.get(1000) < 810)
			{
				if(st.getQuestItemsCount(8088) + 1 >= 120)
				{
					if(st.getQuestItemsCount(8088) < 120)
					{
						st.giveItems(8088, 120 - st.getQuestItemsCount(8088));
						st.playSound("ItemSound.quest_middle");
					}
					st.setCond(2);
					st.set("sweep_the_snatcher", String.valueOf(12), true);
				}
				else
				{
					st.giveItems(8088, 1);
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		return null;
	}
}