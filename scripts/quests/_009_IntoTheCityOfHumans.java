package quests;

import l2.gameserver.model.base.Race;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _009_IntoTheCityOfHumans extends Quest implements ScriptFile
{
	public final int PETUKAI = 30583;
	public final int TANAPI = 30571;
	public final int TAMIL = 30576;
	public final int SCROLL_OF_ESCAPE_GIRAN = 7126;
	public final int MARK_OF_TRAVELER = 7570;
	
	public _009_IntoTheCityOfHumans()
	{
		super(false);
		addStartNpc(30583);
		addTalkId(30583);
		addTalkId(30571);
		addTalkId(30576);
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
		if(event.equalsIgnoreCase("centurion_petukai_q0009_0104.htm"))
		{
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("seer_tanapi_q0009_0201.htm"))
		{
			st.setCond(2);
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("gatekeeper_tamil_q0009_0301.htm"))
		{
			st.giveItems(7126, 1);
			st.giveItems(7570, 1);
			st.unset("cond");
			st.playSound("ItemSound.quest_finish");
			st.exitCurrentQuest(false);
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
		if(npcId == 30583)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getRace() == Race.orc && st.getPlayer().getLevel() >= 3)
				{
					htmltext = "centurion_petukai_q0009_0101.htm";
				}
				else
				{
					htmltext = "centurion_petukai_q0009_0102.htm";
					st.exitCurrentQuest(true);
				}
			}
			else if(cond == 1)
			{
				htmltext = "centurion_petukai_q0009_0105.htm";
			}
		}
		else if(npcId == 30571)
		{
			if(cond == 1)
			{
				htmltext = "seer_tanapi_q0009_0101.htm";
			}
			else if(cond == 2)
			{
				htmltext = "seer_tanapi_q0009_0202.htm";
			}
		}
		else if(npcId == 30576 && cond == 2)
		{
			htmltext = "gatekeeper_tamil_q0009_0201.htm";
		}
		return htmltext;
	}
}