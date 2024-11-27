package ai;

import l2.commons.util.Rnd;
import l2.gameserver.ai.CtrlEvent;
import l2.gameserver.ai.Fighter;
import l2.gameserver.data.xml.holder.NpcHolder;
import l2.gameserver.model.Creature;
import l2.gameserver.model.SimpleSpawner;
import l2.gameserver.model.Skill;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.utils.Location;

public class FrostBuffalo extends Fighter
{
	private static final int MOBS = 22093;
	private static final int MOBS_COUNT = 4;
	private boolean _mobsNotSpawned = true;
	
	public FrostBuffalo(NpcInstance actor)
	{
		super(actor);
	}
	
	@Override
	protected void onEvtSeeSpell(Skill skill, Creature caster)
	{
		NpcInstance actor = getActor();
		if(skill.isMagic())
		{
			return;
		}
		if(_mobsNotSpawned)
		{
			_mobsNotSpawned = false;
			for(int i = 0;i < 4;++i)
			{
				try
				{
					SimpleSpawner sp = new SimpleSpawner(NpcHolder.getInstance().getTemplate(22093));
					sp.setLoc(Location.findPointToStay(actor, 100, 120));
					NpcInstance npc = sp.doSpawn(true);
					if(caster.isPet() || caster.isSummon())
					{
						npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, caster, Rnd.get(2, 100));
					}
					npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, caster.getPlayer(), Rnd.get(1, 100));
					continue;
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	@Override
	protected void onEvtDead(Creature killer)
	{
		_mobsNotSpawned = true;
		super.onEvtDead(killer);
	}
	
	@Override
	protected boolean randomWalk()
	{
		return _mobsNotSpawned;
	}
}