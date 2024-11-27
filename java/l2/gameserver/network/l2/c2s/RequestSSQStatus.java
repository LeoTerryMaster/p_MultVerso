package l2.gameserver.network.l2.c2s;

import l2.gameserver.model.Player;
import l2.gameserver.model.entity.SevenSigns;
import l2.gameserver.network.l2.s2c.SSQStatus;

public class RequestSSQStatus extends L2GameClientPacket
{
	private int _page;
	
	@Override
	protected void readImpl()
	{
		_page = readC();
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		if((SevenSigns.getInstance().isSealValidationPeriod() || SevenSigns.getInstance().isCompResultsPeriod()) && _page == 4)
		{
			return;
		}
		activeChar.sendPacket(new SSQStatus(activeChar, _page));
	}
}