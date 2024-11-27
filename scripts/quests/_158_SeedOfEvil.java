package quests;

import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _158_SeedOfEvil extends Quest implements ScriptFile
{
	int CLAY_TABLET_ID = 1025;
	int ENCHANT_ARMOR_D = 956;
	
	public _158_SeedOfEvil()
	{
		super(false);
		addStartNpc(30031);
		addKillId(27016);
		addQuestItem(CLAY_TABLET_ID);
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
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
			htmltext = "30031-04.htm";
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
			st.set("id", "0");
		}
		String htmltext = "noquest";
		if(npcId == 30031 && st.getCond() == 0)
		{
			if(st.getCond() < 15)
			{
				if(st.getPlayer().getLevel() >= 21)
				{
					htmltext = "30031-03.htm";
					return htmltext;
				}
				htmltext = "30031-02.htm";
				st.exitCurrentQuest(true);
			}
			else
			{
				htmltext = "30031-02.htm";
				st.exitCurrentQuest(true);
			}
		}
		else if(npcId == 30031 && st.getCond() == 0)
		{
			htmltext = "completed";
		}
		else if(npcId == 30031 && st.getCond() != 0 && st.getQuestItemsCount(CLAY_TABLET_ID) == 0)
		{
			htmltext = "30031-05.htm";
		}
		else if(npcId == 30031 && st.getCond() != 0 && st.getQuestItemsCount(CLAY_TABLET_ID) != 0)
		{
			st.takeItems(CLAY_TABLET_ID, st.getQuestItemsCount(CLAY_TABLET_ID));
			st.playSound("ItemSound.quest_finish");
			st.giveItems(ENCHANT_ARMOR_D, 1);
			htmltext = "30031-06.htm";
			st.exitCurrentQuest(false);
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getQuestItemsCount(CLAY_TABLET_ID) == 0)
		{
			st.giveItems(CLAY_TABLET_ID, 1);
			st.playSound("ItemSound.quest_middle");
			st.setCond(2);
		}
		return null;
	}
}