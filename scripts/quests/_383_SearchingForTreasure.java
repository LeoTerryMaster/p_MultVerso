package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _383_SearchingForTreasure extends Quest implements ScriptFile
{
	private static final int trader_espen = 30890;
	private static final int pirates_t_chest = 31148;
	private static final int pirates_treasure_map = 5915;
	private static final int key_of_thief = 1661;
	private static final int elven_mithril_gloves = 2450;
	private static final int sages_worn_gloves = 2451;
	private static final int scrl_of_ench_am_d = 956;
	private static final int scrl_of_ench_am_c = 952;
	private static final int dye_s1c3_c = 4481;
	private static final int dye_s1d3_c = 4482;
	private static final int dye_c1s3_c = 4483;
	private static final int dye_c1c3_c = 4484;
	private static final int dye_d1s3_c = 4485;
	private static final int dye_d1c3_c = 4486;
	private static final int dye_i1m3_c = 4487;
	private static final int dye_i1w3_c = 4488;
	private static final int dye_m1i3_c = 4489;
	private static final int dye_m1w3_c = 4490;
	private static final int dye_w1i3_c = 4491;
	private static final int dye_w1m3_c = 4492;
	private static final int emerald = 1337;
	private static final int blue_onyx = 1338;
	private static final int onyx = 1339;
	private static final int q_loot_4 = 3447;
	private static final int q_loot_7 = 3450;
	private static final int q_loot_10 = 3453;
	private static final int q_loot_13 = 3456;
	private static final int q_musicnote_love = 4408;
	private static final int q_musicnote_battle = 4409;
	private static final int q_musicnote_celebration = 4418;
	private static final int q_musicnote_comedy = 4419;
	
	public _383_SearchingForTreasure()
	{
		super(false);
		addStartNpc(30890);
		addTalkId(31148);
		addQuestItem(5915);
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
		int GetMemoState = st.getInt("treasure_hunt");
		int npcId = npc.getNpcId();
		if(npcId == 30890)
		{
			if(event.equalsIgnoreCase("quest_accept"))
			{
				st.setCond(1);
				st.set("treasure_hunt", String.valueOf(1), true);
				st.takeItems(5915, 1);
				st.setState(2);
				st.playSound("ItemSound.quest_accept");
				htmltext = "trader_espen_q0383_08.htm";
			}
			else if(event.equalsIgnoreCase("reply_1"))
			{
				htmltext = "trader_espen_q0383_04.htm";
			}
			else if(event.equalsIgnoreCase("reply_2") && st.getQuestItemsCount(5915) > 0)
			{
				st.giveItems(57, 1000);
				st.unset("treasure_hunt");
				st.takeItems(5915, 1);
				htmltext = "trader_espen_q0383_05.htm";
			}
			else if(event.equalsIgnoreCase("reply_3"))
			{
				htmltext = st.getQuestItemsCount(5915) > 0 ? "trader_espen_q0383_06.htm" : "trader_espen_q0383_07.htm";
			}
			else if(event.equalsIgnoreCase("reply_4"))
			{
				htmltext = "trader_espen_q0383_09.htm";
			}
			else if(event.equalsIgnoreCase("reply_5"))
			{
				htmltext = "trader_espen_q0383_10.htm";
			}
			else if(event.equalsIgnoreCase("reply_6"))
			{
				htmltext = "trader_espen_q0383_11.htm";
			}
			else if(event.equalsIgnoreCase("reply_7") && GetMemoState == 1)
			{
				st.setCond(2);
				st.set("treasure_hunt", String.valueOf(2), true);
				st.playSound("ItemSound.quest_middle");
				htmltext = "trader_espen_q0383_12.htm";
			}
		}
		else if(npcId == 31148 && event.equalsIgnoreCase("reply_1"))
		{
			if(st.getQuestItemsCount(1661) == 0)
			{
				htmltext = "pirates_t_chest_q0383_02.htm";
			}
			else if(GetMemoState == 2 && st.getQuestItemsCount(1661) >= 1)
			{
				st.takeItems(1661, 1);
				st.unset("treasure_hunt");
				st.playSound("ItemSound.quest_finish");
				st.exitCurrentQuest(true);
				htmltext = "pirates_t_chest_q0383_03.htm";
				int i1 = 0;
				int i0 = Rnd.get(100);
				if(i0 < 5)
				{
					st.giveItems(2450, 1);
				}
				else if(i0 < 6)
				{
					st.giveItems(2451, 1);
				}
				else if(i0 < 18)
				{
					st.giveItems(956, 1);
				}
				else if(i0 < 28)
				{
					st.giveItems(952, 1);
				}
				else
				{
					i1 += 500;
				}
				int i2 = Rnd.get(1000);
				if(i2 < 25)
				{
					st.giveItems(4481, 1);
				}
				else if(i2 < 50)
				{
					st.giveItems(4482, 1);
				}
				else if(i2 < 75)
				{
					st.giveItems(4483, 1);
				}
				else if(i2 < 100)
				{
					st.giveItems(4484, 1);
				}
				else if(i2 < 125)
				{
					st.giveItems(4485, 1);
				}
				else if(i2 < 150)
				{
					st.giveItems(4486, 1);
				}
				else if(i2 < 175)
				{
					st.giveItems(4487, 1);
				}
				else if(i2 < 200)
				{
					st.giveItems(4488, 1);
				}
				else if(i2 < 225)
				{
					st.giveItems(4489, 1);
				}
				else if(i2 < 250)
				{
					st.giveItems(4490, 1);
				}
				else if(i2 < 275)
				{
					st.giveItems(4491, 1);
				}
				else if(i2 < 300)
				{
					st.giveItems(4492, 1);
				}
				else
				{
					i1 += 300;
				}
				int i3 = Rnd.get(100);
				if(i3 < 4)
				{
					st.giveItems(1337, 1);
				}
				else if(i3 < 8)
				{
					st.giveItems(1338, 2);
				}
				else if(i3 < 12)
				{
					st.giveItems(1339, 2);
				}
				else if(i3 < 16)
				{
					st.giveItems(3447, 2);
				}
				else if(i3 < 20)
				{
					st.giveItems(3450, 1);
				}
				else if(i3 < 25)
				{
					st.giveItems(3453, 1);
				}
				else if(i3 < 27)
				{
					st.giveItems(3456, 1);
				}
				else
				{
					i1 += 500;
				}
				int i4 = Rnd.get(100);
				if(i4 < 20)
				{
					st.giveItems(4408, 1);
				}
				else if(i4 < 40)
				{
					st.giveItems(4409, 1);
				}
				else if(i4 < 60)
				{
					st.giveItems(4418, 1);
				}
				else if(i4 < 80)
				{
					st.giveItems(4419, 1);
				}
				else
				{
					i1 += 500;
				}
				st.giveItems(57, (long) i1);
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "no-quest";
		int GetMemoState = st.getInt("treasure_hunt");
		int npcId = npc.getNpcId();
		int id = st.getState();
		switch(id)
		{
			case 1:
			{
				if(npcId != 30890)
					break;
				if(st.getPlayer().getLevel() < 42)
				{
					htmltext = "trader_espen_q0383_01.htm";
					st.exitCurrentQuest(true);
					break;
				}
				if(st.getPlayer().getLevel() >= 42 && st.getQuestItemsCount(5915) == 0)
				{
					htmltext = "trader_espen_q0383_02.htm";
					st.exitCurrentQuest(true);
					break;
				}
				if(st.getPlayer().getLevel() < 42 || st.getQuestItemsCount(5915) <= 0)
					break;
				htmltext = "trader_espen_q0383_03.htm";
				break;
			}
			case 2:
			{
				if(npcId == 30890)
				{
					if(GetMemoState == 1)
					{
						htmltext = "trader_espen_q0383_13.htm";
						break;
					}
					if(GetMemoState != 2)
						break;
					htmltext = "trader_espen_q0383_14.htm";
					break;
				}
				if(npcId != 31148 || GetMemoState != 2)
					break;
				htmltext = "pirates_t_chest_q0383_01.htm";
			}
		}
		return htmltext;
	}
}