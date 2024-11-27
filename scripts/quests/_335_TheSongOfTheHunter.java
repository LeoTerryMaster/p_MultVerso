package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.Functions;
import l2.gameserver.scripts.ScriptFile;
import l2.gameserver.utils.Util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class _335_TheSongOfTheHunter extends Quest implements ScriptFile
{
	private static final int Grey = 30744;
	private static final int Tor = 30745;
	private static final int Cybellin = 30746;
	private static final int Breka_Orc_Warrior = 20271;
	private static final int Windsus = 20553;
	private static final int Tarlk_Bugbear_Warrior = 20571;
	private static final int Breka_Overlord_Haka = 27140;
	private static final int Breka_Overlord_Jaka = 27141;
	private static final int Breka_Overlord_Marka = 27142;
	private static final int Windsus_Aleph = 27143;
	private static final int Tarlk_Raider_Athu = 27144;
	private static final int Tarlk_Raider_Lanka = 27145;
	private static final int Tarlk_Raider_Triska = 27146;
	private static final int Tarlk_Raider_Motura = 27147;
	private static final int Tarlk_Raider_Kalath = 27148;
	private static final int Cybellins_Dagger = 3471;
	private static final int _1st_Circle_Hunter_License = 3692;
	private static final int _2nd_Circle_Hunter_License = 3693;
	private static final int Laurel_Leaf_Pin = 3694;
	private static final int _1st_Test_Instructions = 3695;
	private static final int _2nd_Test_Instructions = 3696;
	private static final int Cybellins_Request = 3697;
	private static final int Guardian_Basilisk_Scale = 3709;
	private static final int Karut_Weed = 3710;
	private static final int Hakas_Head = 3711;
	private static final int Jakas_Head = 3712;
	private static final int Markas_Head = 3713;
	private static final int Windsus_Aleph_Skin = 3714;
	private static final int Indigo_Runestone = 3715;
	private static final int Sporesea_Seed = 3716;
	private static final int Timak_Orc_Totem = 3717;
	private static final int Trisalim_Silk = 3718;
	private static final int Ambrosius_Fruit = 3719;
	private static final int Balefire_Crystal = 3720;
	private static final int Imperial_Arrowhead = 3721;
	private static final int Athus_Head = 3722;
	private static final int Lankas_Head = 3723;
	private static final int Triskas_Head = 3724;
	private static final int Moturas_Head = 3725;
	private static final int Kalaths_Head = 3726;
	private static final int[] q_blood_crystal = {3708, 3698, 3699, 3700, 3701, 3702, 3703, 3704, 3705, 3706, 3707};
	private static final int[] q_blood_crystal_lizardmen = {20578, 20579, 20580, 20581, 20582, 20641, 20642, 20643, 20644, 20645};
	private static final int[][][] Items_1st_Circle = {{{3709}, {40}, {20550, 75}}, {{3710}, {20}, {20581, 50}}, {{3711, 3712, 3713}, {3}}, {{3714}, {1}, {27143, 100}}, {{3715}, {20}, {20563, 50}, {20565, 50}}, {{3716}, {30}, {20555, 70}}};
	private static final int[][][] Items_2nd_Circle = {{{3717}, {20}, {20586, 50}}, {{3718}, {20}, {20560, 50}, {20561, 50}}, {{3719}, {30}, {20591, 75}, {20597, 75}}, {{3720}, {20}, {20675, 50}}, {{3721}, {20}, {20660, 50}}, {{3722, 3723, 3724, 3725, 3726}, {5}}};
	private static final Request[] Requests1 = {new Request(3727, 3769, 40, 2090, "C: 40 Totems of Kadesh").addDrop(20578, 80).addDrop(20579, 83), new Request(3728, 3770, 50, 6340, "C: 50 Jade Necklaces of Timak").addDrop(20586, 89).addDrop(20588, 100), new Request(3729, 3771, 50, 9480, "C: 50 Enchanted Golem Shards").addDrop(20565, 100), new Request(3730, 3772, 30, 9110, "C: 30 Pieces Monster Eye Meat").addDrop(20556, 50), new Request(3731, 3773, 40, 8690, "C: 40 Eggs of Dire Wyrm").addDrop(20557, 80), new Request(3732, 3774, 100, 9480, "C: 100 Claws of Guardian Basilisk").addDrop(20550, 150), new Request(3733, 3775, 50, 11280, "C: 50 Revenant Chains").addDrop(20552, 100), new Request(3734, 3776, 30, 9640, "C: 30 Windsus Tusks").addDrop(20553, 50), new Request(3735, 3777, 100, 9180, "C: 100 Skulls of Grandis").addDrop(20554, 200), new Request(3736, 3778, 50, 5160, "C: 50 Taik Obsidian Amulets").addDrop(20631, 100).addDrop(20632, 93), new Request(3737, 3779, 30, 3140, "C: 30 Heads of Karul Bugbear").addDrop(20600, 50), new Request(3738, 3780, 40, 3160, "C: 40 Ivory Charms of Tamlin").addDrop(20601, 62).addDrop(20602, 80), new Request(3739, 3781, 1, 6370, "B: Situation Preparation - Leto Chief").addSpawn(20582, 27157, 10).addDrop(27157, 100), new Request(3740, 3782, 50, 19080, "B: 50 Enchanted Gargoyle Horns").addDrop(20567, 50), new Request(3741, 3783, 50, 17730, "B: 50 Coiled Serpent Totems").addDrop(20269, 93).addDrop(20271, 100), new Request(3742, 3784, 1, 5790, "B: Situation Preparation - Sorcerer Catch of Leto").addSpawn(20581, 27156, 10).addDrop(27156, 100), new Request(3743, 3785, 1, 8560, "B: Situation Preparation - Timak Raider Kaikee").addSpawn(20586, 27158, 10).addDrop(27158, 100), new Request(3744, 3786, 30, 8320, "B: 30 Kronbe Venom Sacs").addDrop(20603, 50), new Request(3745, 3787, 30, 30310, "A: 30 Eva's Charm").addDrop(20562, 50), new Request(3746, 3788, 1, 27540, "A: Titan's Tablet").addSpawn(20554, 27160, 10).addDrop(27160, 100), new Request(3747, 3789, 1, 20560, "A: Book of Shunaiman").addSpawn(20600, 27164, 10).addDrop(27164, 100)};
	private static final Request[] Requests2 = {new Request(3748, 3790, 40, 6850, "C: 40 Rotting Tree Spores").addDrop(20558, 67), new Request(3749, 3791, 40, 7250, "C: 40 Trisalim Venom Sacs").addDrop(20560, 66).addDrop(20561, 75), new Request(3750, 3792, 50, 7160, "C: 50 Totems of Taik Orc").addDrop(20633, 53).addDrop(20634, 99), new Request(3751, 3793, 40, 6580, "C: 40 Harit Barbed Necklaces").addDrop(20641, 88).addDrop(20642, 88).addDrop(20643, 91), new Request(3752, 3794, 20, 10100, "C: 20 Coins of Ancient Empire").addDrop(20661, 50).addSpawn(20661, 27149, 5).addDrop(20662, 52).addSpawn(20662, 27149, 5).addDrop(27149, 300), new Request(3753, 3795, 30, 13000, "C: 30 Skins of Farkran").addDrop(20667, 90), new Request(3754, 3796, 40, 7660, "C: 40 Tempest Shards").addDrop(20589, 49).addSpawn(20589, 27149, 5).addDrop(27149, 500), new Request(3755, 3797, 40, 7660, "C: 40 Tsunami Shards").addDrop(20590, 51).addSpawn(20590, 27149, 5).addDrop(27149, 500), new Request(3756, 3798, 40, 11260, "C: 40 Manes of Pan Ruem").addDrop(20592, 80).addDrop(20598, 100), new Request(3757, 3799, 40, 7660, "C: 40 Hamadryad Shard").addDrop(20594, 64).addSpawn(20594, 27149, 5).addDrop(27149, 500), new Request(3758, 3800, 30, 8810, "C: 30 Manes of Vanor Silenos").addDrop(20682, 70).addDrop(20683, 85).addDrop(20684, 90), new Request(3759, 3801, 30, 7350, "C: 30 Totems of Tarlk Bugbears").addDrop(20571, 63), new Request(3760, 3802, 1, 8760, "B: Situation Preparation - Overlord Okun of Timak").addSpawn(20588, 27159, 10).addDrop(27159, 100), new Request(3761, 3803, 1, 9380, "B: Situation Preparation - Overlord Kakran of Taik").addSpawn(20634, 27161, 10).addDrop(27161, 100), new Request(3762, 3804, 40, 17820, "B: 40 Narcissus Soulstones").addDrop(20639, 86).addSpawn(20639, 27149, 5).addDrop(27149, 500), new Request(3763, 3805, 20, 17540, "B: 20 Eyes of Deprived").addDrop(20664, 77), new Request(3764, 3806, 20, 14160, "B: 20 Unicorn Horns").addDrop(20593, 68).addDrop(20599, 86), new Request(3765, 3807, 1, 15960, "B: Golden Mane of Silenos").addSpawn(20686, 27163, 10).addDrop(27163, 100), new Request(3766, 3808, 20, 39100, "A: 20 Skulls of Executed Person").addDrop(20659, 73), new Request(3767, 3809, 1, 39550, "A: Bust of Travis").addSpawn(20662, 27162, 10).addDrop(27162, 100), new Request(3768, 3810, 10, 41200, "A: 10 Swords of Cadmus").addDrop(20676, 64)};
	
	public _335_TheSongOfTheHunter()
	{
		super(false);
		addStartNpc(30744);
		addTalkId(30746);
		addTalkId(30745);
		addKillId(27140);
		addKillId(27141);
		addKillId(27142);
		addKillId(27144);
		addKillId(27145);
		addKillId(27146);
		addKillId(27147);
		addKillId(27148);
		addKillId(q_blood_crystal_lizardmen);
		for(int[][] ItemsCond : Items_1st_Circle)
		{
			addQuestItem(ItemsCond[0]);
			for(int i = 2;i < ItemsCond.length;++i)
			{
				addKillId(ItemsCond[i][0]);
			}
		}
		for(int[][] ItemsCond : Items_2nd_Circle)
		{
			addQuestItem(ItemsCond[0]);
			for(int i2 = 2;i2 < ItemsCond.length;++i2)
			{
				addKillId(ItemsCond[i2][0]);
			}
		}
		int id;
		for(Request r : Requests1)
		{
			addQuestItem(r.request_id);
			addQuestItem(r.request_item);
			Iterator<Integer> i2 = r.droplist.keySet().iterator();
			while(i2.hasNext())
			{
				id = i2.next();
				addKillId(id);
			}
			i2 = r.spawnlist.keySet().iterator();
			while(i2.hasNext())
			{
				id = i2.next();
				addKillId(id);
			}
		}
		for(Request r : Requests2)
		{
			addQuestItem(r.request_id);
			addQuestItem(r.request_item);
			Iterator<Integer> i2 = r.droplist.keySet().iterator();
			while(i2.hasNext())
			{
				id = i2.next();
				addKillId(id);
			}
			i2 = r.spawnlist.keySet().iterator();
			while(i2.hasNext())
			{
				id = i2.next();
				addKillId(id);
			}
		}
		addQuestItem(3692);
		addQuestItem(3693);
		addQuestItem(3694);
		addQuestItem(3695);
		addQuestItem(3696);
		addQuestItem(3697);
		addQuestItem(3471);
		addQuestItem(q_blood_crystal);
	}
	
	private static int CalcItemsConds(QuestState st, int[][][] ItemsConds)
	{
		int result = 0;
		for(int[][] ItemsCond : ItemsConds)
		{
			if(st.getQuestItemsCount(ItemsCond[0]) < (long) ItemsCond[1][0])
				continue;
			++result;
		}
		return result;
	}
	
	private static void DelItemsConds(QuestState st, int[][][] ItemsConds)
	{
		for(int[][] ItemsCond : ItemsConds)
		{
			st.takeAllItems(ItemsCond[0]);
		}
	}
	
	private static int Get_Blood_Crystal_Level(QuestState st)
	{
		for(int i = q_blood_crystal.length - 1;i >= 0;--i)
		{
			if(st.getQuestItemsCount(q_blood_crystal[i]) <= 0)
				continue;
			return i;
		}
		return -1;
	}
	
	private static boolean Blood_Crystal2Adena(QuestState st, int Blood_Crystal_Level)
	{
		if(Blood_Crystal_Level < 2)
		{
			return false;
		}
		st.takeAllItems(q_blood_crystal);
		st.giveItems(57, (long) (3400 * (int) Math.pow(2.0, Blood_Crystal_Level - 2)));
		return true;
	}
	
	private static void GenList(QuestState st)
	{
		int grade_c = 12;
		int grade_b = 6;
		int grade_a = 3;
		if(st.get("list") == null || st.get("list").isEmpty())
		{
			long Laurel_Leaf_Pin_count = st.getQuestItemsCount(3694);
			int[] list = new int[5];
			if(Laurel_Leaf_Pin_count < 4)
			{
				if(Laurel_Leaf_Pin_count == 0 || Rnd.chance(80))
				{
					for(int i = 0;i < 5;++i)
					{
						list[i] = Rnd.get(12);
					}
				}
				else
				{
					list[0] = 12 + Rnd.get(6);
					list[1] = Rnd.get(12);
					list[2] = Rnd.get(6);
					list[3] = 6 + Rnd.get(6);
					list[4] = Rnd.get(12);
				}
			}
			else if(Rnd.chance(20))
			{
				list[0] = 12 + Rnd.get(6);
				list[1] = Rnd.chance(5) ? 18 + Rnd.get(3) : Rnd.get(12);
				list[2] = Rnd.get(6);
				list[3] = 6 + Rnd.get(6);
				list[4] = Rnd.get(12);
			}
			else
			{
				list[0] = Rnd.get(12);
				list[1] = Rnd.chance(5) ? 18 + Rnd.get(3) : Rnd.get(12);
				list[2] = Rnd.get(6);
				list[3] = 6 + Rnd.get(6);
				list[4] = Rnd.get(12);
			}
			boolean sort_flag;
			do
			{
				sort_flag = false;
				for(int i = 1;i < list.length;++i)
				{
					if(list[i] >= list[i - 1])
						continue;
					int tmp = list[i];
					list[i] = list[i - 1];
					list[i - 1] = tmp;
					sort_flag = true;
				}
			}
			while(sort_flag);
			int packedlist = 0;
			try
			{
				packedlist = Util.packInt(list, 5);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			st.set("list", String.valueOf(packedlist));
		}
	}
	
	private static String FormatList(QuestState st, Request[] requests)
	{
		String result = "<html><head><body>Guild Member Tor:<br>%reply%<br>%reply%<br>%reply%<br>%reply%<br>%reply%<br></body></html>";
		int[] listpacked = Util.unpackInt(st.getInt("list"), 5);
		for(int i = 0;i <= 5;++i)
		{
			String s = "<a action=\"bypass -h Quest _335_TheSongOfTheHunter 30745-request-" + requests[listpacked[i]].request_id + "\">" + requests[listpacked[i]].text + "</a>";
			result = result.replaceFirst("%reply%", s);
		}
		return result;
	}
	
	private static Request GetCurrentRequest(QuestState st, Request[] requests)
	{
		for(Request r : requests)
		{
			if(st.getQuestItemsCount(r.request_id) <= 0)
				continue;
			return r;
		}
		return null;
	}
	
	private static boolean isValidRequest(int id)
	{
		for(Request r : Requests1)
		{
			if(r.request_id != id)
				continue;
			return true;
		}
		for(Request r : Requests2)
		{
			if(r.request_id != id)
				continue;
			return true;
		}
		return false;
	}
	
	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		int _state = st.getState();
		if(event.equalsIgnoreCase("30744_03.htm") && _state == 1)
		{
			if(st.getQuestItemsCount(3695) == 0)
			{
				st.giveItems(3695, 1);
			}
			st.setState(2);
			st.setCond(1);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("30744_09.htm") && _state == 2)
		{
			if(GetCurrentRequest(st, Requests1) != null)
			{
				return "30744_09a.htm";
			}
			if(st.getQuestItemsCount(3696) == 0)
			{
				st.playSound("ItemSound.quest_middle");
				st.giveItems(3696, 1);
			}
		}
		else if(event.equalsIgnoreCase("30744_16.htm") && _state == 2)
		{
			if(st.getQuestItemsCount(3694) >= 20)
			{
				st.giveItems(57, 20000);
				event = "30744_17.htm";
			}
			st.playSound("ItemSound.quest_finish");
			st.exitCurrentQuest(true);
		}
		else if(event.equalsIgnoreCase("30746_03.htm") && _state == 2)
		{
			if(st.getQuestItemsCount(3692) == 0 && st.getQuestItemsCount(3693) == 0)
			{
				return null;
			}
			if(st.getQuestItemsCount(3471) == 0)
			{
				st.giveItems(3471, 1);
			}
			if(st.getQuestItemsCount(3697) == 0)
			{
				st.giveItems(3697, 1);
			}
			st.takeAllItems(q_blood_crystal);
			st.playSound("ItemSound.quest_middle");
			st.giveItems(q_blood_crystal[1], 1);
		}
		else if(event.equalsIgnoreCase("30746_06.htm") && _state == 2)
		{
			if(!Blood_Crystal2Adena(st, Get_Blood_Crystal_Level(st)))
			{
				return null;
			}
		}
		else if(event.equalsIgnoreCase("30746_10.htm") && _state == 2)
		{
			st.takeAllItems(3471);
			st.takeAllItems(3697);
			st.takeAllItems(q_blood_crystal);
		}
		else if(event.equalsIgnoreCase("30745_02.htm") && _state == 2)
		{
			if(st.getQuestItemsCount(3696) > 0)
			{
				return "30745_03.htm";
			}
		}
		else if(event.equalsIgnoreCase("30745_05b.htm") && _state == 2)
		{
			if(st.getQuestItemsCount(3694) > 0)
			{
				st.takeItems(3694, 1);
			}
			for(Request r : Requests1)
			{
				st.takeAllItems(r.request_id);
				st.takeAllItems(r.request_item);
			}
			for(Request r : Requests2)
			{
				st.takeAllItems(r.request_id);
				st.takeAllItems(r.request_item);
			}
		}
		else
		{
			if(event.equalsIgnoreCase("30745-list1") && _state == 2)
			{
				GenList(st);
				return FormatList(st, Requests1);
			}
			if(event.equalsIgnoreCase("30745-list2") && _state == 2)
			{
				GenList(st);
				return FormatList(st, Requests2);
			}
			if(event.startsWith("30745-request-") && _state == 2)
			{
				event = event.replaceFirst("30745-request-", "");
				int request_id;
				try
				{
					request_id = Integer.valueOf(event);
				}
				catch(Exception e)
				{
					return null;
				}
				if(!isValidRequest(request_id))
				{
					return null;
				}
				st.giveItems(request_id, 1);
				return "30745-" + request_id + ".htm";
			}
		}
		return event;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int _state = st.getState();
		int npcId = npc.getNpcId();
		if(_state == 1)
		{
			if(npcId != 30744)
			{
				return "noquest";
			}
			if(st.getPlayer().getLevel() < 35)
			{
				st.exitCurrentQuest(true);
				return "30744_01.htm";
			}
			st.setCond(0);
			st.unset("list");
			return "30744_02.htm";
		}
		if(_state != 2)
		{
			return "noquest";
		}
		if(npcId == 30744)
		{
			if(st.getQuestItemsCount(3695) > 0)
			{
				if(CalcItemsConds(st, Items_1st_Circle) < 3)
				{
					return "30744_05.htm";
				}
				DelItemsConds(st, Items_1st_Circle);
				st.takeAllItems(3695);
				st.playSound("ItemSound.quest_middle");
				st.giveItems(3692, 1);
				st.setCond(2);
				return "30744_06.htm";
			}
			if(st.getQuestItemsCount(3692) > 0)
			{
				if(st.getPlayer().getLevel() < 45)
				{
					return "30744_07.htm";
				}
				if(st.getQuestItemsCount(3696) == 0)
				{
					return "30744_08.htm";
				}
			}
			if(st.getQuestItemsCount(3696) > 0)
			{
				if(CalcItemsConds(st, Items_2nd_Circle) < 3)
				{
					return "30744_11.htm";
				}
				DelItemsConds(st, Items_2nd_Circle);
				st.takeAllItems(3696);
				st.takeAllItems(3692);
				st.playSound("ItemSound.quest_middle");
				st.giveItems(3693, 1);
				st.setCond(3);
				return "30744_12.htm";
			}
			if(st.getQuestItemsCount(3693) > 0)
			{
				return "30744_14.htm";
			}
		}
		if(npcId == 30746)
		{
			if(st.getQuestItemsCount(3692) == 0 && st.getQuestItemsCount(3693) == 0)
			{
				return "30746_01.htm";
			}
			if(st.getQuestItemsCount(3697) == 0)
			{
				return "30746_02.htm";
			}
			int Blood_Crystal_Level = Get_Blood_Crystal_Level(st);
			if(Blood_Crystal_Level == -1)
			{
				return "30746_08.htm";
			}
			if(Blood_Crystal_Level == 0)
			{
				return "30746_09.htm";
			}
			if(Blood_Crystal_Level == 1)
			{
				return "30746_04.htm";
			}
			if(Blood_Crystal_Level > 1 && Blood_Crystal_Level < 10)
			{
				return "30746_05.htm";
			}
			if(Blood_Crystal_Level == 10 && Blood_Crystal2Adena(st, Blood_Crystal_Level))
			{
				return "30746_05a.htm";
			}
		}
		if(npcId == 30745)
		{
			if(st.getQuestItemsCount(3692) == 0 && st.getQuestItemsCount(3693) == 0)
			{
				return "30745_01a.htm";
			}
			if(st.getQuestItemsCount(3692) > 0)
			{
				Request request = GetCurrentRequest(st, Requests1);
				if(request == null)
				{
					if(st.getPlayer().getLevel() < 45)
					{
						return "30745_01b.htm";
					}
					return st.getQuestItemsCount(3696) > 0 ? "30745_03.htm" : "30745_03a.htm";
				}
				return request.Complete(st) ? "30745_06a.htm" : "30745_05.htm";
			}
			if(st.getQuestItemsCount(3693) > 0)
			{
				Request request = GetCurrentRequest(st, Requests2);
				if(request == null)
				{
					return "30745_03b.htm";
				}
				return request.Complete(st) ? "30745_06b.htm" : "30745_05.htm";
			}
		}
		return "noquest";
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getState() != 2)
		{
			return null;
		}
		int npcId = npc.getNpcId();
		int[][][] Items_Circle = null;
		if(st.getQuestItemsCount(3695) > 0)
		{
			Items_Circle = Items_1st_Circle;
		}
		else if(st.getQuestItemsCount(3696) > 0)
		{
			Items_Circle = Items_2nd_Circle;
		}
		if(Items_Circle != null)
		{
			for(int[][] ItemsCond : Items_Circle)
			{
				for(int i = 2;i < ItemsCond.length;++i)
				{
					if(npcId != ItemsCond[i][0])
						continue;
					st.rollAndGive(ItemsCond[0][0], 1, 1, ItemsCond[1][0], (double) ItemsCond[i][1]);
				}
			}
			if(st.getQuestItemsCount(3695) > 0)
			{
				long Hakas_Head_count = st.getQuestItemsCount(3711);
				long Jakas_Head_count = st.getQuestItemsCount(3712);
				long Markas_Head_count = st.getQuestItemsCount(3713);
				if(npcId == 20271)
				{
					if(Hakas_Head_count == 0 && Rnd.chance(10))
					{
						st.addSpawn(27140, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), 100, 300000);
					}
					else if(Jakas_Head_count == 0 && Rnd.chance(10))
					{
						st.addSpawn(27141, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), 100, 300000);
					}
					else if(Markas_Head_count == 0 && Rnd.chance(10))
					{
						st.addSpawn(27142, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), 100, 300000);
					}
				}
				else if(npcId == 27140)
				{
					if(Hakas_Head_count == 0)
					{
						st.rollAndGive(3711, 1, 1, 1, 100.0);
					}
				}
				else if(npcId == 27141)
				{
					if(Jakas_Head_count == 0)
					{
						st.rollAndGive(3712, 1, 1, 1, 100.0);
					}
				}
				else if(npcId == 27142)
				{
					if(Markas_Head_count == 0)
					{
						st.rollAndGive(3713, 1, 1, 1, 100.0);
					}
				}
				else if(npcId == 20553 && st.getQuestItemsCount(3714) == 0 && Rnd.chance(10))
				{
					st.addSpawn(27143, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), 100, 300000);
				}
			}
			else if(st.getQuestItemsCount(3696) > 0)
			{
				long Athus_Head_count = st.getQuestItemsCount(3722);
				long Lankas_Head_count = st.getQuestItemsCount(3723);
				long Triskas_Head_count = st.getQuestItemsCount(3724);
				long Moturas_Head_count = st.getQuestItemsCount(3725);
				long Kalaths_Head_count = st.getQuestItemsCount(3726);
				if(npcId == 20571)
				{
					if(Athus_Head_count == 0 && Rnd.chance(10))
					{
						st.addSpawn(27144, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), 100, 300000);
					}
					else if(Lankas_Head_count == 0 && Rnd.chance(10))
					{
						st.addSpawn(27145, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), 100, 300000);
					}
					else if(Triskas_Head_count == 0 && Rnd.chance(10))
					{
						st.addSpawn(27146, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), 100, 300000);
					}
					else if(Moturas_Head_count == 0 && Rnd.chance(10))
					{
						st.addSpawn(27147, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), 100, 300000);
					}
					else if(Kalaths_Head_count == 0 && Rnd.chance(10))
					{
						st.addSpawn(27148, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), 100, 300000);
					}
				}
				else if(npcId == 27144)
				{
					if(Athus_Head_count == 0)
					{
						st.rollAndGive(3722, 1, 1, 1, 100.0);
					}
				}
				else if(npcId == 27145)
				{
					if(Lankas_Head_count == 0)
					{
						st.rollAndGive(3723, 1, 1, 1, 100.0);
					}
				}
				else if(npcId == 27146)
				{
					if(Triskas_Head_count == 0)
					{
						st.rollAndGive(3724, 1, 1, 1, 100.0);
					}
				}
				else if(npcId == 27147)
				{
					if(Moturas_Head_count == 0)
					{
						st.rollAndGive(3725, 1, 1, 1, 100.0);
					}
				}
				else if(npcId == 27148 && Kalaths_Head_count == 0)
				{
					st.rollAndGive(3726, 1, 1, 1, 100.0);
				}
			}
		}
		if(st.getQuestItemsCount(3692) > 0 || st.getQuestItemsCount(3693) > 0)
		{
			int Blood_Crystal_Level;
			if(st.getQuestItemsCount(3697) > 0 && st.getPlayer().getActiveWeaponItem() != null && st.getPlayer().getActiveWeaponItem().getItemId() == 3471 && (Blood_Crystal_Level = Get_Blood_Crystal_Level(st)) > 0 && Blood_Crystal_Level < 10)
			{
				for(int lizardmen_id : q_blood_crystal_lizardmen)
				{
					if(npcId != lizardmen_id)
						continue;
					if(Rnd.chance(50))
					{
						st.takeAllItems(q_blood_crystal[Blood_Crystal_Level]);
						st.playSound(Blood_Crystal_Level < 6 ? "ItemSound.quest_middle" : "ItemSound.quest_jackpot");
						st.giveItems(q_blood_crystal[Blood_Crystal_Level + 1], 1);
						continue;
					}
					st.takeAllItems(q_blood_crystal);
					st.giveItems(q_blood_crystal[0], 1);
				}
			}
			Request request;
			if((request = GetCurrentRequest(st, Requests1)) == null)
			{
				request = GetCurrentRequest(st, Requests2);
			}
			if(request != null)
			{
				if(request.droplist.containsKey(npcId))
				{
					st.rollAndGive(request.request_item, 1, 1, request.request_count, (double) request.droplist.get(npcId).intValue());
				}
				int[] spawn_n_chance;
				if(request.spawnlist.containsKey(npcId) && st.getQuestItemsCount(request.request_item) < (long) request.request_count && Rnd.chance((spawn_n_chance = request.spawnlist.get(npcId))[1]))
				{
					st.addSpawn(spawn_n_chance[0], npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), 100, 300000);
					if(spawn_n_chance[0] == 27149)
					{
						Functions.npcSay(npc, "Show me the pretty sparkling things! They're all mine!");
					}
				}
			}
		}
		if((npcId == 27160 || npcId == 27162 || npcId == 27164) && Rnd.chance(50))
		{
			Functions.npcSay(npc, "We'll take the property of the ancient empire!");
			st.addSpawn(27150, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), 100, 300000);
			st.addSpawn(27150, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), 100, 300000);
		}
		return null;
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
	
	public static class Request
	{
		public final int request_id;
		public final int request_item;
		public final int request_count;
		public final int reward_adena;
		public final String text;
		public final Map<Integer, Integer> droplist = new HashMap<>();
		public final Map<Integer, int[]> spawnlist = new HashMap<>();
		
		public Request(int requestid, int requestitem, int requestcount, int rewardadena, String txt)
		{
			request_id = requestid;
			request_item = requestitem;
			request_count = requestcount;
			reward_adena = rewardadena;
			text = txt;
		}
		
		public Request addDrop(int kill_mob_id, int chance)
		{
			droplist.put(kill_mob_id, chance);
			return this;
		}
		
		public Request addSpawn(int kill_mob_id, int spawn_mob_id, int chance)
		{
			try
			{
				spawnlist.put(kill_mob_id, new int[] {spawn_mob_id, chance});
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			return this;
		}
		
		public boolean Complete(QuestState st)
		{
			if(st.getQuestItemsCount(request_item) < (long) request_count)
			{
				return false;
			}
			st.takeAllItems(request_id);
			st.takeAllItems(request_item);
			st.playSound("ItemSound.quest_middle");
			st.giveItems(3694, 1);
			st.giveItems(57, (long) reward_adena);
			st.unset("list");
			return true;
		}
	}
}