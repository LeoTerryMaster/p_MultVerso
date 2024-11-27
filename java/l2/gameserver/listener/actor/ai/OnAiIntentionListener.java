package l2.gameserver.listener.actor.ai;

import l2.gameserver.ai.CtrlIntention;
import l2.gameserver.listener.AiListener;
import l2.gameserver.model.Creature;

public interface OnAiIntentionListener extends AiListener
{
	void onAiIntention(Creature actor, CtrlIntention intention, Object arg0, Object arg1);
}