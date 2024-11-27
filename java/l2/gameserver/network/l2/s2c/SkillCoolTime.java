package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Player;
import l2.gameserver.skills.TimeStamp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class SkillCoolTime extends L2GameServerPacket
{
	private List<Skill> _list = Collections.emptyList();
	
	public SkillCoolTime(Player player)
	{
		Collection<TimeStamp> list = player.getSkillReuses();
		_list = new ArrayList<>(list.size());
		for(TimeStamp stamp : list)
		{
			l2.gameserver.model.Skill skill;
			if(!stamp.hasNotPassed() || (skill = player.getKnownSkill(stamp.getId())) == null)
				continue;
			Skill sk = new Skill();
			sk.skillId = skill.getId();
			sk.level = skill.getLevel();
			sk.reuseBase = (int) Math.floor((double) stamp.getReuseBasic() / 1000.0);
			sk.reuseCurrent = (int) Math.floor((double) stamp.getReuseCurrent() / 1000.0);
			_list.add(sk);
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(193);
		writeD(_list.size());
		for(int i = 0;i < _list.size();++i)
		{
			Skill sk = _list.get(i);
			writeD(sk.skillId);
			writeD(sk.level);
			writeD(sk.reuseBase);
			writeD(sk.reuseCurrent);
		}
	}
	
	private static class Skill
	{
		public int skillId;
		public int level;
		public int reuseBase;
		public int reuseCurrent;
	}
}