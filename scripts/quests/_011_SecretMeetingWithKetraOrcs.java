package quests;

import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _011_SecretMeetingWithKetraOrcs extends Quest implements ScriptFile
{
	int CADMON = 31296;
	int LEON = 31256;
	int WAHKAN = 31371;
	int MUNITIONS_BOX = 7231;
	
	public _011_SecretMeetingWithKetraOrcs()
	{
		super(false);
		addStartNpc(CADMON);
		addTalkId(LEON);
		addTalkId(WAHKAN);
		addQuestItem(MUNITIONS_BOX);
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
		if(event.equalsIgnoreCase("guard_cadmon_q0011_0104.htm"))
		{
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("trader_leon_q0011_0201.htm"))
		{
			st.giveItems(MUNITIONS_BOX, 1);
			st.setCond(2);
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("herald_wakan_q0011_0301.htm"))
		{
			st.takeItems(MUNITIONS_BOX, 1);
			st.addExpAndSp(82045, 6047);
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
		if(npcId == CADMON)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 74)
				{
					htmltext = "guard_cadmon_q0011_0101.htm";
				}
				else
				{
					htmltext = "guard_cadmon_q0011_0103.htm";
					st.exitCurrentQuest(true);
				}
			}
			else if(cond == 1)
			{
				htmltext = "guard_cadmon_q0011_0105.htm";
			}
		}
		else if(npcId == LEON)
		{
			if(cond == 1)
			{
				htmltext = "trader_leon_q0011_0101.htm";
			}
			else if(cond == 2)
			{
				htmltext = "trader_leon_q0011_0202.htm";
			}
		}
		else if(npcId == WAHKAN && cond == 2 && st.getQuestItemsCount(MUNITIONS_BOX) > 0)
		{
			htmltext = "herald_wakan_q0011_0201.htm";
		}
		return htmltext;
	}
}