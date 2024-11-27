package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Player;
import l2.gameserver.model.items.ManufactureItem;

import java.util.List;

public class RecipeShopSellList extends L2GameServerPacket
{
	private final int objId;
	private final int curMp;
	private final int maxMp;
	private final long adena;
	private final List<ManufactureItem> createList;
	
	public RecipeShopSellList(Player buyer, Player manufacturer)
	{
		objId = manufacturer.getObjectId();
		curMp = (int) manufacturer.getCurrentMp();
		maxMp = manufacturer.getMaxMp();
		adena = buyer.getAdena();
		createList = manufacturer.getCreateList();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(217);
		writeD(objId);
		writeD(curMp);
		writeD(maxMp);
		writeD((int) adena);
		writeD(createList.size());
		for(ManufactureItem mi : createList)
		{
			writeD(mi.getRecipeId());
			writeD(0);
			writeD((int) mi.getCost());
		}
	}
}