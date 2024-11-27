package l2.gameserver.network.l2.c2s;

import l2.gameserver.Config;
import l2.gameserver.network.l2.GameClient;
import l2.gameserver.network.l2.SecondPasswordAuth;
import l2.gameserver.network.l2.s2c.Ex2ndPasswordCheck;

public class RequestEx2ndPasswordCheck extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		GameClient client = getClient();
		if(!Config.USE_SECOND_PASSWORD_AUTH)
		{
			client.sendPacket(new Ex2ndPasswordCheck(Ex2ndPasswordCheck.Ex2ndPasswordCheckResult.SUCCESS));
			return;
		}
		SecondPasswordAuth spa = client.getSecondPasswordAuth();
		if(spa == null)
		{
			client.sendPacket(new Ex2ndPasswordCheck(Ex2ndPasswordCheck.Ex2ndPasswordCheckResult.ERROR));
			return;
		}
		if(!spa.isSecondPasswordSet())
		{
			client.sendPacket(new Ex2ndPasswordCheck(Ex2ndPasswordCheck.Ex2ndPasswordCheckResult.CREATE));
			return;
		}
		if(spa.isBlocked())
		{
			client.sendPacket(new Ex2ndPasswordCheck(Ex2ndPasswordCheck.Ex2ndPasswordCheckResult.BLOCK_TIME, spa.getBlockTimeLeft()));
			return;
		}
		if(client.isSecondPasswordAuthed())
		{
			client.sendPacket(new Ex2ndPasswordCheck(Ex2ndPasswordCheck.Ex2ndPasswordCheckResult.SUCCESS));
			return;
		}
		client.sendPacket(new Ex2ndPasswordCheck(Ex2ndPasswordCheck.Ex2ndPasswordCheckResult.CHECK));
	}
}