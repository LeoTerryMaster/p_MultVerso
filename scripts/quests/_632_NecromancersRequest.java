package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _632_NecromancersRequest extends Quest implements ScriptFile
{
	private static final int WIZARD = 31522;
	private static final int V_HEART = 7542;
	private static final int Z_BRAIN = 7543;
	private static final int ADENA_AMOUNT = 120000;
	private static final int[] VAMPIRES = {21568, 21573, 21582, 21585, 21586, 21587, 21588, 21589, 21590, 21591, 21592, 21593, 21594, 21595};
	private static final int[] UNDEADS = {21547, 21548, 21549, 21550, 21551, 21552, 21555, 21556, 21562, 21571, 21576, 21577, 21579};
	
	public _632_NecromancersRequest()
	{
		super(true);
		addStartNpc(31522);
		addKillId(VAMPIRES);
		addKillId(UNDEADS);
		addQuestItem(7542, 7543);
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
		if(event.equals("632_4"))
		{
			st.playSound("ItemSound.quest_finish");
			htmltext = "shadow_hardin_q0632_0204.htm";
			st.exitCurrentQuest(true);
		}
		else if(event.equals("632_1"))
		{
			htmltext = "shadow_hardin_q0632_0104.htm";
		}
		else if(event.equals("632_3"))
		{
			if(st.getCond() == 2 && st.getQuestItemsCount(7542) > 199)
			{
				st.takeItems(7542, 200);
				st.giveItems(57, 120000, true);
				st.playSound("ItemSound.quest_finish");
				st.setCond(1);
				htmltext = "shadow_hardin_q0632_0202.htm";
			}
		}
		else if(event.equals("quest_accept"))
		{
			if(st.getPlayer().getLevel() > 62)
			{
				htmltext = "shadow_hardin_q0632_0104.htm";
				st.setCond(1);
				st.setState(2);
				st.playSound("ItemSound.quest_accept");
			}
			else
			{
				htmltext = "shadow_hardin_q0632_0103.htm";
				st.exitCurrentQuest(true);
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
		if(cond == 0 && npcId == 31522)
		{
			htmltext = "shadow_hardin_q0632_0101.htm";
		}
		if(cond == 1)
		{
			htmltext = "shadow_hardin_q0632_0202.htm";
		}
		if(cond == 2 && st.getQuestItemsCount(7542) > 199)
		{
			htmltext = "shadow_hardin_q0632_0105.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		for(int i : VAMPIRES)
		{
			if(i != npc.getNpcId())
				continue;
			if(st.getCond() < 2 && Rnd.chance(50))
			{
				st.rollAndGive(7542, 1, 100.0);
				if(st.getQuestItemsCount(7542) > 199)
				{
					st.setCond(2);
				}
			}
			return null;
		}
		st.rollAndGive(7543, 1, 33.0);
		return null;
	}
}