package l2.authserver.network.gamecomm.as2gs;

import l2.authserver.network.gamecomm.SendablePacket;

public class KickPlayer extends SendablePacket
{
	private final String account;
	
	public KickPlayer(String login)
	{
		account = login;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(3);
		writeS(account);
	}
}