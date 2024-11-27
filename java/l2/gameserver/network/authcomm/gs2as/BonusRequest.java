package l2.gameserver.network.authcomm.gs2as;

import l2.gameserver.network.authcomm.SendablePacket;

public class BonusRequest extends SendablePacket
{
	private final String account;
	private final double bonus;
	private final int bonusExpire;
	
	public BonusRequest(String account, double bonus, int bonusExpire)
	{
		this.account = account;
		this.bonus = bonus;
		this.bonusExpire = bonusExpire;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(16);
		writeS(account);
		writeF(bonus);
		writeD(bonusExpire);
	}
}