package l2.gameserver.utils;

import l2.gameserver.Config;
import l2.gameserver.cache.Msg;
import l2.gameserver.data.xml.holder.ItemHolder;
import l2.gameserver.idfactory.IdFactory;
import l2.gameserver.instancemanager.CursedWeaponsManager;
import l2.gameserver.model.Playable;
import l2.gameserver.model.Player;
import l2.gameserver.model.instances.PetInstance;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.model.items.attachment.PickableAttachment;
import l2.gameserver.model.pledge.Clan;
import l2.gameserver.network.l2.s2c.L2GameServerPacket;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.network.l2.s2c.SystemMessage2;
import l2.gameserver.tables.PetDataTable;
import l2.gameserver.templates.item.ItemTemplate;
import org.apache.commons.lang3.ArrayUtils;

public final class ItemFunctions
{
	public static final int[][] catalyst = {{12362, 14078, 14702}, {12363, 14079, 14703}, {12364, 14080, 14704}, {12365, 14081, 14705}, {12366, 14082, 14706}, {12367, 14083, 14707}, {12368, 14084, 14708}, {12369, 14085, 14709}, {12370, 14086, 14710}, {12371, 14087, 14711}};
	
	private ItemFunctions()
	{
	}
	
	public static ItemInstance createItem(int itemId)
	{
		ItemInstance item = new ItemInstance(IdFactory.getInstance().getNextId(), itemId);
		item.setLocation(ItemInstance.ItemLocation.VOID);
		item.setCount(1);
		return item;
	}
	
	public static void addItem(Playable playable, int itemId, long count, boolean notify)
	{
		if(playable == null || count < 1)
		{
			return;
		}
		Playable player = playable.isSummon() ? playable.getPlayer() : playable;
		ItemTemplate t = ItemHolder.getInstance().getTemplate(itemId);
		if(t.isStackable())
		{
			player.getInventory().addItem(itemId, count);
		}
		else
		{
			for(long i = 0;i < count;++i)
			{
				player.getInventory().addItem(itemId, 1);
			}
		}
		if(notify)
		{
			player.sendPacket(SystemMessage2.obtainItems(itemId, count, 0));
		}
	}
	
	public static long getItemCount(Playable playable, int itemId)
	{
		if(playable == null)
		{
			return 0;
		}
		Player player = playable.getPlayer();
		return player.getInventory().getCountOf(itemId);
	}
	
	public static long removeItem(Playable playable, int itemId, long count, boolean notify)
	{
		long removed = 0;
		if(playable == null || count < 1)
		{
			return removed;
		}
		Player player = playable.getPlayer();
		ItemTemplate t = ItemHolder.getInstance().getTemplate(itemId);
		if(t.isStackable())
		{
			if(player.getInventory().destroyItemByItemId(itemId, count))
			{
				removed = count;
			}
		}
		else
		{
			for(long i = 0;i < count;++i)
			{
				if(!player.getInventory().destroyItemByItemId(itemId, 1))
					continue;
				++removed;
			}
		}
		if(removed > 0 && notify)
		{
			player.sendPacket(SystemMessage2.removeItems(itemId, removed));
		}
		return removed;
	}
	
	public static final SystemMessage checkIfCanEquip(PetInstance pet, ItemInstance item)
	{
		if(!item.isEquipable())
		{
			return Msg.ITEM_NOT_AVAILABLE_FOR_PETS;
		}
		int petId = pet.getNpcId();
		if(item.getTemplate().isPendant() || PetDataTable.isWolf(petId) && item.getTemplate().isForWolf() || PetDataTable.isHatchling(petId) && item.getTemplate().isForHatchling() || PetDataTable.isStrider(petId) && item.getTemplate().isForStrider() || PetDataTable.isGWolf(petId) && item.getTemplate().isForGWolf() || PetDataTable.isBabyPet(petId) && item.getTemplate().isForPetBaby() || PetDataTable.isImprovedBabyPet(petId) && item.getTemplate().isForPetBaby())
		{
			return null;
		}
		return Msg.ITEM_NOT_AVAILABLE_FOR_PETS;
	}
	
