package ai.residences.clanhall;

import ai.residences.SiegeGuardMystic;
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

public class GiselleVonHellmann extends SiegeGuardMystic
{
	private static final Skill DAMAGE_SKILL = SkillTable.getInstance().getInfo(5003, 1);
	private static final Zone ZONE_1 = ReflectionUtils.getZone((String) "lidia_zone1");
	private static final Zone ZONE_2 = ReflectionUtils.getZone((String) "lidia_zone2");
	
	public GiselleVonHellmann(NpcInstance actor)
	{
		super(actor);
	}
	
	@Override
	public void onEvtSpawn()
	{
		super.onEvtSpawn();
		ZONE_1.setActive(true);
		ZONE_2.setActive(true);
		Functions.npcShoutCustomMessage(getActor(), "clanhall.GiselleVonHellmann.ARISE_MY_FAITHFUL_SERVANTS_YOU_MY_PEOPLE_WHO_HAVE_INHERITED_THE_BLOOD", (Object[]) new Object[0]);
	}
	
	@Override
	public void onEvtDead(Creature killer)
	{
		SiegeGuardInstance actor = getActor();
		super.onEvtDead(killer);
		ZONE_1.setActive(false);
		ZONE_2.setActive(false);
		Functions.npcShoutCustomMessage(actor, "clanhall.GiselleVonHellmann.AARGH_IF_I_DIE_THEN_THE_MAGIC_FORCE_FIELD_OF_BLOOD_WILL", (Object[]) new Object[0]);
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
			addTaskCast(attacker, DAMAGE_SKILL);
		}
	}
}