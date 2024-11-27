package quests;

import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _608_SlayTheEnemyCommander extends Quest implements ScriptFile
{
	private static final int KADUN_ZU_KETRA = 31370;
	private static final int VARKAS_COMMANDER_MOS = 25312;
	private static final int HEAD_OF_MOS = 7236;
	private static final int TOTEM_OF_WISDOM = 7220;
	private static final int MARK_OF_KETRA_ALLIANCE1 = 7211;
	private static final int MARK_OF_KETRA_ALLIANCE2 = 7212;
	private static final int MARK_OF_KETRA_ALLIANCE3 = 7213;
	private static final int MARK_OF_KETRA_ALLIANCE4 = 7214;
	private static final int MARK_OF_KETRA_ALLIANCE5 = 7215;
	
	public _608_SlayTheEnemyCommander()
	{
		super(true);
		addStartNpc(31370);
		addKillId(25312);
		addQuestItem(7236);
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
			htmltext = "elder_kadun_zu_ketra_q0608_0104.htm";
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("608_3"))
		{
			if(st.getQuestItemsCount(7236) >= 1)
			{
				htmltext = "elder_kadun_zu_ketra_q0608_0201.htm";
				st.takeItems(7236, -1);
				st.giveItems(7220, 1);
				st.addExpAndSp(0, 10000);
				st.unset("cond");
				st.playSound("ItemSound.quest_finish");
				st.exitCurrentQuest(true);
			}
			else
			{
				htmltext = "elder_kadun_zu_ketra_q0608_0106.htm";
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
				if(st.getQuestItemsCount(7214) == 1 || st.getQuestItemsCount(7215) == 1)
				{
					htmltext = "elder_kadun_zu_ketra_q0608_0101.htm";
				}
				else
				{
					htmltext = "elder_kadun_zu_ketra_q0608_0102.htm";
					st.exitCurrentQuest(true);
				}
			}
			else
			{
				htmltext = "elder_kadun_zu_ketra_q0608_0103.htm";
				st.exitCurrentQuest(true);
			}
		}
		else if(cond == 1 && st.getQuestItemsCount(7236) == 0)
		{
			htmltext = "elder_kadun_zu_ketra_q0608_0106.htm";
		}
		else if(cond == 2 && st.getQuestItemsCount(7236) >= 1)
		{
			htmltext = "elder_kadun_zu_ketra_q0608_0105.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getCond() == 1)
		{
			st.giveItems(7236, 1);
			st.setCond(2);
			st.playSound("ItemSound.quest_itemget");
		}
		return null;
	}
}