package quests;

import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _650_ABrokenDream extends Quest implements ScriptFile
{
	private static final int RailroadEngineer = 32054;
	private static final int ForgottenCrewman = 22027;
	private static final int VagabondOfTheRuins = 22028;
	private static final int RemnantsOfOldDwarvesDreams = 8514;
	
	public _650_ABrokenDream()
	{
		super(false);
		addStartNpc(32054);
		addKillId(22027);
		addKillId(22028);
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
			htmltext = "ghost_of_railroadman_q0650_0103.htm";
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("650_4"))
		{
			htmltext = "ghost_of_railroadman_q0650_0205.htm";
			st.playSound("ItemSound.quest_finish");
			st.exitCurrentQuest(true);
			st.unset("cond");
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int cond = st.getCond();
		String htmltext = "noquest";
		if(cond == 0)
		{
			QuestState OceanOfDistantStar = st.getPlayer().getQuestState(_117_OceanOfDistantStar.class);
			if(OceanOfDistantStar != null)
			{
				if(OceanOfDistantStar.isCompleted())
				{
					if(st.getPlayer().getLevel() < 39)
					{
						st.exitCurrentQuest(true);
						htmltext = "ghost_of_railroadman_q0650_0102.htm";
					}
					else
					{
						htmltext = "ghost_of_railroadman_q0650_0101.htm";
					}
				}
				else
				{
					htmltext = "ghost_of_railroadman_q0650_0104.htm";
					st.exitCurrentQuest(true);
				}
			}
			else
			{
				htmltext = "ghost_of_railroadman_q0650_0104.htm";
				st.exitCurrentQuest(true);
			}
		}
		else if(cond == 1)
		{
			htmltext = "ghost_of_railroadman_q0650_0202.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		st.rollAndGive(8514, 1, 1, 68.0);
		return null;
	}
}