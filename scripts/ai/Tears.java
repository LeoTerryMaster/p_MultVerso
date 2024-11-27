package ai;

import gnu.trove.TIntObjectHashMap;
import l2.commons.threading.RunnableImpl;
import l2.commons.util.Rnd;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.ai.CtrlEvent;
import l2.gameserver.ai.DefaultAI;
import l2.gameserver.data.xml.holder.NpcHolder;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Party;
import l2.gameserver.model.Player;
import l2.gameserver.model.SimpleSpawner;
import l2.gameserver.model.Skill;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.network.l2.s2c.MagicSkillUse;
import l2.gameserver.scripts.Functions;
import l2.gameserver.utils.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

public class Tears extends DefaultAI
{
	private static final int Water_Dragon_Scale = 2369;
	private static final int Tears_Copy = 25535;
	final Skill Invincible;
	final Skill Freezing;
	ScheduledFuture<?> spawnTask;
	ScheduledFuture<?> despawnTask;
	List<NpcInstance> spawns = new ArrayList<>();
	private boolean _isUsedInvincible;
	private int _scale_count;
	private long _last_scale_time;
	
	public Tears(NpcInstance actor)
	{
		super(actor);
		TIntObjectHashMap skills = getActor().getTemplate().getSkills();
		Invincible = (Skill) skills.get(5420);
		Freezing = (Skill) skills.get(5238);
	}
	
	@Override
	protected void onEvtSeeSpell(Skill skill, Creature caster)
	{
		NpcInstance actor = getActor();
		if(actor.isDead() || skill == null || caster == null)
		{
			return;
		}
		if(System.currentTimeMillis() - _last_scale_time > 5000)
		{
			_scale_count = 0;
		}
		if(skill.getId() == 2369)
		{
			++_scale_count;
			_last_scale_time = System.currentTimeMillis();
		}
		Player player;
		if((player = caster.getPlayer()) == null)
		{
			return;
		}
		int count = 1;
		Party party = player.getParty();
		if(party != null)
		{
			count = party.getMemberCount();
		}
		if(_scale_count >= count)
		{
			_scale_count = 0;
			actor.getEffectList().stopEffect(Invincible);
		}
	}
	
	@Override
	protected boolean createNewTask()
	{
		clearTasks();
		Creature target = prepareTarget();
		if(target == null)
		{
			return false;
		}
		NpcInstance actor = getActor();
		if(actor.isDead())
		{
			return false;
		}
		double distance = actor.getDistance(target);
		double actor_hp_precent = actor.getCurrentHpPercents();
		int rnd_per = Rnd.get(100);
		if(actor_hp_precent < 15.0 && !_isUsedInvincible)
		{
			_isUsedInvincible = true;
			addTaskBuff(actor, Invincible);
			Functions.npcSay(actor, "Готовьтесь к смерти!!!");
			return true;
		}
		if(rnd_per < 5 && spawnTask == null && despawnTask == null)
		{
			actor.broadcastPacketToOthers(new MagicSkillUse(actor, actor, 5441, 1, 3000, 0));
			spawnTask = ThreadPoolManager.getInstance().schedule(new SpawnMobsTask(), 3000);
			return true;
		}
		if(!actor.isAMuted() && rnd_per < 75)
		{
			return chooseTaskAndTargets(null, target, distance);
		}
		return chooseTaskAndTargets(Freezing, target, distance);
	}
	
	private void spawnMobs()
	{
		Location pos;
		Creature hated;
		NpcInstance actor = getActor();
		for(int i = 0;i < 9;++i)
		{
			try
			{
				pos = Location.findPointToStay(144298, 154420, -11854, 300, 320, actor.getGeoIndex());
				SimpleSpawner sp = new SimpleSpawner(NpcHolder.getInstance().getTemplate(25535));
				sp.setLoc(pos);
				sp.setReflection(actor.getReflection());
				NpcInstance copy = sp.doSpawn(true);
				spawns.add(copy);
				hated = actor.getAggroList().getRandomHated();
				if(hated == null)
					continue;
				copy.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, hated, Rnd.get(1, 100));
				continue;
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		pos = Location.findPointToStay(144298, 154420, -11854, 300, 320, actor.getReflectionId());
		actor.teleToLocation(pos);
		hated = actor.getAggroList().getRandomHated();
		if(hated != null)
		{
			actor.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, hated, Rnd.get(1, 100));
		}
		if(despawnTask != null)
		{
			despawnTask.cancel(false);
		}
		despawnTask = ThreadPoolManager.getInstance().schedule(new DeSpawnTask(), 30000);
	}
	
	@Override
	protected boolean randomWalk()
	{
		return false;
	}
	
	private class SpawnMobsTask extends RunnableImpl
	{
		@Override
		public void runImpl()
		{
			spawnMobs();
			spawnTask = null;
		}
	}
	
	private class DeSpawnTask extends RunnableImpl
	{
		@Override
		public void runImpl()
		{
			for(NpcInstance npc : spawns)
			{
				if(npc == null)
					continue;
				npc.deleteMe();
			}
			spawns.clear();
			despawnTask = null;
		}
	}
}