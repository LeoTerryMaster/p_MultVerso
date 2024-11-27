package l2.gameserver.model.items.attachment;

import l2.gameserver.model.Player;

public interface PickableAttachment extends ItemAttachment
{
	boolean canPickUp(Player player);
	
	void pickUp(Player player);
}