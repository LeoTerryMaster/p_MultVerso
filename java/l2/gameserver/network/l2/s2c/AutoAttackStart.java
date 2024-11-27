package l2.gameserver.network.l2.s2c;

public class AutoAttackStart extends L2GameServerPacket
{
	private final int _targetId;
	
	public AutoAttackStart(int targetId)
	{
		_targetId = targetId;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(43);
		writeD(_targetId);
	}
}