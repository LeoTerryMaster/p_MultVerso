package l2.gameserver.listener.actor.player;

import l2.gameserver.listener.PlayerListener;

public interface OnAnswerListener extends PlayerListener
{
	void sayYes();
	
	void sayNo();
}