package ai.residences.clanhall;

import l2.gameserver.model.Skill;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.instances.residences.clanhall.CTBBossInstance;
import l2.gameserver.tables.SkillTable;

public class MatchTrief extends MatchFighter
{
	public static final Skill HOLD = SkillTable.getInstance().getInfo(4047, 6);
	
	public MatchTrief(NpcInstance actor)
	{
		super(actor);
	}
	
	public void hold()
	{
		CTBBossInstance actor = getActor();
		addTaskCast(actor, HOLD);
		doTask();
	}
}