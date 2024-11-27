package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Creature;

public class FinishRotating extends L2GameServerPacket
{
	private final int _charId;
	private final int _degree;
	private final int _speed;
	
	public FinishRotating(Creature player, int degree, int speed)
	{
		_charId = player.getObjectId();
		_degree = degree;
		_speed = speed;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(99);
		writeD(_charId);
		writeD(_degree);
		writeD(_speed);
		writeD(0);
	}
}