package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Manor;
import l2.gameserver.templates.manor.SeedProduction;

import java.util.List;

public class ExShowSeedInfo extends L2GameServerPacket
{
	private final List<SeedProduction> _seeds;
	private final int _manorId;
	
	public ExShowSeedInfo(int manorId, List<SeedProduction> seeds)
	{
		_manorId = manorId;
		_seeds = seeds;
	}
	
	@Override
	protected void writeImpl()
	{
		writeEx(28);
		writeC(0);
		writeD(_manorId);
		writeD(0);
		writeD(_seeds.size());
		for(SeedProduction seed : _seeds)
		{
			writeD(seed.getId());
			writeD((int) seed.getCanProduce());
			writeD((int) seed.getStartProduce());
			writeD((int) seed.getPrice());
			writeD(Manor.getInstance().getSeedLevel(seed.getId()));
			writeC(1);
			writeD(Manor.getInstance().getRewardItemBySeed(seed.getId(), 1));
			writeC(1);
			writeD(Manor.getInstance().getRewardItemBySeed(seed.getId(), 2));
		}
	}
}