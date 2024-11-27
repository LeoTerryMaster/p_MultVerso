package npc.model.residences.clanhall;

import l2.gameserver.model.Creature;
import l2.gameserver.model.Skill;
import l2.gameserver.templates.npc.NpcTemplate;

public class MatchLeaderInstance extends MatchBerserkerInstance
{
	public MatchLeaderInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void reduceCurrentHp(double damage, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp, boolean canReflect, boolean transferDamage, boolean isDot, boolean sendMessage)
	{
		damage = attacker.isPlayer() ? damage / (double) getMaxHp() / 0.05 * 100.0 : damage / (double) getMaxHp() / 0.05 * 10.0;
		super.reduceCurrentHp(damage, attacker, skill, awake, standUp, directHp, canReflect, transferDamage, isDot, sendMessage);
	}
}