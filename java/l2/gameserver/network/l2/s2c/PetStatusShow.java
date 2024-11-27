package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Summon;

public class PetStatusShow extends L2GameServerPacket
{
	private final int _summonType;
	
	public PetStatusShow(Summon summon)
	{
		_summonType = summon.getSummonType();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(176);
		writeD(_summonType);
	}
}