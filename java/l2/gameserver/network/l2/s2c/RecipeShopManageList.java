package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Player;
import l2.gameserver.model.Recipe;
import l2.gameserver.model.items.ManufactureItem;

import java.util.Collection;
import java.util.List;

public class RecipeShopManageList extends L2GameServerPacket
{
	private final List<ManufactureItem> createList;
	private final Collection<Recipe> recipes;
	private final int sellerId;
	private final long adena;
	private final boolean isDwarven;
	
	public RecipeShopManageList(Player seller, boolean isDwarvenCraft)
	{
		sellerId = seller.getObjectId();
		adena = seller.getAdena();
		isDwarven = isDwarvenCraft;
		recipes = isDwarven ? seller.getDwarvenRecipeBook() : seller.getCommonRecipeBook();
		createList = seller.getCreateList();
		for(ManufactureItem mi : createList)
		{
			if(seller.findRecipe(mi.getRecipeId()))
				continue;
			createList.remove(mi);
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(216);
		writeD(sellerId);
		writeD((int) Math.min(adena, Integer.MAX_VALUE));
		writeD(isDwarven ? 0 : 1);
		writeD(recipes.size());
		int i = 1;
		for(Recipe recipe : recipes)
		{
			writeD(recipe.getId());
			writeD(i++);
		}
		writeD(createList.size());
		for(ManufactureItem mi : createList)
		{
			writeD(mi.getRecipeId());
			writeD(0);
			writeD((int) mi.getCost());
		}
	}
}