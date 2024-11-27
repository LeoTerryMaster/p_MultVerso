package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Manor;
import l2.gameserver.model.Player;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.templates.manor.CropProcure;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ExShowSellCropList extends L2GameServerPacket
{
	private final Map<Integer, ItemInstance> _cropsItems;
	private final Map<Integer, CropProcure> _castleCrops;
	private int _manorId = 1;
	
	public ExShowSellCropList(Player player, int manorId, List<CropProcure> crops)
	{
		_manorId = manorId;
		_castleCrops = new TreeMap<>();
		_cropsItems = new TreeMap<>();
		List<Integer> allCrops = Manor.getInstance().getAllCrops();
		for(int cropId : allCrops)
		{
			ItemInstance item = player.getInventory().getItemByItemId(cropId);
			if(item == null)
				continue;
			_cropsItems.put(cropId, item);
		}
		for(CropProcure crop : crops)
		{
			if(!_cropsItems.containsKey(crop.getId()) || crop.getAmount() <= 0)
				continue;
			_castleCrops.put(crop.getId(), crop);
		}
	}
	
	@Override
	public void writeImpl()
	{
		writeEx(33);
		writeD(_manorId);
		writeD(_cropsItems.size());
		for(ItemInstance item : _cropsItems.values())
		{
			writeD(item.getObjectId());
			writeD(item.getItemId());
			writeD(Manor.getInstance().getSeedLevelByCrop(item.getItemId()));
			writeC(1);
			writeD(Manor.getInstance().getRewardItem(item.getItemId(), 1));
			writeC(1);
			writeD(Manor.getInstance().getRewardItem(item.getItemId(), 2));
			if(_castleCrops.containsKey(item.getItemId()))
			{
				CropProcure crop = _castleCrops.get(item.getItemId());
				writeD(_manorId);
				writeD((int) crop.getAmount());
				writeD((int) crop.getPrice());
				writeC(crop.getReward());
			}
			else
			{
				writeD(-1);
				writeD(0);
				writeD(0);
				writeC(0);
			}
			writeD((int) item.getCount());
		}
	}
}