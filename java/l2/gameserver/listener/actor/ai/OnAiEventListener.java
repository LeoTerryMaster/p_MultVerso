package l2.gameserver.listener.actor.ai;

import l2.gameserver.ai.CtrlEvent;
import l2.gameserver.listener.AiListener;
import l2.gameserver.model.Creature;

public interface OnAiEventListener extends AiListener
{
	void onAiEvent(Creature actor, CtrlEvent evt, Object[] args);
}