package l2.gameserver.network.l2.c2s;

import l2.gameserver.model.Player;
import l2.gameserver.network.l2.s2c.SkillList;

public final class RequestSkillList extends L2GameClientPacket
{
	private static final String _C__50_REQUESTSKILLLIST = "[C] 50 RequestSkillList";
	
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		Player cha = getClient().getActiveChar();
		if(cha != null)
		{
			cha.sendPacket(new SkillList(cha));
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] 50 RequestSkillList";
	}
}