package l2.gameserver.listener.actor;

import l2.gameserver.listener.CharListener;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Skill;

public interface OnMagicHitListener extends CharListener
{
	void onMagicHit(Creature actor, Skill skill, Creature castter);
}