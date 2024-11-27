package quests;

import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _109_InSearchOfTheNest extends Quest implements ScriptFile
{
	private static final int PIERCE = 31553;
	private static final int CORPSE = 32015;
	private static final int KAHMAN = 31554;
	private static final int MEMO = 8083;
	private static final int GOLDEN_BADGE_RECRUIT = 7246;
	private static final int GOLDEN_BADGE_SOLDIER = 7247;
	
	public _109_InSearchOfTheNest()
	{
		super(false);
		addStartNpc(31553);
		addTalkId(32015);
		addTalkId(31554);
		addQuestItem(8083);
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
		if(event.equalsIgnoreCase("Memo") && cond == 1)
		{
			st.giveItems(8083, 1);
			st.setCond(2);
			st.playSound("ItemSound.quest_itemget");
			htmltext = "You've find something...";
		}
		else if(event.equalsIgnoreCase("merc_cap_peace_q0109_0301.htm") && cond == 2)
		{
			st.takeItems(8083, -1);
			st.setCond(3);
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int id = st.getState();
		if(id == 3)
		{
			return "completed";
		}
		int cond = st.getCond();
		String htmltext = "noquest";
		if(id == 1)
		{
			if(st.getPlayer().getLevel() >= 66 && npcId == 31553 && (st.getQuestItemsCount(7246) > 0 || st.getQuestItemsCount(7247) > 0))
			{
				st.setState(2);
				st.playSound("ItemSound.quest_accept");
				st.setCond(1);
				htmltext = "merc_cap_peace_q0109_0105.htm";
			}
			else
			{
				htmltext = "merc_cap_peace_q0109_0103.htm";
				st.exitCurrentQuest(true);
			}
		}
		else if(id == 2)
		{
			if(npcId == 32015)
			{
				if(cond == 1)
				{
					htmltext = "corpse_of_scout_q0109_0101.htm";
				}
				else if(cond == 2)
				{
					htmltext = "corpse_of_scout_q0109_0203.htm";
				}
			}
			else if(npcId == 31553)
			{
				if(cond == 1)
				{
					htmltext = "merc_cap_peace_q0109_0304.htm";
				}
				else if(cond == 2)
				{
					htmltext = "merc_cap_peace_q0109_0201.htm";
				}
				else if(cond == 3)
				{
					htmltext = "merc_cap_peace_q0109_0303.htm";
				}
			}
			else if(npcId == 31554 && cond == 3)
			{
				htmltext = "merc_kahmun_q0109_0401.htm";
				st.giveItems(57, 5168);
				st.exitCurrentQuest(false);
				st.playSound("ItemSound.quest_finish");
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		return null;
	}
}