	public static final L2GameServerPacket checkIfCanEquip(Player player, ItemInstance item)
	{
		int itemId = item.getItemId();
		int targetSlot = item.getTemplate().getBodyPart();
		Clan clan = player.getClan();
		if(itemId >= 7850 && itemId <= 7859 && player.getLvlJoinedAcademy() == 0)
		{
			return Msg.THIS_ITEM_CAN_ONLY_BE_WORN_BY_A_MEMBER_OF_THE_CLAN_ACADEMY;
		}
		if(ArrayUtils.contains(ItemTemplate.ITEM_ID_CASTLE_CIRCLET, itemId) && (clan == null || itemId != ItemTemplate.ITEM_ID_CASTLE_CIRCLET[clan.getCastle()]))
		{
			return Msg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;
		}
		if(!(itemId != 6841 || clan != null && player.isClanLeader() && clan.getCastle() != 0))
		{
			return Msg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;
		}
		if(targetSlot == 16384 || targetSlot == 256 || targetSlot == 128)
		{
			if(itemId != player.getInventory().getPaperdollItemId(7) && CursedWeaponsManager.getInstance().isCursed(player.getInventory().getPaperdollItemId(7)))
			{
				return Msg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;
			}
			if(player.isCursedWeaponEquipped() && itemId != player.getCursedWeaponEquippedId())
			{
				return Msg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;
			}
		}
		return null;
	}
	
	public static boolean checkIfCanPickup(Playable playable, ItemInstance item)
	{
		Player player = playable.getPlayer();
		return item.getDropTimeOwner() <= System.currentTimeMillis() || item.getDropPlayers().contains(player.getObjectId());
	}
	
	public static boolean canAddItem(Player player, ItemInstance item)
	{
		if(!player.getInventory().validateWeight(item))
		{
			player.sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
			return false;
		}
		if(!player.getInventory().validateCapacity(item))
		{
			player.sendPacket(Msg.YOUR_INVENTORY_IS_FULL);
			return false;
		}
		if(!item.getTemplate().getHandler().pickupItem(player, item))
		{
			return false;
		}
		PickableAttachment attachment;
		PickableAttachment pickableAttachment = attachment = item.getAttachment() instanceof PickableAttachment ? (PickableAttachment) item.getAttachment() : null;
		return attachment == null || attachment.canPickUp(player);
	}
	
	public static final boolean checkIfCanDiscard(Player player, ItemInstance item)
	{
		if(item.isHeroWeapon())
		{
			return false;
		}
		if(PetDataTable.isPetControlItem(item) && player.isMounted())
		{
			return false;
		}
		if(player.getPetControlItem() == item)
		{
			return false;
		}
		if(player.getEnchantScroll() == item)
		{
			return false;
		}
		if(item.isCursed())
		{
			return false;
		}
		return !item.getTemplate().isQuest();
	}
	
	public static final boolean isBlessedEnchantScroll(int itemId)
	{
		switch(itemId)
		{
			case 6569:
			case 6570:
			case 6571:
			case 6572:
			case 6573:
			case 6574:
			case 6575:
			case 6576:
			case 6577:
			case 6578:
			{
				return true;
			}
		}
		return false;
	}
	
	public static final boolean isCrystallEnchantScroll(int itemId)
	{
		switch(itemId)
		{
			case 731:
			case 732:
			case 949:
			case 950:
			case 953:
			case 954:
			case 957:
			case 958:
			case 961:
			case 962:
			{
				return true;
			}
		}
		return false;
	}
	
	public static final int getEnchantCrystalId(ItemInstance item, ItemInstance scroll, ItemInstance catalyst)
	{
		boolean scrollValid = false;
		for(int scrollId : getEnchantScrollId(item))
		{
			if(scroll.getItemId() != scrollId)
				continue;
			scrollValid = true;
			break;
		}
		boolean catalystValid = false;
		if(catalyst == null)
		{
			catalystValid = true;
		}
		else
		{
			for(int catalystId : getEnchantCatalystId(item))
			{
				if(catalystId != catalyst.getItemId())
					continue;
				catalystValid = true;
				break;
			}
		}
		if(scrollValid && catalystValid)
		{
			switch(item.getCrystalType().cry)
			{
				case 0:
				{
					return 0;
				}
				case 1458:
				{
					return 1458;
				}
				case 1459:
				{
					return 1459;
				}
				case 1460:
				{
					return 1460;
				}
				case 1461:
				{
					return 1461;
				}
				case 1462:
				{
					return 1462;
				}
			}
		}
		return -1;
	}
	
