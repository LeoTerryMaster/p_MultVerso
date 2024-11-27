package l2.gameserver.listener.inventory;

import l2.commons.listener.Listener;
import l2.gameserver.model.Playable;
import l2.gameserver.model.items.ItemInstance;

public interface OnEquipListener extends Listener<Playable>
{
	void onEquip(int slot, ItemInstance item, Playable actor);
	
	void onUnequip(int slot, ItemInstance item, Playable actor);
}