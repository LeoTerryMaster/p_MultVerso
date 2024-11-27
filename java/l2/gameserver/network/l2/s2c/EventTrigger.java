package l2.gameserver.network.l2.s2c;

public class EventTrigger extends L2GameServerPacket
{
	private final int _trapId;
	private final boolean _active;
	
	public EventTrigger(int trapId, boolean active)
	{
		_trapId = trapId;
		_active = active;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(207);
		writeD(_trapId);
		writeC(_active ? 1 : 0);
	}
}