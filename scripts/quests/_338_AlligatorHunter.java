package quests;

import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _338_AlligatorHunter extends Quest implements ScriptFile
{
	private static final int Enverun = 30892;
	private static final int AlligatorLeather = 4337;
	private static final int CrokianLad = 20804;
	private static final int DailaonLad = 20805;
	private static final int CrokianLadWarrior = 20806;
	private static final int FarhiteLad = 20807;
	private static final int NosLad = 20808;
	private static final int SwampTribe = 20991;
	public final int[][] DROPLIST_COND = {{1, 0, 20804, 0, 4337, 0, 60, 1}, {1, 0, 20805, 0, 4337, 0, 60, 1}, {1, 0, 20806, 0, 4337, 0, 60, 1}, {1, 0, 20807, 0, 4337, 0, 60, 1}, {1, 0, 20808, 0, 4337, 0, 60, 1}, {1, 0, 20991, 0, 4337, 0, 60, 1}};
	
	public _338_AlligatorHunter()
	{
		super(false);
		addStartNpc(30892);
		for(int i = 0;i < DROPLIST_COND.length;++i)
		{
			addKillId(DROPLIST_COND[i][2]);
		}
		addQuestItem(4337);
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
		if(event.equalsIgnoreCase("30892-02.htm"))
		{
			st.playSound("ItemSound.quest_accept");
			st.setCond(1);
			st.setState(2);
		}
		else if(event.equalsIgnoreCase("30892-02-afmenu.htm"))
		{
			long AdenaCount = st.getQuestItemsCount(4337) * 40;
			st.takeItems(4337, -1);
			st.giveItems(57, AdenaCount);
		}
		else if(event.equalsIgnoreCase("quit"))
		{
			if(st.getQuestItemsCount(4337) >= 1)
			{
				long AdenaCount = st.getQuestItemsCount(4337) * 40;
				st.takeItems(4337, -1);
				st.giveItems(57, AdenaCount);
				htmltext = "30892-havequit.htm";
			}
			else
			{
				htmltext = "30892-havent.htm";
			}
			st.playSound("ItemSound.quest_finish");
			st.exitCurrentQuest(true);
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "no-quest";
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == 30892)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 40)
				{
					htmltext = "30892-01.htm";
				}
				else
				{
					htmltext = "30892-00.htm";
					st.exitCurrentQuest(true);
				}
			}
			else
			{
				htmltext = st.getQuestItemsCount(4337) == 0 ? "30892-02-rep.htm" : "30892-menu.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		for(int i = 0;i < DROPLIST_COND.length;++i)
		{
			if(cond != DROPLIST_COND[i][0] || npcId != DROPLIST_COND[i][2] || DROPLIST_COND[i][3] != 0 && st.getQuestItemsCount(DROPLIST_COND[i][3]) <= 0)
				continue;
			if(DROPLIST_COND[i][5] == 0)
			{
				st.rollAndGive(DROPLIST_COND[i][4], DROPLIST_COND[i][7], (double) DROPLIST_COND[i][6]);
				continue;
			}
			if(!st.rollAndGive(DROPLIST_COND[i][4], DROPLIST_COND[i][7], DROPLIST_COND[i][7], DROPLIST_COND[i][5], (double) DROPLIST_COND[i][6]) || DROPLIST_COND[i][1] == cond || DROPLIST_COND[i][1] == 0)
				continue;
			st.setCond(Integer.valueOf(DROPLIST_COND[i][1]).intValue());
			st.setState(2);
		}
		return null;
	}
}