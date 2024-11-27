package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Summon;

public class SetSummonRemainTime extends L2GameServerPacket
{
	private final int _maxFed;
	private final int _curFed;
	
	public SetSummonRemainTime(Summon summon)
	{
		_curFed = summon.getCurrentFed();
		_maxFed = summon.getMaxFed();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(209);
		writeD(_maxFed);
		writeD(_curFed);
	}
}