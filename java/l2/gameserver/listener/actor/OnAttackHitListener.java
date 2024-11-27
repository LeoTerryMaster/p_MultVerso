package l2.gameserver.listener.actor;

import l2.gameserver.listener.CharListener;
import l2.gameserver.model.Creature;

public interface OnAttackHitListener extends CharListener
{
	void onAttackHit(Creature actor, Creature attacker);
}