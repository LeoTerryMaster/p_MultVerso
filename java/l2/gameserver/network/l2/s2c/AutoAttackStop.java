package l2.gameserver.network.l2.s2c;

public class AutoAttackStop extends L2GameServerPacket
{
	private final int _targetId;
	
	public AutoAttackStop(int targetId)
	{
		_targetId = targetId;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(44);
		writeD(_targetId);
	}
}