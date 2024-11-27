package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Manor;

import java.util.Iterator;
import java.util.List;

public class ExShowManorDefaultInfo extends L2GameServerPacket
{
	private final List<Integer> _crops = Manor.getInstance().getAllCrops();
	
	@Override
	protected void writeImpl()
	{
		writeEx(30);
		writeC(0);
		writeD(_crops.size());
		Iterator<Integer> iterator = _crops.iterator();
		while(iterator.hasNext())
		{
			int cropId = iterator.next();
			writeD(cropId);
			writeD(Manor.getInstance().getSeedLevelByCrop(cropId));
			writeD(Manor.getInstance().getSeedBasicPriceByCrop(cropId));
			writeD(Manor.getInstance().getCropBasicPrice(cropId));
			writeC(1);
			writeD(Manor.getInstance().getRewardItem(cropId, 1));
			writeC(1);
			writeD(Manor.getInstance().getRewardItem(cropId, 2));
		}
	}
}