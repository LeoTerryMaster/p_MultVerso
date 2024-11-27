package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.entity.events.impl.DuelEvent;

public class ExDuelStart extends L2GameServerPacket
{
	private final int _duelType;
	
	public ExDuelStart(DuelEvent e)
	{
		_duelType = e.getDuelType();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeEx(77);
		writeD(_duelType);
	}
}