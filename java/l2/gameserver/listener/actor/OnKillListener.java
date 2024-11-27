package l2.gameserver.listener.actor;

import l2.gameserver.listener.CharListener;
import l2.gameserver.model.Creature;

public interface OnKillListener extends CharListener
{
	void onKill(Creature actor, Creature victim);
	
	boolean ignorePetOrSummon();
}