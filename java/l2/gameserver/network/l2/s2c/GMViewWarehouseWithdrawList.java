package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Player;
import l2.gameserver.model.items.ItemInstance;

public class GMViewWarehouseWithdrawList extends L2GameServerPacket
{
	private final ItemInstance[] _items;
	private final String _charName;
	private final long _charAdena;
	
	public GMViewWarehouseWithdrawList(Player cha)
	{
		_charName = cha.getName();
		_charAdena = cha.getAdena();
		_items = cha.getWarehouse().getItems();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(149);
		writeS(_charName);
		writeD((int) _charAdena);
		writeH(_items.length);
		for(ItemInstance temp : _items)
		{
			writeH(temp.getTemplate().getType1());
			writeD(temp.getObjectId());
			writeD(temp.getItemId());
			writeD((int) temp.getCount());
			writeH(temp.getTemplate().getType2ForPackets());
			writeH(temp.getBlessed());
			if(temp.getTemplate().getType1() < 4)
			{
				writeD(temp.getTemplate().getBodyPart());
				writeH(temp.getEnchantLevel());
				writeH(temp.getDamaged());
				writeH(0);
				writeD(temp.getVariationStat1());
				writeD(temp.getVariationStat2());
			}
			writeD(temp.getObjectId());
		}
	}
}