package l2.gameserver.network.l2.c2s;

import l2.gameserver.network.l2.GameClient;
import l2.gameserver.network.l2.SecondPasswordAuth;
import l2.gameserver.network.l2.s2c.Ex2ndPasswordAck;

public class RequestEx2ndPasswordReq extends L2GameClientPacket
{
	private boolean _isChangePass;
	private String _password;
	private String _newPassword;
	
	@Override
	protected void readImpl()
	{
		_isChangePass = readC() == 2;
		_password = readS(8);
		if(_isChangePass)
		{
			_newPassword = readS(8);
		}
	}
	
	@Override
	protected void runImpl()
	{
		GameClient client = getClient();
		SecondPasswordAuth spa = client.getSecondPasswordAuth();
		if(spa == null)
		{
			client.sendPacket(new Ex2ndPasswordAck(Ex2ndPasswordAck.Ex2ndPasswordAckResult.ERROR));
			return;
		}
		if(_isChangePass)
		{
			if(spa.changePassword(_password, _newPassword))
			{
				client.sendPacket(new Ex2ndPasswordAck(Ex2ndPasswordAck.Ex2ndPasswordAckResult.SUCCESS_CREATE));
				return;
			}
			if(spa.isBlocked())
			{
				client.sendPacket(new Ex2ndPasswordAck(Ex2ndPasswordAck.Ex2ndPasswordAckResult.BLOCK_HOMEPAGE));
			}
			else
			{
				client.sendPacket(new Ex2ndPasswordAck(Ex2ndPasswordAck.Ex2ndPasswordAckResult.FAIL_VERIFY, spa.getTrysCount()));
			}
		}
		else if(!spa.isSecondPasswordSet())
		{
			spa.changePassword(null, _password);
			client.sendPacket(new Ex2ndPasswordAck(Ex2ndPasswordAck.Ex2ndPasswordAckResult.SUCCESS_CREATE));
		}
		else
		{
			if(spa.isValidSecondPassword(_password))
			{
				client.sendPacket(new Ex2ndPasswordAck(Ex2ndPasswordAck.Ex2ndPasswordAckResult.SUCCESS_VERIFY));
				client.setSecondPasswordAuthed(true);
				return;
			}
			if(spa.isBlocked())
			{
				client.sendPacket(new Ex2ndPasswordAck(Ex2ndPasswordAck.Ex2ndPasswordAckResult.BLOCK_HOMEPAGE));
			}
			else
			{
				client.sendPacket(new Ex2ndPasswordAck(Ex2ndPasswordAck.Ex2ndPasswordAckResult.FAIL_VERIFY, spa.getTrysCount()));
			}
		}
	}
}