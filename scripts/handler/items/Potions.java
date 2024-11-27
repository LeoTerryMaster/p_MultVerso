package handler.items;

import l2.gameserver.model.Player;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.network.l2.s2c.MagicSkillUse;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.tables.SkillTable;

public class Potions extends SimpleItemHandler
{
	private static final int[] ITEM_IDS = {7906, 7907, 7908, 7909, 7910, 7911};
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
	
	@Override
	protected boolean useItemImpl(Player player, ItemInstance item, boolean ctrl)
	{
		int itemId = item.getItemId();
		if(player.isOlyParticipant())
		{
			player.sendPacket(new SystemMessage(113).addItemName(itemId));
			return false;
		}
		if(!useItem(player, item, 1))
		{
			return false;
		}
		switch(itemId)
		{
			case 7906:
			{
				player.broadcastPacket(new MagicSkillUse(player, player, 2248, 1, 0, 0));
				player.altOnMagicUseTimer(player, SkillTable.getInstance().getInfo(2248, 1));
				break;
			}
			case 7907:
			{
				player.broadcastPacket(new MagicSkillUse(player, player, 2249, 1, 0, 0));
				player.altOnMagicUseTimer(player, SkillTable.getInstance().getInfo(2249, 1));
				break;
			}
			case 7908:
			{
				player.broadcastPacket(new MagicSkillUse(player, player, 2250, 1, 0, 0));
				player.altOnMagicUseTimer(player, SkillTable.getInstance().getInfo(2250, 1));
				break;
			}
			case 7909:
			{
				player.broadcastPacket(new MagicSkillUse(player, player, 2251, 1, 0, 0));
				player.altOnMagicUseTimer(player, SkillTable.getInstance().getInfo(2251, 1));
				break;
			}
			case 7910:
			{
				player.broadcastPacket(new MagicSkillUse(player, player, 2252, 1, 0, 0));
				player.altOnMagicUseTimer(player, SkillTable.getInstance().getInfo(2252, 1));
				break;
			}
			case 7911:
			{
				player.broadcastPacket(new MagicSkillUse(player, player, 2253, 1, 0, 0));
				player.altOnMagicUseTimer(player, SkillTable.getInstance().getInfo(2253, 1));
				break;
			}
			default:
			{
				return false;
			}
		}
		return true;
	}
}