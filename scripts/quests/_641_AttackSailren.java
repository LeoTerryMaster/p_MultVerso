package quests;

import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _641_AttackSailren extends Quest implements ScriptFile
{
	private static final int STATUE = 32109;
	private static final int VEL1 = 22196;
	private static final int VEL2 = 22197;
	private static final int VEL3 = 22198;
	private static final int VEL4 = 22218;
	private static final int VEL5 = 22223;
	private static final int PTE = 22199;
	private static final int FRAGMENTS = 8782;
	private static final int GAZKH = 8784;
	
	public _641_AttackSailren()
	{
		super(true);
		addStartNpc(STATUE);
		addKillId(VEL1);
		addKillId(VEL2);
		addKillId(VEL3);
		addKillId(VEL4);
		addKillId(VEL5);
		addKillId(PTE);
		addQuestItem(FRAGMENTS);
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
		if(event.equalsIgnoreCase("statue_of_shilen_q0641_05.htm"))
		{
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("statue_of_shilen_q0641_08.htm"))
		{
			st.playSound("ItemSound.quest_finish");
			st.takeItems(FRAGMENTS, -1);
			st.giveItems(GAZKH, 1);
			st.exitCurrentQuest(true);
			st.unset("cond");
		}
		String htmltext = event;
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int cond = st.getCond();
		if(cond == 0)
		{
			QuestState qs = st.getPlayer().getQuestState(_126_IntheNameofEvilPart2.class);
			if(qs == null || !qs.isCompleted())
			{
				htmltext = "statue_of_shilen_q0641_02.htm";
			}
			else if(st.getPlayer().getLevel() >= 77)
			{
				htmltext = "statue_of_shilen_q0641_01.htm";
			}
			else
			{
				st.exitCurrentQuest(true);
			}
		}
		else if(cond == 1)
		{
			htmltext = "statue_of_shilen_q0641_05.htm";
		}
		else if(cond == 2)
		{
			htmltext = "statue_of_shilen_q0641_07.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getQuestItemsCount(FRAGMENTS) < 30)
		{
			st.giveItems(FRAGMENTS, 1);
			if(st.getQuestItemsCount(FRAGMENTS) == 30)
			{
				st.playSound("ItemSound.quest_middle");
				st.setCond(2);
				st.setState(2);
			}
			else
			{
				st.playSound("ItemSound.quest_itemget");
			}
		}
		return null;
	}
}