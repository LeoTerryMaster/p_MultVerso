package l2.gameserver.network.l2.s2c;

public class ExChangeNpcState extends L2GameServerPacket
{
	private final int _objId;
	private final int _state;
	
	public ExChangeNpcState(int objId, int state)
	{
		_objId = objId;
		_state = state;
	}
	
	@Override
	protected void writeImpl()
	{
		writeEx(190);
		writeD(_objId);
		writeD(_state);
	}
}