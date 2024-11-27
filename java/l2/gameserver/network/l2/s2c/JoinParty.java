package l2.gameserver.network.l2.s2c;

public class JoinParty extends L2GameServerPacket
{
	public static final L2GameServerPacket SUCCESS = new JoinParty(1);
	public static final L2GameServerPacket FAIL = new JoinParty(0);
	private final int _response;
	
	public JoinParty(int response)
	{
		_response = response;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(58);
		writeD(_response);
	}
}