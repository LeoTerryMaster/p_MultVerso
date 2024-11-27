package l2.gameserver.network.l2.c2s;

import l2.gameserver.model.Player;
import l2.gameserver.network.l2.s2c.ExListPartyMatchingWaitingRoom;

public class RequestListPartyMatchingWaitingRoom extends L2GameClientPacket
{
	private int _minLevel;
	private int _maxLevel;
	private int _page;
	private int[] _classes;
	
	@Override
	protected void readImpl()
	{
		_page = readD();
		_minLevel = readD();
		_maxLevel = readD();
		int size = readD();
		if(size > 127 || size < 0)
		{
			size = 0;
		}
		_classes = new int[size];
		for(int i = 0;i < size;++i)
		{
			_classes[i] = readD();
		}
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		activeChar.sendPacket(new ExListPartyMatchingWaitingRoom(activeChar, _minLevel, _maxLevel, _page, _classes));
	}
}