package l2.gameserver.network.authcomm.gs2as;

import l2.gameserver.network.authcomm.SendablePacket;

public class PlayerLogout extends SendablePacket
{
	private final String account;
	
	public PlayerLogout(String account)
	{
		this.account = account;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(4);
		writeS(account);
	}
}