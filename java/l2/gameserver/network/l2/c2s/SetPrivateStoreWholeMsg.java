package l2.gameserver.network.l2.c2s;

import l2.gameserver.model.Player;

public class SetPrivateStoreWholeMsg extends L2GameClientPacket
{
	private String _storename;
	
	@Override
	protected void readImpl()
	{
		_storename = readS(32);
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		activeChar.setSellStoreName(_storename);
	}
}