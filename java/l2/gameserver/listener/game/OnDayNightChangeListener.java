package l2.gameserver.listener.game;

import l2.gameserver.listener.GameListener;

public interface OnDayNightChangeListener extends GameListener
{
	void onDay();
	
	void onNight();
}