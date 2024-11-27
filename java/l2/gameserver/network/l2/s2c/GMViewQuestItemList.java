package l2.gameserver.network.l2.s2c;

import l2.gameserver.Config;
import l2.gameserver.model.Player;
import l2.gameserver.model.items.ItemInstance;

public class GMViewQuestItemList extends L2GameServerPacket
{
	private final int _size;
	private final ItemInstance[] _items;
	private final int _limit;
	private final String _name;
	
	public GMViewQuestItemList(Player player, ItemInstance[] items, int size)
	{
		_items = items;
		_size = size;
		_name = player.getName();
		_limit = Config.QUEST_INVENTORY_MAXIMUM;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(147);
		writeS(_name);
		writeD(_limit);
		writeH(1);
		writeH(_size);
		for(ItemInstance temp : _items)
		{
			if(!temp.getTemplate().isQuest())
				continue;
			writeItemInfo(temp);
		}
	}
}