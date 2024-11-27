package quests;

import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _277_GatekeepersOffering extends Quest implements ScriptFile
{
	private static final int STARSTONE1_ID = 1572;
	private static final int GATEKEEPER_CHARM_ID = 1658;
	
	public _277_GatekeepersOffering()
	{
		super(false);
		addStartNpc(30576);
		addKillId(20333);
		addQuestItem(1572);
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
			if(st.getPlayer().getLevel() >= 15)
			{
				htmltext = "gatekeeper_tamil_q0277_03.htm";
				st.setCond(1);
				st.setState(2);
				st.playSound("ItemSound.quest_accept");
			}
			else
			{
				htmltext = "gatekeeper_tamil_q0277_01.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == 30576 && cond == 0)
		{
			htmltext = "gatekeeper_tamil_q0277_02.htm";
		}
		else if(npcId == 30576 && cond == 1 && st.getQuestItemsCount(1572) < 20)
		{
			htmltext = "gatekeeper_tamil_q0277_04.htm";
		}
		else if(npcId == 30576 && cond == 2 && st.getQuestItemsCount(1572) < 20)
		{
			htmltext = "gatekeeper_tamil_q0277_04.htm";
		}
		else if(npcId == 30576 && cond == 2 && st.getQuestItemsCount(1572) >= 20)
		{
			htmltext = "gatekeeper_tamil_q0277_05.htm";
			st.takeItems(1572, -1);
			st.giveItems(1658, 2);
			st.playSound("ItemSound.quest_finish");
			st.exitCurrentQuest(true);
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		st.rollAndGive(1572, 1, 1, 20, 33.0);
		if(st.getQuestItemsCount(1572) >= 20)
		{
			st.setCond(2);
		}
		return null;
	}
}