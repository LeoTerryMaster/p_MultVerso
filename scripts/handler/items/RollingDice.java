package handler.items;

import l2.commons.util.Rnd;
import l2.gameserver.cache.Msg;
import l2.gameserver.model.Playable;
import l2.gameserver.model.Player;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.network.l2.s2c.Dice;
import l2.gameserver.network.l2.s2c.SystemMessage;

public class RollingDice extends ScriptItemHandler
{
	private static final int[] _itemIds = {4625, 4626, 4627, 4628};
	
	@Override
	public boolean useItem(Playable playable, ItemInstance item, boolean ctrl)
	{
		if(playable == null || !playable.isPlayer())
		{
			return false;
		}
		Player player = (Player) playable;
		int itemId = item.getItemId();
		if(player.isOlyParticipant())
		{
			player.sendPacket(Msg.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
			return false;
		}
		if(player.isSitting())
		{
			player.sendPacket(Msg.YOU_CANNOT_MOVE_WHILE_SITTING);
			return false;
		}
		int number = Rnd.get(1, 6);
		if(number == 0)
		{
			player.sendPacket(Msg.YOU_MAY_NOT_THROW_THE_DICE_AT_THIS_TIMETRY_AGAIN_LATER);
			return false;
		}
		player.broadcastPacket(new Dice(player.getObjectId(), itemId, number, player.getX() - 30, player.getY() - 30, player.getZ()), new SystemMessage(834).addString(player.getName()).addNumber(number));
		return true;
	}
	
	@Override
	public final int[] getItemIds()
	{
		return _itemIds;
	}
}