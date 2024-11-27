package ai.events;

import l2.commons.math.random.RndSelector;
import l2.commons.util.Rnd;
import l2.gameserver.ai.DefaultAI;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.network.l2.s2c.MagicSkillUse;
import l2.gameserver.tables.SkillTable;

public class SpecialTree extends DefaultAI
{
	private static final RndSelector<Integer> SOUNDS = new RndSelector(5);
	
	static
	{
		SOUNDS.add(2140, 20);
		SOUNDS.add(2142, 20);
		SOUNDS.add(2145, 20);
		SOUNDS.add(2147, 20);
		SOUNDS.add(2149, 20);
	}
	
	private boolean _buffsEnabled;
	private int _timer;
	
	public SpecialTree(NpcInstance actor)
	{
		super(actor);
	}
	
	@Override
	protected boolean thinkActive()
	{
		if(_buffsEnabled)
		{
			++_timer;
			if(_timer >= 180)
			{
				_timer = 0;
				NpcInstance actor = getActor();
				if(actor == null)
				{
					return false;
				}
				addTaskBuff(actor, SkillTable.getInstance().getInfo(2139, 1));
				if(Rnd.chance(33))
				{
					actor.broadcastPacketToOthers(new MagicSkillUse(actor, actor, SOUNDS.select().intValue(), 1, 500, 0));
				}
			}
		}
		return super.thinkActive();
	}
	
	@Override
	protected void onEvtSpawn()
	{
		super.onEvtSpawn();
		_buffsEnabled = !getActor().isInZonePeace();
		_timer = 0;
	}
}