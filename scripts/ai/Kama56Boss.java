package ai;

import l2.commons.collections.LazyArrayList;
import l2.commons.lang.reference.HardReference;
import l2.commons.lang.reference.HardReferences;
import l2.commons.util.Rnd;
import l2.gameserver.ai.CtrlEvent;
import l2.gameserver.ai.Fighter;
import l2.gameserver.model.MinionList;
import l2.gameserver.model.Player;
import l2.gameserver.model.World;
import l2.gameserver.model.instances.MinionInstance;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.scripts.Functions;

import java.util.List;

public class Kama56Boss extends Fighter
{
	private long _nextOrderTime;
	private HardReference<Player> _lastMinionsTargetRef = HardReferences.emptyRef();
	
	public Kama56Boss(NpcInstance actor)
	{
		super(actor);
	}
	
	private void sendOrderToMinions(NpcInstance actor)
	{
		if(!actor.isInCombat())
		{
			_lastMinionsTargetRef = HardReferences.emptyRef();
			return;
		}
		MinionList ml = actor.getMinionList();
		if(ml == null || !ml.hasMinions())
		{
			_lastMinionsTargetRef = HardReferences.emptyRef();
			return;
		}
		long now = System.currentTimeMillis();
		Player old_target;
		if(_nextOrderTime > now && _lastMinionsTargetRef.get() != null && (old_target = _lastMinionsTargetRef.get()) != null && !old_target.isAlikeDead())
		{
			for(MinionInstance m : ml.getAliveMinions())
			{
				if(m.getAI().getAttackTarget() == old_target)
					continue;
				m.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, old_target, 10000000);
			}
			return;
		}
		_nextOrderTime = now + 30000;
		List<Player> pl = World.getAroundPlayers(actor);
		if(pl.isEmpty())
		{
			_lastMinionsTargetRef = HardReferences.emptyRef();
			return;
		}
		LazyArrayList alive = new LazyArrayList();
		for(Player p : pl)
		{
			if(p.isAlikeDead())
				continue;
			alive.add(p);
		}
		if(alive.isEmpty())
		{
			_lastMinionsTargetRef = HardReferences.emptyRef();
			return;
		}
		Player target = (Player) alive.get(Rnd.get(alive.size()));
		_lastMinionsTargetRef = target.getRef();
		Functions.npcSayCustomMessage(actor, "Kama56Boss.attack", (Object[]) new Object[] {target.getName()});
		for(MinionInstance m : ml.getAliveMinions())
		{
			m.getAggroList().clear();
			m.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, target, 10000000);
		}
	}
	
	@Override
	protected void thinkAttack()
	{
		NpcInstance actor = getActor();
		if(actor == null)
		{
			return;
		}
		sendOrderToMinions(actor);
		super.thinkAttack();
	}
}