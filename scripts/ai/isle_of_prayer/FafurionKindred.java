package ai.isle_of_prayer;

import l2.commons.threading.RunnableImpl;
import l2.commons.util.Rnd;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.ai.Fighter;
import l2.gameserver.data.xml.holder.NpcHolder;
import l2.gameserver.model.Creature;
import l2.gameserver.model.SimpleSpawner;
import l2.gameserver.model.Skill;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.stats.Stats;
import l2.gameserver.stats.funcs.EFunction;
import l2.gameserver.stats.funcs.FuncTemplate;
import l2.gameserver.utils.ItemFunctions;
import l2.gameserver.utils.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

public class FafurionKindred extends Fighter
{
	private static final int DETRACTOR1 = 22270;
	private static final int DETRACTOR2 = 22271;
	private static final int Spirit_of_the_Lake = 2368;
	private static final int Water_Dragon_Scale = 9691;
	private static final int Water_Dragon_Claw = 9700;
	private static final FuncTemplate ft = new FuncTemplate(null, EFunction.Mul, Stats.HEAL_EFFECTIVNESS, 144, 0.0);
	ScheduledFuture<?> poisonTask;
	ScheduledFuture<?> despawnTask;
	List<SimpleSpawner> spawns = new ArrayList<>();
	
	public FafurionKindred(NpcInstance actor)
	{
		super(actor);
		actor.addStatFunc(ft.getFunc(this));
	}
	
	@Override
	protected void onEvtSpawn()
	{
		super.onEvtSpawn();
		spawns.clear();
		ThreadPoolManager.getInstance().schedule(new SpawnTask(22270), 500);
		ThreadPoolManager.getInstance().schedule(new SpawnTask(22271), 500);
		ThreadPoolManager.getInstance().schedule(new SpawnTask(22270), 500);
		ThreadPoolManager.getInstance().schedule(new SpawnTask(22271), 500);
		poisonTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new PoisonTask(), 3000, 3000);
		despawnTask = ThreadPoolManager.getInstance().schedule(new DeSpawnTask(), 300000);
	}
	
	@Override
	protected void onEvtDead(Creature killer)
	{
		cleanUp();
		super.onEvtDead(killer);
	}
	
	@Override
	protected void onEvtSeeSpell(Skill skill, Creature caster)
	{
		NpcInstance actor = getActor();
		if(actor.isDead() || skill == null)
		{
			return;
		}
		if(skill.getId() == 2368)
		{
			actor.setCurrentHp(actor.getCurrentHp() + 3000.0, false);
		}
		actor.getAggroList().remove(caster, true);
	}
	
	@Override
	protected boolean randomWalk()
	{
		return false;
	}
	
	private void cleanUp()
	{
		if(poisonTask != null)
		{
			poisonTask.cancel(false);
			poisonTask = null;
		}
		if(despawnTask != null)
		{
			despawnTask.cancel(false);
			despawnTask = null;
		}
		for(SimpleSpawner spawn : spawns)
		{
			spawn.deleteAll();
		}
		spawns.clear();
	}
	
	private void dropItem(NpcInstance actor, int id, int count)
	{
		ItemInstance item = ItemFunctions.createItem(id);
		item.setCount((long) count);
		item.dropToTheGround(actor, Location.findPointToStay(actor, 100));
	}
	
	private class DeSpawnTask extends RunnableImpl
	{
		@Override
		public void runImpl()
		{
			NpcInstance actor = getActor();
			dropItem(actor, 9691, Rnd.get(1, 2));
			if(Rnd.chance(36))
			{
				dropItem(actor, 9700, Rnd.get(1, 3));
			}
			cleanUp();
			actor.deleteMe();
		}
	}
	
	private class PoisonTask extends RunnableImpl
	{
		@Override
		public void runImpl()
		{
			NpcInstance actor = getActor();
			actor.reduceCurrentHp(500.0, actor, null, true, false, true, false, false, false, false);
		}
	}
	
	private class SpawnTask extends RunnableImpl
	{
		private final int _id;
		
		public SpawnTask(int id)
		{
			_id = id;
		}
		
		@Override
		public void runImpl()
		{
			NpcInstance actor = getActor();
			SimpleSpawner sp = new SimpleSpawner(NpcHolder.getInstance().getTemplate(_id));
			sp.setLoc(Location.findPointToStay(actor, 100, 120));
			sp.setRespawnDelay(30, 40);
			sp.doSpawn(true);
			spawns.add(sp);
		}
	}
}