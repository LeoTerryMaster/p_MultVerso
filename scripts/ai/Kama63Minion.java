package ai;

import l2.commons.threading.RunnableImpl;
import l2.commons.util.Rnd;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.ai.CtrlEvent;
import l2.gameserver.ai.Fighter;
import l2.gameserver.model.Creature;
import l2.gameserver.model.World;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.scripts.Functions;

import java.util.concurrent.ScheduledFuture;

public class Kama63Minion extends Fighter
{
	private static final int BOSS_ID = 18571;
	private static final int MINION_DIE_TIME = 25000;
	ScheduledFuture<?> _dieTask;
	private long _wait_timeout;
	private NpcInstance _boss;
	private boolean _spawned;
	
	public Kama63Minion(NpcInstance actor)
	{
		super(actor);
	}
	
	@Override
	protected void onEvtSpawn()
	{
		_boss = findBoss(18571);
		super.onEvtSpawn();
	}
	
	@Override
	protected boolean thinkActive()
	{
		if(_boss == null)
		{
			_boss = findBoss(18571);
		}
		else if(!_spawned)
		{
			_spawned = true;
			Functions.npcSayCustomMessage(_boss, "Kama63Boss", (Object[]) new Object[0]);
			NpcInstance minion = getActor();
			minion.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, _boss.getAggroList().getRandomHated(), Rnd.get(1, 100));
			_dieTask = ThreadPoolManager.getInstance().schedule(new DieScheduleTimerTask(minion, _boss), 25000);
		}
		return super.thinkActive();
	}
	
	private NpcInstance findBoss(int npcId)
	{
		if(System.currentTimeMillis() < _wait_timeout)
		{
			return null;
		}
		_wait_timeout = System.currentTimeMillis() + 15000;
		NpcInstance minion = getActor();
		if(minion == null)
		{
			return null;
		}
		for(NpcInstance npc : World.getAroundNpc(minion))
		{
			if(npc.getNpcId() != npcId)
				continue;
			return npc;
		}
		return null;
	}
	
	@Override
	protected void onEvtDead(Creature killer)
	{
		_spawned = false;
		if(_dieTask != null)
		{
			_dieTask.cancel(false);
			_dieTask = null;
		}
		super.onEvtDead(killer);
	}
	
	public class DieScheduleTimerTask extends RunnableImpl
	{
		NpcInstance _minion;
		NpcInstance _master;
		
		public DieScheduleTimerTask(NpcInstance minion, NpcInstance master)
		{
			_minion = null;
			_master = null;
			_minion = minion;
			_master = master;
		}
		
		@Override
		public void runImpl()
		{
			if(_master != null && _minion != null && !_master.isDead() && !_minion.isDead())
			{
				_master.setCurrentHp(_master.getCurrentHp() + _minion.getCurrentHp() * 5.0, false);
			}
			Functions.npcSayCustomMessage(_minion, "Kama63Minion", (Object[]) new Object[0]);
			_minion.doDie(_minion);
		}
	}
}