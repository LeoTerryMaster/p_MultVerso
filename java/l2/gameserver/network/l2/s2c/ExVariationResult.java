package l2.gameserver.network.l2.s2c;

public class ExVariationResult extends L2GameServerPacket
{
	public static final ExVariationResult FAIL_PACKET = new ExVariationResult(0, 0, 0);
	private final int _stat1;
	private final int _stat2;
	private final int _result;
	
	public ExVariationResult(int unk1, int unk2, int unk3)
	{
		_stat1 = unk1;
		_stat2 = unk2;
		_result = unk3;
	}
	
	@Override
	protected void writeImpl()
	{
		writeEx(85);
		writeD(_stat1);
		writeD(_stat2);
		writeD(_result);
	}
}