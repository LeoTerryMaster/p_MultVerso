package ai.primeval_isle;

import l2.gameserver.ai.DefaultAI;
import l2.gameserver.model.Skill;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.tables.SkillTable;

public class SprigantStun extends DefaultAI
{
	private static final int TICK_IN_MILISECONDS = 15000;
	private final Skill SKILL = SkillTable.getInstance().getInfo(5085, 1);
	private long _waitTime;
	
	public SprigantStun(NpcInstance actor)
	{
		super(actor);
	}
	
	@Override
	protected boolean thinkActive()
	{
		if(System.currentTimeMillis() > _waitTime)
		{
			NpcInstance actor = getActor();
			actor.doCast(SKILL, actor, false);
			_waitTime = System.currentTimeMillis() + 15000;
			return true;
		}
		return false;
	}
}