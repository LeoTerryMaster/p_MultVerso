package l2.gameserver.network.l2.c2s;

import l2.gameserver.model.Player;

public class RequestRecipeShopMessageSet extends L2GameClientPacket
{
	private String _name;
	
	@Override
	protected void readImpl()
	{
		_name = readS(16);
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		activeChar.setManufactureName(_name);
	}
}