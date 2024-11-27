package handler.items;

import l2.gameserver.Config;
import l2.gameserver.cache.Msg;
import l2.gameserver.model.Playable;
import l2.gameserver.model.Player;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.model.reward.RewardData;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.tables.FishTable;
import l2.gameserver.utils.ItemFunctions;
import l2.gameserver.utils.Util;

import java.util.List;

public class FishItem extends ScriptItemHandler
{
	@Override
	public boolean useItem(Playable playable, ItemInstance item, boolean ctrl)
	{
		if(playable == null || !playable.isPlayer())
		{
			return false;
		}
		Player player = (Player) playable;
		if(player.getWeightPenalty() >= 3 || player.getInventory().getSize() > player.getInventoryLimit() - 10)
		{
			player.sendPacket(Msg.YOUR_INVENTORY_IS_FULL);
			return false;
		}
		if(!player.getInventory().destroyItem(item, 1))
		{
			player.sendActionFailed();
			return false;
		}
		int count = 0;
		List<RewardData> rewards = FishTable.getInstance().getFishReward(item.getItemId());
		for(RewardData d : rewards)
		{
			long roll = Util.rollDrop(d.getMinDrop(), d.getMaxDrop(), d.getChance() * Config.RATE_FISH_DROP_COUNT * Config.RATE_DROP_ITEMS * player.getRateItems(), false);
			if(roll <= 0)
				continue;
			ItemFunctions.addItem(player, d.getItemId(), roll, true);
			++count;
		}
		if(count == 0)
		{
			player.sendPacket(SystemMsg.THERE_WAS_NOTHING_FOUND_INSIDE);
		}
		return true;
	}
	
	@Override
	public int[] getItemIds()
	{
		return FishTable.getInstance().getFishIds();
	}
}