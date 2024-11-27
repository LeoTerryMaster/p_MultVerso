package l2.gameserver.network.l2.s2c;

public class Ex2ndPasswordVerify extends L2GameServerPacket
{
	private final Ex2ndPasswordVerifyResult _result;
	private final int _arg;
	
	public Ex2ndPasswordVerify(Ex2ndPasswordVerifyResult result)
	{
		_result = result;
		_arg = 0;
	}
	
	public Ex2ndPasswordVerify(Ex2ndPasswordVerifyResult result, int count)
	{
		_result = result;
		_arg = count;
	}
	
	@Override
	protected void writeImpl()
	{
		writeEx(230);
		writeD(_result.getVal());
		writeD(_arg);
	}
	
	public enum Ex2ndPasswordVerifyResult
	{
		SUCCESS(0),
		FAILED(1),
		BLOCK_HOMEPAGE(2),
		ERROR(3);
		
		private final int _val;
		
		Ex2ndPasswordVerifyResult(int arg)
		{
			_val = arg;
		}
		
		public int getVal()
		{
			return _val;
		}
	}
}