package l2.gameserver.network.l2.s2c;

public class Ex2ndPasswordAck extends L2GameServerPacket
{
	private final Ex2ndPasswordAckResult _result;
	private final int _arg3;
	
	public Ex2ndPasswordAck(Ex2ndPasswordAckResult result)
	{
		_result = result;
		_arg3 = 0;
	}
	
	public Ex2ndPasswordAck(Ex2ndPasswordAckResult result, int arg)
	{
		_result = result;
		_arg3 = arg;
	}
	
	@Override
	protected void writeImpl()
	{
		writeEx(231);
		writeC(_result.getArg0());
		writeD(_result.getArg1());
		writeD(_arg3);
	}
	
	public enum Ex2ndPasswordAckResult
	{
		SUCCESS_CREATE(0, 0),
		SUCCESS_VERIFY(2, 0),
		FAIL_CREATE(0, 1),
		FAIL_VERIFY(2, 1),
		BLOCK_HOMEPAGE(0, 2),
		ERROR(3, 0);
		
		private final int _arg0;
		private final int _arg1;
		
		Ex2ndPasswordAckResult(int arg0, int arg1)
		{
			_arg0 = arg0;
			_arg1 = arg1;
		}
		
		public int getArg0()
		{
			return _arg0;
		}
		
		public int getArg1()
		{
			return _arg1;
		}
	}
}