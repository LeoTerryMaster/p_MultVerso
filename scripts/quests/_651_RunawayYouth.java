package quests;

import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _651_RunawayYouth extends Quest implements ScriptFile
{
	private static final int IVAN = 32014;
	private static final int BATIDAE = 31989;
	private static final int SOE = 736;
	protected NpcInstance _npc;
	
	public _651_RunawayYouth()
	{
		super(false);
		addStartNpc(IVAN);
		addTalkId(BATIDAE);
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
		if(event.equalsIgnoreCase("runaway_boy_ivan_q0651_03.htm"))
		{
			if(st.getQuestItemsCount(SOE) > 0)
			{
				st.setCond(1);
				st.setState(2);
				st.playSound("ItemSound.quest_accept");
				st.takeItems(SOE, 1);
				htmltext = "runaway_boy_ivan_q0651_04.htm";
				st.startQuestTimer("ivan_timer", 20000);
			}
		}
		else if(event.equalsIgnoreCase("runaway_boy_ivan_q0651_05.htm"))
		{
			st.exitCurrentQuest(true);
			st.playSound("ItemSound.quest_giveup");
		}
		else if(event.equalsIgnoreCase("ivan_timer"))
		{
			_npc.deleteMe();
			htmltext = null;
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == IVAN && cond == 0)
		{
			if(st.getPlayer().getLevel() >= 26)
			{
				htmltext = "runaway_boy_ivan_q0651_01.htm";
			}
			else
			{
				htmltext = "runaway_boy_ivan_q0651_01a.htm";
				st.exitCurrentQuest(true);
			}
		}
		else if(npcId == BATIDAE && cond == 1)
		{
			htmltext = "fisher_batidae_q0651_01.htm";
			st.giveItems(57, Math.round(2883.0 * st.getRateQuestsReward()));
			st.playSound("ItemSound.quest_finish");
			st.exitCurrentQuest(true);
		}
		return htmltext;
	}
}