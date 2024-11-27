package l2.gameserver.listener.actor.door;

import l2.gameserver.listener.CharListener;
import l2.gameserver.model.instances.DoorInstance;

public interface OnOpenCloseListener extends CharListener
{
	void onOpen(DoorInstance doorInstance);
	
	void onClose(DoorInstance doorInstance);
}