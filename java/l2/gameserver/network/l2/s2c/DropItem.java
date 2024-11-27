package l2.gameserver.network.l2.s2c;

import l2.gameserver.Config;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.utils.Location;

public class DropItem extends L2GameServerPacket
{
	private final Location _loc;
	private final int _playerId;
	private final int item_obj_id;
	private final int item_id;
	private final int _stackable;
	private final long _count;
	
	public DropItem(ItemInstance item, int playerId)
	{
		_playerId = playerId;
		item_obj_id = item.getObjectId();
		item_id = item.getItemId();
		_loc = item.getLoc();
		_stackable = item.isStackable() ? 1 : 0;
		_count = item.getCount();
	}
	
	public DropItem(int dropperId, int itemObjId, int itemId, Location loc, boolean isStackable, int count)
	{
		_playerId = dropperId;
		item_obj_id = itemObjId;
		item_id = itemId;
		_loc = loc.clone();
		_stackable = isStackable ? 1 : 0;
		_count = count;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(12);
		writeD(_playerId);
		writeD(item_obj_id);
		writeD(item_id);
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z + Config.CLIENT_Z_SHIFT);
		writeD(_stackable);
		writeD((int) _count);
		writeD(1);
	}
}