package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Player;
import l2.gameserver.model.items.ItemInfo;
import l2.gameserver.model.items.ItemInstance;

import java.util.ArrayList;
import java.util.List;

public class TradeStart extends L2GameServerPacket
{
	private final List<ItemInfo> _tradelist = new ArrayList<>();
	private final int targetId;
	
	public TradeStart(Player player, Player target)
	{
		ItemInstance[] items = player.getInventory().getItems();
		targetId = target.getObjectId();
		for(ItemInstance item : items)
		{
			if(!item.canBeTraded(player))
				continue;
			_tradelist.add(new ItemInfo(item));
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(30);
		writeD(targetId);
		writeH(_tradelist.size());
		for(ItemInfo item : _tradelist)
		{
			writeH(item.getItem().getType1());
			writeD(item.getObjectId());
			writeD(item.getItemId());
			writeD((int) item.getCount());
			writeH(item.getItem().getType2ForPackets());
			writeH(0);
			writeD(item.getItem().getBodyPart());
			writeH(item.getEnchantLevel());
			writeH(0);
			writeH(0);
		}
	}
}