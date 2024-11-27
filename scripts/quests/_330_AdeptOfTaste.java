package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _330_AdeptOfTaste extends Quest implements ScriptFile
{
	private static final int Sonia = 30062;
	private static final int Glyvka = 30067;
	private static final int Rollant = 30069;
	private static final int Jacob = 30073;
	private static final int Pano = 30078;
	private static final int Mirien = 30461;
	private static final int Jonas = 30469;
	private static final int Hobgoblin = 20147;
	private static final int Mandragora_Sprout = 20154;
	private static final int Mandragora_Sapling = 20155;
	private static final int Mandragora_Blossom = 20156;
	private static final int Bloody_Bee = 20204;
	private static final int Mandragora_Sprout2 = 20223;
	private static final int Gray_Ant = 20226;
	private static final int Giant_Crimson_Ant = 20228;
	private static final int Stinger_Wasp = 20229;
	private static final int Monster_Eye_Searcher = 20265;
	private static final int Monster_Eye_Gazer = 20266;
	private static final int Ingredient_List = 1420;
	private static final int Sonias_Botany_Book = 1421;
	private static final int Red_Mandragora_Root = 1422;
	private static final int White_Mandragora_Root = 1423;
	private static final int Red_Mandragora_Sap = 1424;
	private static final int White_Mandragora_Sap = 1425;
	private static final int Jacobs_Insect_Book = 1426;
	private static final int Nectar = 1427;
	private static final int Royal_Jelly = 1428;
	private static final int Honey = 1429;
	private static final int Golden_Honey = 1430;
	private static final int Panos_Contract = 1431;
	private static final int Hobgoblin_Amulet = 1432;
	private static final int Dionian_Potato = 1433;
	private static final int Glyvkas_Botany_Book = 1434;
	private static final int Green_Marsh_Moss = 1435;
	private static final int Brown_Marsh_Moss = 1436;
	private static final int Green_Moss_Bundle = 1437;
	private static final int Brown_Moss_Bundle = 1438;
	private static final int Rollants_Creature_Book = 1439;
	private static final int Body_of_Monster_Eye = 1440;
	private static final int Meat_of_Monster_Eye = 1441;
	private static final int[] Jonass_Steak_Dishes = {1442, 1443, 1444, 1445, 1446};
	private static final int[] Miriens_Reviews = {1447, 1448, 1449, 1450, 1451};
	private static final int[] ingredients = {1424, 1429, 1433, 1437, 1441};
	private static final int[] spec_ingredients = {1425, 1430, 1438};
	private static final int[] rewards = {0, 0, 1455, 1456, 1457};
	private static final int[] adena_rewards = {10000, 14870, 6490, 12220, 16540};
	
	public _330_AdeptOfTaste()
	{
		super(false);
		addStartNpc(30469);
		addTalkId(30062);
		addTalkId(30067);
		addTalkId(30069);
		addTalkId(30073);
		addTalkId(30078);
		addTalkId(30461);
		addKillId(20147);
		addKillId(20154);
		addKillId(20155);
		addKillId(20156);
		addKillId(20204);
		addKillId(20223);
		addKillId(20226);
		addKillId(20228);
		addKillId(20229);
		addKillId(20265);
		addKillId(20266);
		addQuestItem(1420);
		addQuestItem(1421);
		addQuestItem(1422);
		addQuestItem(1423);
		addQuestItem(1426);
		addQuestItem(1427);
		addQuestItem(1428);
		addQuestItem(1431);
		addQuestItem(1432);
		addQuestItem(1434);
		addQuestItem(1435);
		addQuestItem(1436);
		addQuestItem(1439);
		addQuestItem(1440);
		addQuestItem(ingredients);
		addQuestItem(spec_ingredients);
		addQuestItem(Jonass_Steak_Dishes);
		addQuestItem(Miriens_Reviews);
	}
	
	private static void MandragoraDrop(QuestState st, int i1, int i2)
	{
		int i = Rnd.get(100);
		if(i < i1)
		{
			st.rollAndGive(1422, 1, 1, 40, 100.0);
		}
		else if(i < i2)
		{
			st.rollAndGive(1423, 1, 1, 40, 100.0);
		}
	}
	
	private static void BeeDrop(QuestState st, int i1, int i2)
	{
		int i = Rnd.get(100);
		if(i < i1)
		{
			st.rollAndGive(1427, 1, 1, 20, 100.0);
		}
		else if(i < i2)
		{
			st.rollAndGive(1428, 1, 1, 10, 100.0);
		}
	}
	
	private static void AntDrop(QuestState st, int i1, int i2)
	{
		int i = Rnd.get(100);
		if(i < i1)
		{
			st.rollAndGive(1435, 1, 1, 20, 100.0);
		}
		else if(i < i2)
		{
			st.rollAndGive(1436, 1, 1, 20, 100.0);
		}
	}
	
	private static void Root2Sap(QuestState st, int sap_id)
	{
		st.takeItems(1421, -1);
		st.takeItems(1423, -1);
		st.takeItems(1422, -1);
		st.playSound("ItemSound.quest_middle");
		st.giveItems(sap_id, 1);
	}
	
	private static void Moss2Bundle(QuestState st, int bundle_id)
	{
		st.takeItems(1434, -1);
		st.takeItems(1436, -1);
		st.takeItems(1435, -1);
		st.playSound("ItemSound.quest_middle");
		st.giveItems(bundle_id, 1);
	}
	
	private static void Nectar2Honey(QuestState st, int honey_id)
	{
		st.takeItems(1426, -1);
		st.takeItems(1427, -1);
		st.takeItems(1428, -1);
		st.playSound("ItemSound.quest_middle");
		st.giveItems(honey_id, 1);
	}
	
	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		int _state = st.getState();
		if(event.equalsIgnoreCase("30469_03.htm") && _state == 1)
		{
			if(st.getQuestItemsCount(1420) == 0)
			{
				st.giveItems(1420, 1);
			}
			st.setState(2);
			st.setCond(1);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("30062_05.htm") && _state == 2)
		{
			if(st.getQuestItemsCount(1423) + st.getQuestItemsCount(1422) < 40)
			{
				return null;
			}
			Root2Sap(st, 1424);
		}
		else if(event.equalsIgnoreCase("30067_05.htm") && _state == 2)
		{
			if(st.getQuestItemsCount(1436) + st.getQuestItemsCount(1435) < 20)
			{
				return null;
			}
			Moss2Bundle(st, 1437);
		}
		else if(event.equalsIgnoreCase("30073_05.htm") && _state == 2)
		{
			if(st.getQuestItemsCount(1427) < 20)
			{
				return null;
			}
			Nectar2Honey(st, 1429);
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
			if(npcId != 30469)
			{
				return "noquest";
			}
			if(st.getPlayer().getLevel() < 24)
			{
				st.exitCurrentQuest(true);
				return "30469_01.htm";
			}
			st.setCond(0);
			return "30469_02.htm";
		}
		if(_state != 2)
		{
			return "noquest";
		}
		long ingredients_count = st.getQuestItemsCount(ingredients);
		long spec_ingredients_count = st.getQuestItemsCount(spec_ingredients);
		long all_ingredients_count = ingredients_count + spec_ingredients_count;
		boolean Has_Ingredient_List;
		boolean bl = Has_Ingredient_List = st.getQuestItemsCount(1420) > 0;
		if(npcId == 30469)
		{
			if(Has_Ingredient_List)
			{
				if(all_ingredients_count < 5)
				{
					return "30469_04.htm";
				}
				st.takeAllItems(1420);
				st.takeAllItems(ingredients);
				st.takeAllItems(spec_ingredients);
				if(spec_ingredients_count > 3)
				{
					spec_ingredients_count = 3;
				}
				st.playSound((spec_ingredients_count += (long) Rnd.get(0, 1)) == 4 ? "ItemSound.quest_jackpot" : "ItemSound.quest_middle");
				st.giveItems(Jonass_Steak_Dishes[(int) spec_ingredients_count], 1);
				return "30469_05t" + ++spec_ingredients_count + ".htm";
			}
			if(all_ingredients_count == 0)
			{
				long Jonass_Steak_Dish_count = st.getQuestItemsCount(Jonass_Steak_Dishes);
				long Miriens_Review_count = st.getQuestItemsCount(Miriens_Reviews);
				if(Jonass_Steak_Dish_count > 0 && Miriens_Review_count == 0)
				{
					return "30469_06.htm";
				}
				if(Jonass_Steak_Dish_count == 0 && Miriens_Review_count > 0)
				{
					for(int i = Miriens_Reviews.length;i > 0;--i)
					{
						if(st.getQuestItemsCount(Miriens_Reviews[i - 1]) <= 0)
							continue;
						st.takeAllItems(Miriens_Reviews);
						if(adena_rewards[i - 1] > 0)
						{
							st.giveItems(57, (long) adena_rewards[i - 1]);
						}
						if(rewards[i - 1] > 0)
						{
							st.giveItems(rewards[i - 1], 1);
						}
						st.playSound("ItemSound.quest_finish");
						st.exitCurrentQuest(true);
						return "30469_06t" + i + ".htm";
					}
				}
			}
		}
		if(npcId == 30461)
		{
			if(Has_Ingredient_List)
			{
				return "30461_01.htm";
			}
			if(all_ingredients_count == 0)
			{
				if(st.getQuestItemsCount(Miriens_Reviews) > 0)
				{
					return "30461_04.htm";
				}
				for(int i = Jonass_Steak_Dishes.length;i > 0;--i)
				{
					if(st.getQuestItemsCount(Jonass_Steak_Dishes[i - 1]) <= 0)
						continue;
					st.takeAllItems(Jonass_Steak_Dishes);
					st.playSound("ItemSound.quest_middle");
					st.giveItems(Miriens_Reviews[i - 1], 1);
					return "30461_02t" + i + ".htm";
				}
			}
		}
		if(!Has_Ingredient_List || all_ingredients_count >= 5)
		{
			return "noquest";
		}
		if(npcId == 30062)
		{
			boolean has_sap;
			boolean bl2 = has_sap = st.getQuestItemsCount(1424) > 0 || st.getQuestItemsCount(1425) > 0;
			if(st.getQuestItemsCount(1421) > 0)
			{
				if(!has_sap)
				{
					long Root_count = st.getQuestItemsCount(1423);
					if(Root_count >= 40)
					{
						Root2Sap(st, 1425);
						return "30062_06.htm";
					}
					Root_count += st.getQuestItemsCount(1422);
					return Root_count < 40 ? "30062_02.htm" : "30062_03.htm";
				}
			}
			else
			{
				if(has_sap)
				{
					return "30062_07.htm";
				}
				st.giveItems(1421, 1);
				return "30062_01.htm";
			}
		}
		if(npcId == 30067)
		{
			boolean has_bundle;
			boolean bl3 = has_bundle = st.getQuestItemsCount(1437) > 0 || st.getQuestItemsCount(1438) > 0;
			if(st.getQuestItemsCount(1434) > 0)
			{
				if(!has_bundle)
				{
					long moss_count = st.getQuestItemsCount(1436);
					if(moss_count >= 20)
					{
						Moss2Bundle(st, 1438);
						return "30067_06.htm";
					}
					moss_count += st.getQuestItemsCount(1435);
					return moss_count < 20 ? "30067_02.htm" : "30067_03.htm";
				}
			}
			else if(has_bundle)
			{
				return "30067_07.htm";
			}
			st.giveItems(1434, 1);
			return "30067_01.htm";
		}
		if(npcId == 30069)
		{
			boolean has_meat;
			boolean bl4 = has_meat = st.getQuestItemsCount(1441) > 0;
			if(st.getQuestItemsCount(1439) > 0)
			{
				if(!has_meat)
				{
					if(st.getQuestItemsCount(1440) < 30)
					{
						return "30069_02.htm";
					}
					st.takeItems(1439, -1);
					st.takeItems(1440, -1);
					st.playSound("ItemSound.quest_middle");
					st.giveItems(1441, 1);
					return "30069_03.htm";
				}
			}
			else
			{
				if(has_meat)
				{
					return "30069_04.htm";
				}
				st.giveItems(1439, 1);
				return "30069_01.htm";
			}
		}
		if(npcId == 30073)
		{
			boolean has_honey;
			boolean bl5 = has_honey = st.getQuestItemsCount(1429) > 0 || st.getQuestItemsCount(1430) > 0;
			if(st.getQuestItemsCount(1426) > 0)
			{
				if(!has_honey)
				{
					if(st.getQuestItemsCount(1427) < 20)
					{
						return "30073_02.htm";
					}
					if(st.getQuestItemsCount(1428) < 10)
					{
						return "30073_03.htm";
					}
					Nectar2Honey(st, 1430);
					return "30073_06.htm";
				}
			}
			else
			{
				if(has_honey)
				{
					return "30073_07.htm";
				}
				st.giveItems(1426, 1);
				return "30073_01.htm";
			}
		}
		if(npcId == 30078)
		{
			boolean has_potato;
			boolean bl6 = has_potato = st.getQuestItemsCount(1433) > 0;
			if(st.getQuestItemsCount(1431) > 0)
			{
				if(!has_potato)
				{
					if(st.getQuestItemsCount(1432) < 30)
					{
						return "30078_02.htm";
					}
					st.takeItems(1431, -1);
					st.takeItems(1432, -1);
					st.playSound("ItemSound.quest_middle");
					st.giveItems(1433, 1);
					return "30078_03.htm";
				}
			}
			else
			{
				if(has_potato)
				{
					return "30078_04.htm";
				}
				st.giveItems(1431, 1);
				return "30078_01.htm";
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
		long ingredients_count = st.getQuestItemsCount(ingredients);
		long spec_ingredients_count = st.getQuestItemsCount(spec_ingredients);
		long all_ingredients_count = ingredients_count + spec_ingredients_count;
		boolean Has_Ingredient_List;
		boolean bl = Has_Ingredient_List = st.getQuestItemsCount(1420) > 0;
		if(!Has_Ingredient_List || all_ingredients_count >= 5)
		{
			return null;
		}
		if(npcId == 20147 && st.getQuestItemsCount(1431) > 0)
		{
			st.rollAndGive(1432, 1, 1, 30, 100.0);
		}
		else if(npcId == 20154 && st.getQuestItemsCount(1421) > 0)
		{
			MandragoraDrop(st, 70, 77);
		}
		else if(npcId == 20155 && st.getQuestItemsCount(1421) > 0)
		{
			MandragoraDrop(st, 77, 85);
		}
		else if(npcId == 20156 && st.getQuestItemsCount(1421) > 0)
		{
			MandragoraDrop(st, 87, 96);
		}
		else if(npcId == 20223 && st.getQuestItemsCount(1421) > 0)
		{
			MandragoraDrop(st, 70, 77);
		}
		else if(npcId == 20204 && st.getQuestItemsCount(1426) > 0)
		{
			BeeDrop(st, 80, 95);
		}
		else if(npcId == 20229 && st.getQuestItemsCount(1426) > 0)
		{
			BeeDrop(st, 92, 100);
		}
		else if(npcId == 20226 && st.getQuestItemsCount(1434) > 0)
		{
			AntDrop(st, 87, 96);
		}
		else if(npcId == 20228 && st.getQuestItemsCount(1434) > 0)
		{
			AntDrop(st, 90, 100);
		}
		else if(npcId == 20265 && st.getQuestItemsCount(1439) > 0)
		{
			st.rollAndGive(1440, 1, 3, 30, 97.0);
		}
		else if(npcId == 20266 && st.getQuestItemsCount(1439) > 0)
		{
			st.rollAndGive(1440, 1, 2, 30, 100.0);
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
}