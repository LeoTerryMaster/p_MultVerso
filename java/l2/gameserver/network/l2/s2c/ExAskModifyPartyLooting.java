package l2.gameserver.network.l2.s2c;

public class ExAskModifyPartyLooting extends L2GameServerPacket
{
	private final String _requestor;
	private final int _mode;
	
	public ExAskModifyPartyLooting(String name, int mode)
	{
		_requestor = name;
		_mode = mode;
	}
	
	@Override
	protected void writeImpl()
	{
		writeEx(191);
		writeS(_requestor);
		writeD(_mode);
	}
}