	public static final int[] getEnchantScrollId(ItemInstance item)
	{
		if(item.getTemplate().getType2() == 0)
		{
			switch(item.getCrystalType().cry)
			{
				case 0:
				{
					return new int[] {13540};
				}
				case 1458:
				{
					return new int[] {955, 6575, 957};
				}
				case 1459:
				{
					return new int[] {951, 6573, 953};
				}
				case 1460:
				{
					return new int[] {947, 6571, 949};
				}
				case 1461:
				{
					return new int[] {729, 6569, 731};
				}
				case 1462:
				{
					return new int[] {959, 6577, 961};
				}
			}
		}
		else if(item.getTemplate().getType2() == 1 || item.getTemplate().getType2() == 2)
		{
			switch(item.getCrystalType().cry)
			{
				case 1458:
				{
					return new int[] {956, 6576, 958};
				}
				case 1459:
				{
					return new int[] {952, 6574, 954};
				}
				case 1460:
				{
					return new int[] {948, 6572, 950};
				}
				case 1461:
				{
					return new int[] {730, 6570, 732};
				}
				case 1462:
				{
					return new int[] {960, 6578, 962};
				}
			}
		}
		return new int[0];
	}
	
	public static final int[] getEnchantCatalystId(ItemInstance item)
	{
		if(item.getTemplate().getType2() == 0)
		{
			switch(item.getCrystalType().cry)
			{
				case 1461:
				{
					return catalyst[3];
				}
				case 1460:
				{
					return catalyst[2];
				}
				case 1459:
				{
					return catalyst[1];
				}
				case 1458:
				{
					return catalyst[0];
				}
				case 1462:
				{
					return catalyst[4];
				}
			}
		}
		else if(item.getTemplate().getType2() == 1 || item.getTemplate().getType2() == 2)
		{
			switch(item.getCrystalType().cry)
			{
				case 1461:
				{
					return catalyst[8];
				}
				case 1460:
				{
					return catalyst[7];
				}
				case 1459:
				{
					return catalyst[6];
				}
				case 1458:
				{
					return catalyst[5];
				}
				case 1462:
				{
					return catalyst[9];
				}
			}
		}
		return new int[] {0, 0, 0};
	}
	
	public static double getEnchantChance(ItemInstance item)
	{
		int enchantLevel = item.getEnchantLevel();
		double[] enchantChances = null;
		switch(item.getTemplate().getBodyPart())
		{
			case 32768:
			{
				enchantChances = Config.ENCHANT_CHANCES_FULL_ARMOR;
				break;
			}
			case 1:
			case 64:
			case 256:
			case 512:
			case 1024:
			case 2048:
			case 4096:
			case 8192:
			{
				enchantChances = Config.ENCHANT_CHANCES_ARMOR;
				break;
			}
			case 6:
			case 8:
			case 48:
			{
				enchantChances = Config.ENCHANT_CHANCES_JEWELRY;
				break;
			}
			case 128:
			case 16384:
			{
				enchantChances = Config.ENCHANT_CHANCES_WEAPON;
			}
		}
		if(enchantChances == null && Config.ALT_HAIR_TO_ACC_SLOT && (item.getTemplate().getBodyPart() == 65536 || item.getTemplate().getBodyPart() == 262144 || item.getTemplate().getBodyPart() == 524288))
		{
			enchantChances = Config.ENCHANT_CHANCES_ARMOR;
		}
		if(enchantChances == null)
		{
			return 0.0;
		}
		if(enchantLevel >= enchantChances.length)
		{
			enchantLevel = enchantChances.length - 1;
		}
		return enchantChances[enchantLevel];
	}
}