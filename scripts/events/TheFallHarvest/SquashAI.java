package events.TheFallHarvest;

import l2.commons.threading.RunnableImpl;
import l2.commons.util.Rnd;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.ai.Fighter;
import l2.gameserver.data.xml.holder.NpcHolder;
import l2.gameserver.model.Creature;
import l2.gameserver.model.SimpleSpawner;
import l2.gameserver.model.Skill;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.network.l2.s2c.MagicSkillUse;
import l2.gameserver.scripts.Functions;
import l2.gameserver.utils.Log;
import npc.model.SquashInstance;

import java.util.concurrent.ScheduledFuture;

public class SquashAI extends Fighter
{
	public static final int Young_Squash = 12774;
	public static final int High_Quality_Squash = 12775;
	public static final int Low_Quality_Squash = 12776;
	public static final int Large_Young_Squash = 12777;
	public static final int High_Quality_Large_Squash = 12778;
	public static final int Low_Quality_Large_Squash = 12779;
	public static final int King_Squash = 13016;
	public static final int Emperor_Squash = 13017;
	public static final int Squash_Level_up = 4513;
	public static final int Squash_Poisoned = 4514;
	private static final String[] textOnSpawn = {"scripts.events.TheFallHarvest.SquashAI.textOnSpawn.0", "scripts.events.TheFallHarvest.SquashAI.textOnSpawn.1", "scripts.events.TheFallHarvest.SquashAI.textOnSpawn.2"};
	private static final String[] textOnAttack = {"Bites rat-a-tat... to change... body...!", "Ha ha, grew up! Completely on all!", "Cannot to aim all? Had a look all to flow out...", "Is that also calculated hit? Look for person which has the strength!", "Don't waste your time!", "Ha, this sound is really pleasant to hear?", "I eat your attack to grow!", "Time to hit again! Come again!", "Only useful music can open big pumpkin... It can not be opened with weapon!"};
	private static final String[] textTooFast = {"heh heh,looks well hit!", "yo yo? Your skill is mediocre?", "Time to hit again! Come again!", "I eat your attack to grow!", "Make an effort... to get down like this, I walked...", "What is this kind of degree to want to open me? Really is indulges in fantasy!", "Good fighting method. Evidently flies away the fly also can overcome.", "Strives to excel strength oh! But waste your time..."};
	private static final String[] textSuccess0 = {"The lovely pumpkin young fruit start to glisten when taken to the threshing ground! From now on will be able to grow healthy and strong!", "Oh, Haven't seen for a long time?", "Suddenly, thought as soon as to see my beautiful appearance?", "Well! This is something! Is the nectar?", "Refuels! Drink 5 bottles to be able to grow into the big pumpkin oh!"};
	private static final String[] textFail0 = {"If I drink nectar, I can grow up faster!", "Come, believe me, sprinkle a nectar! I can certainly turn the big pumpkin!!!", "Take nectar to come, pumpkin nectar!"};
	private static final String[] textSuccess1 = {"Wish the big pumpkin!", "completely became the recreation area! Really good!", "Guessed I am mature or am rotten?", "Nectar is just the best! Ha! Ha! Ha!"};
	private static final String[] textFail1 = {"oh! Randomly missed! Too quickly sprinkles the nectar?", "If I die like this, you only could get young pumpkin...", "Cultivate a bit faster! The good speech becomes the big pumpkin, the young pumpkin is not good!", "The such small pumpkin you all must eat? Bring the nectar, I can be bigger!"};
	private static final String[] textSuccess2 = {"Young pumpkin wishing! Has how already grown up?", "Already grew up! Quickly sneaked off...", "Graciousness, is very good. Come again to see, now felt more and more well"};
	private static final String[] textFail2 = {"Hey! Was not there! Here is! Here! Not because I can not properly care? Small!", "Wow, stops? Like this got down to have to thank", "Hungry for a nectar oh...", "Do you want the big pumpkin? But I like young pumpkin..."};
	private static final String[] textSuccess3 = {"Big pumpkin wishing! Ask, to sober!", "Rumble rumble... it's really tasty! Hasn't it?", "Cultivating me just to eat? Good, is casual your... not to give the manna on the suicide!"};
	private static final String[] textFail3 = {"Isn't it the water you add? What flavor?", "Master, rescue my... I don't have the nectar flavor, I must die..."};
	private static final String[] textSuccess4 = {"is very good, does extremely well! Knew what next step should make?", "If you catch me, I give you 10 million adena!!! Agree?"};
	private static final String[] textFail4 = {"Hungry for a nectar oh...", "If I drink nectar, I can grow up faster!"};
	private static final int NECTAR_REUSE = 3000;
	private int _npcId;
	private int _nectar;
	private int _tryCount;
	private long _lastNectarUse;
	private long _timeToUnspawn;
	private ScheduledFuture<?> _polimorphTask;
	
