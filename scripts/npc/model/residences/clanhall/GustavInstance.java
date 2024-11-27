package npc.model.residences.clanhall;

import l2.commons.threading.RunnableImpl;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.ai.CtrlEvent;
import l2.gameserver.model.AggroList;
import l2.gameserver.model.Creature;
import l2.gameserver.model.GameObjectTasks;
import l2.gameserver.model.Playable;
import l2.gameserver.model.Player;
import l2.gameserver.model.World;
import l2.gameserver.model.entity.events.impl.ClanHallSiegeEvent;
import l2.gameserver.model.entity.events.impl.SiegeEvent;
import l2.gameserver.model.entity.events.objects.SpawnExObject;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.pledge.Clan;
import l2.gameserver.network.l2.s2c.MagicSkillUse;
import l2.gameserver.scripts.Functions;
import l2.gameserver.templates.npc.NpcTemplate;
import l2.gameserver.utils.Location;
import npc.model.residences.SiegeGuardInstance;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

public class GustavInstance extends SiegeGuardInstance implements _34SiegeGuard
{
	private final AtomicBoolean _canDead = new AtomicBoolean();
	private Future<?> _teleportTask;
	
	public GustavInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onSpawn()
	{
		super.onSpawn();
		_canDead.set(false);
		Functions.npcShoutCustomMessage(this, "clanhall.siege.GustavInstance.PREPARE_TO_DIE_FOREIGN_INVADERS_I_AM_GUSTAV_THE_ETERNAL_RULER_OF_THIS_FORTRESS_AND_I_HAVE_TAKEN_UP_MY_SWORD_TO_REPEL_THEE", (Object[]) new Object[0]);
	}
	
	@Override
	public void onDeath(Creature killer)
	{
		if(!_canDead.get())
		{
			_canDead.set(true);
			setCurrentHp(1.0, true);
			for(Creature cha : World.getAroundCharacters(this))
			{
				ThreadPoolManager.getInstance().execute(new GameObjectTasks.NotifyAITask(cha, CtrlEvent.EVT_FORGET_OBJECT, this, null));
			}
			ClanHallSiegeEvent siegeEvent = getEvent(ClanHallSiegeEvent.class);
			if(siegeEvent == null)
			{
				return;
			}
			SpawnExObject obj = siegeEvent.getFirstObject("boss");
			for(int i = 0;i < 3;++i)
			{
				NpcInstance npc = obj.getSpawns().get(i).getFirstSpawned();
				Functions.npcSay(npc, ((_34SiegeGuard) npc).teleChatSay());
				npc.broadcastPacket(new MagicSkillUse(npc, npc, 4235, 1, 10000, 0));
				_teleportTask = ThreadPoolManager.getInstance().schedule(new RunnableImpl()
				{
					
					@Override
					public void runImpl() throws Exception
					{
						Location loc = Location.findAroundPosition(177134, -18807, -2256, 50, 100, npc.getGeoIndex());
						npc.teleToLocation(loc);
						if(npc == GustavInstance.this)
						{
							npc.reduceCurrentHp(npc.getCurrentHp(), npc, null, false, false, false, false, false, false, false);
						}
					}
				}, 10000);
			}
		}
		else
		{
			if(_teleportTask != null)
			{
				_teleportTask.cancel(false);
				_teleportTask = null;
			}
			SiegeEvent siegeEvent;
			if((siegeEvent = getEvent(SiegeEvent.class)) == null)
			{
				return;
			}
			siegeEvent.processStep(getMostDamagedClan());
			super.onDeath(killer);
		}
	}
	
	public Clan getMostDamagedClan()
	{
		ClanHallSiegeEvent siegeEvent = getEvent(ClanHallSiegeEvent.class);
		Player temp = null;
		HashMap<Player, Integer> damageMap = new HashMap<>();
		for(AggroList.HateInfo info : getAggroList().getPlayableMap().values())
		{
			Playable killer = (Playable) info.attacker;
			int damage = info.damage;
			if(killer.isPet() || killer.isSummon())
			{
				temp = killer.getPlayer();
			}
			else if(killer.isPlayer())
			{
				temp = (Player) killer;
			}
			if(temp == null || siegeEvent.getSiegeClan("attackers", temp.getClan()) == null)
				continue;
			if(!damageMap.containsKey(temp))
			{
				damageMap.put(temp, damage);
				continue;
			}
			int dmg = damageMap.get(temp) + damage;
			damageMap.put(temp, dmg);
		}
		int mostDamage = 0;
		Player player = null;
		for(Map.Entry entry : damageMap.entrySet())
		{
			int damage = (Integer) entry.getValue();
			Player t = (Player) entry.getKey();
			if(damage <= mostDamage)
				continue;
			mostDamage = damage;
			player = t;
		}
		return player == null ? null : player.getClan();
	}
	
	@Override
	public String teleChatSay()
	{
		return "This is unbelievable! Have I really been defeated? I shall return and take your head!";
	}
	
	@Override
	public boolean isEffectImmune()
	{
		return true;
	}
}