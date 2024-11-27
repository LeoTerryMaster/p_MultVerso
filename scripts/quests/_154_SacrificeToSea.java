package quests;

import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _154_SacrificeToSea extends Quest implements ScriptFile
{
	private static final int FOX_FUR_ID = 1032;
	private static final int FOX_FUR_YARN_ID = 1033;
	private static final int MAIDEN_DOLL_ID = 1034;
	private static final int MYSTICS_EARRING_ID = 113;
	
	public _154_SacrificeToSea()
	{
		super(false);
		addStartNpc(30312);
		addTalkId(30051);
		addTalkId(30055);
		addKillId(20481);
		addKillId(20544);
		addKillId(20545);
		addQuestItem(1032, 1033, 1034);
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
		if(event.equals("1"))
		{
			st.set("id", "0");
			htmltext = "30312-04.htm";
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int id = st.getState();
		if(id == 1)
		{
			st.setState(2);
			st.setCond(0);
			st.set("id", "0");
		}
		String htmltext = "noquest";
		if(npcId == 30312 && st.getCond() == 0)
		{
			if(st.getCond() < 15)
			{
				if(st.getPlayer().getLevel() >= 2)
				{
					htmltext = "30312-03.htm";
					return htmltext;
				}
				htmltext = "30312-02.htm";
				st.exitCurrentQuest(true);
			}
			else
			{
				htmltext = "30312-02.htm";
				st.exitCurrentQuest(true);
			}
		}
		else if(npcId == 30312 && st.getCond() == 0)
		{
			htmltext = "completed";
		}
		else if(npcId == 30312 && st.getCond() == 1 && st.getQuestItemsCount(1033) == 0 && st.getQuestItemsCount(1034) == 0 && st.getQuestItemsCount(1032) < 10)
		{
			htmltext = "30312-05.htm";
		}
		else if(npcId == 30312 && st.getCond() == 1 && st.getQuestItemsCount(1032) >= 10)
		{
			htmltext = "30312-08.htm";
		}
		else if(npcId == 30051 && st.getCond() == 1 && st.getQuestItemsCount(1032) < 10 && st.getQuestItemsCount(1032) > 0)
		{
			htmltext = "30051-01.htm";
		}
		else if(npcId == 30051 && st.getCond() == 1 && st.getQuestItemsCount(1032) >= 10 && st.getQuestItemsCount(1033) == 0 && st.getQuestItemsCount(1034) == 0 && st.getQuestItemsCount(1034) < 10)
		{
			htmltext = "30051-02.htm";
			st.giveItems(1033, 1);
			st.takeItems(1032, st.getQuestItemsCount(1032));
		}
		else if(npcId == 30051 && st.getCond() == 1 && st.getQuestItemsCount(1033) >= 1)
		{
			htmltext = "30051-03.htm";
		}
		else if(npcId == 30051 && st.getCond() == 1 && st.getQuestItemsCount(1034) == 1)
		{
			htmltext = "30051-04.htm";
		}
		else if(npcId == 30312 && st.getCond() == 1 && st.getQuestItemsCount(1033) >= 1)
		{
			htmltext = "30312-06.htm";
		}
		else if(npcId == 30055 && st.getCond() == 1 && st.getQuestItemsCount(1033) >= 1)
		{
			htmltext = "30055-01.htm";
			st.giveItems(1034, 1);
			st.takeItems(1033, st.getQuestItemsCount(1033));
		}
		else if(npcId == 30055 && st.getCond() == 1 && st.getQuestItemsCount(1034) >= 1)
		{
			htmltext = "30055-02.htm";
		}
		else if(npcId == 30055 && st.getCond() == 1 && st.getQuestItemsCount(1033) == 0 && st.getQuestItemsCount(1034) == 0)
		{
			htmltext = "30055-03.htm";
		}
		else if(npcId == 30312 && st.getCond() == 1 && st.getQuestItemsCount(1034) >= 1 && st.getInt("id") != 154)
		{
			st.set("id", "154");
			htmltext = "30312-07.htm";
			st.takeItems(1034, st.getQuestItemsCount(1034));
			st.giveItems(113, 1);
			st.playSound("ItemSound.quest_finish");
			st.exitCurrentQuest(false);
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getCond() == 1 && st.getQuestItemsCount(1033) == 0)
		{
			st.rollAndGive(1032, 1, 1, 10, 14.0);
		}
		return null;
	}
}