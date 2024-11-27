package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.GameObjectsStorage;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.Functions;
import l2.gameserver.scripts.ScriptFile;

import java.util.List;

public class _234_FatesWhisper extends Quest implements ScriptFile
{
	private static final int PIPETTE_KNIFE = 4665;
	private static final int REIRIAS_SOUL_ORB = 4666;
	private static final int KERNONS_INFERNIUM_SCEPTER = 4667;
	private static final int GOLCONDAS_INFERNIUM_SCEPTER = 4668;
	private static final int HALLATES_INFERNIUM_SCEPTER = 4669;
	private static final int REORINS_HAMMER = 4670;
	private static final int REORINS_MOLD = 4671;
	private static final int INFERNIUM_VARNISH = 4672;
	private static final int RED_PIPETTE_KNIFE = 4673;
	private static final int STAR_OF_DESTINY = 5011;
	private static final int CRYSTAL_B = 1460;
	private static final int Damaskus = 79;
	private static final int Lance = 97;
	private static final int Samurai = 2626;
	private static final int Staff = 210;
	private static final int BOP = 287;
	private static final int Battle = 175;
	private static final int Demons = 234;
	private static final int Bellion = 268;
	private static final int Glory = 171;
	private static final int WizTear = 7889;
	private static final int GuardianSword = 7883;
	private static final int Tallum = 80;
	private static final int Infernal = 7884;
	private static final int Carnage = 288;
	private static final int Halberd = 98;
	private static final int Elemental = 150;
	private static final int Dasparion = 212;
	private static final int Spiritual = 7894;
	private static final int Bloody = 235;
	private static final int Blood = 269;
	private static final int Meteor = 2504;
	private static final int Destroyer = 7899;
	private static final int Keshanberk = 5233;
	private static final int REORIN = 31002;
	private static final int CLIFF = 30182;
	private static final int FERRIS = 30847;
	private static final int ZENKIN = 30178;
	private static final int KASPAR = 30833;
	private static final int CABRIOCOFFER = 31027;
	private static final int CHEST_KERNON = 31028;
	private static final int CHEST_GOLKONDA = 31029;
	private static final int CHEST_HALLATE = 31030;
	private static final int SHILLEN_MESSAGER = 25035;
	private static final int DEATH_LORD = 25220;
	private static final int KERNON = 25054;
	private static final int LONGHORN = 25126;
	private static final int BAIUM = 29020;
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
	
