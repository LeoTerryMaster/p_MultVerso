package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.base.ClassId;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

import java.util.HashMap;
import java.util.Map;

public class _226_TestOfHealer extends Quest implements ScriptFile
{
	private static final int COND1 = 1;
	private static final int COND2 = 2;
	private static final int COND3 = 3;
	private static final int COND4 = 4;
	private static final int COND5 = 5;
	private static final int COND6 = 6;
	private static final int COND7 = 7;
	private static final int COND8 = 8;
	private static final int COND9 = 9;
	private static final int COND10 = 10;
	private static final int COND11 = 11;
	private static final int COND12 = 12;
	private static final int COND13 = 13;
	private static final int COND14 = 14;
	private static final int COND15 = 15;
	private static final int COND16 = 16;
	private static final int COND17 = 17;
	private static final int COND18 = 18;
	private static final int COND19 = 19;
	private static final int COND20 = 20;
	private static final int COND21 = 21;
	private static final int COND22 = 22;
	private static final int COND23 = 23;
	private static final int Bandellos = 30473;
	private static final int Perrin = 30428;
	private static final int OrphanGirl = 30659;
	private static final int Allana = 30424;
	private static final int FatherGupu = 30658;
	private static final int Windy = 30660;
	private static final int Sorius = 30327;
	private static final int Daurin = 30674;
	private static final int Piper = 30662;
	private static final int Slein = 30663;
	private static final int Kein = 30664;
	private static final int MysteryDarkElf = 30661;
	private static final int Kristina = 30665;
	private static final int REPORT_OF_PERRIN_ID = 2810;
	private static final int CRISTINAS_LETTER_ID = 2811;
	private static final int PICTURE_OF_WINDY_ID = 2812;
	private static final int GOLDEN_STATUE_ID = 2813;
	private static final int WINDYS_PEBBLES_ID = 2814;
	private static final int ORDER_OF_SORIUS_ID = 2815;
	private static final int SECRET_LETTER1_ID = 2816;
	private static final int SECRET_LETTER2_ID = 2817;
	private static final int SECRET_LETTER3_ID = 2818;
	private static final int SECRET_LETTER4_ID = 2819;
	private static final int MARK_OF_HEALER_ID = 2820;
	private static final Map<Integer, Integer[]> DROPLIST = new HashMap<>();
	
	static
	{
		DROPLIST.put(27134, new Integer[] {2, 3, 0});
		DROPLIST.put(27123, new Integer[] {11, 12, 2816});
		DROPLIST.put(27124, new Integer[] {14, 15, 2817});
		DROPLIST.put(27125, new Integer[] {16, 17, 2818});
		DROPLIST.put(27127, new Integer[] {18, 19, 2819});
	}
	
