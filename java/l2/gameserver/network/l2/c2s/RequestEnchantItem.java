package l2.gameserver.network.l2.c2s;

import l2.commons.util.Rnd;
import l2.gameserver.Config;
import l2.gameserver.data.xml.holder.EnchantItemHolder;
import l2.gameserver.model.Player;
import l2.gameserver.model.World;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.model.items.PcInventory;
import l2.gameserver.network.l2.GameClient;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.EnchantResult;
import l2.gameserver.network.l2.s2c.InventoryUpdate;
import l2.gameserver.network.l2.s2c.MagicSkillUse;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.templates.item.support.EnchantScroll;
import l2.gameserver.utils.ItemFunctions;
import l2.gameserver.utils.Log;

import java.util.List;

public class RequestEnchantItem extends L2GameClientPacket
{
	private int _objectId;
	
	private static final void broadcastResult(Player enchanter, ItemInstance item)
	{
		if(item.getTemplate().getType2() == 0)
		{
			if(item.getEnchantLevel() != 7 && item.getEnchantLevel() != 15)
				return;
			MagicSkillUse msu = new MagicSkillUse(enchanter, enchanter, 2025, 1, 500, 1500);
			List<Player> players = World.getAroundPlayers(enchanter);
			for(Player player : players)
			{
				player.sendMessage(new CustomMessage("_C1_HAS_SUCCESSFULLY_ENCHANTED_A_S2_S3", player, enchanter, item, item.getEnchantLevel()));
				player.sendPacket(msu);
			}
			return;
		}
		else
		{
			if(item.getEnchantLevel() != 6)
				return;
			MagicSkillUse msu = new MagicSkillUse(enchanter, enchanter, 2025, 1, 500, 1500);
			List<Player> players = World.getAroundPlayers(enchanter);
			for(Player player : players)
			{
				player.sendMessage(new CustomMessage("_C1_HAS_SUCCESSFULLY_ENCHANTED_A_S2_S3", player, enchanter, item, item.getEnchantLevel()));
				player.sendPacket(msu);
			}
		}
	}
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		GameClient client = getClient();
		if(client == null)
		{
			return;
		}
		Player player = client.getActiveChar();
		if(player == null)
		{
			return;
		}
		if(player.isActionsDisabled())
		{
			player.setEnchantScroll(null);
			player.sendActionFailed();
			return;
		}
		if(System.currentTimeMillis() - client.getLastEnchantPacket() < (long) Config.ATTACK_PACKET_DELAY)
		{
			player.setEnchantScroll(null);
			player.sendActionFailed();
			return;
		}
		client.setLastEnchantPacket();
		if(player.isInTrade())
		{
			player.setEnchantScroll(null);
			player.sendActionFailed();
			return;
		}
		if(player.isInStoreMode())
		{
			player.setEnchantScroll(null);
			player.sendPacket(EnchantResult.CANCEL);
			player.sendPacket(SystemMsg.YOU_CANNOT_ENCHANT_WHILE_OPERATING_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP);
			player.sendActionFailed();
			return;
		}
		PcInventory inventory = player.getInventory();
		inventory.writeLock();
		try
		{
			ItemInstance item = inventory.getItemByObjectId(_objectId);
			ItemInstance scroll = player.getEnchantScroll();
			if(item == null || scroll == null)
			{
				player.sendActionFailed();
				return;
			}
			EnchantScroll enchantScroll = EnchantItemHolder.getInstance().getEnchantScroll(scroll.getItemId());
			if(enchantScroll == null)
			{
				player.sendActionFailed();
				return;
			}
			if(!item.canBeEnchanted(false))
			{
				player.sendPacket(EnchantResult.CANCEL);
				player.sendPacket(SystemMsg.INAPPROPRIATE_ENCHANT_CONDITIONS);
				player.sendActionFailed();
				return;
			}
			if(!enchantScroll.isUsableWith(item))
			{
				player.sendPacket(EnchantResult.CANCEL);
				player.sendPacket(SystemMsg.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL);
				player.sendActionFailed();
				return;
			}
			double chanceMod = 1.0 + enchantScroll.getChanceMod();
			int itemEnchantLevel = item.getEnchantLevel();
			int itemId = item.getItemId();
			int toLvl = itemEnchantLevel + enchantScroll.getIncrement();
			chanceMod *= (double) player.getBonus().getEnchantItemMul();
			if(!inventory.destroyItem(scroll, 1))
			{
				player.sendPacket(EnchantResult.CANCEL);
				player.sendPacket(SystemMsg.INAPPROPRIATE_ENCHANT_CONDITIONS);
				player.sendActionFailed();
				return;
			}
			boolean equipped = item.isEquipped();
			if(equipped)
			{
				inventory.unEquipItem(item);
			}
			double chance = ItemFunctions.getEnchantChance(item);
			if(enchantScroll.isInfallible() || Rnd.chance(chance * chanceMod))
			{
				item.setEnchantLevel(toLvl);
				Log.LogItem(player, Log.ItemLog.EnchantSuccess, item);
				if(equipped)
				{
					inventory.equipItem(item);
				}
				player.sendPacket(new InventoryUpdate().addModifiedItem(item));
				player.sendPacket(new SystemMessage(63).addNumber(item.getEnchantLevel()).addItemName(item.getItemId()));
				player.sendPacket(EnchantResult.SUCESS);
				if(!Config.SHOW_ENCHANT_EFFECT_RESULT)
					return;
				broadcastResult(player, item);
				return;
			}
			switch(enchantScroll.getOnFailAction())
			{
				case CRYSTALIZE:
				{
					if(item.isEquipped())
					{
						player.sendDisarmMessage(item);
					}
					Log.LogItem(player, Log.ItemLog.EnchantFail, item);
					int itemCrystalId = item.getCrystalType().cry;
					int itemCrystalCount = item.getTemplate().getCrystalCount();
					if(!inventory.destroyItem(item, 1))
					{
						player.sendActionFailed();
						return;
					}
					if(itemCrystalId > 0 && itemCrystalCount > 0)
					{
						int crystalAmount = (int) ((double) itemCrystalCount * 0.87);
						if(itemEnchantLevel > 3)
						{
							crystalAmount = (int) ((double) crystalAmount + (double) itemCrystalCount * 0.25 * (double) (itemEnchantLevel - 3));
						}
						if(crystalAmount < 1)
						{
							crystalAmount = 1;
						}
						player.sendPacket(new EnchantResult(1, itemCrystalId, crystalAmount));
						player.sendPacket(new SystemMessage(65).addNumber(itemEnchantLevel).addItemName(itemId));
						ItemFunctions.addItem(player, itemCrystalId, crystalAmount, true);
						return;
					}
					player.sendPacket(EnchantResult.FAILED_NO_CRYSTALS);
					player.sendPacket(new SystemMessage(64).addItemName(item.getItemId()));
					return;
				}
				case RESET:
				{
					int resetLvl = Math.min(item.getEnchantLevel(), enchantScroll.getFailResultLevel());
					item.setEnchantLevel(resetLvl);
					if(equipped)
					{
						inventory.equipItem(item);
					}
					Log.LogItem(player, Log.ItemLog.EnchantFail, item);
					player.sendPacket(new InventoryUpdate().addModifiedItem(item));
					player.sendPacket(SystemMsg.THE_BLESSED_ENCHANT_FAILED);
					player.sendPacket(EnchantResult.BLESSED_FAILED);
				}
				case NONE:
				{
					if(equipped)
					{
						inventory.equipItem(item);
					}
					Log.LogItem(player, Log.ItemLog.EnchantFail, item);
					player.sendPacket(EnchantResult.ANCIENT_FAILED);
					player.sendPacket(SystemMsg.INAPPROPRIATE_ENCHANT_CONDITIONS);
				}
			}
			return;
		}
		finally
		{
			inventory.writeUnlock();
			player.setEnchantScroll(null);
			player.updateStats();
		}
	}
}