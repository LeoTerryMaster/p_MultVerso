package l2.gameserver.network.l2.c2s;

import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.items.attachment.FlagItemAttachment;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.tables.SkillTable;

public class RequestMagicSkillUse extends L2GameClientPacket
{
	private Integer _magicId;
	private boolean _ctrlPressed;
	private boolean _shiftPressed;
	
	@Override
	protected void readImpl()
	{
		_magicId = readD();
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
		activeChar.setActive();
		if(activeChar.isOutOfControl())
		{
			activeChar.sendActionFailed();
			return;
		}
		Skill skill = SkillTable.getInstance().getInfo(_magicId, activeChar.getSkillLevel(_magicId));
		if(skill != null)
		{
			if(!skill.isActive() && !skill.isToggle())
			{
				return;
			}
			FlagItemAttachment attachment = activeChar.getActiveWeaponFlagAttachment();
			if(attachment != null && !attachment.canCast(activeChar, skill))
			{
				activeChar.sendActionFailed();
				return;
			}
			if(activeChar.getTransformation() != 0 && !activeChar.getAllSkills().contains(skill))
			{
				return;
			}
			if(skill.isToggle() && activeChar.getEffectList().getEffectsBySkill(skill) != null)
			{
				activeChar.getEffectList().stopEffect(skill.getId());
				activeChar.sendPacket(new SystemMessage(335).addSkillName(skill.getId(), skill.getLevel()));
				activeChar.sendActionFailed();
				return;
			}
			Creature target = skill.getAimingTarget(activeChar, activeChar.getTarget());
			activeChar.setGroundSkillLoc(null);
			activeChar.getAI().Cast(skill, target, _ctrlPressed, _shiftPressed);
		}
		else
		{
			activeChar.sendActionFailed();
		}
	}
}