package l2.gameserver.network.l2.c2s;

import l2.gameserver.network.l2.GameClient;
import l2.gameserver.network.l2.SecondPasswordAuth;
import l2.gameserver.network.l2.s2c.Ex2ndPasswordCheck;
import l2.gameserver.network.l2.s2c.Ex2ndPasswordVerify;

public class RequestEx2ndPasswordVerify extends L2GameClientPacket
{
	private String _password;
	
	@Override
	protected void readImpl()
	{
		_password = readS(8);
	}
	
	@Override
	protected void runImpl()
	{
		GameClient client = getClient();
		SecondPasswordAuth spa = client.getSecondPasswordAuth();
		if(spa == null)
		{
			client.sendPacket(new Ex2ndPasswordVerify(Ex2ndPasswordVerify.Ex2ndPasswordVerifyResult.ERROR));
			return;
		}
		if(!spa.isSecondPasswordSet())
		{
			client.sendPacket(new Ex2ndPasswordCheck(Ex2ndPasswordCheck.Ex2ndPasswordCheckResult.CREATE));
			return;
		}
		if(spa.isValidSecondPassword(_password))
		{
			client.sendPacket(new Ex2ndPasswordVerify(Ex2ndPasswordVerify.Ex2ndPasswordVerifyResult.SUCCESS));
			client.setSecondPasswordAuthed(true);
		}
		else if(spa.isBlocked())
		{
			client.sendPacket(new Ex2ndPasswordVerify(Ex2ndPasswordVerify.Ex2ndPasswordVerifyResult.BLOCK_HOMEPAGE));
		}
		else
		{
			client.sendPacket(new Ex2ndPasswordVerify(Ex2ndPasswordVerify.Ex2ndPasswordVerifyResult.FAILED, spa.getTrysCount()));
		}
	}
}