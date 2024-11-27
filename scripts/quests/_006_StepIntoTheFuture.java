package quests;

import l2.gameserver.model.base.Race;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _006_StepIntoTheFuture extends Quest implements ScriptFile
{
	private static final int Roxxy = 30006;
	private static final int Baulro = 30033;
	private static final int Windawood = 30311;
	private static final int BaulrosLetter = 7571;
	private static final int ScrollOfEscapeGiran = 7126;
	private static final int MarkOfTraveler = 7570;
	
	public _006_StepIntoTheFuture()
	{
		super(false);
		addStartNpc(30006);
		addTalkId(30033);
		addTalkId(30311);
		addQuestItem(7571);
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
		if(event.equalsIgnoreCase("rapunzel_q0006_0104.htm"))
		{
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("baul_q0006_0201.htm"))
		{
			st.giveItems(7571, 1, false);
			st.setCond(2);
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("sir_collin_windawood_q0006_0301.htm"))
		{
			st.takeItems(7571, -1);
			st.setCond(3);
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("rapunzel_q0006_0401.htm"))
		{
			st.giveItems(7126, 1, false);
			st.giveItems(7570, 1, false);
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
		if(npcId == 30006)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getRace() == Race.human && st.getPlayer().getLevel() >= 3)
				{
					htmltext = "rapunzel_q0006_0101.htm";
				}
				else
				{
					htmltext = "rapunzel_q0006_0102.htm";
					st.exitCurrentQuest(true);
				}
			}
			else if(cond == 1)
			{
				htmltext = "rapunzel_q0006_0105.htm";
			}
			else if(cond == 3)
			{
				htmltext = "rapunzel_q0006_0301.htm";
			}
		}
		else if(npcId == 30033)
		{
			if(cond == 1)
			{
				htmltext = "baul_q0006_0101.htm";
			}
			else if(cond == 2 && st.getQuestItemsCount(7571) > 0)
			{
				htmltext = "baul_q0006_0202.htm";
			}
		}
		else if(npcId == 30311)
		{
			if(cond == 2 && st.getQuestItemsCount(7571) > 0)
			{
				htmltext = "sir_collin_windawood_q0006_0201.htm";
			}
			else if(cond == 2 && st.getQuestItemsCount(7571) == 0)
			{
				htmltext = "sir_collin_windawood_q0006_0302.htm";
			}
			else if(cond == 3)
			{
				htmltext = "sir_collin_windawood_q0006_0303.htm";
			}
		}
		return htmltext;
	}
}