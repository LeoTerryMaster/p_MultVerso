package l2.gameserver.network.l2.c2s;

import l2.gameserver.cache.Msg;
import l2.gameserver.model.Player;
import l2.gameserver.model.entity.SevenSignsFestival.SevenSignsFestival;
import l2.gameserver.network.l2.GameClient;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.network.l2.s2c.ActionFail;
import l2.gameserver.network.l2.s2c.CharacterSelectionInfo;
import l2.gameserver.network.l2.s2c.RestartResponse;

public class RequestRestart extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		if(activeChar.isInObserverMode())
		{
			activeChar.sendPacket(Msg.OBSERVERS_CANNOT_PARTICIPATE, RestartResponse.FAIL, ActionFail.STATIC);
			return;
		}
		if(activeChar.isInCombat())
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_RESTART_WHILE_IN_COMBAT, RestartResponse.FAIL, ActionFail.STATIC);
			return;
		}
		if(activeChar.isFishing())
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_DO_ANYTHING_ELSE_WHILE_FISHING, RestartResponse.FAIL, ActionFail.STATIC);
			return;
		}
		if(activeChar.isBlocked() && !activeChar.isFlying())
		{
			activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestRestart.OutOfControl", activeChar));
			activeChar.sendPacket(RestartResponse.FAIL, ActionFail.STATIC);
			return;
		}
		if(activeChar.isFestivalParticipant() && SevenSignsFestival.getInstance().isFestivalInitialized())
		{
			activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestRestart.Festival", activeChar));
			activeChar.sendPacket(RestartResponse.FAIL, ActionFail.STATIC);
			return;
		}
		if(getClient() != null)
		{
			getClient().setState(GameClient.GameClientState.AUTHED);
		}
		activeChar.restart();
		CharacterSelectionInfo cl = new CharacterSelectionInfo(getClient().getLogin(), getClient().getSessionKey().playOkID1);
		sendPacket(RestartResponse.OK, cl);
		getClient().setCharSelection(cl.getCharInfo());
	}
}