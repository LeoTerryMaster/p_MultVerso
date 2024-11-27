package quests;

import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _297_GateKeepersFavor extends Quest implements ScriptFile
{
	private static final int STARSTONE = 1573;
	private static final int GATEKEEPER_TOKEN = 1659;
	
	public _297_GateKeepersFavor()
	{
		super(false);
		addStartNpc(30540);
		addTalkId(30540);
		addKillId(20521);
		addQuestItem(1573);
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
		if(event.equalsIgnoreCase("gatekeeper_wirphy_q0297_03.htm"))
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
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == 30540)
		{
			if(cond == 0)
			{
				htmltext = st.getPlayer().getLevel() >= 15 ? "gatekeeper_wirphy_q0297_02.htm" : "gatekeeper_wirphy_q0297_01.htm";
			}
			else if(cond == 1 && st.getQuestItemsCount(1573) < 20)
			{
				htmltext = "gatekeeper_wirphy_q0297_04.htm";
			}
			else if(cond == 2 && st.getQuestItemsCount(1573) < 20)
			{
				htmltext = "gatekeeper_wirphy_q0297_04.htm";
			}
			else if(cond == 2 && st.getQuestItemsCount(1573) >= 20)
			{
				htmltext = "gatekeeper_wirphy_q0297_05.htm";
				st.takeItems(1573, -1);
				st.giveItems(1659, 2);
				st.exitCurrentQuest(true);
				st.playSound("ItemSound.quest_finish");
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		st.rollAndGive(1573, 1, 1, 20, 33.0);
		if(st.getQuestItemsCount(1573) >= 20)
		{
			st.setCond(2);
		}
		return null;
	}
}