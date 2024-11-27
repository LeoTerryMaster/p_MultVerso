package l2.gameserver.network.l2.s2c;

public class ShowCalc extends L2GameServerPacket
{
	private final int _calculatorId;
	
	public ShowCalc(int calculatorId)
	{
		_calculatorId = calculatorId;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(220);
		writeD(_calculatorId);
	}
}