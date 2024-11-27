package l2.gameserver.network.l2.c2s;

import l2.gameserver.cache.Msg;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.tables.SkillTable;
import l2.gameserver.utils.Location;

public class RequestExMagicSkillUseGround extends L2GameClientPacket
{
	private final Location _loc = new Location();
	private int _skillId;
	private boolean _ctrlPressed;
	private boolean _shiftPressed;
	
	@Override
	protected void readImpl()
	{
		_loc.x = readD();
		_loc.y = readD();
		_loc.z = readD();
		_skillId = readD();
		_ctrlPressed = readD() != 0;
		_shiftPressed = readC() != 0;
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		if(activeChar.isOutOfControl())
		{
			activeChar.sendActionFailed();
			return;
		}
		Skill skill = SkillTable.getInstance().getInfo(_skillId, activeChar.getSkillLevel(_skillId));
		if(skill != null)
		{
			if(skill.getAddedSkills().length == 0)
			{
				return;
			}
			if(activeChar.getTransformation() != 0 && !activeChar.getAllSkills().contains(skill))
			{
				return;
			}
			if(!activeChar.isInRange(_loc, (long) skill.getCastRange()))
			{
				activeChar.sendPacket(Msg.YOUR_TARGET_IS_OUT_OF_RANGE);
				activeChar.sendActionFailed();
				return;
			}
			Creature target = skill.getAimingTarget(activeChar, activeChar.getTarget());
			if(skill.checkCondition(activeChar, target, _ctrlPressed, _shiftPressed, true))
			{
				activeChar.setGroundSkillLoc(_loc);
				activeChar.getAI().Cast(skill, target, _ctrlPressed, _shiftPressed);
			}
			else
			{
				activeChar.sendActionFailed();
			}
		}
		else
		{
			activeChar.sendActionFailed();
		}
	}
}