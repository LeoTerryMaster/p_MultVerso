package l2.gameserver.network.l2.c2s;

import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;

public class RequestTargetCanceld extends L2GameClientPacket
{
	private int _unselect;
	
	@Override
	protected void readImpl()
	{
		_unselect = readH();
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		if(_unselect == 0)
		{
			if(activeChar.isCastingNow())
			{
				Skill skill = activeChar.getCastingSkill();
				activeChar.abortCast(skill != null && (skill.isHandler() || skill.getHitTime() > 1000), false);
			}
			else if(activeChar.getTarget() != null)
			{
				activeChar.setTarget(null);
			}
		}
		else if(activeChar.getTarget() != null)
		{
			activeChar.setTarget(null);
		}
	}
}