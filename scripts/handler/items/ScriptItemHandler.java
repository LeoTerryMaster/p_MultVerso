package handler.items;

import l2.gameserver.data.xml.holder.ItemHolder;
import l2.gameserver.handler.items.IItemHandler;
import l2.gameserver.handler.items.ItemHandler;
import l2.gameserver.model.Playable;
import l2.gameserver.model.Player;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.scripts.ScriptFile;
import l2.gameserver.templates.item.ItemTemplate;
import l2.gameserver.utils.Location;
import l2.gameserver.utils.Log;

public abstract class ScriptItemHandler implements ScriptFile, IItemHandler
{
	@Override
	public void dropItem(Player player, ItemInstance item, long count, Location loc)
	{
		if(item.isEquipped())
		{
			player.getInventory().unEquipItem(item);
			player.sendUserInfo(true);
		}
		if((item = player.getInventory().removeItemByObjectId(item.getObjectId(), count)) == null)
		{
			player.sendActionFailed();
			return;
		}
		Log.LogItem(player, Log.ItemLog.Drop, item);
		item.dropToTheGround(player, loc);
		player.disableDrop(1000);
		player.sendChanges();
	}
	
	@Override
	public boolean pickupItem(Playable playable, ItemInstance item)
	{
		return true;
	}
	
	protected boolean isShotsHandler()
	{
		return false;
	}
	
	@Override
	public void onLoad()
	{
		ItemHandler.getInstance().registerItemHandler(this);
		if(isShotsHandler())
		{
			int[] arrn = getItemIds();
			int n = arrn.length;
			for(int i = 0;i < n;++i)
			{
				Integer itemId = arrn[i];
				ItemTemplate itemTemplate = ItemHolder.getInstance().getTemplate(itemId.intValue());
				if(itemTemplate == null)
					continue;
				itemTemplate.setIsShotItem(true);
			}
		}
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