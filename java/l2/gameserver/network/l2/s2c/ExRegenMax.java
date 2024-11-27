package l2.gameserver.network.l2.s2c;

public class ExRegenMax extends L2GameServerPacket
{
	public static final int POTION_HEALING_GREATER = 16457;
	public static final int POTION_HEALING_MEDIUM = 16440;
	public static final int POTION_HEALING_LESSER = 16416;
	private final double _max;
	private final int _count;
	private final int _time;
	
	public ExRegenMax(double max, int count, int time)
	{
		_max = max * 0.66;
		_count = count;
		_time = time;
	}
	
	@Override
	protected void writeImpl()
	{
		writeEx(0x01);
		writeD(1);
		writeD(_count);
		writeD(_time);
		writeF(_max);
	}
}