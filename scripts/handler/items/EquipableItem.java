package handler.items;

import gnu.trove.TIntHashSet;
import l2.gameserver.ai.NextAction;
import l2.gameserver.data.xml.holder.ItemHolder;
import l2.gameserver.model.Playable;
import l2.gameserver.model.Player;
import l2.gameserver.model.Request;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.network.l2.s2c.L2GameServerPacket;
import l2.gameserver.network.l2.s2c.SendTradeDone;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.templates.item.ItemTemplate;
import l2.gameserver.templates.item.WeaponTemplate;
import l2.gameserver.utils.ItemFunctions;

public class EquipableItem extends ScriptItemHandler
{
	private final int[] _itemIds;
	
	public EquipableItem()
	{
		TIntHashSet set = new TIntHashSet();
		for(ItemTemplate template : ItemHolder.getInstance().getAllTemplates())
		{
			if(template == null || !template.isEquipable())
				continue;
			set.add(template.getItemId());
		}
		_itemIds = set.toArray();
	}
	
	@Override
	public boolean useItem(Playable playable, ItemInstance item, boolean ctrl)
	{
		if(!playable.isPlayer())
		{
			return false;
		}
		Player player = playable.getPlayer();
		if(player.isAttackingNow() || player.isCastingNow())
		{
			player.sendPacket(new SystemMessage(104));
			if(player.isProcessingRequest())
			{
				Request request = player.getRequest();
				if(player.isInTrade())
				{
					Player parthner = request.getOtherPlayer(player);
					player.sendPacket(SendTradeDone.FAIL);
					parthner.sendPacket(SendTradeDone.FAIL);
				}
				request.cancel();
			}
			player.getAI().setNextAction(NextAction.EQUIP, item, null, ctrl, false);
			return false;
		}
		if(player.isStunned() || player.isSleeping() || player.isParalyzed() || player.isAlikeDead() || player.isFakeDeath())
		{
			player.sendPacket(new SystemMessage(113).addItemName(item.getItemId()));
			return false;
		}
		int bodyPart = item.getBodyPart();
		if((bodyPart == 16384 || bodyPart == 256 || bodyPart == 128) && (player.isMounted() || player.isCursedWeaponEquipped() || player.getActiveWeaponFlagAttachment() != null || player.isWeaponEquipBlocked() && item.getItemType() != WeaponTemplate.WeaponType.NONE))
		{
			player.sendPacket(new SystemMessage(113).addItemName(item.getItemId()));
			return false;
		}
		if(item.isCursed())
		{
			player.sendPacket(new SystemMessage(113).addItemName(item.getItemId()));
			return false;
		}
		if(item.isEquipped())
		{
			ItemInstance weapon = player.getActiveWeaponInstance();
			if(item == weapon)
			{
				player.abortAttack(true, true);
				player.abortCast(true, true);
			}
			player.sendDisarmMessage(item);
			player.getInventory().unEquipItem(item);
			return false;
		}
		L2GameServerPacket p = ItemFunctions.checkIfCanEquip(player, item);
		if(p != null)
		{
			player.sendPacket(p);
			return false;
		}
		player.getInventory().equipItem(item);
		if(!item.isEquipped())
		{
			player.sendActionFailed();
			return false;
		}
		SystemMessage sm;
		if(item.getEnchantLevel() > 0)
		{
			sm = new SystemMessage(368);
			sm.addNumber(item.getEnchantLevel());
			sm.addItemName(item.getItemId());
		}
		else
		{
			sm = new SystemMessage(49).addItemName(item.getItemId());
		}
		player.sendPacket(sm);
		return true;
	}
	
	@Override
	public int[] getItemIds()
	{
		return _itemIds;
	}
}