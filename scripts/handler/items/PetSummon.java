package handler.items;

import l2.gameserver.model.Playable;
import l2.gameserver.model.Player;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.tables.PetDataTable;
import l2.gameserver.tables.SkillTable;

public class PetSummon extends ScriptItemHandler
{
	private static final int[] _itemIds = PetDataTable.getPetControlItems();
	private static final int _skillId = 2046;
	
	@Override
	public boolean useItem(Playable playable, ItemInstance item, boolean ctrl)
	{
		if(playable == null || !playable.isPlayer())
		{
			return false;
		}
		Player player = (Player) playable;
		player.setPetControlItem(item);
		player.getAI().Cast(SkillTable.getInstance().getInfo(2046, 1), player, false, true);
		return true;
	}
	
	@Override
	public final int[] getItemIds()
	{
		return _itemIds;
	}
}