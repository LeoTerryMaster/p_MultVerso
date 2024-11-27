package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _343_UndertheShadowoftheIvoryTower extends Quest implements ScriptFile
{
	public final int CEMA = 30834;
	public final int ICARUS = 30835;
	public final int MARSHA = 30934;
	public final int TRUMPIN = 30935;
	public final int[] MOBS = {20563, 20564, 20565, 20566};
	public final int ORB = 4364;
	public final int ECTOPLASM = 4365;
	public final int[] AllowClass = {11, 12, 13, 14, 26, 27, 28, 39, 40, 41};
	public final int CHANCE = 50;
	
	public _343_UndertheShadowoftheIvoryTower()
	{
		super(false);
		addStartNpc(30834);
		addTalkId(30834);
		addTalkId(30835);
		addTalkId(30934);
		addTalkId(30935);
		for(int i : MOBS)
		{
			addKillId(i);
		}
		addQuestItem(4364);
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
		int random1 = Rnd.get(3);
		int random2 = Rnd.get(2);
		long orbs = st.getQuestItemsCount(4364);
		if(event.equalsIgnoreCase("30834-03.htm"))
		{
			st.setState(2);
			st.setCond(1);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("30834-08.htm"))
		{
			if(orbs > 0)
			{
				st.giveItems(57, orbs * 120);
				st.takeItems(4364, -1);
			}
			else
			{
				htmltext = "30834-08.htm";
			}
		}
		else if(event.equalsIgnoreCase("30834-09.htm"))
		{
			st.playSound("ItemSound.quest_finish");
			st.exitCurrentQuest(true);
		}
		else if(event.equalsIgnoreCase("30934-02.htm") || event.equalsIgnoreCase("30934-03.htm"))
		{
			if(orbs < 10)
			{
				htmltext = "noorbs.htm";
			}
			else if(event.equalsIgnoreCase("30934-03.htm"))
			{
				if(orbs >= 10)
				{
					st.takeItems(4364, 10);
					st.set("playing", "1");
				}
				else
				{
					htmltext = "noorbs.htm";
				}
			}
		}
		else if(event.equalsIgnoreCase("30934-04.htm"))
		{
			if(st.getInt("playing") > 0)
			{
				if(random1 == 0)
				{
					htmltext = "30934-05.htm";
					st.giveItems(4364, 10);
				}
				else if(random1 == 1)
				{
					htmltext = "30934-06.htm";
				}
				else
				{
					htmltext = "30934-04.htm";
					st.giveItems(4364, 20);
				}
				st.unset("playing");
			}
			else
			{
				htmltext = "Player is cheating";
				st.takeItems(4364, -1);
				st.exitCurrentQuest(true);
			}
		}
		else if(event.equalsIgnoreCase("30934-05.htm"))
		{
			if(st.getInt("playing") > 0)
			{
				if(random1 == 0)
				{
					htmltext = "30934-04.htm";
					st.giveItems(4364, 20);
				}
				else if(random1 == 1)
				{
					htmltext = "30934-05.htm";
					st.giveItems(4364, 10);
				}
				else
				{
					htmltext = "30934-06.htm";
				}
				st.unset("playing");
			}
			else
			{
				htmltext = "Player is cheating";
				st.takeItems(4364, -1);
				st.exitCurrentQuest(true);
			}
		}
		else if(event.equalsIgnoreCase("30934-06.htm"))
		{
			if(st.getInt("playing") > 0)
			{
				if(random1 == 0)
				{
					htmltext = "30934-04.htm";
					st.giveItems(4364, 20);
				}
				else if(random1 == 1)
				{
					htmltext = "30934-06.htm";
				}
				else
				{
					htmltext = "30934-05.htm";
					st.giveItems(4364, 10);
				}
				st.unset("playing");
			}
			else
			{
				htmltext = "Player is cheating";
				st.takeItems(4364, -1);
				st.exitCurrentQuest(true);
			}
		}
		else if(event.equalsIgnoreCase("30935-02.htm") || event.equalsIgnoreCase("30935-03.htm"))
		{
			st.unset("toss");
			if(orbs < 10)
			{
				htmltext = "noorbs.htm";
			}
		}
		else if(event.equalsIgnoreCase("30935-05.htm"))
		{
			if(orbs >= 10)
			{
				if(random2 == 0)
				{
					int toss = st.getInt("toss");
					if(toss == 4)
					{
						st.unset("toss");
						st.giveItems(4364, 150);
						htmltext = "30935-07.htm";
					}
					else
					{
						st.set("toss", String.valueOf(toss + 1));
						htmltext = "30935-04.htm";
					}
				}
				else
				{
					st.unset("toss");
					st.takeItems(4364, 10);
				}
			}
			else
			{
				htmltext = "noorbs.htm";
			}
		}
		else if(event.equalsIgnoreCase("30935-06.htm"))
		{
			if(orbs >= 10)
			{
				int toss = st.getInt("toss");
				st.unset("toss");
				if(toss == 1)
				{
					st.giveItems(4364, 10);
				}
				else if(toss == 2)
				{
					st.giveItems(4364, 30);
				}
				else if(toss == 3)
				{
					st.giveItems(4364, 70);
				}
				else if(toss == 4)
				{
					st.giveItems(4364, 150);
				}
			}
			else
			{
				htmltext = "noorbs.htm";
			}
		}
		else if(event.equalsIgnoreCase("30835-02.htm"))
		{
			if(st.getQuestItemsCount(4365) > 0)
			{
				st.takeItems(4365, 1);
				int random = Rnd.get(1000);
				if(random <= 119)
				{
					st.giveItems(955, 1);
				}
				else if(random <= 169)
				{
					st.giveItems(951, 1);
				}
				else if(random <= 329)
				{
					st.giveItems(2511, (long) (Rnd.get(200) + 401));
				}
				else if(random <= 559)
				{
					st.giveItems(2510, (long) (Rnd.get(200) + 401));
				}
				else if(random <= 561)
				{
					st.giveItems(316, 1);
				}
				else if(random <= 578)
				{
					st.giveItems(630, 1);
				}
				else if(random <= 579)
				{
					st.giveItems(188, 1);
				}
				else if(random <= 581)
				{
					st.giveItems(885, 1);
				}
				else if(random <= 582)
				{
					st.giveItems(103, 1);
				}
				else if(random <= 584)
				{
					st.giveItems(917, 1);
				}
				else
				{
					st.giveItems(736, 1);
				}
			}
			else
			{
				htmltext = "30835-03.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		String htmltext = "noquest";
		int id = st.getState();
		if(npcId == 30834)
		{
			if(id != 2)
			{
				for(int i : AllowClass)
				{
					if(st.getPlayer().getClassId().getId() != i || st.getPlayer().getLevel() < 40)
						continue;
					htmltext = "30834-01.htm";
				}
				if(!htmltext.equals("30834-01.htm"))
				{
					htmltext = "30834-07.htm";
					st.exitCurrentQuest(true);
				}
			}
			else
			{
				htmltext = st.getQuestItemsCount(4364) > 0 ? "30834-06.htm" : "30834-05.htm";
			}
		}
		else if(npcId == 30835)
		{
			htmltext = "30835-01.htm";
		}
		else if(npcId == 30934)
		{
			htmltext = "30934-01.htm";
		}
		else if(npcId == 30935)
		{
			htmltext = "30935-01.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(Rnd.chance(50))
		{
			st.giveItems(4364, 1);
			st.playSound("ItemSound.quest_itemget");
		}
		return null;
	}
}