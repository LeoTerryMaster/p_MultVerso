package quests;

import l2.gameserver.model.entity.oly.NoblesController;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.network.l2.s2c.SkillList;
import l2.gameserver.scripts.ScriptFile;

public class _247_PossessorOfaPreciousSoul4 extends Quest implements ScriptFile
{
	private static final int CARADINE = 31740;
	private static final int LADY_OF_LAKE = 31745;
	private static final int CARADINE_LETTER_LAST = 7679;
	private static final int NOBLESS_TIARA = 7694;
	
	public _247_PossessorOfaPreciousSoul4()
	{
		super(false);
		addStartNpc(CARADINE);
		addTalkId(LADY_OF_LAKE);
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
		int cond = st.getCond();
		if(cond == 0 && event.equals("caradine_q0247_03.htm"))
		{
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		else if(cond == 1)
		{
			if(event.equals("caradine_q0247_04.htm"))
			{
				return htmltext;
			}
			if(event.equals("caradine_q0247_05.htm"))
			{
				st.setCond(2);
				st.takeItems(CARADINE_LETTER_LAST, 1);
				st.getPlayer().teleToLocation(143230, 44030, -3030);
				return htmltext;
			}
		}
		else if(cond == 2)
		{
			if(event.equals("caradine_q0247_06.htm"))
			{
				return htmltext;
			}
			if(event.equals("caradine_q0247_05.htm"))
			{
				st.getPlayer().teleToLocation(143230, 44030, -3030);
				return htmltext;
			}
			if(event.equals("lady_of_the_lake_q0247_02.htm"))
			{
				return htmltext;
			}
			if(event.equals("lady_of_the_lake_q0247_03.htm"))
			{
				return htmltext;
			}
			if(event.equals("lady_of_the_lake_q0247_04.htm"))
			{
				return htmltext;
			}
			if(event.equals("lady_of_the_lake_q0247_05.htm"))
			{
				if(st.getPlayer().getLevel() >= 75)
				{
					st.giveItems(NOBLESS_TIARA, 1);
					st.playSound("ItemSound.quest_finish");
					st.unset("cond");
					st.exitCurrentQuest(false);
					NoblesController.getInstance().addNoble(st.getPlayer());
					st.getPlayer().setNoble(true);
					st.getPlayer().updatePledgeClass();
					st.getPlayer().updateNobleSkills();
					st.getPlayer().sendPacket(new SkillList(st.getPlayer()));
					st.getPlayer().broadcastUserInfo(true);
				}
				else
				{
					htmltext = "lady_of_the_lake_q0247_06.htm";
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		if(!st.getPlayer().isSubClassActive())
		{
			return "Subclass only!";
		}
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int id = st.getState();
		int cond = st.getCond();
		if(npcId == CARADINE)
		{
			QuestState previous = st.getPlayer().getQuestState(_246_PossessorOfaPreciousSoul3.class);
			if(id == 1 && previous != null && previous.getState() == 3)
			{
				if(st.getPlayer().getLevel() < 75)
				{
					htmltext = "caradine_q0247_02.htm";
					st.exitCurrentQuest(true);
				}
				else
				{
					htmltext = "caradine_q0247_01.htm";
				}
			}
			else if(cond == 1)
			{
				htmltext = "caradine_q0247_03.htm";
			}
			else if(cond == 2)
			{
				htmltext = "caradine_q0247_06.htm";
			}
		}
		else if(npcId == LADY_OF_LAKE && cond == 2)
		{
			htmltext = st.getPlayer().getLevel() >= 75 ? "lady_of_the_lake_q0247_01.htm" : "lady_of_the_lake_q0247_06.htm";
		}
		return htmltext;
	}
}