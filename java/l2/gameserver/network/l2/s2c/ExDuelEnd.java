package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.entity.events.impl.DuelEvent;

public class ExDuelEnd extends L2GameServerPacket
{
	private final int _duelType;
	
	public ExDuelEnd(DuelEvent e)
	{
		_duelType = e.getDuelType();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeEx(78);
		writeD(_duelType);
	}
}