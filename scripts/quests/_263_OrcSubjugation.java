package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.base.Race;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _263_OrcSubjugation extends Quest implements ScriptFile
{
	public final int KAYLEEN = 30346;
	public final int BALOR_ORC_ARCHER = 20385;
	public final int BALOR_ORC_FIGHTER = 20386;
	public final int BALOR_ORC_FIGHTER_LEADER = 20387;
	public final int BALOR_ORC_LIEUTENANT = 20388;
	public final int ORC_AMULET = 1116;
	public final int ORC_NECKLACE = 1117;
	
	public _263_OrcSubjugation()
	{
		super(false);
		addStartNpc(30346);
		addKillId(20385, 20386, 20387, 20388);
		addQuestItem(1116, 1117);
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
		if(event.equals("sentry_kayleen_q0263_03.htm"))
		{
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equals("sentry_kayleen_q0263_06.htm"))
		{
			st.setCond(0);
			st.playSound("ItemSound.quest_finish");
			st.exitCurrentQuest(true);
		}
		String htmltext = event;
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int cond = st.getCond();
		if(cond == 0)
		{
			if(st.getPlayer().getLevel() >= 8 && st.getPlayer().getRace() == Race.darkelf)
			{
				htmltext = "sentry_kayleen_q0263_02.htm";
				return htmltext;
			}
			if(st.getPlayer().getRace() != Race.darkelf)
			{
				htmltext = "sentry_kayleen_q0263_00.htm";
				st.exitCurrentQuest(true);
			}
			else if(st.getPlayer().getLevel() < 8)
			{
				htmltext = "sentry_kayleen_q0263_01.htm";
				st.exitCurrentQuest(true);
			}
		}
		else if(cond == 1)
		{
			if(st.getQuestItemsCount(1116) == 0 && st.getQuestItemsCount(1117) == 0)
			{
				htmltext = "sentry_kayleen_q0263_04.htm";
			}
			else if(st.getQuestItemsCount(1116) + st.getQuestItemsCount(1117) >= 10)
			{
				htmltext = "sentry_kayleen_q0263_05.htm";
				st.giveItems(57, st.getQuestItemsCount(1116) * 20 + st.getQuestItemsCount(1117) * 30 + 1100);
				st.takeItems(1116, -1);
				st.takeItems(1117, -1);
			}
			else
			{
				htmltext = "sentry_kayleen_q0263_05.htm";
				st.giveItems(57, st.getQuestItemsCount(1116) * 20 + st.getQuestItemsCount(1117) * 30);
				st.takeItems(1116, -1);
				st.takeItems(1117, -1);
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		if(st.getCond() == 1 && Rnd.chance(60))
		{
			if(npcId == 20385)
			{
				st.giveItems(1116, 1);
			}
			else if(npcId == 20386 || npcId == 20387 || npcId == 20388)
			{
				st.giveItems(1117, 1);
			}
			st.playSound("ItemSound.quest_itemget");
		}
		return null;
	}
}