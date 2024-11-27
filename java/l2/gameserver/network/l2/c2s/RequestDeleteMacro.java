package l2.gameserver.network.l2.c2s;

import l2.gameserver.model.Player;

public class RequestDeleteMacro extends L2GameClientPacket
{
	private int _id;
	
	@Override
	protected void readImpl()
	{
		_id = readD();
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		activeChar.deleteMacro(_id);
	}
}