package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _647_InfluxOfMachines extends Quest implements ScriptFile
{
	private static final int DROP_CHANCE = 60;
	private static final int DESTROYED_GOLEM_SHARD = 8100;
	private static final int[] RECIPES = {4963, 4964, 4965, 4966, 4967, 4968, 4969, 4970, 4971, 4972, 5000, 5001, 5002, 5003, 5004, 5005, 5006, 5007, 8298, 8306, 8310, 8312, 8322, 8324};
	
	public _647_InfluxOfMachines()
	{
		super(true);
		addStartNpc(32069);
		addTalkId(32069);
		int i = 22052;
		while(i < 22079)
		{
			addKillId(i++);
		}
		addQuestItem(8100);
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
			htmltext = "collecter_gutenhagen_q0647_0103.htm";
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("647_3"))
		{
			if(st.getQuestItemsCount(8100) >= 500)
			{
				st.takeItems(8100, -1);
				st.giveItems(RECIPES[Rnd.get(RECIPES.length)], 1);
				htmltext = "collecter_gutenhagen_q0647_0201.htm";
				st.playSound("ItemSound.quest_finish");
				st.exitCurrentQuest(true);
			}
			else
			{
				htmltext = "collecter_gutenhagen_q0647_0106.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int cond = st.getCond();
		long count = st.getQuestItemsCount(8100);
		if(cond == 0)
		{
			if(st.getPlayer().getLevel() >= 46)
			{
				htmltext = "collecter_gutenhagen_q0647_0101.htm";
			}
			else
			{
				htmltext = "collecter_gutenhagen_q0647_0102.htm";
				st.exitCurrentQuest(true);
			}
		}
		else if(cond == 1 && count < 500)
		{
			htmltext = "collecter_gutenhagen_q0647_0106.htm";
		}
		else if(cond == 2 && count >= 500)
		{
			htmltext = "collecter_gutenhagen_q0647_0105.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getCond() == 1 && st.rollAndGive(8100, 1, 1, 500, 60.0 * npc.getTemplate().rateHp))
		{
			st.setCond(2);
		}
		return null;
	}
}