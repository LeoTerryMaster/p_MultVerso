package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;

public class MagicSkillUse extends L2GameServerPacket
{
	private final int _targetId;
	private final int _skillId;
	private final int _skillLevel;
	private final int _hitTime;
	private final int _reuseDelay;
	private final int _chaId;
	private final int _x;
	private final int _y;
	private final int _z;
	private final int _tx;
	private final int _ty;
	private final int _tz;
	
	public MagicSkillUse(Creature cha, Creature target, int skillId, int skillLevel, int hitTime, long reuseDelay)
	{
		_chaId = cha.getObjectId();
		_targetId = target.getObjectId();
		_skillId = skillId;
		_skillLevel = skillLevel;
		_hitTime = hitTime;
		_reuseDelay = (int) reuseDelay;
		_x = cha.getX();
		_y = cha.getY();
		_z = cha.getZ();
		_tx = target.getX();
		_ty = target.getY();
		_tz = target.getZ();
	}
	
	public MagicSkillUse(Creature cha, Creature target, Skill skill, int hitTime, long reuseDelay)
	{
		_chaId = cha.getObjectId();
		_targetId = target.getObjectId();
		_skillId = skill.getDisplayId();
		_skillLevel = skill.getDisplayLevel();
		_hitTime = hitTime;
		_reuseDelay = (int) reuseDelay;
		_x = cha.getX();
		_y = cha.getY();
		_z = cha.getZ();
		_tx = target.getX();
		_ty = target.getY();
		_tz = target.getZ();
	}
	
	public MagicSkillUse(Creature cha, int skillId, int skillLevel, int hitTime, long reuseDelay)
	{
		_chaId = cha.getObjectId();
		_targetId = cha.getTargetId();
		_skillId = skillId;
		_skillLevel = skillLevel;
		_hitTime = hitTime;
		_reuseDelay = (int) reuseDelay;
		_x = cha.getX();
		_y = cha.getY();
		_z = cha.getZ();
		_tx = cha.getX();
		_ty = cha.getY();
		_tz = cha.getZ();
	}
	
	public MagicSkillUse(Creature cha, Skill skill, int hitTime, long reuseDelay)
	{
		_chaId = cha.getObjectId();
		_targetId = cha.getTargetId();
		_skillId = skill.getDisplayId();
		_skillLevel = skill.getDisplayLevel();
		_hitTime = hitTime;
		_reuseDelay = (int) reuseDelay;
		_x = cha.getX();
		_y = cha.getY();
		_z = cha.getZ();
		_tx = cha.getX();
		_ty = cha.getY();
		_tz = cha.getZ();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(72);
		writeD(_chaId);
		writeD(_targetId);
		writeD(_skillId);
		writeD(_skillLevel);
		writeD(_hitTime);
		writeD(_reuseDelay);
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeD(0);
		writeD(_tx);
		writeD(_ty);
		writeD(_tz);
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
				return _chaId == player.getObjectId() ? super.packet(player) : null;
			}
			return player.getDistance(_x, _y) < (double) player.buffAnimRange() ? super.packet(player) : null;
		}
		return super.packet(player);
	}
}