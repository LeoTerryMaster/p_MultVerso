package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Player;
import l2.gameserver.model.Recipe;

import java.util.Collection;

public class RecipeBookItemList extends L2GameServerPacket
{
	private final boolean _isDwarvenCraft;
	private final int _currentMp;
	private final Collection<Recipe> _recipes;
	
	public RecipeBookItemList(Player player, boolean isDwarvenCraft)
	{
		_isDwarvenCraft = isDwarvenCraft;
		_currentMp = (int) player.getCurrentMp();
		_recipes = isDwarvenCraft ? player.getDwarvenRecipeBook() : player.getCommonRecipeBook();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(214);
		writeD(_isDwarvenCraft ? 0 : 1);
		writeD(_currentMp);
		writeD(_recipes.size());
		for(Recipe recipe : _recipes)
		{
			writeD(recipe.getId());
			writeD(1);
		}
	}
}