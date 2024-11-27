package l2.gameserver.network.l2.s2c;

public class AskJoinParty extends L2GameServerPacket
{
	private final String _requestorName;
	private final int _itemDistribution;
	
	public AskJoinParty(String requestorName, int itemDistribution)
	{
		_requestorName = requestorName;
		_itemDistribution = itemDistribution;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(57);
		writeS(_requestorName);
		writeD(_itemDistribution);
	}
}