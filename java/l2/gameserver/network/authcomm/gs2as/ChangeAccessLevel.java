package l2.gameserver.network.authcomm.gs2as;

import l2.gameserver.network.authcomm.SendablePacket;

public class ChangeAccessLevel extends SendablePacket
{
	private final String account;
	private final int level;
	private final int banExpire;
	
	public ChangeAccessLevel(String account, int level, int banExpire)
	{
		this.account = account;
		this.level = level;
		this.banExpire = banExpire;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(17);
		writeS(account);
		writeD(level);
		writeD(banExpire);
	}
}