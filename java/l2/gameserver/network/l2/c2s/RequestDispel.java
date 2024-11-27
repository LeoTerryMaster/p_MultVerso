package l2.gameserver.network.l2.c2s;

import l2.gameserver.model.Effect;
import l2.gameserver.model.Playable;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.network.l2.s2c.SystemMessage;

public class RequestDispel extends L2GameClientPacket
{
	private int _objectId;
	private int _id;
	private int _level;
	
	@Override
	protected void readImpl() throws Exception
	{
		_objectId = readD();
		_id = readD();
		_level = readD();
	}
	
	@Override
	protected void runImpl() throws Exception
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null || activeChar.getObjectId() != _objectId && activeChar.getPet() == null)
		{
			return;
		}
		Playable target = activeChar;
		if(activeChar.getObjectId() != _objectId)
		{
			target = activeChar.getPet();
		}
		for(Effect e : target.getEffectList().getAllEffects())
		{
			if(e.getDisplayId() != _id || e.getDisplayLevel() != _level)
				continue;
			if(!e.isOffensive() && !e.getSkill().isMusic() && e.getSkill().isSelfDispellable() && e.getSkill().getSkillType() != Skill.SkillType.TRANSFORMATION)
			{
				e.exit();
				continue;
			}
			return;
		}
		activeChar.sendPacket(new SystemMessage(92).addSkillName(_id, _level));
	}
}