	public _226_TestOfHealer()
	{
		super(false);
		addStartNpc(30473);
		addTalkId(30327, 30424, 30428, 30473, 30658, 30659, 30660, 30661, 30662, 30663, 30664, 30665, 30674);
		addKillId(20150, 27123, 27124, 27125, 27127, 27134);
		addQuestItem(2810, 2811, 2812, 2813, 2814, 2815, 2816, 2817, 2818, 2819);
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
			htmltext = "30473-04.htm";
			if(!st.getPlayer().getVarB("dd3"))
			{
				st.giveItems(7562, 45);
				st.getPlayer().setVar("dd3", "1", -1);
			}
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
			st.giveItems(2810, 1);
		}
		else if(event.equalsIgnoreCase("30473_1"))
		{
			htmltext = "30473-08.htm";
		}
		else if(event.equalsIgnoreCase("30473_2"))
		{
			htmltext = "30473-09.htm";
			st.takeItems(2813, -1);
			st.giveItems(2820, 1);
			st.addExpAndSp(37831, 18750);
			st.playSound("ItemSound.quest_finish");
			st.exitCurrentQuest(false);
		}
		else if(event.equalsIgnoreCase("30428_1"))
		{
			htmltext = "30428-02.htm";
			st.setCond(2);
			st.addSpawn(27134, -93254, 147559, -2679);
		}
		else if(event.equalsIgnoreCase("30658_1"))
		{
			if(st.getQuestItemsCount(57) >= 100000)
			{
				htmltext = "30658-02.htm";
				st.takeItems(57, 100000);
				st.giveItems(2812, 1);
				st.setCond(7);
			}
			else
			{
				htmltext = "30658-05.htm";
			}
		}
		else if(event.equalsIgnoreCase("30658_2"))
		{
			st.setCond(6);
			htmltext = "30658-03.htm";
		}
		else if(event.equalsIgnoreCase("30660-03.htm"))
		{
			st.takeItems(2812, 1);
			st.giveItems(2814, 1);
			st.setCond(8);
		}
		else if(event.equalsIgnoreCase("30674_1"))
		{
			htmltext = "30674-02.htm";
			st.setCond(11);
			st.takeItems(2815, 1);
			st.addSpawn(27122);
			st.addSpawn(27122);
			st.addSpawn(27123);
			st.playSound("Itemsound.quest_before_battle");
		}
		else if(event.equalsIgnoreCase("30665_1"))
		{
			htmltext = "30665-02.htm";
			st.takeItems(2816, 1);
			st.takeItems(2817, 1);
			st.takeItems(2818, 1);
			st.takeItems(2819, 1);
			st.giveItems(2811, 1);
			st.setCond(22);
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		if(st.getQuestItemsCount(2820) > 0)
		{
			st.exitCurrentQuest(true);
			return "completed";
		}
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == 30473)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getClassId() == ClassId.knight || st.getPlayer().getClassId() == ClassId.cleric || st.getPlayer().getClassId() == ClassId.oracle || st.getPlayer().getClassId() == ClassId.elvenKnight)
				{
					htmltext = st.getPlayer().getLevel() >= 39 ? "30473-03.htm" : "30473-01.htm";
				}
				else
				{
					htmltext = "30473-02.htm";
					st.exitCurrentQuest(true);
				}
			}
			else if(cond == 23)
			{
				if(st.getQuestItemsCount(2813) == 0)
				{
					st.giveItems(2820, 1);
					htmltext = "30690-08.htm";
					if(!st.getPlayer().getVarB("prof2.3"))
					{
						st.addExpAndSp(738283, 50662);
						st.giveItems(57, 133490);
						st.getPlayer().setVar("prof2.3", "1", -1);
					}
					st.playSound("ItemSound.quest_finish");
					st.exitCurrentQuest(false);
				}
				else
				{
					htmltext = "30473-07.htm";
				}
			}
			else
			{
				htmltext = "30473-05.htm";
			}
		}
		else if(npcId == 30428)
		{
			if(cond == 1)
			{
				htmltext = "30428-01.htm";
			}
			else if(cond == 3)
			{
				htmltext = "30428-03.htm";
				st.takeItems(2810, 1);
				st.setCond(4);
			}
			else if(cond != 2)
			{
				htmltext = "30428-04.htm";
			}
		}
		else if(npcId == 30659)
		{
			int n = Rnd.get(5);
			if(n == 0)
			{
				htmltext = "30659-01.htm";
			}
			else if(n == 1)
			{
				htmltext = "30659-02.htm";
			}
			else if(n == 2)
			{
				htmltext = "30659-03.htm";
			}
			else if(n == 3)
			{
				htmltext = "30659-04.htm";
			}
			else if(n == 4)
			{
				htmltext = "30659-05.htm";
			}
		}
		else if(npcId == 30424)
		{
			if(cond == 4)
			{
				htmltext = "30424-01.htm";
				st.setCond(5);
			}
			else
			{
				htmltext = "30424-02.htm";
			}
		}
		else if(npcId == 30658)
		{
			if(cond == 5)
			{
				htmltext = "30658-01.htm";
			}
			else if(cond == 7)
			{
				htmltext = "30658-04.htm";
			}
			else if(cond == 8)
			{
				htmltext = "30658-06.htm";
				st.giveItems(2813, 1);
				st.takeItems(2814, 1);
				st.setCond(9);
			}
			else if(cond == 6)
			{
				st.setCond(9);
				htmltext = "30658-07.htm";
			}
			else if(cond == 9)
			{
				htmltext = "30658-07.htm";
			}
		}
		else if(npcId == 30660)
		{
			if(cond == 7)
			{
				htmltext = "30660-01.htm";
			}
			else if(cond == 8)
			{
				htmltext = "30660-04.htm";
			}
		}
		else if(npcId == 30327)
		{
			if(cond == 9)
			{
				htmltext = "30327-01.htm";
				st.giveItems(2815, 1);
				st.setCond(10);
			}
			else if(cond > 9 && cond < 22)
			{
				htmltext = "30327-02.htm";
			}
			else if(cond == 22)
			{
				htmltext = "30327-03.htm";
				st.takeItems(2811, 1);
				st.setCond(23);
			}
		}
		else if(npcId == 30674)
		{
			if(cond == 10 && st.getQuestItemsCount(2815) > 0)
			{
				htmltext = "30674-01.htm";
			}
			else if(cond == 12 && st.getQuestItemsCount(2816) > 0)
			{
				htmltext = "30674-03.htm";
				st.setCond(13);
			}
		}
		else if(npcId == 30662 || npcId == 30663 || npcId == 30664)
		{
			if(cond == 13)
			{
				htmltext = "" + npcId + "-01.htm";
			}
			else if(cond == 15)
			{
				htmltext = "" + npcId + "-02.htm";
			}
			else if(cond == 20)
			{
				st.setCond(21);
				htmltext = "" + npcId + "-03.htm";
			}
			else if(cond == 21)
			{
				htmltext = "" + npcId + "-04.htm";
			}
		}
		else if(npcId == 30661)
		{
			if(cond == 13)
			{
				htmltext = "30661-01.htm";
				st.addSpawn(27124);
				st.addSpawn(27124);
				st.addSpawn(27124);
				st.playSound("Itemsound.quest_before_battle");
				st.setCond(14);
			}
			else if(cond == 15)
			{
				htmltext = "30661-02.htm";
				st.addSpawn(27125);
				st.addSpawn(27125);
				st.addSpawn(27125);
				st.playSound("Itemsound.quest_before_battle");
				st.setCond(16);
			}
			else if(cond == 17)
			{
				htmltext = "30661-03.htm";
				st.addSpawn(27126);
				st.addSpawn(27126);
				st.addSpawn(27127);
				st.playSound("Itemsound.quest_before_battle");
				st.setCond(18);
			}
			else if(cond == 19)
			{
				htmltext = "30661-04.htm";
				st.setCond(20);
			}
		}
		else if(npcId == 30665)
		{
			htmltext = cond == 20 || cond == 21 ? "30665-01.htm" : "30665-03.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		Integer[] d = DROPLIST.get(npc.getNpcId());
		if(st.getCond() == d[0].intValue() && (d[2] == 0 || st.getQuestItemsCount(d[2].intValue()) == 0))
		{
			if(d[2] != 0)
			{
				st.giveItems(d[2].intValue(), 1);
			}
			st.setCond(d[1].intValue());
		}
		return null;
	}
}