package ai;

import l2.commons.util.Rnd;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.ai.DefaultAI;
import l2.gameserver.model.Creature;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.scripts.Functions;
import l2.gameserver.utils.ItemFunctions;
import l2.gameserver.utils.Location;

import java.lang.ref.WeakReference;
import java.util.concurrent.ScheduledFuture;

public class FlyingSantaAI extends DefaultAI
{
	private static final int ActorHeight = 100;
	private static final long ForceDeleteTime = 300000;
	private static final double RandomFraseChance = 50.0;
	private static final String[] RandomFrase = {"Хо-Хо-Хо", "С Праздниками Вас!", "Ловите подарки :D"};
	private static final double ItemDropChance = 50.0;
	private static final int[] ItemDropIds = {57};
	private static final int[] ItemDropCount = {1};
	private static final Location[][] _paths = {{new Location(82632, 149128, -3472), new Location(81051, 149288, -3472), new Location(81051, 148062, -3472), new Location(82904, 148056, -3472)}, {new Location(148408, 27928, -2272), new Location(146504, 27928, -2272)}, {new Location(146400, -54903, -2807), new Location(147694, -56555, -2807), new Location(148613, -55572, -2807)}};
	private int _curr_path_idx;
	private int _curr_point_idx;
	private ScheduledFuture<?> _delete_task;
	
	public FlyingSantaAI(NpcInstance actor)
	{
		super(actor);
	}
	
	private static int getNearestPath(Location curr)
	{
		int path_idx = -1;
		double curdistsq = Double.MAX_VALUE;
		for(int pathes = 0;pathes < _paths.length;++pathes)
		{
			for(int pnt_idx = 0;pnt_idx < _paths[pathes].length;++pnt_idx)
			{
				double distsq = curr.distance(_paths[pathes][pnt_idx]);
				if(distsq >= curdistsq)
					continue;
				curdistsq = distsq;
				path_idx = pathes;
			}
		}
		return path_idx != -1 ? path_idx : 0;
	}
	
	@Override
	protected void onEvtSpawn()
	{
		NpcInstance actor = getActor();
		if(actor == null)
		{
			return;
		}
		actor.setCollisionHeight(100.0);
		actor.setWalking();
		_curr_path_idx = getNearestPath(actor.getSpawnedLoc());
		_curr_point_idx = 0;
		addTaskMove(_paths[_curr_path_idx][_curr_point_idx], false);
		_delete_task = ThreadPoolManager.getInstance().schedule(new UnspawnTask(actor), 300000);
		doTask();
	}
	
	private boolean moveNext()
	{
		NpcInstance actor = getActor();
		if(actor == null)
		{
			return false;
		}
		++_curr_point_idx;
		if(_curr_point_idx < _paths[_curr_path_idx].length)
		{
			if(Rnd.chance(50.0))
			{
				Functions.npcShout(actor, RandomFrase[Rnd.get(RandomFrase.length)]);
			}
			if(Rnd.chance(50.0))
			{
				int item_idx = Rnd.get(ItemDropIds.length);
				ItemInstance item = ItemFunctions.createItem(ItemDropIds[item_idx]);
				item.setCount((long) ItemDropCount[item_idx]);
				item.dropToTheGround(actor, Location.coordsRandomize(actor.getLoc(), 10, 50));
			}
			Location next = _paths[_curr_path_idx][_curr_point_idx];
			addTaskMove(next, false);
			return doTask();
		}
		if(_delete_task != null)
		{
			_delete_task.cancel(true);
			_delete_task = null;
		}
		actor.decayMe();
		actor.deleteMe();
		return false;
	}
	
	@Override
	protected boolean thinkActive()
	{
		NpcInstance actor = getActor();
		if(actor == null || actor.isDead())
		{
			return true;
		}
		if(_def_think)
		{
			doTask();
			return true;
		}
		return moveNext();
	}
	
	@Override
	public boolean isGlobalAI()
	{
		return false;
	}
	
	@Override
	protected void onEvtArrived()
	{
		super.onEvtArrived();
		moveNext();
	}
	
	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
	}
	
	@Override
	protected void onEvtAggression(Creature target, int aggro)
	{
	}
	
	private class UnspawnTask implements Runnable
	{
		private final WeakReference<NpcInstance> _actor_ref;
		
		public UnspawnTask(NpcInstance actor)
		{
			_actor_ref = new WeakReference<>(actor);
		}
		
		@Override
		public void run()
		{
			if(_actor_ref != null && _actor_ref.get() != null)
			{
				_actor_ref.get().decayMe();
				_actor_ref.get().deleteMe();
			}
		}
	}
}