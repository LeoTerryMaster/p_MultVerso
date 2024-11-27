package quests;

import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _121_PavelTheGiants extends Quest implements ScriptFile
{
	private static final int NEWYEAR = 31961;
	private static final int YUMI = 32041;
	
	public _121_PavelTheGiants()
	{
		super(false);
		addStartNpc(NEWYEAR);
		addTalkId(NEWYEAR, YUMI);
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
		if(event.equals("collecter_yumi_q0121_0201.htm"))
		{
			st.playSound("ItemSound.quest_finish");
			st.addExpAndSp(10000, 0);
			st.exitCurrentQuest(false);
		}
		return event;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int id = st.getState();
		int cond = st.getCond();
		if(id == 1 && npcId == NEWYEAR)
		{
			if(st.getPlayer().getLevel() >= 46)
			{
				htmltext = "head_blacksmith_newyear_q0121_0101.htm";
				st.setCond(1);
				st.setState(2);
				st.playSound("ItemSound.quest_accept");
			}
			else
			{
				htmltext = "head_blacksmith_newyear_q0121_0103.htm";
				st.exitCurrentQuest(false);
			}
		}
		else if(id == 2)
		{
			htmltext = npcId == YUMI && cond == 1 ? "collecter_yumi_q0121_0101.htm" : "head_blacksmith_newyear_q0121_0105.htm";
		}
		return htmltext;
	}
}