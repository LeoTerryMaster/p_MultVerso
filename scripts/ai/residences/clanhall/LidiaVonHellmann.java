package ai.residences.clanhall;

import ai.residences.SiegeGuardFighter;
import l2.commons.util.Rnd;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Skill;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.scripts.Functions;
import l2.gameserver.tables.SkillTable;
import l2.gameserver.utils.PositionUtils;
import npc.model.residences.SiegeGuardInstance;

public class LidiaVonHellmann extends SiegeGuardFighter
{
	private static final Skill DRAIN_SKILL = SkillTable.getInstance().getInfo(4999, 1);
	private static final Skill DAMAGE_SKILL = SkillTable.getInstance().getInfo(4998, 1);
	
	public LidiaVonHellmann(NpcInstance actor)
	{
		super(actor);
	}
	
	@Override
	public void onEvtSpawn()
	{
		super.onEvtSpawn();
		Functions.npcShoutCustomMessage(getActor(), "clanhall.LidiaVonHellmann.HMM_THOSE_WHO_ARE_NOT_OF_THE_BLOODLINE_ARE_COMING_THIS_WAY_TO_TAKE_OVER_THE_CASTLE__HUMPH__THE_BITTER_GRUDGES_OF_THE_DEAD", (Object[]) new Object[0]);
	}
	
	@Override
	public void onEvtDead(Creature killer)
	{
		super.onEvtDead(killer);
		Functions.npcShoutCustomMessage(getActor(), "clanhall.LidiaVonHellmann.GRARR_FOR_THE_NEXT_2_MINUTES_OR_SO_THE_GAME_ARENA_ARE_WILL_BE_CLEANED", (Object[]) new Object[0]);
	}
	
	@Override
	public void onEvtAttacked(Creature attacker, int damage)
	{
		SiegeGuardInstance actor = getActor();
		super.onEvtAttacked(attacker, damage);
		if(Rnd.chance(0.22))
		{
			addTaskCast(attacker, DRAIN_SKILL);
		}
		else if(actor.getCurrentHpPercents() < 20.0 && Rnd.chance(0.22))
		{
			addTaskCast(attacker, DRAIN_SKILL);
		}
		if(PositionUtils.calculateDistance(actor, attacker, false) > 300.0 && Rnd.chance(0.13))
		{
			addTaskCast(attacker, DAMAGE_SKILL);
		}
	}
}