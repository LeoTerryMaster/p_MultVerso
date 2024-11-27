package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.base.Race;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _274_SkirmishWithTheWerewolves extends Quest implements ScriptFile
{
	private static final int MARAKU_WEREWOLF_HEAD = 1477;
	private static final int NECKLACE_OF_VALOR = 1507;
	private static final int NECKLACE_OF_COURAGE = 1506;
	private static final int MARAKU_WOLFMEN_TOTEM = 1501;
	
	public _274_SkirmishWithTheWerewolves()
	{
		super(false);
		addStartNpc(30569);
		addKillId(20363);
		addKillId(20364);
		addQuestItem(1477);
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
		if(event.equals("prefect_brukurse_q0274_03.htm"))
		{
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		String htmltext = event;
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int id = st.getState();
		int cond = st.getCond();
		if(id == 1)
		{
			if(st.getPlayer().getRace() != Race.orc)
			{
				htmltext = "prefect_brukurse_q0274_00.htm";
				st.exitCurrentQuest(true);
			}
			else if(st.getPlayer().getLevel() < 9)
			{
				htmltext = "prefect_brukurse_q0274_01.htm";
				st.exitCurrentQuest(true);
			}
			else
			{
				if(st.getQuestItemsCount(1507) > 0 || st.getQuestItemsCount(1506) > 0)
				{
					htmltext = "prefect_brukurse_q0274_02.htm";
					return htmltext;
				}
				htmltext = "prefect_brukurse_q0274_07.htm";
			}
		}
		else if(cond == 1)
		{
			htmltext = "prefect_brukurse_q0274_04.htm";
		}
		else if(cond == 2)
		{
			if(st.getQuestItemsCount(1477) < 40)
			{
				htmltext = "prefect_brukurse_q0274_04.htm";
			}
			else
			{
				st.takeItems(1477, -1);
				st.giveItems(57, 3500, true);
				if(st.getQuestItemsCount(1501) >= 1)
				{
					st.giveItems(57, st.getQuestItemsCount(1501) * 600, true);
					st.takeItems(1501, -1);
				}
				htmltext = "prefect_brukurse_q0274_05.htm";
				st.exitCurrentQuest(true);
				st.playSound("ItemSound.quest_finish");
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getCond() == 1 && st.getQuestItemsCount(1477) < 40)
		{
			if(st.getQuestItemsCount(1477) < 39)
			{
				st.playSound("ItemSound.quest_itemget");
			}
			else
			{
				st.playSound("ItemSound.quest_middle");
				st.setCond(2);
			}
			st.giveItems(1477, 1);
		}
		if(Rnd.chance(5))
		{
			st.giveItems(1501, 1);
		}
		return null;
	}
}