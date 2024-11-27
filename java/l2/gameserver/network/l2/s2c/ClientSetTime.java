package l2.gameserver.network.l2.s2c;

import l2.gameserver.GameTimeController;

public class ClientSetTime extends L2GameServerPacket
{
	public static final L2GameServerPacket STATIC = new ClientSetTime();
	
	@Override
	protected final void writeImpl()
	{
		writeC(236);
		writeD(GameTimeController.getInstance().getGameTime());
		writeD(6);
	}
}