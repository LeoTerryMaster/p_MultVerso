package l2.gameserver.network.l2.s2c;

import l2.gameserver.data.xml.holder.ResidenceHolder;
import l2.gameserver.model.Manor;
import l2.gameserver.model.entity.residence.Castle;
import l2.gameserver.templates.manor.SeedProduction;

import java.util.Iterator;
import java.util.List;

public class ExShowSeedSetting extends L2GameServerPacket
{
	private final int _manorId;
	private final int _count;
	private final long[] _seedData;
	
	public ExShowSeedSetting(int manorId)
	{
		_manorId = manorId;
		Castle c = ResidenceHolder.getInstance().getResidence(Castle.class, _manorId);
		List<Integer> seeds = Manor.getInstance().getSeedsForCastle(_manorId);
		_count = seeds.size();
		_seedData = new long[_count * 12];
		int i = 0;
		Iterator<Integer> iterator = seeds.iterator();
		while(iterator.hasNext())
		{
			int s = iterator.next();
			_seedData[i * 12 + 0] = s;
			_seedData[i * 12 + 1] = Manor.getInstance().getSeedLevel(s);
			_seedData[i * 12 + 2] = Manor.getInstance().getRewardItemBySeed(s, 1);
			_seedData[i * 12 + 3] = Manor.getInstance().getRewardItemBySeed(s, 2);
			_seedData[i * 12 + 4] = Manor.getInstance().getSeedSaleLimit(s);
			_seedData[i * 12 + 5] = Manor.getInstance().getSeedBuyPrice(s);
			_seedData[i * 12 + 6] = Manor.getInstance().getSeedBasicPrice(s) * 60 / 100;
			_seedData[i * 12 + 7] = Manor.getInstance().getSeedBasicPrice(s) * 10;
			SeedProduction seedPr = c.getSeed(s, 0);
			if(seedPr != null)
			{
				_seedData[i * 12 + 8] = seedPr.getStartProduce();
				_seedData[i * 12 + 9] = seedPr.getPrice();
			}
			else
			{
				_seedData[i * 12 + 8] = 0;
				_seedData[i * 12 + 9] = 0;
			}
			seedPr = c.getSeed(s, 1);
			if(seedPr != null)
			{
				_seedData[i * 12 + 10] = seedPr.getStartProduce();
				_seedData[i * 12 + 11] = seedPr.getPrice();
			}
			else
			{
				_seedData[i * 12 + 10] = 0;
				_seedData[i * 12 + 11] = 0;
			}
			++i;
		}
	}
	
	@Override
	public void writeImpl()
	{
		writeEx(31);
		writeD(_manorId);
		writeD(_count);
		for(int i = 0;i < _count;++i)
		{
			writeD((int) _seedData[i * 12 + 0]);
			writeD((int) _seedData[i * 12 + 1]);
			writeC(1);
			writeD((int) _seedData[i * 12 + 2]);
			writeC(1);
			writeD((int) _seedData[i * 12 + 3]);
			writeD((int) _seedData[i * 12 + 4]);
			writeD((int) _seedData[i * 12 + 5]);
			writeD((int) _seedData[i * 12 + 6]);
			writeD((int) _seedData[i * 12 + 7]);
			writeD((int) _seedData[i * 12 + 8]);
			writeD((int) _seedData[i * 12 + 9]);
			writeD((int) _seedData[i * 12 + 10]);
			writeD((int) _seedData[i * 12 + 11]);
		}
	}
}