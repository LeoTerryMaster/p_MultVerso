package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _618_IntoTheFlame extends Quest implements ScriptFile
{
	private static final int KLEIN = 31540;
	private static final int HILDA = 31271;
	private static final int VACUALITE_ORE = 7265;
	private static final int VACUALITE = 7266;
	private static final int FLOATING_STONE = 7267;
	private static final int CHANCE_FOR_QUEST_ITEMS = 50;
	
	public _618_IntoTheFlame()
	{
		super(true);
		addStartNpc(31540);
		addTalkId(31271);
		addKillId(21274, 21275, 21276, 21278);
		addKillId(21282, 21283, 21284, 21286);
		addKillId(21290, 21291, 21292, 21294);
		addQuestItem(7265);
		addQuestItem(7266);
		addQuestItem(7267);
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
		if(event.equalsIgnoreCase("watcher_valakas_klein_q0618_0104.htm") && cond == 0)
		{
			st.setState(2);
			st.setCond(1);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("watcher_valakas_klein_q0618_0401.htm"))
		{
			if(st.getQuestItemsCount(7266) > 0 && cond == 4)
			{
				st.playSound("ItemSound.quest_finish");
				st.exitCurrentQuest(true);
				st.giveItems(7267, 1);
			}
			else
			{
				htmltext = "watcher_valakas_klein_q0618_0104.htm";
			}
		}
		else if(event.equalsIgnoreCase("blacksmith_hilda_q0618_0201.htm") && cond == 1)
		{
			st.setCond(2);
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("blacksmith_hilda_q0618_0301.htm"))
		{
			if(cond == 3 && st.getQuestItemsCount(7265) == 50)
			{
				st.takeItems(7265, -1);
				st.giveItems(7266, 1);
				st.setCond(4);
				st.playSound("ItemSound.quest_middle");
			}
			else
			{
				htmltext = "blacksmith_hilda_q0618_0203.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == 31540)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() < 60)
				{
					htmltext = "watcher_valakas_klein_q0618_0103.htm";
					st.exitCurrentQuest(true);
				}
				else
				{
					htmltext = "watcher_valakas_klein_q0618_0101.htm";
				}
			}
			else
			{
				htmltext = cond == 4 && st.getQuestItemsCount(7266) > 0 ? "watcher_valakas_klein_q0618_0301.htm" : "watcher_valakas_klein_q0618_0104.htm";
			}
		}
		else if(npcId == 31271)
		{
			htmltext = cond == 1 ? "blacksmith_hilda_q0618_0101.htm" : cond == 3 && st.getQuestItemsCount(7265) >= 50 ? "blacksmith_hilda_q0618_0202.htm" : cond == 4 ? "blacksmith_hilda_q0618_0303.htm" : "blacksmith_hilda_q0618_0203.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		long count = st.getQuestItemsCount(7265);
		if(Rnd.chance(50) && count < 50)
		{
			st.giveItems(7265, 1);
			if(count == 49)
			{
				st.setCond(3);
				st.playSound("ItemSound.quest_middle");
			}
			else
			{
				st.playSound("ItemSound.quest_itemget");
			}
		}
		return null;
	}
}