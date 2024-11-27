package l2.gameserver.network.authcomm.gs2as;

import l2.gameserver.network.authcomm.SendablePacket;

public class PlayerInGame extends SendablePacket
{
	private final String account;
	
	public PlayerInGame(String account)
	{
		this.account = account;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(3);
		writeS(account);
	}
}