package l2.gameserver.listener.actor;

import l2.gameserver.listener.CharListener;
import l2.gameserver.model.Creature;

public interface OnDeathListener extends CharListener
{
	void onDeath(Creature self, Creature killer);
}