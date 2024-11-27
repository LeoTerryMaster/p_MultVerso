package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;

import java.util.Collection;

public class GMViewSkillInfo extends L2GameServerPacket
{
	private final String _charName;
	private final Collection<Skill> _skills;
	private final Player _targetChar;
	
	public GMViewSkillInfo(Player cha)
	{
		_charName = cha.getName();
		_skills = cha.getAllSkills();
		_targetChar = cha;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(145);
		writeS(_charName);
		writeD(_skills.size());
		for(Skill skill : _skills)
		{
			writeD(skill.isPassive() ? 1 : 0);
			writeD(skill.getDisplayLevel());
			writeD(skill.getId());
			writeC(_targetChar.isUnActiveSkill(skill.getId()) ? 1 : 0);
		}
	}
}