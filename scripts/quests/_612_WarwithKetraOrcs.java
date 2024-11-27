package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _612_WarwithKetraOrcs extends Quest implements ScriptFile
{
	private static final int elder_ashas_barka_durai = 31377;
	private static final int ketra_orc_footman = 21324;
	private static final int ketra_orc_trooper = 21327;
	private static final int ketra_orc_scout = 21328;
	private static final int ketra_orc_shaman = 21329;
	private static final int ketra_orc_warrior = 21331;
	private static final int ketra_orc_captain = 21332;
	private static final int ketra_orc_medium = 21334;
	private static final int ketra_orc_centurion = 21336;
	private static final int ketra_orc_seer = 21338;
	private static final int ketra_orc_officer = 21339;
	private static final int ketra_orc_praefect = 21340;
	private static final int ketra_orc_overseer = 21342;
	private static final int ketra_orc_legatus = 21343;
	private static final int ketra_high_shaman = 21345;
	private static final int ketra_soothsayer = 21347;
	private static final int q_ketra_molar = 7234;
	private static final int q_nephentes_seed = 7187;
	
	public _612_WarwithKetraOrcs()
	{
		super(true);
		addStartNpc(31377);
		addKillId(21324, 21327, 21328, 21329, 21331, 21332, 21334, 21336, 21338, 21339, 21340, 21342, 21343, 21345, 21347);
		addQuestItem(7234);
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
			st.setCond(1);
			st.set("war_with_ketra_orcs", String.valueOf(11), true);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
			htmltext = "elder_ashas_barka_durai_q0612_0104.htm";
		}
		else if(event.equalsIgnoreCase("reply_1"))
		{
			htmltext = "elder_ashas_barka_durai_q0612_0201.htm";
		}
		else if(event.equalsIgnoreCase("reply_3"))
		{
			if(st.getQuestItemsCount(7234) >= 100)
			{
				st.takeItems(7234, 100);
				st.giveItems(7187, 20);
				htmltext = "elder_ashas_barka_durai_q0612_0202.htm";
			}
			else
			{
				htmltext = "elder_ashas_barka_durai_q0612_0203.htm";
			}
		}
		else if(event.equalsIgnoreCase("reply_4"))
		{
			st.takeItems(7234, -1);
			st.unset("war_with_ketra_orcs");
			st.playSound("ItemSound.quest_finish");
			st.exitCurrentQuest(true);
			htmltext = "elder_ashas_barka_durai_q0612_0204.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "no-quest";
		int GetMemoState = st.getInt("war_with_ketra_orcs");
		int npcId = npc.getNpcId();
		int id = st.getState();
		switch(id)
		{
			case 1:
			{
				if(npcId != 31377)
					break;
				if(st.getPlayer().getLevel() >= 74)
				{
					htmltext = "elder_ashas_barka_durai_q0612_0101.htm";
					break;
				}
				htmltext = "elder_ashas_barka_durai_q0612_0103.htm";
				st.exitCurrentQuest(true);
				break;
			}
			case 2:
			{
				if(npcId != 31377 || GetMemoState != 11)
					break;
				if(st.getQuestItemsCount(7234) == 0)
				{
					htmltext = "elder_ashas_barka_durai_q0612_0106.htm";
					break;
				}
				htmltext = "elder_ashas_barka_durai_q0612_0105.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int GetMemoState = st.getInt("war_with_ketra_orcs");
		int npcId = npc.getNpcId();
		if(GetMemoState == 11)
		{
			if(npcId == 21324)
			{
				int i42 = Rnd.get(1000);
				if(i42 < 500)
				{
					st.giveItems(7234, 1);
					st.playSound("ItemSound.quest_itemget");
				}
			}
			else if(npcId == 21327)
			{
				int i43 = Rnd.get(1000);
				if(i43 < 510)
				{
					st.giveItems(7234, 1);
					st.playSound("ItemSound.quest_itemget");
				}
			}
			else if(npcId == 21328)
			{
				int i44 = Rnd.get(1000);
				if(i44 < 522)
				{
					st.giveItems(7234, 1);
					st.playSound("ItemSound.quest_itemget");
				}
			}
			else if(npcId == 21329)
			{
				int i45 = Rnd.get(1000);
				if(i45 < 519)
				{
					st.giveItems(7234, 1);
					st.playSound("ItemSound.quest_itemget");
				}
			}
			else if(npcId == 21331 || npcId == 21332)
			{
				int i46 = Rnd.get(1000);
				if(i46 < 529)
				{
					st.giveItems(7234, 1);
					st.playSound("ItemSound.quest_itemget");
				}
			}
			else if(npcId == 21334)
			{
				int i47 = Rnd.get(1000);
				if(i47 < 539)
				{
					st.giveItems(7234, 1);
					st.playSound("ItemSound.quest_itemget");
				}
			}
			else if(npcId == 21336)
			{
				int i48 = Rnd.get(1000);
				if(i48 < 548)
				{
					st.giveItems(7234, 1);
					st.playSound("ItemSound.quest_itemget");
				}
			}
			else if(npcId == 21338)
			{
				int i49 = Rnd.get(1000);
				if(i49 < 558)
				{
					st.giveItems(7234, 1);
					st.playSound("ItemSound.quest_itemget");
				}
			}
			else if(npcId == 21339 || npcId == 21340)
			{
				int i410 = Rnd.get(1000);
				if(i410 < 568)
				{
					st.giveItems(7234, 1);
					st.playSound("ItemSound.quest_itemget");
				}
			}
			else if(npcId == 21342)
			{
				int i411 = Rnd.get(1000);
				if(i411 < 578)
				{
					st.giveItems(7234, 1);
					st.playSound("ItemSound.quest_itemget");
				}
			}
			else if(npcId == 21343)
			{
				int i412 = Rnd.get(1000);
				if(i412 < 664)
				{
					st.giveItems(7234, 1);
					st.playSound("ItemSound.quest_itemget");
				}
			}
			else if(npcId == 21345)
			{
				int i413 = Rnd.get(1000);
				if(i413 < 713)
				{
					st.giveItems(7234, 1);
					st.playSound("ItemSound.quest_itemget");
				}
			}
			else if(npcId == 21347 && Rnd.get(1000) < 738)
			{
				st.giveItems(7234, 1);
				st.playSound("ItemSound.quest_itemget");
			}
		}
		return null;
	}
}