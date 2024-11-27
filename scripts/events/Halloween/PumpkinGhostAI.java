package events.Halloween;

import l2.commons.util.Rnd;
import l2.gameserver.ai.DefaultAI;
import l2.gameserver.idfactory.IdFactory;
import l2.gameserver.model.Creature;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.network.l2.components.ChatType;
import l2.gameserver.network.l2.s2c.MagicSkillLaunched;
import l2.gameserver.network.l2.s2c.MagicSkillUse;
import l2.gameserver.network.l2.s2c.Say2;
import l2.gameserver.tables.SkillTable;
import l2.gameserver.utils.Location;

import java.util.concurrent.atomic.AtomicInteger;

public class PumpkinGhostAI extends DefaultAI
{
	private static final String[] _song = {"Boys and girls of every age", "Wouldn't you like to see something strange?", "Come with us and you will see", "This, our town of Halloween", "This is Halloween, this is Halloween", "Pumpkins scream in the dead of night", "This is Halloween, everybody make a scene", "Trick or treat till the neighbors gonna die of fright", "It's our town, everybody scream", "In this town of Halloween", "I am the one hiding under your bed", "Teeth ground sharp and eyes glowing red", "I am the one hiding under yours stairs", "Fingers like snakes and spiders in my hair", "This is Halloween, this is Halloween", "Halloween! Halloween! Halloween! Halloween!", "In this town we call home", "Everyone hail to the pumpkin song", "In this town, don't we love it now?", "Everybody's waiting for the next surprise", "Round that corner, man hiding in the trash can", "Something's waiting now to pounce, and how you'll...", "Scream! This is Halloween", "Red 'n' black, slimy green", "Aren't you scared?", "Well, that's just fine", "Say it once, say it twice", "Take a chance and roll the dice", "Ride with the moon in the dead of night", "Everybody scream! Everybody scream!", "In our town of Halloween!", "I am the clown with the tear-away face", "Here in a flash and gone without a trace", "I am the 'who' when you call, 'Who's there?'", "I am the wind blowing through your hair", "I am the shadow on the moonlit night", "Filling your dreams to the brim with fright", "This is Halloween, this is Halloween", "Halloween! Halloween! Halloween! Halloween!", "Halloween! Halloween!", "Tender lumpings everywhere", "Life's no fun without a good scare", "That's our job, but we're not mean", "In our town of Halloween", "In this town", "Don't we love it now?", "Everyone’s waiting for the next surprise", "Skeleton Jack might catch you in the back", "And scream like a banshee", "Make you jump out of your skin", "This is Halloween, everybody scream", "Won’t ya please make way for a very special guy", "Our man jack is King of the Pumpkin patch", "Everyone hail to the Pumpkin King", "This is Halloween, this is Halloween", "Halloween! Halloween! Halloween! Halloween!", "In this town we call home", "Everyone hail to the pumpkin song", "La lala la, la la la la..."};
	private static final AtomicInteger _fraseIdx = new AtomicInteger(0);
	private static final int Firework = 2024;
	private final Location[] _flyPoints;
	private final int _chance;
	private final int[] _item_ids;
	private int _pointIdx;
	private long _lastTask = System.currentTimeMillis();
	
	public PumpkinGhostAI(NpcInstance actor, Location[] points, int idx, int chance, int[] item_ids)
	{
		super(actor);
		_flyPoints = points;
		_pointIdx = idx + 1;
		if(_pointIdx >= _flyPoints.length)
		{
			_pointIdx = 0;
		}
		_chance = chance;
		_item_ids = item_ids;
	}
	
	@Override
	protected void onEvtSpawn()
	{
		NpcInstance actor = getActor();
		if(actor == null)
		{
			return;
		}
		actor.setWalking();
		addTaskMove(_flyPoints[_pointIdx], false);
		super.onEvtSpawn();
	}
	
	@Override
	protected boolean thinkActive()
	{
		NpcInstance actor = getActor();
		if(actor == null || actor.isDead())
		{
			return true;
		}
		if(_def_think)
		{
			return doTask();
		}
		long currentTime = System.currentTimeMillis();
		if(currentTime - _lastTask > 3000)
		{
			if(Rnd.chance(30))
			{
				actor.broadcastPacket(new MagicSkillUse(actor, actor, 2024, 1, 0, 0), new MagicSkillLaunched(actor, 2024, 1, actor));
			}
			else if(_pointIdx == 0 && Rnd.chance(40))
			{
				actor.broadcastPacket(new Say2(actor.getObjectId(), ChatType.NPC_NORMAL, actor.getName(), _song[_fraseIdx.getAndIncrement() % _song.length]));
			}
			else if(Rnd.chance(_chance))
			{
				ItemInstance item = new ItemInstance(IdFactory.getInstance().getNextId(), _item_ids[Rnd.get(_item_ids.length)]);
				item.setCount(1);
				item.dropToTheGround(actor, Location.coordsRandomize(actor.getLoc(), 10, 50));
			}
			if(isOutOfRange())
			{
				addTaskMove(_flyPoints[_pointIdx], false);
			}
			_lastTask = currentTime;
		}
		return true;
	}
	
	private boolean isOutOfRange()
	{
		NpcInstance actor = getActor();
		if(actor == null || actor.isDead())
		{
			return false;
		}
		return actor.getLoc().distance(_flyPoints[_pointIdx]) > 512.0;
	}
	
	@Override
	protected void onEvtArrived()
	{
		++_pointIdx;
		if(_pointIdx >= _flyPoints.length)
		{
			_pointIdx = 0;
		}
		addTaskMove(_flyPoints[_pointIdx], false);
		super.onEvtArrived();
	}
	
	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		if(attacker != null)
		{
			SkillTable.getInstance().getInfo(4515, 1).getEffects(attacker, attacker, false, false);
		}
		doTask();
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
	public boolean isGlobalAI()
	{
		return true;
	}
}