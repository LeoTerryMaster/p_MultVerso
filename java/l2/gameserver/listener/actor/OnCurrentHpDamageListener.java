package l2.gameserver.listener.actor;

import l2.gameserver.listener.CharListener;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Skill;

public interface OnCurrentHpDamageListener extends CharListener
{
	void onCurrentHpDamage(Creature actor, double damage, Creature attacker, Skill skill);
}