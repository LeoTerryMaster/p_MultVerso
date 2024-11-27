package l2.gameserver.network.l2.s2c;

import l2.gameserver.data.xml.holder.ResidenceHolder;
import l2.gameserver.model.Player;
import l2.gameserver.model.entity.residence.Castle;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.templates.manor.CropProcure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SellListProcure extends L2GameServerPacket
{
	private final long _money;
	private final Map<ItemInstance, Long> _sellList = new HashMap<>();
	private final int _castle;
	private List<CropProcure> _procureList = new ArrayList<>();
	
	public SellListProcure(Player player, int castleId)
	{
		_money = player.getAdena();
		_castle = castleId;
		_procureList = ResidenceHolder.getInstance().getResidence(Castle.class, _castle).getCropProcure(0);
		for(CropProcure c : _procureList)
		{
			ItemInstance item = player.getInventory().getItemByItemId(c.getId());
			if(item == null || c.getAmount() <= 0)
				continue;
			_sellList.put(item, c.getAmount());
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(233);
		writeD((int) _money);
		writeD(0);
		writeH(_sellList.size());
		for(ItemInstance item : _sellList.keySet())
		{
			writeH(0);
			writeD(item.getObjectId());
			writeD(item.getItemId());
			writeD(_sellList.get(item).intValue());
			writeH(item.getTemplate().getType2ForPackets());
			writeH(0);
			writeD(0);
		}
	}
}