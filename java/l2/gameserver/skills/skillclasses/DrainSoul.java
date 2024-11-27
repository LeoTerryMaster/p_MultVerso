package l2.gameserver.skills.skillclasses;

import l2.gameserver.model.Creature;
import l2.gameserver.model.Skill;
import l2.gameserver.model.instances.MonsterInstance;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.templates.StatsSet;

import java.util.List;

public class DrainSoul extends Skill
{
	public DrainSoul(StatsSet set)
	{
		super(set);
	}
	
	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		if(target == null || !target.isMonster() || target.isDead())
		{
			activeChar.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
			return false;
		}
		return super.checkCondition(activeChar, target, forceUse, dontMove, first);
	}
	
	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		if(!activeChar.isPlayer() || activeChar.isDead())
		{
			return;
		}
		for(Creature c : targets)
		{
			if(c == null || c.isDead() || !c.isMonster())
			{
				return;
			}
			MonsterInstance monster = (MonsterInstance) c;
			if(monster.getTemplate().getAbsorbInfo().isEmpty())
				continue;
			monster.addAbsorber(activeChar.getPlayer());
		}
	}
}