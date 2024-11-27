package ai;

import l2.commons.lang.reference.HardReference;
import l2.commons.lang.reference.HardReferences;
import l2.commons.util.Rnd;
import l2.gameserver.ai.CtrlEvent;
import l2.gameserver.ai.Fighter;
import l2.gameserver.model.Creature;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.scripts.Functions;

public class WatchmanMonster extends Fighter
{
	static final String[] flood = {"I'll be back", "You are stronger than expected"};
	static final String[] flood2 = {"Help me!", "Alarm! We are under attack!"};
	private long _lastSearch;
	private boolean isSearching;
	private HardReference<? extends Creature> _attackerRef = HardReferences.emptyRef();
	
	public WatchmanMonster(NpcInstance actor)
	{
		super(actor);
	}
	
	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		NpcInstance actor = getActor();
		if(attacker != null && !actor.getFaction().isNone() && actor.getCurrentHpPercents() < 50.0 && _lastSearch < System.currentTimeMillis() - 15000)
		{
			_lastSearch = System.currentTimeMillis();
			_attackerRef = attacker.getRef();
			if(findHelp())
			{
				return;
			}
			Functions.npcSay(actor, "Anyone, help me!");
		}
		super.onEvtAttacked(attacker, damage);
	}
	
	private boolean findHelp()
	{
		isSearching = false;
		NpcInstance actor = getActor();
		Creature attacker = _attackerRef.get();
		if(attacker == null)
		{
			return false;
		}
		for(NpcInstance npc : actor.getAroundNpc(1000, 150))
		{
			if(actor.isDead() || !npc.isInFaction(actor) || npc.isInCombat())
				continue;
			clearTasks();
			isSearching = true;
			addTaskMove(npc.getLoc(), true);
			Functions.npcSay(actor, flood[Rnd.get(flood.length)]);
			return true;
		}
		return false;
	}
	
	@Override
	protected void onEvtDead(Creature killer)
	{
		_lastSearch = 0;
		_attackerRef = HardReferences.emptyRef();
		isSearching = false;
		super.onEvtDead(killer);
	}
	
	@Override
	protected void onEvtArrived()
	{
		NpcInstance actor = getActor();
		if(isSearching)
		{
			Creature attacker = _attackerRef.get();
			if(attacker != null)
			{
				Functions.npcSay(actor, flood2[Rnd.get(flood2.length)]);
				notifyFriends(attacker, 100);
			}
			isSearching = false;
			notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, 100);
		}
		else
		{
			super.onEvtArrived();
		}
	}
	
	@Override
	protected void onEvtAggression(Creature target, int aggro)
	{
		if(!isSearching)
		{
			super.onEvtAggression(target, aggro);
		}
	}
}