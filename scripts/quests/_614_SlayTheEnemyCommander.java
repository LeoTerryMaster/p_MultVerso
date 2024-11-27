package quests;

import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _614_SlayTheEnemyCommander extends Quest implements ScriptFile
{
	private static final int DURAI = 31377;
	private static final int KETRAS_COMMANDER_TAYR = 25302;
	private static final int MARK_OF_VARKA_ALLIANCE1 = 7221;
	private static final int MARK_OF_VARKA_ALLIANCE2 = 7222;
	private static final int MARK_OF_VARKA_ALLIANCE3 = 7223;
	private static final int MARK_OF_VARKA_ALLIANCE4 = 7224;
	private static final int MARK_OF_VARKA_ALLIANCE5 = 7225;
	private static final int HEAD_OF_TAYR = 7241;
	private static final int FEATHER_OF_WISDOM = 7230;
	
	public _614_SlayTheEnemyCommander()
	{
		super(true);
		addStartNpc(31377);
		addKillId(25302);
		addQuestItem(7241);
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
			htmltext = "elder_ashas_barka_durai_q0614_0104.htm";
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("614_3"))
		{
			if(st.getQuestItemsCount(7241) >= 1)
			{
				htmltext = "elder_ashas_barka_durai_q0614_0201.htm";
				st.takeItems(7241, -1);
				st.giveItems(7230, 1);
				st.addExpAndSp(0, 10000);
				st.unset("cond");
				st.playSound("ItemSound.quest_finish");
				st.exitCurrentQuest(true);
			}
			else
			{
				htmltext = "elder_ashas_barka_durai_q0614_0106.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int cond = st.getCond();
		if(cond == 0)
		{
			if(st.getPlayer().getLevel() >= 75)
			{
				if(st.getQuestItemsCount(7224) == 1 || st.getQuestItemsCount(7225) == 1)
				{
					htmltext = "elder_ashas_barka_durai_q0614_0101.htm";
				}
				else
				{
					htmltext = "elder_ashas_barka_durai_q0614_0102.htm";
					st.exitCurrentQuest(true);
				}
			}
			else
			{
				htmltext = "elder_ashas_barka_durai_q0614_0103.htm";
				st.exitCurrentQuest(true);
			}
		}
		else if(cond == 1 && st.getQuestItemsCount(7241) == 0)
		{
			htmltext = "elder_ashas_barka_durai_q0614_0106.htm";
		}
		else if(cond == 2 && st.getQuestItemsCount(7241) >= 1)
		{
			htmltext = "elder_ashas_barka_durai_q0614_0105.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getCond() == 1)
		{
			st.giveItems(7241, 1);
			st.setCond(2);
			st.playSound("ItemSound.quest_itemget");
		}
		return null;
	}
}