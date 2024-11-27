package ai.isle_of_prayer;

import l2.commons.util.Rnd;
import l2.gameserver.ai.CtrlEvent;
import l2.gameserver.ai.Fighter;
import l2.gameserver.data.xml.holder.NpcHolder;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.SimpleSpawner;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.utils.Location;

public class DarkWaterDragon extends Fighter
{
	private static final int FAFURION = 18482;
	private static final int SHADE1 = 22268;
	private static final int SHADE2 = 22269;
	private static final int[] MOBS = {22268, 22269};
	private static final int MOBS_COUNT = 5;
	private static final int RED_CRYSTAL = 9596;
	private int _mobsSpawned;
	
	public DarkWaterDragon(NpcInstance actor)
	{
		super(actor);
	}
	
	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		NpcInstance actor = getActor();
		if(!actor.isDead())
		{
			switch(_mobsSpawned)
			{
				case 0:
				{
					_mobsSpawned = 1;
					spawnShades(attacker);
					break;
				}
				case 1:
				{
					if(actor.getCurrentHp() >= (double) (actor.getMaxHp() / 2))
						break;
					_mobsSpawned = 2;
					spawnShades(attacker);
				}
			}
		}
		super.onEvtAttacked(attacker, damage);
	}
	
	private void spawnShades(Creature attacker)
	{
		NpcInstance actor = getActor();
		for(int i = 0;i < 5;++i)
		{
			try
			{
				SimpleSpawner sp = new SimpleSpawner(NpcHolder.getInstance().getTemplate(MOBS[Rnd.get(MOBS.length)]));
				sp.setLoc(Location.findPointToStay(actor, 100, 120));
				NpcInstance npc = sp.doSpawn(true);
				npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, Rnd.get(1, 100));
				continue;
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	@Override
	protected void onEvtDead(Creature killer)
	{
		_mobsSpawned = 0;
		NpcInstance actor = getActor();
		try
		{
			SimpleSpawner sp = new SimpleSpawner(NpcHolder.getInstance().getTemplate(18482));
			sp.setLoc(Location.findPointToStay(actor, 100, 120));
			sp.doSpawn(true);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		Player player;
		if(killer != null && (player = killer.getPlayer()) != null && Rnd.chance(77))
		{
			actor.dropItem(player, 9596, 1);
		}
		super.onEvtDead(killer);
	}
	
	@Override
	protected boolean randomWalk()
	{
		return _mobsSpawned == 0;
	}
}