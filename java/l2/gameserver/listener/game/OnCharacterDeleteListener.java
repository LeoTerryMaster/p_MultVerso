package l2.gameserver.listener.game;

import l2.gameserver.listener.GameListener;

public interface OnCharacterDeleteListener extends GameListener
{
	void onCharacterDelate(int charObjId);
}