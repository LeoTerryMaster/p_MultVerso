package npc.model.residences.clanhall;

import l2.commons.threading.RunnableImpl;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.Spawner;
import l2.gameserver.model.World;
import l2.gameserver.model.entity.events.impl.ClanHallMiniGameEvent;
import l2.gameserver.model.entity.events.objects.CMGSiegeClanObject;
import l2.gameserver.model.entity.events.objects.SpawnExObject;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.templates.npc.NpcTemplate;
import l2.gameserver.utils.Location;
import l2.gameserver.utils.NpcUtils;

import java.util.List;

public class RainbowGourdInstance extends NpcInstance
{
	private CMGSiegeClanObject _winner;
	
	public RainbowGourdInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		setHasChatWindow(false);
	}
	
	public void doDecrease(Creature character)
	{
		if(isDead())
		{
			return;
		}
		reduceCurrentHp((double) getMaxHp() * 0.2, character, null, false, false, false, false, false, false, false);
	}
	
	public void doHeal()
	{
		if(isDead())
		{
			return;
		}
		setCurrentHp(getCurrentHp() + (double) getMaxHp() * 0.2, false);
	}
	
	public void doSwitch(RainbowGourdInstance npc)
	{
		if(isDead() || npc.isDead())
		{
			return;
		}
		double currentHp = getCurrentHp();
		setCurrentHp(npc.getCurrentHp(), false);
		npc.setCurrentHp(currentHp, false);
	}
	
	@Override
	public void onDeath(Creature killer)
	{
		super.onDeath(killer);
		ClanHallMiniGameEvent miniGameEvent = getEvent(ClanHallMiniGameEvent.class);
		if(miniGameEvent == null)
		{
			return;
		}
		Player player = killer.getPlayer();
		CMGSiegeClanObject siegeClanObject = miniGameEvent.getSiegeClan("attackers", player.getClan());
		if(siegeClanObject == null)
		{
			return;
		}
		_winner = siegeClanObject;
		List attackers = miniGameEvent.getObjects("attackers");
		for(int i = 0;i < attackers.size();++i)
		{
			if(attackers.get(i) == siegeClanObject)
				continue;
			String arenaName = "arena_" + i;
			SpawnExObject spawnEx = miniGameEvent.getFirstObject(arenaName);
			RainbowYetiInstance yetiInstance = (RainbowYetiInstance) spawnEx.getSpawns().get(0).getFirstSpawned();
			yetiInstance.teleportFromArena();
			miniGameEvent.spawnAction(arenaName, false);
		}
	}
	
	@Override
	public void onDecay()
	{
		super.onDecay();
		ClanHallMiniGameEvent miniGameEvent = getEvent(ClanHallMiniGameEvent.class);
		if(miniGameEvent == null)
		{
			return;
		}
		if(_winner == null)
		{
			return;
		}
		List attackers = miniGameEvent.getObjects("attackers");
		int index = attackers.indexOf(_winner);
		String arenaName = "arena_" + index;
		miniGameEvent.spawnAction(arenaName, false);
		SpawnExObject spawnEx = miniGameEvent.getFirstObject(arenaName);
		Spawner spawner = spawnEx.getSpawns().get(0);
		Location loc = (Location) spawner.getCurrentSpawnRange();
		miniGameEvent.removeBanishItems();
		NpcInstance npc = NpcUtils.spawnSingle(35600, loc.x, loc.y, loc.z, (long) 0);
		ThreadPoolManager.getInstance().schedule(new RunnableImpl()
		{
			
			@Override
			public void runImpl() throws Exception
			{
				List<Player> around = World.getAroundPlayers(npc, 750, 100);
				npc.deleteMe();
				for(Player player : around)
				{
					player.teleToLocation(miniGameEvent.getResidence().getOwnerRestartPoint());
				}
				miniGameEvent.processStep(_winner.getClan());
			}
		}, 10000);
	}
	
	@Override
	public boolean isAttackable(Creature c)
	{
		return false;
	}
	
	@Override
	public boolean isAutoAttackable(Creature c)
	{
		return false;
	}
	
	@Override
	public boolean isInvul()
	{
		return false;
	}
}