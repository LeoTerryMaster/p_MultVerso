package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Creature;

public class ChangeMoveType extends L2GameServerPacket
{
	public static int WALK;
	public static int RUN = 1;
	private final int _chaId;
	private final boolean _running;
	
	public ChangeMoveType(Creature cha)
	{
		_chaId = cha.getObjectId();
		_running = cha.isRunning();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(46);
		writeD(_chaId);
		writeD(_running ? 1 : 0);
		writeD(0);
	}
}