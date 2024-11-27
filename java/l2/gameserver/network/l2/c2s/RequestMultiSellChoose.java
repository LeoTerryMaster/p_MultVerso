package l2.gameserver.network.l2.c2s;

import l2.commons.math.SafeMath;
import l2.gameserver.Config;
import l2.gameserver.cache.Msg;
import l2.gameserver.data.xml.holder.ItemHolder;
import l2.gameserver.data.xml.holder.MultiSellHolder;
import l2.gameserver.instancemanager.ReflectionManager;
import l2.gameserver.model.Player;
import l2.gameserver.model.base.MultiSellEntry;
import l2.gameserver.model.base.MultiSellIngredient;
import l2.gameserver.model.entity.residence.Castle;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.items.ItemAttributes;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.model.items.PcInventory;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.network.l2.s2c.SystemMessage2;
import l2.gameserver.templates.item.ItemTemplate;
import l2.gameserver.utils.ItemFunctions;
import l2.gameserver.utils.Log;

import java.util.ArrayList;
import java.util.List;

public class RequestMultiSellChoose extends L2GameClientPacket
{
	private int _listId;
	private int _entryId;
	private long _amount;
	
	@Override
	protected void readImpl()
	{
		_listId = readD();
		_entryId = readD();
		_amount = readD();
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null || _amount < 1)
		{
			return;
		}
		MultiSellHolder.MultiSellListContainer list1 = activeChar.getMultisell();
		if(list1 == null)
		{
			activeChar.sendActionFailed();
			activeChar.setMultisell(null);
			return;
		}
		if(list1.getListId() != _listId)
		{
			Log.add("Player " + activeChar.getName() + " trying to change multisell list id, ban this player!", "illegal-actions");
			activeChar.sendActionFailed();
			activeChar.setMultisell(null);
			return;
		}
		if(activeChar.isActionsDisabled())
		{
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.isInStoreMode())
		{
			activeChar.sendPacket(Msg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
			return;
		}
		if(activeChar.isInTrade())
		{
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.isFishing())
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_DO_THAT_WHILE_FISHING);
			return;
		}
		if(!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && activeChar.getKarma() > 0 && !activeChar.isGM())
		{
			activeChar.sendActionFailed();
			return;
		}
		NpcInstance merchant = activeChar.getLastNpc();
		if(list1.getListId() >= 0 && !activeChar.isGM() && !NpcInstance.canBypassCheck(activeChar, merchant))
		{
			activeChar.setMultisell(null);
			return;
		}
		MultiSellEntry entry = null;
		for(MultiSellEntry listEntry : list1.getEntries())
		{
			if(listEntry.getEntryId() != _entryId)
				continue;
			entry = listEntry;
			break;
		}
		if(entry == null)
		{
			return;
		}
		boolean keepenchant = list1.isKeepEnchant();
		boolean notax = list1.isNoTax();
		PcInventory inventory = activeChar.getInventory();
		Castle castle = merchant != null ? merchant.getCastle(activeChar) : null;
		inventory.writeLock();
		try
		{
			long tax = SafeMath.mulAndCheck(entry.getTax(), _amount);
			long slots = 0;
			long weight = 0;
			for(MultiSellIngredient i : entry.getProduction())
			{
				if(i.getItemId() <= 0)
					continue;
				ItemTemplate item = ItemHolder.getInstance().getTemplate(i.getItemId());
				weight = SafeMath.addAndCheck(weight, SafeMath.mulAndCheck(SafeMath.mulAndCheck(i.getItemCount(), _amount), (long) item.getWeight()));
				if(item.isStackable())
				{
					if(inventory.getItemByItemId(i.getItemId()) != null)
						continue;
					++slots;
					continue;
				}
				slots = SafeMath.addAndCheck(slots, _amount);
			}
			if(!inventory.validateWeight(weight))
			{
				activeChar.sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
				activeChar.sendActionFailed();
				return;
			}
			if(!inventory.validateCapacity(slots))
			{
				activeChar.sendPacket(Msg.YOUR_INVENTORY_IS_FULL);
				activeChar.sendActionFailed();
				return;
			}
			if(entry.getIngredients().size() == 0)
			{
				activeChar.sendActionFailed();
				activeChar.setMultisell(null);
				return;
			}
			long totalPrice = 0;
			ArrayList<ItemData> items = new ArrayList<>();
			for(MultiSellIngredient ingridient : entry.getIngredients())
			{
				long totalAmount;
				int ingridientItemId = ingridient.getItemId();
				long ingridientItemCount = ingridient.getItemCount();
				int ingridientEnchant = ingridient.getItemEnchant();
				long l = totalAmount = !ingridient.getMantainIngredient() ? SafeMath.mulAndCheck(ingridientItemCount, _amount) : ingridientItemCount;
				if(ingridientItemId == -200)
				{
					if(activeChar.getClan() == null)
					{
						activeChar.sendPacket(Msg.YOU_ARE_NOT_A_CLAN_MEMBER);
						return;
					}
					if((long) activeChar.getClan().getReputationScore() < totalAmount)
					{
						activeChar.sendPacket(Msg.THE_CLAN_REPUTATION_SCORE_IS_TOO_LOW);
						return;
					}
					if(activeChar.getClan().getLeaderId() != activeChar.getObjectId())
					{
						activeChar.sendPacket(new SystemMessage(9).addString(activeChar.getName()));
						return;
					}
					if(!ingridient.getMantainIngredient())
					{
						items.add(new ItemData(-200, totalAmount, null));
					}
				}
				else if(ingridientItemId == -100)
				{
					if((long) activeChar.getPcBangPoints() < totalAmount)
					{
						activeChar.sendPacket(Msg.YOU_ARE_SHORT_OF_ACCUMULATED_POINTS);
						return;
					}
					if(!ingridient.getMantainIngredient())
					{
						items.add(new ItemData(-100, totalAmount, null));
					}
				}
				else
				{
					ItemTemplate template = ItemHolder.getInstance().getTemplate(ingridientItemId);
					if(!template.isStackable())
					{
						int i = 0;
						while((long) i < ingridientItemCount * _amount)
						{
							ItemInstance itemToTake;
							List<ItemInstance> list = inventory.getItemsByItemId(ingridientItemId);
							if(keepenchant)
							{
								itemToTake = null;
								for(ItemInstance item : list)
								{
									ItemData itmd = new ItemData(item.getItemId(), item.getCount(), item);
									if(item.getEnchantLevel() != ingridientEnchant && item.getTemplate().isEquipment() || items.contains(itmd) || !item.canBeExchanged(activeChar))
										continue;
									itemToTake = item;
									break;
								}
								if(itemToTake == null)
								{
									activeChar.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
									return;
								}
								if(!ingridient.getMantainIngredient())
								{
									items.add(new ItemData(itemToTake.getItemId(), 1, itemToTake));
								}
							}
							else
							{
								itemToTake = null;
								for(ItemInstance item : list)
								{
									if(!(items.contains(new ItemData(item.getItemId(), item.getCount(), item)) || itemToTake != null && item.getEnchantLevel() >= itemToTake.getEnchantLevel() || item.isShadowItem() || item.isTemporalItem() || item.isAugmented() && !Config.ALT_ALLOW_DROP_AUGMENTED || !ItemFunctions.checkIfCanDiscard(activeChar, item) || (itemToTake = item).getEnchantLevel() != 0))
										break;
								}
								if(itemToTake == null)
								{
									activeChar.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
									return;
								}
								if(!ingridient.getMantainIngredient())
								{
									items.add(new ItemData(itemToTake.getItemId(), 1, itemToTake));
								}
							}
							++i;
						}
					}
					else
					{
						if(ingridientItemId == 57)
						{
							totalPrice = SafeMath.addAndCheck(totalPrice, SafeMath.mulAndCheck(ingridientItemCount, _amount));
						}
						ItemInstance item;
						if((item = inventory.getItemByItemId(ingridientItemId)) == null || item.getCount() < totalAmount)
						{
							activeChar.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
							return;
						}
						if(!ingridient.getMantainIngredient())
						{
							items.add(new ItemData(item.getItemId(), totalAmount, item));
						}
					}
				}
				if(activeChar.getAdena() >= totalPrice)
					continue;
				activeChar.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
				return;
			}
			int enchantLevel = 0;
			ItemAttributes attributes = null;
			int variation_stat1 = 0;
			int variation_stat2 = 0;
			for(ItemData id : items)
			{
				long count = id.getCount();
				if(count <= 0)
					continue;
				if(id.getId() == -200)
				{
					activeChar.getClan().incReputation((int) -count, false, "MultiSell");
					activeChar.sendPacket(new SystemMessage(1787).addNumber(count));
					continue;
				}
				if(id.getId() == -100)
				{
					activeChar.reducePcBangPoints((int) count);
					continue;
				}
				ItemInstance it = id.getItem();
				if(inventory.destroyItem(id.getItem(), count))
				{
					if(keepenchant && id.getItem().canBeEnchanted(true))
					{
						enchantLevel = id.getItem().getEnchantLevel();
						attributes = id.getItem().getAttributes();
						variation_stat1 = id.getItem().getVariationStat1();
						variation_stat2 = id.getItem().getVariationStat2();
					}
					activeChar.sendPacket(SystemMessage2.removeItems(id.getId(), count));
					Log.LogItem(activeChar, Log.ItemLog.MultiSellIngredient, it, count, 0, _listId);
					continue;
				}
				return;
			}
			if(tax > 0 && !notax && castle != null)
			{
				activeChar.sendMessage(new CustomMessage("trade.HavePaidTax", activeChar).addNumber(tax));
				if(merchant != null && merchant.getReflection() == ReflectionManager.DEFAULT)
				{
					castle.addToTreasury(tax, true, false);
				}
			}
			for(MultiSellIngredient in : entry.getProduction())
			{
				if(in.getItemId() <= 0)
				{
					if(in.getItemId() == -200)
					{
						activeChar.getClan().incReputation((int) (in.getItemCount() * _amount), false, "MultiSell");
						activeChar.sendPacket(new SystemMessage(1781).addNumber(in.getItemCount() * _amount));
						continue;
					}
					if(in.getItemId() != -100)
						continue;
					activeChar.addPcBangPoints((int) (in.getItemCount() * _amount), false);
					continue;
				}
				if(ItemHolder.getInstance().getTemplate(in.getItemId()).isStackable())
				{
					long total = SafeMath.mulAndLimit(in.getItemCount(), _amount);
					inventory.addItem(in.getItemId(), total);
					activeChar.sendPacket(SystemMessage2.obtainItems(in.getItemId(), total, 0));
					Log.LogItem(activeChar, Log.ItemLog.MultiSellProduct, in.getItemId(), total, 0, _listId);
					continue;
				}
				int i = 0;
				while((long) i < _amount)
				{
					int j = 0;
					while((long) j < in.getItemCount())
					{
						ItemInstance product = ItemFunctions.createItem(in.getItemId());
						if(keepenchant)
						{
							if(product.canBeEnchanted(true))
							{
								product.setEnchantLevel(enchantLevel);
								if(attributes != null)
								{
									product.setAttributes(attributes.clone());
								}
								if(variation_stat1 != 0 || variation_stat2 != 0)
								{
									product.setVariationStat1(variation_stat1);
									product.setVariationStat2(variation_stat2);
								}
							}
						}
						else
						{
							product.setEnchantLevel(in.getItemEnchant());
							product.setAttributes(in.getItemAttributes().clone());
						}
						inventory.addItem(product);
						activeChar.sendPacket(SystemMessage2.obtainItems(product));
						Log.LogItem(activeChar, Log.ItemLog.MultiSellProduct, product, product.getCount(), 0, _listId);
						++j;
					}
					++i;
				}
			}
		}
		catch(ArithmeticException e)
		{
			sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
			return;
		}
		finally
		{
			inventory.writeUnlock();
		}
		activeChar.sendChanges();
		if(!list1.isShowAll())
		{
			MultiSellHolder.getInstance().SeparateAndSend(list1, activeChar, castle == null ? 0.0 : castle.getTaxRate());
		}
	}
	
	private class ItemData
	{
		private final int _id;
		private final long _count;
		private final ItemInstance _item;
		
		public ItemData(int id, long count, ItemInstance item)
		{
			_id = id;
			_count = count;
			_item = item;
		}
		
		public int getId()
		{
			return _id;
		}
		
		public long getCount()
		{
			return _count;
		}
		
		public ItemInstance getItem()
		{
			return _item;
		}
		
		@Override
		public boolean equals(Object obj)
		{
			if(!(obj instanceof ItemData))
			{
				return false;
			}
			ItemData i = (ItemData) obj;
			return _id == i._id && _count == i._count && _item == i._item;
		}
	}
}