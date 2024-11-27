package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _363_SorrowfulSoundofFlute extends Quest implements ScriptFile
{
	public final int NANARIN = 30956;
	public final int BARBADO = 30959;
	public final int POITAN = 30458;
	public final int HOLVAS = 30058;
	public final int MUSICAL_SCORE = 4420;
	public final int EVENT_CLOTHES = 4318;
	public final int NANARINS_FLUTE = 4319;
	public final int SABRINS_BLACK_BEER = 4320;
	public final int Musical_Score = 4420;
	
	public _363_SorrowfulSoundofFlute()
	{
		super(false);
		addStartNpc(30956);
		addTalkId(30956);
		addTalkId(30458);
		addTalkId(30058);
		addTalkId(30959);
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
		if(event.equalsIgnoreCase("30956_2.htm"))
		{
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
			st.takeItems(4318, -1);
			st.takeItems(4319, -1);
			st.takeItems(4320, -1);
		}
		else if(event.equalsIgnoreCase("30956_4.htm"))
		{
			st.giveItems(4319, 1);
			st.playSound("ItemSound.quest_middle");
			st.setCond(3);
		}
		else if(event.equalsIgnoreCase("answer1"))
		{
			st.giveItems(4318, 1);
			st.playSound("ItemSound.quest_middle");
			st.setCond(3);
			htmltext = "30956_6.htm";
		}
		else if(event.equalsIgnoreCase("answer2"))
		{
			st.giveItems(4320, 1);
			st.playSound("ItemSound.quest_middle");
			st.setCond(3);
			htmltext = "30956_6.htm";
		}
		else if(event.equalsIgnoreCase("30956_7.htm"))
		{
			st.giveItems(4420, 1);
			st.playSound("ItemSound.quest_finish");
			st.exitCurrentQuest(true);
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		String htmltext = "noquest";
		int cond = st.getCond();
		if(npcId == 30956)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() < 15)
				{
					htmltext = "30956-00.htm";
					st.exitCurrentQuest(true);
				}
				else
				{
					htmltext = "30956_1.htm";
				}
			}
			else if(cond == 1)
			{
				htmltext = "30956_8.htm";
			}
			else if(cond == 2)
			{
				htmltext = "30956_3.htm";
			}
			else if(cond == 3)
			{
				htmltext = "30956_6.htm";
			}
			else if(cond == 4)
			{
				htmltext = "30956_5.htm";
			}
		}
		else if(npcId == 30959)
		{
			if(cond == 3)
			{
				if(st.getQuestItemsCount(4318) > 0)
				{
					st.takeItems(4318, -1);
					htmltext = "30959_2.htm";
					st.exitCurrentQuest(true);
				}
				else if(st.getQuestItemsCount(4320) > 0)
				{
					st.takeItems(4320, -1);
					htmltext = "30959_2.htm";
					st.exitCurrentQuest(true);
				}
				else
				{
					st.takeItems(4319, -1);
					st.setCond(4);
					st.playSound("ItemSound.quest_middle");
					htmltext = "30959_1.htm";
				}
			}
			else if(cond == 4)
			{
				htmltext = "30959_3.htm";
			}
		}
		else if(npcId == 30058 && (cond == 1 || cond == 2))
		{
			st.setCond(2);
			htmltext = Rnd.chance(60) ? "30058_2.htm" : "30058_1.htm";
		}
		else if(npcId == 30458 && (cond == 1 || cond == 2))
		{
			st.setCond(2);
			htmltext = Rnd.chance(60) ? "30458_2.htm" : "30458_1.htm";
		}
		return htmltext;
	}
}