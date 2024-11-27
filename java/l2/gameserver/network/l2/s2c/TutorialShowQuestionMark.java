package l2.gameserver.network.l2.s2c;

public class TutorialShowQuestionMark extends L2GameServerPacket
{
	private final int _number;
	
	public TutorialShowQuestionMark(int number)
	{
		_number = number;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(161);
		writeD(_number);
	}
}