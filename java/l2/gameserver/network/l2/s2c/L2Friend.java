package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Player;

public class L2Friend extends L2GameServerPacket
{
	private final boolean _add;
	private final boolean _online;
	private final String _name;
	private final int _object_id;
	
	public L2Friend(Player player, boolean add)
	{
		_add = add;
		_name = player.getName();
		_object_id = player.getObjectId();
		_online = true;
	}
	
	public L2Friend(String name, boolean add, boolean online, int object_id)
	{
		_name = name;
		_add = add;
		_object_id = object_id;
		_online = online;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(251);
		writeD(_add ? 1 : 3);
		writeD(0);
		writeS(_name);
		writeD(_online ? 1 : 0);
		writeD(_object_id);
	}
}