package quests;

import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _634_InSearchofDimensionalFragments extends Quest implements ScriptFile
{
	int DIMENSION_FRAGMENT_ID = 7079;
	
	public _634_InSearchofDimensionalFragments()
	{
		super(true);
		for(int npcId = 31494;npcId < 31508;++npcId)
		{
			addTalkId(npcId);
			addStartNpc(npcId);
		}
		int mobs = 21208;
		while(mobs < 21256)
		{
			addKillId(mobs++);
		}
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
			htmltext = "dimension_keeper_1_q0634_03.htm";
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("634_2"))
		{
			htmltext = "dimension_keeper_1_q0634_06.htm";
			st.playSound("ItemSound.quest_finish");
			st.exitCurrentQuest(true);
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int id = st.getState();
		if(id == 1)
		{
			if(st.getPlayer().getLevel() > 20)
			{
				htmltext = "dimension_keeper_1_q0634_01.htm";
			}
			else
			{
				htmltext = "dimension_keeper_1_q0634_02.htm";
				st.exitCurrentQuest(true);
			}
		}
		else if(id == 2)
		{
			htmltext = "dimension_keeper_1_q0634_04.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int cnt = (int) (1.6 + (double) ((float) npc.getLevel() * 0.15f));
		st.rollAndGive(DIMENSION_FRAGMENT_ID, cnt, 90.0);
		return null;
	}
}