package l2.gameserver.network.l2.s2c;

public class ExChangeClientEffectInfo extends L2GameServerPacket
{
	private final int _state;
	
	public ExChangeClientEffectInfo(int state)
	{
		_state = state;
	}
	
	@Override
	protected void writeImpl()
	{
		writeEx(193);
		writeD(0);
		writeD(_state);
	}
}