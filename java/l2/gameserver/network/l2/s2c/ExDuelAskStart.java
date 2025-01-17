package l2.gameserver.network.l2.s2c;

public class ExDuelAskStart extends L2GameServerPacket
{
	String _requestor;
	int _isPartyDuel;
	
	public ExDuelAskStart(String requestor, int isPartyDuel)
	{
		_requestor = requestor;
		_isPartyDuel = isPartyDuel;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeEx(75);
		writeS(_requestor);
		writeD(_isPartyDuel);
	}
}