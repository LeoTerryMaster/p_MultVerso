package l2.gameserver.ai;

import l2.commons.util.Rnd;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.entity.events.impl.SiegeEvent;
import l2.gameserver.model.instances.DoorInstance;
import l2.gameserver.model.instances.NpcInstance;

public class DoorAI extends CharacterAI
{
	public DoorAI(DoorInstance actor)
	{
		super(actor);
	}
	
	public void onEvtTwiceClick(Player player)
	{
	}
	
	public void onEvtOpen(Player player)
	{
	}
	
	public void onEvtClose(Player player)
	{
	}
	
	@Override
	public DoorInstance getActor()
	{
		return (DoorInstance) super.getActor();
	}
	
	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		DoorInstance actor;
		if(attacker == null || (actor = getActor()) == null)
		{
			return;
		}
		Player player = attacker.getPlayer();
		if(player == null)
		{
			return;
		}
		SiegeEvent siegeEvent1 = player.getEvent(SiegeEvent.class);
		SiegeEvent siegeEvent2 = actor.getEvent(SiegeEvent.class);
		if(siegeEvent1 == null || siegeEvent1 == siegeEvent2 && siegeEvent1.getSiegeClan("attackers", player.getClan()) != null)
		{
			for(NpcInstance npc : actor.getAroundNpc(900, 200))
			{
				if(!npc.isSiegeGuard())
					continue;
				if(Rnd.chance(20))
				{
					npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, 10000);
					continue;
				}
				npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, 2000);
			}
		}
	}
}