package l2.gameserver.network.l2.s2c;

public class ExAskJoinPartyRoom extends L2GameServerPacket
{
	private final String _charName;
	private final String _roomName;
	
	public ExAskJoinPartyRoom(String charName, String roomName)
	{
		_charName = charName;
		_roomName = roomName;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeEx(52);
		writeS(_charName);
	}
}