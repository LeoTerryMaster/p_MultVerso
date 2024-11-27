package quests;

import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _607_ProveYourCourage extends Quest implements ScriptFile
{
	private static final int KADUN_ZU_KETRA = 31370;
	private static final int VARKAS_HERO_SHADITH = 25309;
	private static final int HEAD_OF_SHADITH = 7235;
	private static final int TOTEM_OF_VALOR = 7219;
	private static final int MARK_OF_KETRA_ALLIANCE1 = 7211;
	private static final int MARK_OF_KETRA_ALLIANCE2 = 7212;
	private static final int MARK_OF_KETRA_ALLIANCE3 = 7213;
	private static final int MARK_OF_KETRA_ALLIANCE4 = 7214;
	private static final int MARK_OF_KETRA_ALLIANCE5 = 7215;
	
	public _607_ProveYourCourage()
	{
		super(true);
		addStartNpc(31370);
		addKillId(25309);
		addQuestItem(7235);
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
		if(event.equals("quest_accept"))
		{
			htmltext = "elder_kadun_zu_ketra_q0607_0104.htm";
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equals("607_3"))
		{
			if(st.getQuestItemsCount(7235) >= 1)
			{
				htmltext = "elder_kadun_zu_ketra_q0607_0201.htm";
				st.takeItems(7235, -1);
				st.giveItems(7219, 1);
				st.addExpAndSp(0, 10000);
				st.unset("cond");
				st.playSound("ItemSound.quest_finish");
				st.exitCurrentQuest(true);
			}
			else
			{
				htmltext = "elder_kadun_zu_ketra_q0607_0106.htm";
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
				if(st.getQuestItemsCount(7213) == 1 || st.getQuestItemsCount(7214) == 1 || st.getQuestItemsCount(7215) == 1)
				{
					htmltext = "elder_kadun_zu_ketra_q0607_0101.htm";
				}
				else
				{
					htmltext = "elder_kadun_zu_ketra_q0607_0102.htm";
					st.exitCurrentQuest(true);
				}
			}
			else
			{
				htmltext = "elder_kadun_zu_ketra_q0607_0103.htm";
				st.exitCurrentQuest(true);
			}
		}
		else if(cond == 1 && st.getQuestItemsCount(7235) == 0)
		{
			htmltext = "elder_kadun_zu_ketra_q0607_0106.htm";
		}
		else if(cond == 2 && st.getQuestItemsCount(7235) >= 1)
		{
			htmltext = "elder_kadun_zu_ketra_q0607_0105.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		if(npcId == 25309 && st.getCond() == 1)
		{
			st.giveItems(7235, 1);
			st.setCond(2);
			st.playSound("ItemSound.quest_itemget");
		}
		return null;
	}
}