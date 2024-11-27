package l2.gameserver.listener.game;

import l2.gameserver.listener.GameListener;

public interface OnSSPeriodListener extends GameListener
{
	void onPeriodChange(int mode);
}