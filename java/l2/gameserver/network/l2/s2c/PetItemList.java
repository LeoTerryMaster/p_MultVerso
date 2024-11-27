package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.instances.PetInstance;
import l2.gameserver.model.items.ItemInstance;

public class PetItemList extends L2GameServerPacket
{
	private final ItemInstance[] items;
	
	public PetItemList(PetInstance cha)
	{
		items = cha.getInventory().getItems();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(178);
		writeH(items.length);
		for(ItemInstance item : items)
		{
			writeH(item.getTemplate().getType1());
			writeD(item.getObjectId());
			writeD(item.getItemId());
			writeD((int) item.getCount());
			writeH(item.getTemplate().getType2ForPackets());
			writeH(item.getBlessed());
			writeH(item.isEquipped() ? 1 : 0);
			writeD(item.getTemplate().getBodyPart());
			writeH(item.getEnchantLevel());
			writeH(item.getDamaged());
		}
	}
}