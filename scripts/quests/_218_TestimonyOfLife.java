package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.base.Race;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _218_TestimonyOfLife extends Quest implements ScriptFile
{
	private static final int MARK_OF_LIFE = 3140;
	private static final int CARDIENS_LETTER = 3141;
	private static final int CAMOMILE_CHARM = 3142;
	private static final int HIERARCHS_LETTER = 3143;
	private static final int MOONFLOWER_CHARM = 3144;
	private static final int GRAIL_DIAGRAM = 3145;
	private static final int THALIAS_LETTER1 = 3146;
	private static final int THALIAS_LETTER2 = 3147;
	private static final int THALIAS_INSTRUCTIONS = 3148;
	private static final int PUSHKINS_LIST = 3149;
	private static final int PURE_MITHRIL_CUP = 3150;
	private static final int ARKENIAS_CONTRACT = 3151;
	private static final int ARKENIAS_INSTRUCTIONS = 3152;
	private static final int ADONIUS_LIST = 3153;
	private static final int ANDARIEL_SCRIPTURE_COPY = 3154;
	private static final int STARDUST = 3155;
	private static final int ISAELS_INSTRUCTIONS = 3156;
	private static final int ISAELS_LETTER = 3157;
	private static final int GRAIL_OF_PURITY = 3158;
	private static final int TEARS_OF_UNICORN = 3159;
	private static final int WATER_OF_LIFE = 3160;
	private static final int PURE_MITHRIL_ORE = 3161;
	private static final int ANT_SOLDIER_ACID = 3162;
	private static final int WYRMS_TALON1 = 3163;
	private static final int SPIDER_ICHOR = 3164;
	private static final int HARPYS_DOWN = 3165;
	private static final int TALINS_SPEAR_BLADE = 3166;
	private static final int TALINS_SPEAR_SHAFT = 3167;
	private static final int TALINS_RUBY = 3168;
	private static final int TALINS_AQUAMARINE = 3169;
	private static final int TALINS_AMETHYST = 3170;
	private static final int TALINS_PERIDOT = 3171;
	private static final int TALINS_SPEAR = 3026;
	private static final int RewardExp = 104591;
	private static final int RewardSP = 11250;
	
	public _218_TestimonyOfLife()
	{
		super(false);
		addStartNpc(30460);
		addTalkId(30154);
		addTalkId(30300);
		addTalkId(30371);
		addTalkId(30375);
		addTalkId(30419);
		addTalkId(30460);
		addTalkId(30655);
		addKillId(20145);
		addKillId(20176);
		addKillId(20233);
		addKillId(27077);
		addKillId(20550);
		addKillId(20581);
		addKillId(20582);
		addKillId(20082);
		addKillId(20084);
		addKillId(20086);
		addKillId(20087);
		addKillId(20088);
		addQuestItem(3142, 3141, 3160, 3144, 3143, 3155, 3150, 3148, 3157, 3159, 3145, 3149, 3146, 3151, 3154, 3152, 3153, 3147, 3166, 3167, 3168, 3169, 3170, 3171, 3156, 3158);
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
			htmltext = "30460-04.htm";
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
			st.giveItems(3141, 1);
			if(!st.getPlayer().getVarB("dd2"))
			{
				st.giveItems(7562, 102);
				st.getPlayer().setVar("dd2", "1", -1);
			}
		}
		else if(event.equalsIgnoreCase("30154_1"))
		{
			htmltext = "30154-02.htm";
		}
		else if(event.equalsIgnoreCase("30154_2"))
		{
			htmltext = "30154-03.htm";
		}
		else if(event.equalsIgnoreCase("30154_3"))
		{
			htmltext = "30154-04.htm";
		}
		else if(event.equalsIgnoreCase("30154_4"))
		{
			htmltext = "30154-05.htm";
		}
		else if(event.equalsIgnoreCase("30154_5"))
		{
			htmltext = "30154-06.htm";
		}
		else if(event.equalsIgnoreCase("30154_6"))
		{
			htmltext = "30154-07.htm";
			st.takeItems(3141, 1);
			st.giveItems(3144, 1);
			st.giveItems(3143, 1);
		}
		else if(event.equalsIgnoreCase("30371_1"))
		{
			htmltext = "30371-02.htm";
		}
		else if(event.equalsIgnoreCase("30371_2"))
		{
			htmltext = "30371-03.htm";
			st.takeItems(3143, 1);
			st.giveItems(3145, 1);
		}
		else if(event.equalsIgnoreCase("30371_3"))
		{
			if(st.getPlayer().getLevel() < 38)
			{
				htmltext = "30371-10.htm";
				st.takeItems(3155, 1);
				st.giveItems(3148, 1);
			}
			else
			{
				htmltext = "30371-11.htm";
				st.takeItems(3155, 1);
				st.giveItems(3147, 1);
			}
		}
		else if(event.equalsIgnoreCase("30300_1"))
		{
			htmltext = "30300-02.htm";
		}
		else if(event.equalsIgnoreCase("30300_2"))
		{
			htmltext = "30300-03.htm";
		}
		else if(event.equalsIgnoreCase("30300_3"))
		{
			htmltext = "30300-04.htm";
		}
		else if(event.equalsIgnoreCase("30300_4"))
		{
			htmltext = "30300-05.htm";
		}
		else if(event.equalsIgnoreCase("30300_5"))
		{
			htmltext = "30300-06.htm";
			st.takeItems(3145, 1);
			st.giveItems(3149, 1);
		}
		else if(event.equalsIgnoreCase("30300_6"))
		{
			htmltext = "30300-09.htm";
		}
		else if(event.equalsIgnoreCase("30300_7"))
		{
			htmltext = "30300-10.htm";
			st.takeItems(3161, -1);
			st.takeItems(3162, -1);
			st.takeItems(3163, -1);
			st.takeItems(3149, 1);
			st.giveItems(3150, 1);
		}
		else if(event.equalsIgnoreCase("30419_1"))
		{
			htmltext = "30419-02.htm";
		}
		else if(event.equalsIgnoreCase("30419_2"))
		{
			htmltext = "30419-03.htm";
		}
		else if(event.equalsIgnoreCase("30419_3"))
		{
			htmltext = "30419-04.htm";
			st.takeItems(3146, 1);
			st.giveItems(3151, 1);
			st.giveItems(3152, 1);
		}
		else if(event.equalsIgnoreCase("30375_1"))
		{
			htmltext = "30375-02.htm";
			st.takeItems(3152, 1);
			st.giveItems(3153, 1);
		}
		else if(event.equalsIgnoreCase("30655_1"))
		{
			htmltext = "30655-02.htm";
			st.takeItems(3147, 1);
			st.giveItems(3156, 1);
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		if(st.getQuestItemsCount(3140) > 0)
		{
			st.exitCurrentQuest(true);
			return "completed";
		}
		int npcId = npc.getNpcId();
		int id = st.getState();
		int cond = st.getCond();
		if(id == 1)
		{
			st.setState(2);
			st.setCond(0);
			st.set("id", "0");
		}
		String htmltext = "noquest";
		if(npcId == 30460 && cond == 0)
		{
			if(0 >= 15)
			{
				htmltext = "30460-03.htm";
				st.exitCurrentQuest(true);
				return htmltext;
			}
			if(st.getPlayer().getRace() != Race.elf)
			{
				return "30460-01.htm";
			}
			if(st.getPlayer().getLevel() < 37)
			{
				htmltext = "30460-02.htm";
				st.exitCurrentQuest(true);
				return htmltext;
			}
			htmltext = "30460-03.htm";
			st.setCond(1);
			return htmltext;
		}
		if(npcId == 30460 && cond == 1 && st.getQuestItemsCount(3141) == 1)
		{
			return "30460-05.htm";
		}
		if(npcId == 30460 && cond == 1 && st.getQuestItemsCount(3144) == 1)
		{
			return "30460-06.htm";
		}
		if(npcId == 30460 && cond == 1 && st.getQuestItemsCount(3142) == 1)
		{
			htmltext = "30460-07.htm";
			st.takeItems(3142, -1);
			st.giveItems(3140, 1);
			if(!st.getPlayer().getVarB("prof2.2"))
			{
				st.addExpAndSp(104591, 11250);
				st.getPlayer().setVar("prof2.2", "1", -1);
			}
			st.playSound("ItemSound.quest_finish");
			st.unset("cond");
			st.exitCurrentQuest(false);
			return htmltext;
		}
		if(npcId == 30154 && cond == 1 && st.getQuestItemsCount(3141) == 1)
		{
			return "30154-01.htm";
		}
		if(npcId == 30154 && cond == 1 && st.getQuestItemsCount(3144) == 1 && st.getQuestItemsCount(3160) == 0)
		{
			return "30154-08.htm";
		}
		if(npcId == 30154 && cond == 1 && st.getQuestItemsCount(3144) > 0 && st.getQuestItemsCount(3160) > 0)
		{
			htmltext = "30154-09.htm";
			st.takeItems(3160, 1);
			st.takeItems(3144, 1);
			st.giveItems(3142, 1);
			return htmltext;
		}
		if(npcId == 30154 && cond == 1 && st.getQuestItemsCount(3142) == 1)
		{
			return "30154-10.htm";
		}
		if(npcId == 30371 && cond == 1 && st.getQuestItemsCount(3144) > 0 && st.getQuestItemsCount(3143) > 0)
		{
			return "30371-01.htm";
		}
		if(npcId == 30371 && cond == 1 && st.getQuestItemsCount(3144) > 0 && st.getQuestItemsCount(3145) > 0)
		{
			return "30371-04.htm";
		}
		if(npcId == 30371 && cond == 1 && st.getQuestItemsCount(3144) > 0 && st.getQuestItemsCount(3149) > 0)
		{
			return "30371-05.htm";
		}
		if(npcId == 30371 && cond == 1 && st.getQuestItemsCount(3144) > 0 && st.getQuestItemsCount(3150) > 0)
		{
			htmltext = "30371-06.htm";
			st.takeItems(3150, 1);
			st.giveItems(3146, 1);
			return htmltext;
		}
		if(npcId == 30371 && cond == 1 && st.getQuestItemsCount(3144) > 0 && st.getQuestItemsCount(3146) > 0)
		{
			return "30371-07.htm";
		}
		if(npcId == 30371 && cond == 1 && st.getQuestItemsCount(3144) > 0 && st.getQuestItemsCount(3151) > 0)
		{
			return "30371-08.htm";
		}
		if(npcId == 30371 && cond == 1 && st.getQuestItemsCount(3144) > 0 && st.getQuestItemsCount(3155) > 0)
		{
			return "30371-09.htm";
		}
		if(npcId == 30371 && cond == 1 && st.getQuestItemsCount(3144) > 0 && st.getQuestItemsCount(3148) > 0)
		{
			if(st.getPlayer().getLevel() < 38)
			{
				return "30371-12.htm";
			}
			htmltext = "30371-13.htm";
			st.takeItems(3148, 1);
			st.giveItems(3147, 1);
			return htmltext;
		}
		if(npcId == 30371 && cond == 1 && st.getQuestItemsCount(3144) > 0 && st.getQuestItemsCount(3147) > 0)
		{
			return "30371-14.htm";
		}
		if(npcId == 30371 && cond == 1 && st.getQuestItemsCount(3144) > 0 && st.getQuestItemsCount(3156) > 0)
		{
			return "30371-15.htm";
		}
		if(npcId == 30371 && cond == 1 && st.getQuestItemsCount(3144) > 0 && st.getQuestItemsCount(3026) > 0 && st.getQuestItemsCount(3157) > 0)
		{
			htmltext = "30371-16.htm";
			st.takeItems(3157, 1);
			st.giveItems(3158, 1);
			return htmltext;
		}
		if(npcId == 30371 && cond == 1 && st.getQuestItemsCount(3144) > 0 && st.getQuestItemsCount(3026) > 0 && st.getQuestItemsCount(3158) > 0)
		{
			return "30371-17.htm";
		}
		if(npcId == 30371 && cond == 1 && st.getQuestItemsCount(3144) > 0 && st.getQuestItemsCount(3159) > 0)
		{
			htmltext = "30371-18.htm";
			st.takeItems(3159, 1);
			st.giveItems(3160, 1);
			return htmltext;
		}
		if(npcId == 30371 && cond == 1)
		{
			if(st.getQuestItemsCount(3142) > 0)
				return "30371-19.htm";
		}
		if(st.getQuestItemsCount(3160) > 0 && st.getQuestItemsCount(3144) == 1)
		{
			return "30371-19.htm";
		}
		if(npcId == 30300 && cond == 1 && st.getQuestItemsCount(3144) > 0 && st.getQuestItemsCount(3145) > 0)
		{
			return "30300-01.htm";
		}
		if(npcId == 30300 && cond == 1 && st.getQuestItemsCount(3144) > 0 && st.getQuestItemsCount(3149) > 0)
		{
			if(st.getQuestItemsCount(3161) < 10)
				return "30300-07.htm";
			if(st.getQuestItemsCount(3162) < 20)
				return "30300-07.htm";
			if(st.getQuestItemsCount(3163) < 20)
				return "30300-07.htm";
			return "30300-08.htm";
		}
		if(npcId == 30300 && cond == 1 && st.getQuestItemsCount(3144) > 0 && st.getQuestItemsCount(3150) > 0)
		{
			return "30300-11.htm";
		}
		if(npcId == 30300 && cond == 1 && st.getQuestItemsCount(3145) == 0 && st.getQuestItemsCount(3149) == 0 && st.getQuestItemsCount(3150) == 0 && st.getQuestItemsCount(3144) == 1)
		{
			return "30300-12.htm";
		}
		if(npcId == 30419 && cond == 1 && st.getQuestItemsCount(3144) > 0 && st.getQuestItemsCount(3146) > 0)
		{
			return "30419-01.htm";
		}
		if(npcId == 30419 && cond == 1 && (st.getQuestItemsCount(3152) > 0 || st.getQuestItemsCount(3153) > 0) && st.getQuestItemsCount(3144) == 1)
		{
			return "30419-05.htm";
		}
		if(npcId == 30419 && cond == 1 && st.getQuestItemsCount(3144) > 0 && st.getQuestItemsCount(3154) > 0)
		{
			htmltext = "30419-06.htm";
			st.takeItems(3151, 1);
			st.takeItems(3154, 1);
			st.giveItems(3155, 1);
			return htmltext;
		}
		if(npcId == 30419 && cond == 1 && st.getQuestItemsCount(3144) > 0 && st.getQuestItemsCount(3155) > 0)
		{
			return "30419-07.htm";
		}
		if(npcId == 30419 && cond == 1 && st.getQuestItemsCount(3146) == 0 && st.getQuestItemsCount(3151) == 0 && st.getQuestItemsCount(3154) == 0 && st.getQuestItemsCount(3155) == 0 && st.getQuestItemsCount(3144) == 1)
		{
			return "30419-08.htm";
		}
		if(npcId == 30375 && cond == 1 && st.getQuestItemsCount(3144) > 0 && st.getQuestItemsCount(3152) > 0)
		{
			return "30375-01.htm";
		}
		if(npcId == 30375 && cond == 1 && st.getQuestItemsCount(3144) > 0 && st.getQuestItemsCount(3153) > 0)
		{
			if(st.getQuestItemsCount(3164) < 20)
				return "30375-03.htm";
			if(st.getQuestItemsCount(3165) < 20)
				return "30375-03.htm";
			htmltext = "30375-04.htm";
			st.takeItems(3164, st.getQuestItemsCount(3164));
			st.takeItems(3165, st.getQuestItemsCount(3165));
			st.takeItems(3153, 1);
			st.giveItems(3154, 1);
			return htmltext;
		}
		if(npcId == 30375 && cond == 1 && st.getQuestItemsCount(3144) > 0 && st.getQuestItemsCount(3154) > 0)
		{
			return "30375-05.htm";
		}
		if(npcId == 30375 && cond == 1 && st.getQuestItemsCount(3152) == 0 && st.getQuestItemsCount(3153) == 0 && st.getQuestItemsCount(3154) == 0 && st.getQuestItemsCount(3144) == 1)
		{
			return "30375-06.htm";
		}
		if(npcId == 30655 && cond == 1 && st.getQuestItemsCount(3144) > 0 && st.getQuestItemsCount(3147) > 0)
		{
			return "30655-01.htm";
		}
		if(npcId == 30655 && cond == 1 && st.getQuestItemsCount(3144) > 0 && st.getQuestItemsCount(3156) > 0)
		{
			if(st.getQuestItemsCount(3166) <= 0)
				return "30655-03.htm";
			if(st.getQuestItemsCount(3167) <= 0)
				return "30655-03.htm";
			if(st.getQuestItemsCount(3168) <= 0)
				return "30655-03.htm";
			if(st.getQuestItemsCount(3169) <= 0)
				return "30655-03.htm";
			if(st.getQuestItemsCount(3170) <= 0)
				return "30655-03.htm";
			if(st.getQuestItemsCount(3171) <= 0)
				return "30655-03.htm";
			htmltext = "30655-04.htm";
			st.takeItems(3166, 1);
			st.takeItems(3167, 1);
			st.takeItems(3168, 1);
			st.takeItems(3169, 1);
			st.takeItems(3170, 1);
			st.takeItems(3171, 1);
			st.takeItems(3156, 1);
			st.giveItems(3157, 1);
			st.giveItems(3026, 1);
			return htmltext;
		}
		if(npcId == 30655 && cond == 1 && st.getQuestItemsCount(3026) > 0 && st.getQuestItemsCount(3157) > 0)
		{
			return "30655-05.htm";
		}
		if(npcId == 30655 && cond == 1)
		{
			if(st.getQuestItemsCount(3158) > 0)
				return "30655-06.htm";
		}
		if(st.getQuestItemsCount(3160) > 0)
			return "30655-06.htm";
		if(st.getQuestItemsCount(3142) <= 0)
			return htmltext;
		if(st.getQuestItemsCount(3144) != 1)
			return htmltext;
		return "30655-06.htm";
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == 20550)
		{
			st.set("id", "0");
			if(cond > 0 && st.getQuestItemsCount(3144) == 1 && st.getQuestItemsCount(3149) == 1 && st.getQuestItemsCount(3161) < 10 && Rnd.chance(50))
			{
				st.giveItems(3161, 1);
				if(st.getQuestItemsCount(3161) < 10)
				{
					st.playSound("ItemSound.quest_itemget");
				}
				else
				{
					st.playSound("ItemSound.quest_middle");
				}
			}
		}
		else if(npcId == 20176)
		{
			st.set("id", "0");
			if(cond > 0 && st.getQuestItemsCount(3144) == 1 && st.getQuestItemsCount(3149) == 1 && st.getQuestItemsCount(3163) < 20 && Rnd.chance(50))
			{
				st.giveItems(3163, 1);
				if(st.getQuestItemsCount(3163) < 20)
				{
					st.playSound("ItemSound.quest_itemget");
				}
				else
				{
					st.playSound("ItemSound.quest_middle");
				}
			}
		}
		else if(npcId == 20082 || npcId == 20084 || npcId == 20086)
		{
			st.set("id", "0");
			if(cond > 0 && st.getQuestItemsCount(3144) == 1 && st.getQuestItemsCount(3149) == 1 && st.getQuestItemsCount(3162) < 20 && Rnd.chance(80))
			{
				st.giveItems(3162, 1);
				if(st.getQuestItemsCount(3162) < 20)
				{
					st.playSound("ItemSound.quest_itemget");
				}
				else
				{
					st.playSound("ItemSound.quest_middle");
				}
			}
		}
		else if(npcId == 20087 || npcId == 20088)
		{
			st.set("id", "0");
			if(cond > 0 && st.getQuestItemsCount(3144) == 1 && st.getQuestItemsCount(3149) == 1 && st.getQuestItemsCount(3162) < 20 && Rnd.chance(50))
			{
				st.giveItems(3162, 1);
				if(st.getQuestItemsCount(3162) < 20)
				{
					st.playSound("ItemSound.quest_itemget");
				}
				else
				{
					st.playSound("ItemSound.quest_middle");
				}
			}
		}
		else if(npcId == 20233)
		{
			st.set("id", "0");
			if(cond > 0 && st.getQuestItemsCount(3144) == 1 && st.getQuestItemsCount(3153) == 1 && st.getQuestItemsCount(3164) < 20 && Rnd.chance(50))
			{
				st.giveItems(3164, 1);
				if(st.getQuestItemsCount(3164) < 20)
				{
					st.playSound("ItemSound.quest_itemget");
				}
				else
				{
					st.playSound("ItemSound.quest_middle");
				}
			}
		}
		else if(npcId == 20145)
		{
			st.set("id", "0");
			if(cond > 0 && st.getQuestItemsCount(3144) == 1 && st.getQuestItemsCount(3153) == 1 && st.getQuestItemsCount(3165) < 20 && Rnd.chance(50))
			{
				st.giveItems(3165, 1);
				if(st.getQuestItemsCount(3165) < 20)
				{
					st.playSound("ItemSound.quest_itemget");
				}
				else
				{
					st.playSound("ItemSound.quest_middle");
				}
			}
		}
		else if(npcId == 27077)
		{
			if(cond > 0 && st.getQuestItemsCount(3144) == 1 && st.getItemEquipped(7) == 3026 && st.getQuestItemsCount(3158) == 1 && st.getQuestItemsCount(3159) == 0 && st.getQuestItemsCount(3026) > 0)
			{
				st.takeItems(3158, 1);
				st.takeItems(3026, 1);
				st.giveItems(3159, 1);
			}
		}
		else if(npcId == 20581 || npcId == 20582)
		{
			st.set("id", "0");
			if(cond > 0 && st.getQuestItemsCount(3156) == 1 && Rnd.chance(50))
			{
				if(st.getQuestItemsCount(3166) == 0)
				{
					st.giveItems(3166, 1);
					st.playSound("ItemSound.quest_itemget");
				}
				else if(st.getQuestItemsCount(3167) == 0)
				{
					st.giveItems(3167, 1);
					st.playSound("ItemSound.quest_itemget");
				}
				else if(st.getQuestItemsCount(3168) == 0)
				{
					st.giveItems(3168, 1);
					st.playSound("ItemSound.quest_itemget");
				}
				else if(st.getQuestItemsCount(3169) == 0)
				{
					st.giveItems(3169, 1);
					st.playSound("ItemSound.quest_itemget");
				}
				else if(st.getQuestItemsCount(3170) == 0)
				{
					st.giveItems(3170, 1);
					st.playSound("ItemSound.quest_itemget");
				}
				else if(st.getQuestItemsCount(3171) == 0)
				{
					st.giveItems(3171, 1);
					st.playSound("ItemSound.quest_middle");
				}
			}
		}
		return null;
	}
}