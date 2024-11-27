package l2.gameserver.network.l2.c2s;

class SuperCmdSummonCmd extends L2GameClientPacket
{
	private String _summonName;
	
	@Override
	protected void readImpl()
	{
		_summonName = readS();
	}
	
	@Override
	protected void runImpl()
	{
	}
}