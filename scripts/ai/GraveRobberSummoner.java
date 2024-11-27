package ai;

import l2.commons.util.Rnd;
import l2.gameserver.ai.Mystic;
import l2.gameserver.model.Creature;
import l2.gameserver.model.instances.MonsterInstance;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.stats.Env;
import l2.gameserver.stats.Stats;
import l2.gameserver.stats.funcs.Func;
import l2.gameserver.templates.npc.MinionData;

public class GraveRobberSummoner extends Mystic
{
	private static final int[] Servitors = {22683, 22684, 22685, 22686};
	private int _lastMinionCount = 1;
	
	public GraveRobberSummoner(NpcInstance actor)
	{
		super(actor);
		actor.addStatFunc(new FuncMulMinionCount(Stats.MAGIC_DEFENCE, 48, actor));
		actor.addStatFunc(new FuncMulMinionCount(Stats.POWER_DEFENCE, 48, actor));
	}
	
	@Override
	protected void onEvtSpawn()
	{
		super.onEvtSpawn();
		NpcInstance actor = getActor();
		actor.getMinionList().addMinion(new MinionData(Servitors[Rnd.get(Servitors.length)], Rnd.get(2)));
		_lastMinionCount = Math.max(actor.getMinionList().getAliveMinions().size(), 1);
	}
	
	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		MonsterInstance actor = (MonsterInstance) getActor();
		if(actor.isDead())
		{
			return;
		}
		_lastMinionCount = Math.max(actor.getMinionList().getAliveMinions().size(), 1);
		super.onEvtAttacked(attacker, damage);
	}
	
	@Override
	protected void onEvtDead(Creature killer)
	{
		NpcInstance actor = getActor();
		actor.getMinionList().deleteMinions();
		super.onEvtDead(killer);
	}
	
	private class FuncMulMinionCount extends Func
	{
		public FuncMulMinionCount(Stats stat, int order, Object owner)
		{
			super(stat, order, owner);
		}
		
		@Override
		public void calc(Env env)
		{
			env.value *= (double) _lastMinionCount;
		}
	}
}