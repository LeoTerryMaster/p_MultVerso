package l2.gameserver.network.l2.s2c;

public class PledgeReceiveUpdatePower extends L2GameServerPacket
{
	private final int _privs;
	
	public PledgeReceiveUpdatePower(int privs)
	{
		_privs = privs;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeEx(66);
		writeD(_privs);
	}
}