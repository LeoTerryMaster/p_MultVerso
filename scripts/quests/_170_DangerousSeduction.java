package quests;

import l2.gameserver.model.base.Race;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _170_DangerousSeduction extends Quest implements ScriptFile
{
	private static final int Vellior = 30305;
	private static final int NightmareCrystal = 1046;
	private static final int Merkenis = 27022;
	
	public _170_DangerousSeduction()
	{
		super(false);
		addStartNpc(30305);
		addTalkId(30305);
		addKillId(27022);
		addQuestItem(1046);
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
		if(event.equalsIgnoreCase("30305-04.htm"))
		{
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		String htmltext = event;
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		String htmltext = "noquest";
		int cond = st.getCond();
		if(npcId == 30305)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getRace() != Race.darkelf)
				{
					htmltext = "30305-00.htm";
					st.exitCurrentQuest(true);
				}
				else if(st.getPlayer().getLevel() < 21)
				{
					htmltext = "30305-02.htm";
					st.exitCurrentQuest(true);
				}
				else
				{
					htmltext = "30305-03.htm";
				}
			}
			else if(cond == 1)
			{
				htmltext = "30305-05.htm";
			}
			else if(cond == 2)
			{
				st.takeItems(1046, -1);
				st.giveItems(57, 102680, true);
				htmltext = "30305-06.htm";
				st.playSound("ItemSound.quest_finish");
				st.exitCurrentQuest(false);
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(cond == 1 && npcId == 27022)
		{
			if(st.getQuestItemsCount(1046) == 0)
			{
				st.giveItems(1046, 1);
			}
			st.playSound("ItemSound.quest_middle");
			st.setCond(2);
			st.setState(2);
		}
		return null;
	}
}