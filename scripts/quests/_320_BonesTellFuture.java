package quests;

import l2.gameserver.model.base.Race;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _320_BonesTellFuture extends Quest implements ScriptFile
{
	public final int BONE_FRAGMENT = 809;
	
	public _320_BonesTellFuture()
	{
		super(false);
		addStartNpc(30359);
		addTalkId(30359);
		addKillId(20517);
		addKillId(20518);
		addQuestItem(809);
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
		if(event.equalsIgnoreCase("tetrarch_kaitar_q0320_04.htm"))
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
		String htmltext;
		int cond = st.getCond();
		if(cond == 0)
		{
			if(st.getPlayer().getRace() != Race.darkelf)
			{
				htmltext = "tetrarch_kaitar_q0320_00.htm";
				st.exitCurrentQuest(true);
			}
			else if(st.getPlayer().getLevel() >= 10)
			{
				htmltext = "tetrarch_kaitar_q0320_03.htm";
			}
			else
			{
				htmltext = "tetrarch_kaitar_q0320_02.htm";
				st.exitCurrentQuest(true);
			}
		}
		else if(st.getQuestItemsCount(809) < 10)
		{
			htmltext = "tetrarch_kaitar_q0320_05.htm";
		}
		else
		{
			htmltext = "tetrarch_kaitar_q0320_06.htm";
			st.giveItems(57, 8470, true);
			st.takeItems(809, -1);
			st.exitCurrentQuest(true);
			st.unset("cond");
			st.playSound("ItemSound.quest_finish");
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		st.rollAndGive(809, 1, 1, 10, 10.0);
		if(st.getQuestItemsCount(809) >= 10)
		{
			st.setCond(2);
		}
		st.setState(2);
		return null;
	}
}