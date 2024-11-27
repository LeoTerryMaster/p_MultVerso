package l2.gameserver.network.l2.c2s;

import l2.gameserver.model.Player;
import l2.gameserver.network.l2.s2c.PrivateStoreManageListBuy;
import l2.gameserver.utils.TradeHelper;

public class RequestPrivateStoreBuy extends L2GameClientPacket
{
	@Override
	protected void readImpl() throws Exception
	{
	}
	
	@Override
	protected void runImpl() throws Exception
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		if(activeChar.isActionsDisabled())
		{
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.getSittingTask())
		{
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.isInStoreMode())
		{
			activeChar.setPrivateStoreType(0);
			activeChar.standUp();
			activeChar.broadcastCharInfo();
		}
		else if(!TradeHelper.checksIfCanOpenStore(activeChar, 3))
		{
			activeChar.sendActionFailed();
			return;
		}
		activeChar.sendPacket(new PrivateStoreManageListBuy(activeChar));
	}
}