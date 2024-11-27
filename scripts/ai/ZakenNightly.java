package ai;

import l2.commons.threading.RunnableImpl;
import l2.commons.util.Rnd;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.ai.Fighter;
import l2.gameserver.model.Creature;
import l2.gameserver.model.entity.Reflection;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.network.l2.s2c.PlaySound;
import l2.gameserver.tables.SkillTable;
import l2.gameserver.utils.Location;

public class ZakenNightly extends Fighter
{
	private static final int doll_blader_b = 29023;
	private static final int vale_master_b = 29024;
	private static final int pirates_zombie_captain_b = 29026;
	private static final int pirates_zombie_b = 29027;
	private static final Location[] _locations = {new Location(53950, 219860, -3496), new Location(55980, 219820, -3496), new Location(54950, 218790, -3496), new Location(55970, 217770, -3496), new Location(53930, 217760, -3496), new Location(55970, 217770, -3224), new Location(55980, 219920, -3224), new Location(54960, 218790, -3224), new Location(53950, 219860, -3224), new Location(53930, 217760, -3224), new Location(55970, 217770, -2952), new Location(55980, 219920, -2952), new Location(54960, 218790, -2952), new Location(53950, 219860, -2952), new Location(53930, 217760, -2952)};
	private final long _teleportSelfReuse = 30000;
	private final NpcInstance actor;
	private long _teleportSelfTimer;
	private int _stage;
	
	public ZakenNightly(NpcInstance actor)
	{
		super(actor);
		this.actor = getActor();
		_stage = 0;
		MAX_PURSUE_RANGE = 1073741823;
	}
	
	@Override
	protected void thinkAttack()
	{
		if(_teleportSelfTimer + _teleportSelfReuse < System.currentTimeMillis())
		{
			_teleportSelfTimer = System.currentTimeMillis();
			if(Rnd.chance(20))
			{
				actor.doCast(SkillTable.getInstance().getInfo(4222, 1), actor, false);
				ThreadPoolManager.getInstance().schedule(new RunnableImpl()
				{
					
					@Override
					public void runImpl()
					{
						actor.teleToLocation(_locations[Rnd.get(_locations.length)]);
						actor.getAggroList().clear(true);
					}
				}, 500);
			}
		}
		double actor_hp_precent = actor.getCurrentHpPercents();
		Reflection r = actor.getReflection();
		switch(_stage)
		{
			case 0:
			{
				if(actor_hp_precent >= 90.0)
					break;
				r.addSpawnWithoutRespawn(29026, actor.getLoc(), 300);
				++_stage;
				break;
			}
			case 1:
			{
				if(actor_hp_precent >= 80.0)
					break;
				r.addSpawnWithoutRespawn(29023, actor.getLoc(), 300);
				++_stage;
				break;
			}
			case 2:
			{
				if(actor_hp_precent >= 70.0)
					break;
				r.addSpawnWithoutRespawn(29024, actor.getLoc(), 300);
				r.addSpawnWithoutRespawn(29024, actor.getLoc(), 300);
				++_stage;
				break;
			}
			case 3:
			{
				if(actor_hp_precent >= 60.0)
					break;
				for(int i = 0;i < 5;++i)
				{
					r.addSpawnWithoutRespawn(29027, actor.getLoc(), 300);
				}
				++_stage;
				break;
			}
			case 4:
			{
				if(actor_hp_precent >= 50.0)
					break;
				for(int i = 0;i < 5;++i)
				{
					r.addSpawnWithoutRespawn(29023, actor.getLoc(), 300);
					r.addSpawnWithoutRespawn(29027, actor.getLoc(), 300);
					r.addSpawnWithoutRespawn(29024, actor.getLoc(), 300);
					r.addSpawnWithoutRespawn(29026, actor.getLoc(), 300);
				}
				++_stage;
				break;
			}
			case 5:
			{
				if(actor_hp_precent >= 40.0)
					break;
				for(int i = 0;i < 6;++i)
				{
					r.addSpawnWithoutRespawn(29023, actor.getLoc(), 300);
					r.addSpawnWithoutRespawn(29027, actor.getLoc(), 300);
					r.addSpawnWithoutRespawn(29024, actor.getLoc(), 300);
					r.addSpawnWithoutRespawn(29026, actor.getLoc(), 300);
				}
				++_stage;
				break;
			}
			case 6:
			{
				if(actor_hp_precent >= 30.0)
					break;
				for(int i = 0;i < 7;++i)
				{
					r.addSpawnWithoutRespawn(29023, actor.getLoc(), 300);
					r.addSpawnWithoutRespawn(29027, actor.getLoc(), 300);
					r.addSpawnWithoutRespawn(29024, actor.getLoc(), 300);
					r.addSpawnWithoutRespawn(29026, actor.getLoc(), 300);
				}
				++_stage;
				break;
			}
		}
		super.thinkAttack();
	}
	
	@Override
	protected void onEvtDead(Creature killer)
	{
		Reflection r = actor.getReflection();
		r.setReenterTime(System.currentTimeMillis());
		actor.broadcastPacket(new PlaySound(PlaySound.Type.MUSIC, "BS02_D", 1, actor.getObjectId(), actor.getLoc()));
		super.onEvtDead(killer);
	}
	
	@Override
	protected void teleportHome()
	{
	}
}