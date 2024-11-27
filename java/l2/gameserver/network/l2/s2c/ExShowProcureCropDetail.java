package l2.gameserver.network.l2.s2c;

import l2.gameserver.data.xml.holder.ResidenceHolder;
import l2.gameserver.model.entity.residence.Castle;
import l2.gameserver.templates.manor.CropProcure;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ExShowProcureCropDetail extends L2GameServerPacket
{
	private final int _cropId;
	private final Map<Integer, CropProcure> _castleCrops;
	
	public ExShowProcureCropDetail(int cropId)
	{
		_cropId = cropId;
		_castleCrops = new TreeMap<>();
		List<Castle> castleList = ResidenceHolder.getInstance().getResidenceList(Castle.class);
		for(Castle c : castleList)
		{
			CropProcure cropItem = c.getCrop(_cropId, 0);
			if(cropItem == null || cropItem.getAmount() <= 0)
				continue;
			_castleCrops.put(c.getId(), cropItem);
		}
	}
	
	@Override
	public void writeImpl()
	{
		writeEx(34);
		writeD(_cropId);
		writeD(_castleCrops.size());
		Iterator<Integer> iterator = _castleCrops.keySet().iterator();
		while(iterator.hasNext())
		{
			int manorId = iterator.next();
			CropProcure crop = _castleCrops.get(manorId);
			writeD(manorId);
			writeD((int) crop.getAmount());
			writeD((int) crop.getPrice());
			writeC(crop.getReward());
		}
	}
}