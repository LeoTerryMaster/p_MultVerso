package l2.gameserver.network.l2.s2c;

public class StartPledgeWar extends L2GameServerPacket
{
	private final String _pledgeName;
	private final String _char;
	
	public StartPledgeWar(String pledge, String charName)
	{
		_pledgeName = pledge;
		_char = charName;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(101);
		writeS(_char);
		writeS(_pledgeName);
	}
}