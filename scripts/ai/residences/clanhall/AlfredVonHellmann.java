package ai.residences.clanhall;

import ai.residences.SiegeGuardFighter;
import l2.commons.util.Rnd;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Skill;
import l2.gameserver.model.Zone;
import l2.gameserver.model.entity.events.impl.ClanHallSiegeEvent;
import l2.gameserver.model.entity.events.objects.SpawnExObject;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.scripts.Functions;
import l2.gameserver.tables.SkillTable;
import l2.gameserver.utils.PositionUtils;
import l2.gameserver.utils.ReflectionUtils;
import npc.model.residences.SiegeGuardInstance;

public class AlfredVonHellmann extends SiegeGuardFighter
{
	public static final Skill DAMAGE_SKILL = SkillTable.getInstance().getInfo(5000, 1);
	public static final Skill DRAIN_SKILL = SkillTable.getInstance().getInfo(5001, 1);
	private static final Zone ZONE_3 = ReflectionUtils.getZone((String) "lidia_zone3");
	
	public AlfredVonHellmann(NpcInstance actor)
	{
		super(actor);
	}
	
	@Override
	public void onEvtSpawn()
	{
		super.onEvtSpawn();
		ZONE_3.setActive(true);
		Functions.npcShoutCustomMessage(getActor(), "clanhall.AlfredVonHellmann.HEH_HEH_I_SEE_THAT_THE_FEAST_HAS_BEGAN_BE_WARY_THE_CURSE_OF_THE_HELLMANN_FAMILY_HAS_POISONED_THIS_LAND", (Object[]) new Object[0]);
	}
	
	@Override
	public void onEvtDead(Creature killer)
	{
		SiegeGuardInstance actor = getActor();
		super.onEvtDead(killer);
		ZONE_3.setActive(false);
		Functions.npcShoutCustomMessage(actor, "clanhall.AlfredVonHellmann.AARGH_IF_I_DIE_THEN_THE_MAGIC_FORCE_FIELD_OF_BLOOD_WILL", (Object[]) new Object[0]);
		ClanHallSiegeEvent siegeEvent = actor.getEvent(ClanHallSiegeEvent.class);
		if(siegeEvent == null)
		{
			return;
		}
		SpawnExObject spawnExObject = siegeEvent.getFirstObject("boss");
		NpcInstance lidiaNpc = spawnExObject.getFirstSpawned();
		if(lidiaNpc.getCurrentHpRatio() == 1.0)
		{
			lidiaNpc.setCurrentHp((double) (lidiaNpc.getMaxHp() / 2), true);
		}
	}
	
	@Override
	public void onEvtAttacked(Creature attacker, int damage)
	{
		SiegeGuardInstance actor = getActor();
		super.onEvtAttacked(attacker, damage);
		if(PositionUtils.calculateDistance(attacker, actor, false) > 300.0 && Rnd.chance(0.13))
		{
			addTaskCast(attacker, DRAIN_SKILL);
		}
		Creature target = actor.getAggroList().getMostHated();
		if(target == attacker && Rnd.chance(0.3))
		{
			addTaskCast(attacker, DAMAGE_SKILL);
		}
	}
}