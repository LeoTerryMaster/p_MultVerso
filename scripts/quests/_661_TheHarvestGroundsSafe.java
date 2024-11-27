package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _661_TheHarvestGroundsSafe extends Quest implements ScriptFile
{
	private static final int NORMAN = 30210;
	private static final int GIANT_POISON_BEE = 21095;
	private static final int CLOYDY_BEAST = 21096;
	private static final int YOUNG_ARANEID = 21097;
	private static final int STING_OF_GIANT_POISON = 8283;
	private static final int TALON_OF_YOUNG_ARANEID = 8285;
	private static final int CLOUDY_GEM = 8284;
	
	public _661_TheHarvestGroundsSafe()
	{
		super(false);
		addStartNpc(NORMAN);
		addKillId(GIANT_POISON_BEE);
		addKillId(CLOYDY_BEAST);
		addKillId(YOUNG_ARANEID);
		addQuestItem(STING_OF_GIANT_POISON);
		addQuestItem(TALON_OF_YOUNG_ARANEID);
		addQuestItem(CLOUDY_GEM);
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
		if(event.equalsIgnoreCase("warehouse_keeper_norman_q0661_0103.htm") || event.equalsIgnoreCase("warehouse_keeper_norman_q0661_0201.htm"))
		{
			st.setState(2);
			st.setCond(1);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("warehouse_keeper_norman_q0661_0205.htm"))
		{
			long STING = st.getQuestItemsCount(STING_OF_GIANT_POISON);
			long TALON = st.getQuestItemsCount(TALON_OF_YOUNG_ARANEID);
			long GEM = st.getQuestItemsCount(CLOUDY_GEM);
			if(STING + GEM + TALON >= 10)
			{
				st.giveItems(57, STING * 57 + GEM * 56 + TALON * 60 + 2800);
				st.takeItems(STING_OF_GIANT_POISON, -1);
				st.takeItems(TALON_OF_YOUNG_ARANEID, -1);
				st.takeItems(CLOUDY_GEM, -1);
			}
			else
			{
				st.giveItems(57, STING * 57 + GEM * 56 + TALON * 60);
				st.takeItems(STING_OF_GIANT_POISON, -1);
				st.takeItems(TALON_OF_YOUNG_ARANEID, -1);
				st.takeItems(CLOUDY_GEM, -1);
			}
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("warehouse_keeper_norman_q0661_0204.htm"))
		{
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
		int cond = st.getCond();
		if(cond == 0)
		{
			if(st.getPlayer().getLevel() >= 21)
			{
				htmltext = "warehouse_keeper_norman_q0661_0101.htm";
			}
			else
			{
				htmltext = "warehouse_keeper_norman_q0661_0102.htm";
				st.exitCurrentQuest(true);
			}
		}
		else if(cond == 1)
		{
			htmltext = st.getQuestItemsCount(STING_OF_GIANT_POISON) + st.getQuestItemsCount(TALON_OF_YOUNG_ARANEID) + st.getQuestItemsCount(CLOUDY_GEM) > 0 ? "warehouse_keeper_norman_q0661_0105.htm" : "warehouse_keeper_norman_q0661_0206.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getState() != 2)
		{
			return null;
		}
		int npcId = npc.getNpcId();
		if(st.getCond() == 1)
		{
			if(npcId == GIANT_POISON_BEE && Rnd.chance(75))
			{
				st.giveItems(STING_OF_GIANT_POISON, 1);
				st.playSound("ItemSound.quest_itemget");
			}
			if(npcId == CLOYDY_BEAST && Rnd.chance(71))
			{
				st.giveItems(CLOUDY_GEM, 1);
				st.playSound("ItemSound.quest_itemget");
			}
			if(npcId == YOUNG_ARANEID && Rnd.chance(67))
			{
				st.giveItems(TALON_OF_YOUNG_ARANEID, 1);
				st.playSound("ItemSound.quest_itemget");
			}
		}
		return null;
	}
}