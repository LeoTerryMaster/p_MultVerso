package ai.residences.clanhall;

import l2.commons.threading.RunnableImpl;
import l2.commons.util.Rnd;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.ai.CharacterAI;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.entity.events.impl.ClanHallMiniGameEvent;
import l2.gameserver.model.entity.events.objects.CMGSiegeClanObject;
import l2.gameserver.model.entity.events.objects.SpawnExObject;
import l2.gameserver.model.entity.events.objects.ZoneObject;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.utils.NpcUtils;
import npc.model.residences.clanhall.RainbowGourdInstance;
import npc.model.residences.clanhall.RainbowYetiInstance;

import java.util.List;

public class RainbowYeti extends CharacterAI
{
	public RainbowYeti(NpcInstance actor)
	{
		super(actor);
	}
	
	@Override
	public void onEvtSeeSpell(Skill skill, Creature character)
	{
		RainbowYetiInstance actor = (RainbowYetiInstance) getActor();
		ClanHallMiniGameEvent miniGameEvent = actor.getEvent(ClanHallMiniGameEvent.class);
		if(miniGameEvent == null)
		{
			return;
		}
		if(!character.isPlayer())
		{
			return;
		}
		Player player = character.getPlayer();
		CMGSiegeClanObject siegeClan = null;
		List<CMGSiegeClanObject> attackers = miniGameEvent.getObjects("attackers");
		for(CMGSiegeClanObject o : attackers)
		{
			if(!o.isParticle(player))
				continue;
			siegeClan = o;
		}
		if(siegeClan == null)
		{
			return;
		}
		int index = attackers.indexOf(siegeClan);
		int warIndex;
		RainbowGourdInstance gourdInstance;
		RainbowGourdInstance gourdInstance2;
		switch(skill.getId())
		{
			case 2240:
			{
				if(Rnd.chance(90))
				{
					gourdInstance = getGourd(index);
					if(gourdInstance == null)
					{
						return;
					}
					gourdInstance.doDecrease(player);
					break;
				}
				actor.addMob(NpcUtils.spawnSingle(35592, actor.getX() + 10, actor.getY() + 10, actor.getZ(), (long) 0));
				break;
			}
			case 2241:
			{
				warIndex = rndEx(attackers.size(), index);
				if(warIndex == Integer.MIN_VALUE)
				{
					return;
				}
				gourdInstance2 = getGourd(warIndex);
				if(gourdInstance2 == null)
				{
					return;
				}
				gourdInstance2.doHeal();
				break;
			}
			case 2242:
			{
				warIndex = rndEx(attackers.size(), index);
				if(warIndex == Integer.MIN_VALUE)
				{
					return;
				}
				gourdInstance = getGourd(index);
				gourdInstance2 = getGourd(warIndex);
				if(gourdInstance2 == null || gourdInstance == null)
				{
					return;
				}
				gourdInstance.doSwitch(gourdInstance2);
				break;
			}
			case 2243:
			{
				warIndex = rndEx(attackers.size(), index);
				if(warIndex == Integer.MIN_VALUE)
				{
					return;
				}
				ZoneObject zone = miniGameEvent.getFirstObject("zone_" + warIndex);
				if(zone == null)
				{
					return;
				}
				zone.setActive(true);
				ThreadPoolManager.getInstance().schedule(new ZoneDeactive(zone), 60000);
			}
		}
	}
	
	private RainbowGourdInstance getGourd(int index)
	{
		ClanHallMiniGameEvent miniGameEvent = getActor().getEvent(ClanHallMiniGameEvent.class);
		SpawnExObject spawnEx = miniGameEvent.getFirstObject("arena_" + index);
		return (RainbowGourdInstance) spawnEx.getSpawns().get(1).getFirstSpawned();
	}
	
	private int rndEx(int size, int ex)
	{
		int rnd = Integer.MIN_VALUE;
		for(int i = 0;i < 127 && (rnd = Rnd.get(size)) == ex;++i)
		{
		}
		return rnd;
	}
	
	private static class ZoneDeactive extends RunnableImpl
	{
		private final ZoneObject _zone;
		
		public ZoneDeactive(ZoneObject zone)
		{
			_zone = zone;
		}
		
		@Override
		public void runImpl() throws Exception
		{
			_zone.setActive(false);
		}
	}
}