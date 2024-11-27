package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Player;
import l2.gameserver.model.items.ItemInstance;

public class GMViewItemList extends L2GameServerPacket
{
	private final int _size;
	private final ItemInstance[] _items;
	private final int _limit;
	private final String _name;
	
	public GMViewItemList(Player cha, ItemInstance[] items, int size)
	{
		_size = size;
		_items = items;
		_name = cha.getName();
		_limit = cha.getInventoryLimit();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(148);
		writeS(_name);
		writeD(_limit);
		writeH(1);
		writeH(_items.length);
		for(ItemInstance temp : _items)
		{
			writeH(temp.getTemplate().getType1());
			writeD(temp.getObjectId());
			writeD(temp.getItemId());
			writeD((int) temp.getCount());
			writeH(temp.getTemplate().getType2ForPackets());
			writeH(temp.getBlessed());
			writeH(temp.isEquipped() ? 1 : 0);
			writeD(temp.getBodyPart());
			writeH(temp.getEnchantLevel());
			writeH(temp.getTemplate().getType2());
			writeH(temp.getVariationStat1());
			writeH(temp.getVariationStat2());
			writeD(temp.getDuration());
		}
	}
}