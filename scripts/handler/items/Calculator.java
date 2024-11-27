package handler.items;

import l2.gameserver.model.Playable;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.network.l2.s2c.ShowCalc;

public class Calculator extends ScriptItemHandler
{
	private static final int CALCULATOR = 4393;
	
	@Override
	public boolean useItem(Playable playable, ItemInstance item, boolean ctrl)
	{
		if(!playable.isPlayer())
		{
			return false;
		}
		playable.sendPacket(new ShowCalc(item.getItemId()));
		return true;
	}
	
	@Override
	public int[] getItemIds()
	{
		return new int[] {4393};
	}
}