	public SquashAI(NpcInstance actor)
	{
		super(actor);
		_npcId = getActor().getNpcId();
		Functions.npcSayInRangeCustomMessage(getActor(), 128, textOnSpawn[Rnd.get(textOnSpawn.length)], (Object[]) new Object[0]);
		_timeToUnspawn = System.currentTimeMillis() + 120000;
	}
	
	@Override
	protected boolean thinkActive()
	{
		if(System.currentTimeMillis() > _timeToUnspawn)
		{
			_timeToUnspawn = Long.MAX_VALUE;
			if(_polimorphTask != null)
			{
				_polimorphTask.cancel(false);
				_polimorphTask = null;
			}
			SquashInstance actor;
			if((actor = getActor()) != null)
			{
				actor.deleteMe();
			}
		}
		return false;
	}
	
	@Override
	protected void onEvtSeeSpell(Skill skill, Creature caster)
	{
		SquashInstance actor = getActor();
		if(actor == null || skill.getId() != 2005)
		{
			return;
		}
		switch(_tryCount)
		{
			case 0:
			{
				++_tryCount;
				_lastNectarUse = System.currentTimeMillis();
				if(Rnd.chance(50))
				{
					++_nectar;
					Functions.npcSayInRange(actor, textSuccess0[Rnd.get(textSuccess0.length)], 128);
					actor.broadcastPacket(new MagicSkillUse(actor, actor, 4513, 1, NECTAR_REUSE, 0));
					break;
				}
				Functions.npcSayInRange(actor, textFail0[Rnd.get(textFail0.length)], 128);
				actor.broadcastPacket(new MagicSkillUse(actor, actor, 4514, 1, NECTAR_REUSE, 0));
				break;
			}
			case 1:
			{
				if(System.currentTimeMillis() - _lastNectarUse < (long) NECTAR_REUSE)
				{
					Functions.npcSayInRange(actor, textTooFast[Rnd.get(textTooFast.length)], 128);
					return;
				}
				++_tryCount;
				_lastNectarUse = System.currentTimeMillis();
				if(Rnd.chance(50))
				{
					++_nectar;
					Functions.npcSayInRange(actor, textSuccess1[Rnd.get(textSuccess1.length)], 128);
					actor.broadcastPacket(new MagicSkillUse(actor, actor, 4513, 1, NECTAR_REUSE, 0));
					break;
				}
				Functions.npcSayInRange(actor, textFail1[Rnd.get(textFail1.length)], 128);
				actor.broadcastPacket(new MagicSkillUse(actor, actor, 4514, 1, NECTAR_REUSE, 0));
				break;
			}
			case 2:
			{
				if(System.currentTimeMillis() - _lastNectarUse < (long) NECTAR_REUSE)
				{
					Functions.npcSayInRange(actor, textTooFast[Rnd.get(textTooFast.length)], 128);
					return;
				}
				++_tryCount;
				_lastNectarUse = System.currentTimeMillis();
				if(Rnd.chance(50))
				{
					++_nectar;
					Functions.npcSayInRange(actor, textSuccess2[Rnd.get(textSuccess2.length)], 128);
					actor.broadcastPacket(new MagicSkillUse(actor, actor, 4513, 1, NECTAR_REUSE, 0));
					break;
				}
				Functions.npcSayInRange(actor, textFail2[Rnd.get(textFail2.length)], 128);
				actor.broadcastPacket(new MagicSkillUse(actor, actor, 4514, 1, NECTAR_REUSE, 0));
				break;
			}
			case 3:
			{
				if(System.currentTimeMillis() - _lastNectarUse < (long) NECTAR_REUSE)
				{
					Functions.npcSayInRange(actor, textTooFast[Rnd.get(textTooFast.length)], 128);
					return;
				}
				++_tryCount;
				_lastNectarUse = System.currentTimeMillis();
				if(Rnd.chance(50))
				{
					++_nectar;
					Functions.npcSayInRange(actor, textSuccess3[Rnd.get(textSuccess3.length)], 128);
					actor.broadcastPacket(new MagicSkillUse(actor, actor, 4513, 1, NECTAR_REUSE, 0));
					break;
				}
				Functions.npcSayInRange(actor, textFail3[Rnd.get(textFail3.length)], 128);
				actor.broadcastPacket(new MagicSkillUse(actor, actor, 4514, 1, NECTAR_REUSE, 0));
				break;
			}
			case 4:
			{
				if(System.currentTimeMillis() - _lastNectarUse < (long) NECTAR_REUSE)
				{
					Functions.npcSayInRange(actor, textTooFast[Rnd.get(textTooFast.length)], 128);
					return;
				}
				++_tryCount;
				_lastNectarUse = System.currentTimeMillis();
				if(Rnd.chance(50))
				{
					++_nectar;
					Functions.npcSayInRange(actor, textSuccess4[Rnd.get(textSuccess4.length)], 128);
					actor.broadcastPacket(new MagicSkillUse(actor, actor, 4513, 1, NECTAR_REUSE, 0));
				}
				else
				{
					Functions.npcSayInRange(actor, textFail4[Rnd.get(textFail4.length)], 128);
					actor.broadcastPacket(new MagicSkillUse(actor, actor, 4514, 1, NECTAR_REUSE, 0));
				}
				if(_npcId == 12774)
				{
					_npcId = _nectar < 3 ? 12776 : _nectar == 5 ? 13016 : 12775;
				}
				else if(_npcId == 12777)
				{
					_npcId = _nectar < 3 ? 12779 : _nectar == 5 ? 13017 : 12778;
				}
				_polimorphTask = ThreadPoolManager.getInstance().schedule(new PolimorphTask(), (long) NECTAR_REUSE);
			}
		}
	}
	
	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		SquashInstance actor = getActor();
		if(actor != null && Rnd.chance(5))
		{
			Functions.npcSayInRange(actor, textOnAttack[Rnd.get(textOnAttack.length)], 128);
		}
	}
	
	@Override
	protected void onEvtDead(Creature killer)
	{
		_tryCount = -1;
		SquashInstance actor = getActor();
		if(actor == null)
		{
			return;
		}
		switch(_npcId)
		{
			case 12776:
			{
				Functions.npcSayInRange(actor, "The pampkin opens!!!", 128);
				Functions.npcSayInRange(actor, "ya yo! Opens! Good thing many...", 128);
				break;
			}
			case 12775:
			{
				Functions.npcSayInRange(actor, "The pampkin opens!!!", 128);
				Functions.npcSayInRange(actor, "ya yo! Opens! Good thing many...", 128);
				break;
			}
			case 13016:
			{
				Functions.npcSayInRange(actor, "The pampkin opens!!!", 128);
				Functions.npcSayInRange(actor, "ya yo! Opens! Good thing many...", 128);
				break;
			}
			case 12779:
			{
				Functions.npcSayInRange(actor, "The pampkin opens!!!", 128);
				Functions.npcSayInRange(actor, "ya yo! Opens! Good thing many...", 128);
				break;
			}
			case 12778:
			{
				Functions.npcSayInRange(actor, "The pampkin opens!!!", 128);
				Functions.npcSayInRange(actor, "ya yo! Opens! Good thing many...", 128);
				break;
			}
			case 13017:
			{
				Functions.npcSayInRange(actor, "The pampkin opens!!!", 128);
				Functions.npcSayInRange(actor, "ya yo! Opens! Good thing many...", 128);
				break;
			}
			default:
			{
				Functions.npcSayInRange(actor, "Ouch, if I had died like this, you could obtain nothing!", 128);
				Functions.npcSayInRange(actor, "The news about my death shouldn't spread, oh!", 100);
			}
		}
		super.onEvtDead(actor);
		if(_polimorphTask != null)
		{
			_polimorphTask.cancel(false);
			_polimorphTask = null;
			Log.add("TheFallHarvest :: Player " + actor.getSpawner().getName() + " tried to use cheat (SquashAI clone): killed " + actor + " after polymorfing started", "illegal-actions");
		}
	}
	
	@Override
	protected boolean randomAnimation()
	{
		return false;
	}
	
	@Override
	protected boolean randomWalk()
	{
		return false;
	}
	
	@Override
	public SquashInstance getActor()
	{
		return (SquashInstance) super.getActor();
	}
	
	public class PolimorphTask extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			SquashInstance actor = getActor();
			if(actor == null)
			{
				return;
			}
			try
			{
				SimpleSpawner spawn = new SimpleSpawner(NpcHolder.getInstance().getTemplate(_npcId));
				spawn.setLoc(actor.getLoc());
				NpcInstance npc = spawn.doSpawn(true);
				npc.setAI(new SquashAI(npc));
				((SquashInstance) npc).setSpawner(actor.getSpawner());
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			_timeToUnspawn = Long.MAX_VALUE;
			actor.deleteMe();
		}
	}
}