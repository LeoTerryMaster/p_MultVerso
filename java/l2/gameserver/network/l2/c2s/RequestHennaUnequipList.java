package l2.gameserver.network.l2.c2s;

import l2.gameserver.model.Player;
import l2.gameserver.network.l2.s2c.HennaUnequipList;

public class RequestHennaUnequipList extends L2GameClientPacket
{
	private int _symbolId;
	
	@Override
	protected void readImpl()
	{
		_symbolId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		HennaUnequipList he = new HennaUnequipList(activeChar);
		activeChar.sendPacket(he);
	}
}