package l2.gameserver.network.l2.s2c;

public class ExVariationCancelResult extends L2GameServerPacket
{
	public static final ExVariationCancelResult FAIL_PACKET = new ExVariationCancelResult(0);
	private final int _closeWindow = 1;
	private final int _unk1;
	
	public ExVariationCancelResult(int result)
	{
		_unk1 = result;
	}
	
	@Override
	protected void writeImpl()
	{
		writeEx(87);
		writeD(_closeWindow);
		writeD(_unk1);
	}
}