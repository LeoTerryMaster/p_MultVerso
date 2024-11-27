package quests;

import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2.gameserver.scripts.ScriptFile;

public class _005_MinersFavor extends Quest implements ScriptFile
{
	public final int BOLTER = 30554;
	public final int SHARI = 30517;
	public final int GARITA = 30518;
	public final int REED = 30520;
	public final int BRUNON = 30526;
	public final int BOLTERS_LIST = 1547;
	public final int MINING_BOOTS = 1548;
	public final int MINERS_PICK = 1549;
	public final int BOOMBOOM_POWDER = 1550;
	public final int REDSTONE_BEER = 1551;
	public final int BOLTERS_SMELLY_SOCKS = 1552;
	public final int NECKLACE = 906;
	
	public _005_MinersFavor()
	{
		super(false);
		addStartNpc(30554);
		addTalkId(30517);
		addTalkId(30518);
		addTalkId(30520);
		addTalkId(30526);
		addQuestItem(1547, 1552, 1548, 1549, 1550, 1551);
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
		if(event.equalsIgnoreCase("miner_bolter_q0005_03.htm"))
		{
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
			st.giveItems(1547, 1, false);
			st.giveItems(1552, 1, false);
		}
		else if(event.equalsIgnoreCase("blacksmith_bronp_q0005_02.htm"))
		{
			st.takeItems(1552, -1);
			st.giveItems(1549, 1, false);
			if(st.getQuestItemsCount(1547) > 0 && st.getQuestItemsCount(1548) + st.getQuestItemsCount(1549) + st.getQuestItemsCount(1550) + st.getQuestItemsCount(1551) == 4)
			{
				st.setCond(2);
				st.playSound("ItemSound.quest_middle");
			}
			else
			{
				st.playSound("ItemSound.quest_itemget");
			}
		}
		String htmltext = event;
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		String htmltext = "noquest";
		int cond = st.getCond();
		if(npcId == 30554)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 2)
				{
					htmltext = "miner_bolter_q0005_02.htm";
				}
				else
				{
					htmltext = "miner_bolter_q0005_01.htm";
					st.exitCurrentQuest(true);
				}
			}
			else if(cond == 1)
			{
				htmltext = "miner_bolter_q0005_04.htm";
			}
			else if(cond == 2 && st.getQuestItemsCount(1548) + st.getQuestItemsCount(1549) + st.getQuestItemsCount(1550) + st.getQuestItemsCount(1551) == 4)
			{
				htmltext = "miner_bolter_q0005_06.htm";
				st.takeItems(1548, -1);
				st.takeItems(1549, -1);
				st.takeItems(1550, -1);
				st.takeItems(1551, -1);
				st.takeItems(1547, -1);
				st.giveItems(906, 1, false);
				if(st.getPlayer().getClassId().getLevel() == 1 && !st.getPlayer().getVarB("ng1"))
				{
					st.getPlayer().sendPacket(new ExShowScreenMessage("  Delivery duty complete.\nGo find the Newbie Guide.", 5000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, true));
				}
				st.giveItems(57, 2466);
				st.unset("cond");
				st.playSound("ItemSound.quest_finish");
				st.exitCurrentQuest(false);
			}
		}
		else if(cond == 1 && st.getQuestItemsCount(1547) > 0)
		{
			if(npcId == 30517)
			{
				if(st.getQuestItemsCount(1550) == 0)
				{
					htmltext = "trader_chali_q0005_01.htm";
					st.giveItems(1550, 1, false);
					st.playSound("ItemSound.quest_itemget");
				}
				else
				{
					htmltext = "trader_chali_q0005_02.htm";
				}
			}
			else if(npcId == 30518)
			{
				if(st.getQuestItemsCount(1548) == 0)
				{
					htmltext = "trader_garita_q0005_01.htm";
					st.giveItems(1548, 1, false);
					st.playSound("ItemSound.quest_itemget");
				}
				else
				{
					htmltext = "trader_garita_q0005_02.htm";
				}
			}
			else if(npcId == 30520)
			{
				if(st.getQuestItemsCount(1551) == 0)
				{
					htmltext = "warehouse_chief_reed_q0005_01.htm";
					st.giveItems(1551, 1, false);
					st.playSound("ItemSound.quest_itemget");
				}
				else
				{
					htmltext = "warehouse_chief_reed_q0005_02.htm";
				}
			}
			else if(npcId == 30526 && st.getQuestItemsCount(1552) > 0)
			{
				htmltext = st.getQuestItemsCount(1549) == 0 ? "blacksmith_bronp_q0005_01.htm" : "blacksmith_bronp_q0005_03.htm";
			}
			if(st.getQuestItemsCount(1547) > 0 && st.getQuestItemsCount(1548) + st.getQuestItemsCount(1549) + st.getQuestItemsCount(1550) + st.getQuestItemsCount(1551) == 4)
			{
				st.setCond(2);
				st.playSound("ItemSound.quest_middle");
			}
		}
		return htmltext;
	}
}