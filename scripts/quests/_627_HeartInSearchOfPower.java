package quests;

import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _627_HeartInSearchOfPower extends Quest implements ScriptFile
{
	private static final int M_NECROMANCER = 31518;
	private static final int ENFEUX = 31519;
	private static final int SEAL_OF_LIGHT = 7170;
	private static final int GEM_OF_SUBMISSION = 7171;
	private static final int GEM_OF_SAINTS = 7172;
	private static final int MOLD_HARDENER = 4041;
	private static final int ENRIA = 4042;
	private static final int ASOFE = 4043;
	private static final int THONS = 4044;
	
	public _627_HeartInSearchOfPower()
	{
		super(true);
		addStartNpc(31518);
		addTalkId(31518);
		addTalkId(31519);
		int mobs = 21520;
		while(mobs <= 21541)
		{
			addKillId(mobs++);
		}
		addQuestItem(7171);
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
		if(event.equals("dark_necromancer_q0627_0104.htm"))
		{
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equals("dark_necromancer_q0627_0201.htm"))
		{
			st.takeItems(7171, -1);
			st.giveItems(7170, 1, false);
			st.setCond(3);
		}
		else if(event.equals("enfeux_q0627_0301.htm"))
		{
			st.takeItems(7170, 1);
			st.giveItems(7172, 1, false);
			st.setCond(4);
		}
		else if(event.equals("dark_necromancer_q0627_0401.htm"))
		{
			st.takeItems(7172, 1);
		}
		else
		{
			if(event.equals("627_11"))
			{
				htmltext = "dark_necromancer_q0627_0402.htm";
				st.giveItems(57, 100000, true);
			}
			else if(event.equals("627_12"))
			{
				htmltext = "dark_necromancer_q0627_0402.htm";
				st.giveItems(4043, 13, true);
				st.giveItems(57, 6400, true);
			}
			else if(event.equals("627_13"))
			{
				htmltext = "dark_necromancer_q0627_0402.htm";
				st.giveItems(4044, 13, true);
				st.giveItems(57, 6400, true);
			}
			else if(event.equals("627_14"))
			{
				htmltext = "dark_necromancer_q0627_0402.htm";
				st.giveItems(4042, 6, true);
				st.giveItems(57, 13600, true);
			}
			else if(event.equals("627_15"))
			{
				htmltext = "dark_necromancer_q0627_0402.htm";
				st.giveItems(4041, 3, true);
				st.giveItems(57, 17200, true);
			}
			st.playSound("ItemSound.quest_finish");
			st.exitCurrentQuest(true);
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == 31518)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 60)
				{
					htmltext = "dark_necromancer_q0627_0101.htm";
				}
				else
				{
					htmltext = "dark_necromancer_q0627_0103.htm";
					st.exitCurrentQuest(true);
				}
			}
			else if(cond == 1)
			{
				htmltext = "dark_necromancer_q0627_0106.htm";
			}
			else if(st.getQuestItemsCount(7171) >= 300)
			{
				htmltext = "dark_necromancer_q0627_0105.htm";
			}
			else if(st.getQuestItemsCount(7172) > 0)
			{
				htmltext = "dark_necromancer_q0627_0301.htm";
			}
		}
		else if(npcId == 31519 && st.getQuestItemsCount(7170) > 0)
		{
			htmltext = "enfeux_q0627_0201.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getCond() == 1)
		{
			st.rollAndGive(7171, 1, 100.0);
			if(st.getQuestItemsCount(7171) >= 300)
			{
				st.playSound("ItemSound.quest_middle");
				st.setCond(2);
			}
			else
			{
				st.playSound("ItemSound.quest_itemget");
			}
		}
		return null;
	}
}