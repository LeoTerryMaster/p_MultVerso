package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _357_WarehouseKeepersAmbition extends Quest implements ScriptFile
{
	private static final int DROPRATE = 50;
	private static final int REWARD1 = 900;
	private static final int REWARD2 = 10000;
	private static final int SILVA = 30686;
	private static final int MOB1 = 20594;
	private static final int MOB2 = 20595;
	private static final int MOB3 = 20596;
	private static final int MOB4 = 20597;
	private static final int MOB5 = 20598;
	private static final int JADE_CRYSTAL = 5867;
	
	public _357_WarehouseKeepersAmbition()
	{
		super(false);
		addStartNpc(30686);
		addKillId(20594);
		addKillId(20595);
		addKillId(20596);
		addKillId(20597);
		addKillId(20598);
		addQuestItem(5867);
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
		if(event.equalsIgnoreCase("warehouse_keeper_silva_q0357_04.htm"))
		{
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("warehouse_keeper_silva_q0357_08.htm"))
		{
			long count = st.getQuestItemsCount(5867);
			if(count > 0)
			{
				long reward = count * 900;
				if(count >= 100)
				{
					reward += 10000;
				}
				st.takeItems(5867, -1);
				st.giveItems(57, reward);
			}
			else
			{
				htmltext = "warehouse_keeper_silva_q0357_06.htm";
			}
		}
		else if(event.equalsIgnoreCase("warehouse_keeper_silva_q0357_11.htm"))
		{
			st.playSound("ItemSound.quest_finish");
			st.exitCurrentQuest(true);
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int id = st.getState();
		int cond = st.getCond();
		long jade = st.getQuestItemsCount(5867);
		if(cond == 0 || id == 1)
		{
			if(st.getPlayer().getLevel() >= 47)
			{
				htmltext = "warehouse_keeper_silva_q0357_02.htm";
			}
			else
			{
				htmltext = "warehouse_keeper_silva_q0357_01.htm";
				st.exitCurrentQuest(true);
			}
		}
		else if(jade == 0)
		{
			htmltext = "warehouse_keeper_silva_q0357_06.htm";
		}
		else if(jade > 0)
		{
			htmltext = "warehouse_keeper_silva_q0357_07.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(Rnd.chance(50))
		{
			st.giveItems(5867, 1);
			st.playSound("ItemSound.quest_itemget");
		}
		return null;
	}
}