package l2.gameserver.network.l2.s2c;

import l2.gameserver.Config;
import l2.gameserver.data.xml.holder.ItemHolder;
import l2.gameserver.data.xml.holder.MultiSellHolder;
import l2.gameserver.model.base.MultiSellEntry;
import l2.gameserver.model.base.MultiSellIngredient;
import l2.gameserver.templates.item.ItemTemplate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MultiSellList extends L2GameServerPacket
{
	private final int _page;
	private final int _finished;
	private final int _listId;
	private final List<MultiSellEntry> _list;
	
	public MultiSellList(MultiSellHolder.MultiSellListContainer list, int page, int finished)
	{
		_list = list.getEntries();
		_listId = list.getListId();
		_page = page;
		_finished = finished;
	}
	
	private static List<MultiSellIngredient> fixIngredients(List<MultiSellIngredient> ingredients)
	{
		int needFix = 0;
		for(MultiSellIngredient ingredient : ingredients)
		{
			if(ingredient.getItemCount() <= Integer.MAX_VALUE)
				continue;
			++needFix;
		}
		if(needFix == 0)
		{
			return ingredients;
		}
		ArrayList<MultiSellIngredient> result = new ArrayList<>(ingredients.size() + needFix);
		Iterator<MultiSellIngredient> iterator = ingredients.iterator();
		while(iterator.hasNext())
		{
			MultiSellIngredient ingredient = iterator.next();
			ingredient = ingredient.clone();
			while(ingredient.getItemCount() > Integer.MAX_VALUE)
			{
				MultiSellIngredient temp = ingredient.clone();
				temp.setItemCount(2000000000);
				result.add(temp);
				ingredient.setItemCount(ingredient.getItemCount() - 2000000000);
			}
			if(ingredient.getItemCount() <= 0)
				continue;
			result.add(ingredient);
		}
		return result;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(208);
		writeD(_listId);
		writeD(_page);
		writeD(_finished);
		writeD(Config.MULTISELL_SIZE);
		writeD(_list.size());
		for(MultiSellEntry ent : _list)
		{
			List<MultiSellIngredient> ingredients = fixIngredients(ent.getIngredients());
			writeD(ent.getEntryId());
			writeD(0);
			writeD(0);
			writeC(1);
			writeH(ent.getProduction().size());
			writeH(ingredients.size());
			int itemId;
			for(MultiSellIngredient prod : ent.getProduction())
			{
				itemId = prod.getItemId();
				ItemTemplate template = itemId > 0 ? ItemHolder.getInstance().getTemplate(prod.getItemId()) : null;
				writeH(itemId);
				writeD(itemId > 0 ? template.getBodyPart() : 0);
				writeH(itemId > 0 ? template.getType2ForPackets() : 0);
				writeD((int) prod.getItemCount());
				writeH(prod.getItemEnchant());
				writeD(0);
				writeD(0);
			}
			for(MultiSellIngredient i : ingredients)
			{
				itemId = i.getItemId();
				ItemTemplate item = itemId > 0 ? ItemHolder.getInstance().getTemplate(i.getItemId()) : null;
				writeH(itemId);
				writeH(itemId > 0 ? item.getType2() : 65535);
				writeD((int) i.getItemCount());
				writeH(i.getItemEnchant());
				writeD(0);
				writeD(0);
			}
		}
	}
}