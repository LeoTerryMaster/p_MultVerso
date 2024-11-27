package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Player;

public class RecipeShopMsg extends L2GameServerPacket
{
	private final int _objectId;
	private final String _storeName;
	
	public RecipeShopMsg(Player player)
	{
		_objectId = player.getObjectId();
		_storeName = player.getManufactureName();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(219);
		writeD(_objectId);
		writeS(_storeName);
	}
}