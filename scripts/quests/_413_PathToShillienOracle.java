package quests;

import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _413_PathToShillienOracle extends Quest implements ScriptFile
{
	public final int SIDRA = 30330;
	public final int ADONIUS = 30375;
	public final int TALBOT = 30377;
	public final int ZOMBIE_SOLDIER = 20457;
	public final int ZOMBIE_WARRIOR = 20458;
	public final int SHIELD_SKELETON = 20514;
	public final int SKELETON_INFANTRYMAN = 20515;
	public final int DARK_SUCCUBUS = 20776;
	public final int SIDRAS_LETTER1_ID = 1262;
	public final int BLANK_SHEET1_ID = 1263;
	public final int BLOODY_RUNE1_ID = 1264;
	public final int GARMIEL_BOOK_ID = 1265;
	public final int PRAYER_OF_ADON_ID = 1266;
	public final int PENITENTS_MARK_ID = 1267;
	public final int ASHEN_BONES_ID = 1268;
	public final int ANDARIEL_BOOK_ID = 1269;
	public final int ORB_OF_ABYSS_ID = 1270;
	public final int[] ASHEN_BONES_DROP = {20457, 20458, 20514, 20515};
	
	public _413_PathToShillienOracle()
	{
		super(false);
		addStartNpc(30330);
		addTalkId(30375);
		addTalkId(30377);
		addKillId(20776);
		for(int i : ASHEN_BONES_DROP)
		{
			addKillId(i);
		}
		addQuestItem(1268);
		addQuestItem(1262);
		addQuestItem(1269);
		addQuestItem(1267);
		addQuestItem(1265);
		addQuestItem(1266);
		addQuestItem(1263);
		addQuestItem(1264);
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
		if(event.equalsIgnoreCase("1"))
		{
			htmltext = "master_sidra_q0413_06.htm";
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
			st.giveItems(1262, 1);
		}
		else if(event.equalsIgnoreCase("413_1"))
		{
			if(st.getPlayer().getLevel() >= 18 && st.getPlayer().getClassId().getId() == 38 && st.getQuestItemsCount(1270) < 1)
			{
				htmltext = "master_sidra_q0413_05.htm";
			}
			else if(st.getPlayer().getClassId().getId() != 38)
			{
				htmltext = st.getPlayer().getClassId().getId() == 42 ? "master_sidra_q0413_02a.htm" : "master_sidra_q0413_03.htm";
			}
			else if(st.getPlayer().getLevel() < 18 && st.getPlayer().getClassId().getId() == 38)
			{
				htmltext = "master_sidra_q0413_02.htm";
			}
			else if(st.getPlayer().getLevel() >= 18 && st.getPlayer().getClassId().getId() == 38 && st.getQuestItemsCount(1270) > 0)
			{
				htmltext = "master_sidra_q0413_04.htm";
			}
		}
		else if(event.equalsIgnoreCase("30377_1"))
		{
			htmltext = "magister_talbot_q0413_02.htm";
			st.takeItems(1262, -1);
			st.giveItems(1263, 5);
			st.playSound("ItemSound.quest_itemget");
			st.setCond(2);
		}
		else if(event.equalsIgnoreCase("30375_1"))
		{
			htmltext = "priest_adonius_q0413_02.htm";
		}
		else if(event.equalsIgnoreCase("30375_2"))
		{
			htmltext = "priest_adonius_q0413_03.htm";
		}
		else if(event.equalsIgnoreCase("30375_3"))
		{
			htmltext = "priest_adonius_q0413_04.htm";
			st.takeItems(1266, -1);
			st.giveItems(1267, 1);
			st.playSound("ItemSound.quest_itemget");
			st.setCond(5);
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == 30330)
		{
			if(cond < 1)
			{
				htmltext = "master_sidra_q0413_01.htm";
			}
			else if(cond == 1)
			{
				htmltext = "master_sidra_q0413_07.htm";
			}
			else if(cond == 2 | cond == 3)
			{
				htmltext = "master_sidra_q0413_08.htm";
			}
			else if(cond > 3 && cond < 7)
			{
				htmltext = "master_sidra_q0413_09.htm";
			}
			else if(cond == 7 && st.getQuestItemsCount(1269) > 0 && st.getQuestItemsCount(1265) > 0)
			{
				htmltext = "master_sidra_q0413_10.htm";
				st.exitCurrentQuest(true);
				if(st.getPlayer().getClassId().getLevel() == 1)
				{
					st.giveItems(1270, 1);
					if(!st.getPlayer().getVarB("prof1"))
					{
						st.getPlayer().setVar("prof1", "1", -1);
						st.addExpAndSp(3200, 3120);
					}
				}
				st.playSound("ItemSound.quest_finish");
			}
		}
		else if(npcId == 30377)
		{
			if(cond == 1 && st.getQuestItemsCount(1262) > 0)
			{
				htmltext = "magister_talbot_q0413_01.htm";
			}
			else if(cond == 2)
			{
				if(st.getQuestItemsCount(1264) < 1)
				{
					htmltext = "magister_talbot_q0413_03.htm";
				}
				else if(st.getQuestItemsCount(1264) > 0)
				{
					htmltext = "magister_talbot_q0413_04.htm";
				}
			}
			else if(cond == 3 && st.getQuestItemsCount(1264) > 4)
			{
				htmltext = "magister_talbot_q0413_05.htm";
				st.takeItems(1264, -1);
				st.giveItems(1265, 1);
				st.giveItems(1266, 1);
				st.playSound("ItemSound.quest_itemget");
				st.setCond(4);
			}
			else if(cond > 3 && cond < 7)
			{
				htmltext = "magister_talbot_q0413_06.htm";
			}
			else if(cond == 7)
			{
				htmltext = "magister_talbot_q0413_07.htm";
			}
		}
		else if(npcId == 30375)
		{
			if(cond == 4 && st.getQuestItemsCount(1266) > 0)
			{
				htmltext = "priest_adonius_q0413_01.htm";
			}
			else if(cond == 5 && st.getQuestItemsCount(1268) < 1)
			{
				htmltext = "priest_adonius_q0413_05.htm";
			}
			else if(cond == 5 && st.getQuestItemsCount(1268) < 10)
			{
				htmltext = "priest_adonius_q0413_06.htm";
			}
			else if(cond == 6 && st.getQuestItemsCount(1268) > 9)
			{
				htmltext = "priest_adonius_q0413_07.htm";
				st.takeItems(1268, -1);
				st.takeItems(1267, -1);
				st.giveItems(1269, 1);
				st.playSound("ItemSound.quest_itemget");
				st.setCond(7);
			}
			else if(cond == 7 && st.getQuestItemsCount(1269) > 0)
			{
				htmltext = "priest_adonius_q0413_08.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == 20776 && cond == 2 && st.getQuestItemsCount(1263) > 0)
		{
			st.giveItems(1264, 1);
			st.takeItems(1263, 1);
			if(st.getQuestItemsCount(1263) < 1)
			{
				st.playSound("ItemSound.quest_middle");
				st.setCond(3);
			}
			else
			{
				st.playSound("ItemSound.quest_itemget");
			}
		}
		for(int i : ASHEN_BONES_DROP)
		{
			if(npcId != i || cond != 5 || st.getQuestItemsCount(1268) >= 10)
				continue;
			st.giveItems(1268, 1);
			if(st.getQuestItemsCount(1268) > 9)
			{
				st.playSound("ItemSound.quest_middle");
				st.setCond(6);
				continue;
			}
			st.playSound("ItemSound.quest_itemget");
		}
		return null;
	}
}