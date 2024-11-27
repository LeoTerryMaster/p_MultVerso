package l2.gameserver.network.l2.c2s;

import l2.gameserver.model.Player;

public class RequestPledgeExtendedInfo extends L2GameClientPacket
{
	private String _name;
	
	@Override
	protected void readImpl()
	{
		_name = readS();
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		if(activeChar.isGM())
		{
			activeChar.sendMessage("RequestPledgeExtendedInfo");
		}
	}
}