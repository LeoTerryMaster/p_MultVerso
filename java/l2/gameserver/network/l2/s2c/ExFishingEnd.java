package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Player;

public class ExFishingEnd extends L2GameServerPacket
{
	private final int _charId;
	private final boolean _win;
	
	public ExFishingEnd(Player character, boolean win)
	{
		_charId = character.getObjectId();
		_win = win;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeEx(20);
		writeD(_charId);
		writeC(_win ? 1 : 0);
	}
}