package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Player;
import l2.gameserver.model.entity.SevenSigns;

public class ShowMiniMap extends L2GameServerPacket
{
	private final int _mapId;
	private final int _period;
	
	public ShowMiniMap(Player player, int mapId)
	{
		_mapId = mapId;
		_period = SevenSigns.getInstance().getCurrentPeriod();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(157);
		writeD(_mapId);
		writeC(_period);
	}
}