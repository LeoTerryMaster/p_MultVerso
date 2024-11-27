package l2.gameserver.network.l2.c2s;

import l2.gameserver.model.Player;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.SocialAction;

public class RequestSocialAction extends L2GameClientPacket
{
	private int _actionId;
	
	@Override
	protected void readImpl() throws Exception
	{
		_actionId = readD();
	}
	
	@Override
	protected void runImpl() throws Exception
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		if(activeChar.isOutOfControl() || activeChar.getTransformation() != 0 || activeChar.isActionsDisabled() || activeChar.isSitting() || activeChar.getPrivateStoreType() != 0 || activeChar.isProcessingRequest())
		{
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.isFishing())
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_DO_THAT_WHILE_FISHING_2);
			return;
		}
		if(_actionId > 1 && _actionId < 14)
		{
			activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), _actionId));
		}
	}
}