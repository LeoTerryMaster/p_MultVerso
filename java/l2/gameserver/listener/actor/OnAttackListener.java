package l2.gameserver.listener.actor;

import l2.gameserver.listener.CharListener;
import l2.gameserver.model.Creature;

public interface OnAttackListener extends CharListener
{
	void onAttack(Creature actor, Creature target);
}