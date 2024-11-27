package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.entity.events.impl.DuelEvent;

public class ExDuelReady extends L2GameServerPacket
{
	private final int _duelType;
	
	public ExDuelReady(DuelEvent event)
	{
		_duelType = event.getDuelType();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeEx(76);
		writeD(_duelType);
	}
}