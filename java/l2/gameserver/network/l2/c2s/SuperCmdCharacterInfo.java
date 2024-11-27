package l2.gameserver.network.l2.c2s;

class SuperCmdCharacterInfo extends L2GameClientPacket
{
	private String _characterName;
	
	@Override
	protected void readImpl()
	{
		_characterName = readS();
	}
	
	@Override
	protected void runImpl()
	{
	}
}