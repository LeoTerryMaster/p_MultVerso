package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;

import java.util.Collection;
import java.util.Collections;

public class MagicSkillLaunched extends L2GameServerPacket
{
	private final int _casterId;
	private final int _skillId;
	private final int _skillLevel;
	private final int _casterX;
	private final int _casterY;
	private final Collection<Creature> _targets;
	
	public MagicSkillLaunched(Creature caster, int skillId, int skillLevel, Creature target)
	{
		_casterId = caster.getObjectId();
		_casterX = caster.getX();
		_casterY = caster.getY();
		_skillId = skillId;
		_skillLevel = skillLevel;
		_targets = Collections.singletonList(target);
	}
	
	public MagicSkillLaunched(Creature caster, Skill skill, Creature target)
	{
		_casterId = caster.getObjectId();
		_casterX = caster.getX();
		_casterY = caster.getY();
		_skillId = skill.getDisplayId();
		_skillLevel = skill.getDisplayLevel();
		_targets = Collections.singletonList(target);
	}
	
	public MagicSkillLaunched(Creature caster, int skillId, int skillLevel, Collection<Creature> targets)
	{
		_casterId = caster.getObjectId();
		_casterX = caster.getX();
		_casterY = caster.getY();
		_skillId = skillId;
		_skillLevel = skillLevel;
		_targets = targets;
	}
	
	public MagicSkillLaunched(Creature caster, Skill skill, Collection<Creature> targets)
	{
		_casterId = caster.getObjectId();
		_casterX = caster.getX();
		_casterY = caster.getY();
		_skillId = skill.getDisplayId();
		_skillLevel = skill.getDisplayLevel();
		_targets = targets;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(118);
		writeD(_casterId);
		writeD(_skillId);
		writeD(_skillLevel);
		writeD(_targets.size());
		for(Creature target : _targets)
		{
			if(target == null)
				continue;
			writeD(target.getObjectId());
		}
	}
	
	@Override
	public L2GameServerPacket packet(Player player)
	{
		if(player != null && !player.isInObserverMode())
		{
			if(player.buffAnimRange() < 0)
			{
				return null;
			}
			if(player.buffAnimRange() == 0)
			{
				return _casterId == player.getObjectId() ? super.packet(player) : null;
			}
			return player.getDistance(_casterX, _casterY) < (double) player.buffAnimRange() ? super.packet(player) : null;
		}
		return super.packet(player);
	}
}