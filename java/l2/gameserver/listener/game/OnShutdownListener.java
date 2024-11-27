package l2.gameserver.listener.game;

import l2.gameserver.listener.GameListener;

public interface OnShutdownListener extends GameListener
{
	void onShutdown();
}