	public _234_FatesWhisper()
	{
		super(true);
		addStartNpc(31002);
		addTalkId(30182);
		addTalkId(30847);
		addTalkId(30178);
		addTalkId(30833);
		addTalkId(31027);
		addTalkId(31028);
		addTalkId(31029);
		addTalkId(31030);
		addKillId(25035);
		addKillId(25220);
		addKillId(25054);
		addKillId(25126);
		addAttackId(29020);
		addQuestItem(4666, 4669, 4667, 4668, 4672, 4670, 4671, 4665, 4673);
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
		NpcInstance isQuest;
		int oldweapon = 0;
		int newweapon = 0;
		if(event.equalsIgnoreCase("31002-03.htm"))
		{
			st.setCond(1);
			st.setState(2);
		}
		else if(event.equalsIgnoreCase("31002-05b.htm"))
		{
			st.takeItems(4666, -1);
			st.setCond(2);
		}
		else if(event.equalsIgnoreCase("31030-02.htm"))
		{
			st.giveItems(4669, 1, false);
		}
		else if(event.equalsIgnoreCase("31028-02.htm"))
		{
			st.giveItems(4667, 1, false);
		}
		else if(event.equalsIgnoreCase("31029-02.htm"))
		{
			st.giveItems(4668, 1, false);
		}
		else if(event.equalsIgnoreCase("31002-06a.htm"))
		{
			st.takeItems(4669, -1);
			st.takeItems(4667, -1);
			st.takeItems(4668, -1);
			st.setCond(3);
		}
		else if(event.equalsIgnoreCase("30182-01c.htm"))
		{
			st.takeItems(4672, -1);
			st.giveItems(4672, 1, false);
		}
		else if(event.equalsIgnoreCase("31002-07a.htm"))
		{
			st.setCond(4);
		}
		else if(event.equalsIgnoreCase("31002-08a.htm"))
		{
			st.takeItems(4670, -1);
			st.setCond(5);
		}
		else if(event.equalsIgnoreCase("30833-01b.htm"))
		{
			st.setCond(7);
			st.giveItems(4665, 1, false);
		}
		else if(event.equalsIgnoreCase("Damaskus.htm"))
		{
			oldweapon = 79;
		}
		else if(event.equalsIgnoreCase("Samurai.htm"))
		{
			oldweapon = 2626;
		}
		else if(event.equalsIgnoreCase("BOP.htm"))
		{
			oldweapon = 287;
		}
		else if(event.equalsIgnoreCase("Lance.htm"))
		{
			oldweapon = 97;
		}
		else if(event.equalsIgnoreCase("Battle.htm"))
		{
			oldweapon = 175;
		}
		else if(event.equalsIgnoreCase("Staff.htm"))
		{
			oldweapon = 210;
		}
		else if(event.equalsIgnoreCase("Demons.htm"))
		{
			oldweapon = 234;
		}
		else if(event.equalsIgnoreCase("Bellion.htm"))
		{
			oldweapon = 268;
		}
		else if(event.equalsIgnoreCase("Glory.htm"))
		{
			oldweapon = 171;
		}
		else if(event.equalsIgnoreCase("WizTear.htm"))
		{
			oldweapon = 7889;
		}
		else if(event.equalsIgnoreCase("GuardianSword.htm"))
		{
			oldweapon = 7883;
		}
		else if(event.equalsIgnoreCase("Tallum"))
		{
			newweapon = 80;
		}
		else if(event.equalsIgnoreCase("Infernal"))
		{
			newweapon = 7884;
		}
		else if(event.equalsIgnoreCase("Carnage"))
		{
			newweapon = 288;
		}
		else if(event.equalsIgnoreCase("Halberd"))
		{
			newweapon = 98;
		}
		else if(event.equalsIgnoreCase("Elemental"))
		{
			newweapon = 150;
		}
		else if(event.equalsIgnoreCase("Dasparion"))
		{
			newweapon = 212;
		}
		else if(event.equalsIgnoreCase("Spiritual"))
		{
			newweapon = 7894;
		}
		else if(event.equalsIgnoreCase("Bloody"))
		{
			newweapon = 235;
		}
		else if(event.equalsIgnoreCase("Blood"))
		{
			newweapon = 269;
		}
		else if(event.equalsIgnoreCase("Meteor"))
		{
			newweapon = 2504;
		}
		else if(event.equalsIgnoreCase("Destroyer"))
		{
			newweapon = 7899;
		}
		else if(event.equalsIgnoreCase("Keshanberk"))
		{
			newweapon = 5233;
		}
		else if(event.equalsIgnoreCase("CABRIOCOFFER_Fail"))
		{
			isQuest = GameObjectsStorage.getByNpcId(31027);
			if(isQuest != null)
			{
				isQuest.deleteMe();
			}
		}
		else if(event.equalsIgnoreCase("CHEST_HALLATE_Fail"))
		{
			isQuest = GameObjectsStorage.getByNpcId(31030);
			if(isQuest != null)
			{
				isQuest.deleteMe();
			}
		}
		else if(event.equalsIgnoreCase("CHEST_KERNON_Fail"))
		{
			isQuest = GameObjectsStorage.getByNpcId(31028);
			if(isQuest != null)
			{
				isQuest.deleteMe();
			}
		}
		else if(event.equalsIgnoreCase("CHEST_GOLKONDA_Fail") && (isQuest = GameObjectsStorage.getByNpcId(31029)) != null)
		{
			isQuest.deleteMe();
		}
		String htmltext = event;
		if(oldweapon != 0)
		{
			if(st.getQuestItemsCount(oldweapon) >= 1)
			{
				if(st.getQuestItemsCount(1460) >= 984)
				{
					st.set("oldweapon", String.valueOf(oldweapon));
					st.takeItems(1460, 984);
					st.setCond(10);
				}
				else
				{
					htmltext = "cheeter.htm";
				}
			}
			else
			{
				htmltext = "noweapon.htm";
			}
		}
		if(newweapon != 0)
		{
			int oldWeaponId = st.getInt("oldweapon");
			List<ItemInstance> olditem = st.getPlayer().getInventory().getItemsByItemId(oldWeaponId);
			ItemInstance itemtotake = null;
			for(ItemInstance itemInstance : olditem)
			{
				if(itemInstance.isAugmented() || itemInstance.getEnchantLevel() != 0)
					continue;
				itemtotake = itemInstance;
				break;
			}
			if(st.getPlayer().getInventory().destroyItem(itemtotake, 1))
			{
				st.giveItems(newweapon, 1, false);
				st.giveItems(5011, 1, false);
				st.unset("cond");
				st.unset("oldweapon");
				st.playSound("ItemSound.quest_finish");
				htmltext = "make.htm";
				st.exitCurrentQuest(false);
			}
			else
			{
				htmltext = "noweapon-" + oldWeaponId + ".htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		if(st.getState() == 3)
		{
			return "completed";
		}
		int npcId = npc.getNpcId();
		String htmltext = "noquest";
		int cond = st.getCond();
		if(npcId == 31002)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 75)
				{
					htmltext = "31002-02.htm";
				}
				else
				{
					htmltext = "31002-01.htm";
					st.exitCurrentQuest(true);
				}
			}
			else if(cond == 1 && st.getQuestItemsCount(4666) >= 1)
			{
				htmltext = "31002-05.htm";
			}
			else if(cond == 2 && st.getQuestItemsCount(4669) >= 1 && st.getQuestItemsCount(4667) >= 1 && st.getQuestItemsCount(4668) >= 1)
			{
				htmltext = "31002-06.htm";
			}
			else if(cond == 3 && st.getQuestItemsCount(4672) >= 1)
			{
				htmltext = "31002-07.htm";
			}
			else if(cond == 4 && st.getQuestItemsCount(4670) >= 1)
			{
				htmltext = "31002-08.htm";
			}
			else if(cond == 8 && st.getQuestItemsCount(4671) >= 1)
			{
				st.takeItems(4671, -1);
				st.setCond(9);
				htmltext = "31002-09.htm";
			}
			else if(cond == 9 && st.getQuestItemsCount(1460) >= 984)
			{
				htmltext = "31002-10.htm";
			}
			else if(cond == 10)
			{
				htmltext = "a-grade.htm";
			}
		}
		else if(npcId == 31027 && cond == 1 && st.getQuestItemsCount(4666) == 0)
		{
			st.giveItems(4666, 1, false);
			htmltext = "31027-01.htm";
		}
		else if(npcId == 31030 && cond == 2 && st.getQuestItemsCount(4669) == 0)
		{
			htmltext = "31030-01.htm";
		}
		else if(npcId == 31028 && cond == 2 && st.getQuestItemsCount(4667) == 0)
		{
			htmltext = "31028-01.htm";
		}
		else if(npcId == 31029 && cond == 2 && st.getQuestItemsCount(4668) == 0)
		{
			htmltext = "31029-01.htm";
		}
		else if(npcId == 30182 && cond == 3 && st.getQuestItemsCount(4672) == 0)
		{
			htmltext = "30182-01.htm";
		}
		else if(npcId == 30847 && cond == 4 && st.getQuestItemsCount(4670) == 0)
		{
			st.giveItems(4670, 1, false);
			htmltext = "30847-01.htm";
		}
		else if(npcId == 30178 && st.getQuestItemsCount(4671) == 0)
		{
			st.setCond(6);
			htmltext = "30178-01.htm";
		}
		else if(npcId == 30833)
		{
			if(cond == 6)
			{
				htmltext = "30833-01.htm";
			}
			else if(cond == 7 && st.getQuestItemsCount(4673) == 1)
			{
				st.setCond(8);
				st.takeItems(4673, -1);
				st.giveItems(4671, 1, false);
				htmltext = "30833-03.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(cond == 1 && npcId == 25035 && GameObjectsStorage.getByNpcId(31027) == null)
		{
			st.addSpawn(31027);
			st.playSound("ItemSound.quest_middle");
			st.startQuestTimer("CABRIOCOFFER_Fail", 120000);
		}
		if(cond == 2 && npcId == 25220 && GameObjectsStorage.getByNpcId(31030) == null)
		{
			st.addSpawn(31030);
			st.playSound("ItemSound.quest_middle");
			st.startQuestTimer("CHEST_HALLATE_Fail", 120000);
		}
		if(cond == 2 && npcId == 25054 && GameObjectsStorage.getByNpcId(31028) == null)
		{
			st.addSpawn(31028);
			st.playSound("ItemSound.quest_middle");
			st.startQuestTimer("CHEST_KERNON_Fail", 120000);
		}
		if(cond == 2 && npcId == 25126 && GameObjectsStorage.getByNpcId(31029) == null)
		{
			st.addSpawn(31029);
			st.playSound("ItemSound.quest_middle");
			st.startQuestTimer("CHEST_GOLKONDA_Fail", 120000);
		}
		return null;
	}
	
	@Override
	public String onAttack(NpcInstance npc, QuestState st)
	{
		int cond = st.getCond();
		if(cond == 7 && npc.getNpcId() == 29020 && st.getQuestItemsCount(4665) >= 1 && st.getQuestItemsCount(4673) == 0 && st.getItemEquipped(7) == 4665)
		{
			if(Rnd.chance(50))
			{
				Functions.npcSay(npc, "Who dares to try steal my blood?");
			}
			st.takeItems(4665, -1);
			st.giveItems(4673, 1, false);
			st.playSound("ItemSound.quest_itemget");
		}
		return null;
	}
}