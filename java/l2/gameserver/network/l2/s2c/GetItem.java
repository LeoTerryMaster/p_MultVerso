package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.utils.Location;

public class GetItem extends L2GameServerPacket
{
	private final int _playerId;
	private final int _itemObjId;
	private final Location _loc;
	
	public GetItem(ItemInstance item, int playerId)
	{
		_itemObjId = item.getObjectId();
		_loc = item.getLoc();
		_playerId = playerId;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(13);
		writeD(_playerId);
		writeD(_itemObjId);
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z);
	}
}