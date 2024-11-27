package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _337_AudienceWithLandDragon extends Quest implements ScriptFile
{
	public final int MOKE = 30498;
	public final int HELTON = 30678;
	public final int CHAKIRIS = 30705;
	public final int KAIENA = 30720;
	public final int GABRIELLE = 30753;
	public final int GILMORE = 30754;
	public final int THEODRIC = 30755;
	public final int KENDRA = 30851;
	public final int ORVEN = 30857;
	public final int MARSH_STALKER = 20679;
	public final int MARSH_DRAKE = 20680;
	public final int BLOOD_QUEEN = 18001;
	public final int HARIT_LIZARDMAN_SHAMAN = 20644;
	public final int HARIT_LIZARDMAN_MATRIARCH = 20645;
	public final int HAMRUT = 20649;
	public final int KRANROT = 20650;
	public final int CAVE_MAIDEN = 20134;
	public final int CAVE_KEEPER = 20246;
	public final int ABYSSAL_JEWEL_1 = 27165;
	public final int ABYSSAL_JEWEL_2 = 27166;
	public final int ABYSSAL_JEWEL_3 = 27167;
	public final int JEWEL_GUARDIAN_MARA = 27168;
	public final int JEWEL_GUARDIAN_MUSFEL = 27169;
	public final int JEWEL_GUARDIAN_PYTON = 27170;
	public final int SACRIFICE_OF_THE_SACRIFICED = 27171;
	public final int HARIT_LIZARDMAN_ZEALOT = 27172;
	public final int FEATHER_OF_GABRIELLE_ID = 3852;
	public final int STALKER_HORN_ID = 3853;
	public final int DRAKE_TALON_ID = 3854;
	public final int REMAINS_OF_SACRIFICED_ID = 3857;
	public final int TOTEM_OF_LAND_DRAGON_ID = 3858;
	public final int HAMRUT_LEG_ID = 3856;
	public final int KRANROT_SKIN_ID = 3855;
	public final int MARA_FANG_ID = 3862;
	public final int MUSFEL_FANG_ID = 3863;
	public final int FIRST_ABYSS_FRAGMENT_ID = 3859;
	public final int SECOND_ABYSS_FRAGMENT_ID = 3860;
	public final int THIRD_ABYSS_FRAGMENT_ID = 3861;
	public final int HERALD_OF_SLAYER_ID = 3890;
	public final int PORTAL_STONE_ID = 3865;
	public final int MARK_OF_WATCHMAN_ID = 3864;
	public final int[][] DROPLIST = {{2, 20679, 3853, 1, 50, 1}, {2, 20680, 3854, 1, 50, 1}, {4, 27171, 3857, 1, 50, 1}, {6, 27172, 3858, 1, 50, 1}, {8, 20649, 3856, 1, 50, 1}, {8, 20650, 3855, 1, 50, 1}, {11, 27168, 3862, 1, 50, 1}, {11, 27165, 3859, 1, 100, 1}, {13, 27169, 3863, 1, 50, 1}, {13, 27166, 3860, 1, 100, 1}, {16, 27167, 3861, 1, 100, 1}};
	public final int[][] SPAWNLIST = {{4, 18001, 27171, 6}, {6, 20644, 27172, 1}, {6, 20645, 27172, 1}, {11, 27165, 27168, 4}, {13, 27166, 27169, 4}, {16, 20246, 27167, 1}, {16, 20134, 27167, 1}, {16, 27167, 27170, 6}};
	
	public _337_AudienceWithLandDragon()
	{
		super(false);
		addStartNpc(30753);
		addTalkId(30498);
		addTalkId(30678);
		addTalkId(30705);
		addTalkId(30720);
		addTalkId(30754);
		addTalkId(30755);
		addTalkId(30851);
		addTalkId(30857);
		addKillId(18001);
		addKillId(20679);
		addKillId(20680);
		addKillId(27171);
		addKillId(20644);
		addKillId(20645);
		addKillId(27172);
		addKillId(20649);
		addKillId(20650);
		addKillId(27165);
		addKillId(27166);
		addKillId(20246);
		addKillId(20134);
		addKillId(27167);
		addKillId(27168);
		addKillId(27169);
		addKillId(27170);
		addAttackId(27165);
		addAttackId(27166);
		addAttackId(27167);
		addQuestItem(3852, 3890, 3853, 3854, 3857, 3858, 3856, 3855, 3862, 3859, 3863, 3860, 3861, 3864);
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
			st.set("step", "1");
			st.setCond(1);
			st.set("guard", "0");
			st.setState(2);
			st.giveItems(3852, 1);
			htmltext = "30753-02.htm";
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("2"))
		{
			st.set("step", "2");
			htmltext = "30720-02.htm";
		}
		else if(event.equalsIgnoreCase("4"))
		{
			st.set("step", "4");
			htmltext = "30857-02.htm";
		}
		else if(event.equalsIgnoreCase("6"))
		{
			st.set("step", "6");
			htmltext = "30851-02.htm";
		}
		else if(event.equalsIgnoreCase("8"))
		{
			st.set("step", "8");
			htmltext = "30705-02.htm";
		}
		else if(event.equalsIgnoreCase("10"))
		{
			st.takeItems(3864, -1);
			st.set("step", "10");
			st.setCond(2);
			htmltext = "30753-05.htm";
		}
		else if(event.equalsIgnoreCase("11"))
		{
			st.set("step", "11");
			htmltext = "30498-02.htm";
		}
		else if(event.equalsIgnoreCase("13"))
		{
			st.set("step", "13");
			htmltext = "30678-02.htm";
		}
		else if(event.equalsIgnoreCase("15"))
		{
			st.set("step", "15");
			st.setCond(3);
			htmltext = "30753-06.htm";
			st.takeItems(3864, -1);
			st.takeItems(3852, -1);
			st.giveItems(3890, 1);
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("16"))
		{
			st.set("step", "16");
			st.setCond(4);
			htmltext = "30754-02.htm";
			st.takeItems(3890, -1);
			st.playSound("ItemSound.quest_middle");
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int step = st.getInt("step");
		int cond = st.getCond();
		if(npcId == 30753)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() < 50)
				{
					htmltext = "30753-00.htm";
					st.exitCurrentQuest(true);
				}
				else
				{
					htmltext = "30753-01.htm";
				}
			}
			else if(step < 9)
			{
				htmltext = "30753-02.htm";
			}
			else if(step == 9)
			{
				htmltext = "30753-03.htm";
			}
			else if(step > 9 && step < 14)
			{
				htmltext = "30753-05.htm";
			}
			else if(step == 14)
			{
				htmltext = "30753-04.htm";
			}
			else if(step > 14)
			{
				htmltext = "30753-06.htm";
			}
		}
		else if(npcId == 30720 && cond == 1 && step < 4)
		{
			if(st.getQuestItemsCount(3853) < 1 && st.getQuestItemsCount(3854) < 1 && step == 1)
			{
				htmltext = "30720-01.htm";
			}
			else if(st.getQuestItemsCount(3853) > 0 && st.getQuestItemsCount(3854) > 0)
			{
				htmltext = "30720-03.htm";
				st.takeItems(3853, -1);
				st.takeItems(3854, -1);
				st.giveItems(3864, 1);
				st.set("step", "3");
				st.playSound("ItemSound.quest_middle");
			}
			else if(step == 2)
			{
				htmltext = "30720-02.htm";
			}
			else if(step == 3)
			{
				htmltext = "30720-03.htm";
			}
		}
		else if(npcId == 30857 && cond == 1 && step > 2 && step < 6)
		{
			if(st.getQuestItemsCount(3857) < 1 && step == 3)
			{
				htmltext = "30857-01.htm";
			}
			else if(st.getQuestItemsCount(3857) > 0)
			{
				htmltext = "30857-03.htm";
				st.takeItems(3857, -1);
				st.giveItems(3864, 1);
				st.set("step", "5");
				st.playSound("ItemSound.quest_middle");
			}
			else if(step == 4)
			{
				htmltext = "30857-02.htm";
			}
			else if(step == 5)
			{
				htmltext = "30857-03.htm";
			}
		}
		else if(npcId == 30851 && cond == 1 && step > 4 && step < 8)
		{
			if(st.getQuestItemsCount(3858) < 1 && step == 5)
			{
				htmltext = "30851-01.htm";
			}
			else if(st.getQuestItemsCount(3858) > 0)
			{
				htmltext = "30851-03.htm";
				st.takeItems(3858, -1);
				st.giveItems(3864, 1);
				st.set("step", "7");
				st.playSound("ItemSound.quest_middle");
			}
			else if(step == 6)
			{
				htmltext = "30851-02.htm";
			}
			else if(step == 7)
			{
				htmltext = "30851-03.htm";
			}
		}
		else if(npcId == 30705 && cond == 1 && step > 6 && step < 10)
		{
			if(st.getQuestItemsCount(3856) < 1 && st.getQuestItemsCount(3855) < 1 && step == 7)
			{
				htmltext = "30705-01.htm";
			}
			else if(st.getQuestItemsCount(3856) > 0 && st.getQuestItemsCount(3855) > 0)
			{
				htmltext = "30705-03.htm";
				st.takeItems(3856, -1);
				st.takeItems(3855, -1);
				st.giveItems(3864, 1);
				st.set("step", "9");
				st.playSound("ItemSound.quest_middle");
			}
			else if(step == 8)
			{
				htmltext = "30705-02.htm";
			}
			else if(step == 9)
			{
				htmltext = "30705-03.htm";
			}
		}
		else if(npcId == 30498 && cond == 2 && step < 13)
		{
			if(st.getQuestItemsCount(3862) < 1 && st.getQuestItemsCount(3859) < 1 && step == 10)
			{
				htmltext = "30498-01.htm";
			}
			else if(st.getQuestItemsCount(3862) > 0 && st.getQuestItemsCount(3859) > 0)
			{
				htmltext = "30498-03.htm";
				st.takeItems(3862, -1);
				st.takeItems(3859, -1);
				st.giveItems(3864, 1);
				st.set("step", "12");
				st.playSound("ItemSound.quest_middle");
			}
			else if(step == 11)
			{
				htmltext = "30498-02.htm";
			}
			else if(step == 12)
			{
				htmltext = "30498-03.htm";
			}
		}
		else if(npcId == 30678 && cond == 2 && step > 11 && step < 15)
		{
			if(st.getQuestItemsCount(3863) < 1 && st.getQuestItemsCount(3860) < 1 && step == 12)
			{
				htmltext = "30678-01.htm";
			}
			else if(st.getQuestItemsCount(3863) > 0 && st.getQuestItemsCount(3860) > 0)
			{
				htmltext = "30678-03.htm";
				st.takeItems(3863, -1);
				st.takeItems(3860, -1);
				st.giveItems(3864, 1);
				st.set("step", "14");
				st.playSound("ItemSound.quest_middle");
			}
			else if(step == 13)
			{
				htmltext = "30678-02.htm";
			}
			else if(step == 14)
			{
				htmltext = "30678-03.htm";
			}
		}
		else if(npcId == 30754 && step < 17)
		{
			if(st.getQuestItemsCount(3890) > 0 && cond == 3)
			{
				htmltext = "30754-01.htm";
			}
			else if(cond == 4)
			{
				htmltext = "30754-02.htm";
			}
		}
		else if(npcId == 30755 && cond == 4 && step == 16)
		{
			if(st.getQuestItemsCount(3861) < 1)
			{
				htmltext = "30755-02.htm";
			}
			else
			{
				htmltext = "30755-01.htm";
				st.takeItems(3861, -1);
				st.unset("step");
				st.unset("cond");
				st.unset("guard");
				st.exitCurrentQuest(true);
				st.giveItems(3865, 1);
				st.playSound("ItemSound.quest_finish");
			}
		}
		return htmltext;
	}
	
	@Override
	public String onAttack(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int step = st.getInt("step");
		for(int[] element : SPAWNLIST)
		{
			if(npcId != element[1] || step != element[0] || npc.getCurrentHpPercents() >= 50.0 || st.getInt("guard") != 0)
				continue;
			for(int j = 0;j < element[3];++j)
			{
				st.addSpawn(element[2]);
			}
			st.playSound("Itemsound.quest_before_battle");
			st.set("guard", "1");
		}
		return null;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int step = st.getInt("step");
		for(int[] element : DROPLIST)
		{
			if(npcId != element[1] || step != element[0] || st.getQuestItemsCount(element[2]) >= (long) element[3] || !Rnd.chance(element[4]))
				continue;
			st.giveItems(element[2], (long) element[5]);
			st.playSound("ItemSound.quest_itemget");
		}
		for(int[] element : SPAWNLIST)
		{
			if(step == element[0] && npcId == element[1] && Rnd.chance(50) && st.getInt("guard") == 0)
			{
				for(int j = 0;j < element[3];++j)
				{
					st.addSpawn(element[2]);
				}
				st.playSound("Itemsound.quest_before_battle");
			}
			if(step != element[0] || npcId != element[1] || st.getInt("guard") != 1)
				continue;
			st.set("guard", "0");
		}
		return null;
	}
}