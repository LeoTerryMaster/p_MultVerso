package l2.gameserver.listener.actor;

import l2.gameserver.listener.CharListener;
import l2.gameserver.model.Creature;

public interface OnCurrentMpReduceListener extends CharListener
{
	void onCurrentMpReduce(Creature actor, double reduce, Creature attacker);
}