package l2.gameserver.listener.actor.npc;

import l2.gameserver.listener.NpcListener;
import l2.gameserver.model.instances.NpcInstance;

public interface OnDecayListener extends NpcListener
{
	void onDecay(NpcInstance actor);
}