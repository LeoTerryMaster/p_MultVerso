package ai.residences.clanhall;

import l2.commons.util.Rnd;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Skill;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.tables.SkillTable;

public class MatchBerserker extends MatchFighter
{
	public static final Skill ATTACK_SKILL = SkillTable.getInstance().getInfo(4032, 6);
	
	public MatchBerserker(NpcInstance actor)
	{
		super(actor);
	}
	
	@Override
	public void onEvtAttacked(Creature attacker, int dam)
	{
		super.onEvtAttacked(attacker, dam);
		if(Rnd.chance(10))
		{
			addTaskCast(attacker, ATTACK_SKILL);
		}
	}
}