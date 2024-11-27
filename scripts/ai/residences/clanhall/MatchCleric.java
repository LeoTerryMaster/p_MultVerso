package ai.residences.clanhall;

import l2.gameserver.model.Skill;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.instances.residences.clanhall.CTBBossInstance;
import l2.gameserver.tables.SkillTable;

public class MatchCleric extends MatchFighter
{
	public static final Skill HEAL = SkillTable.getInstance().getInfo(4056, 6);
	
	public MatchCleric(NpcInstance actor)
	{
		super(actor);
	}
	
	public void heal()
	{
		CTBBossInstance actor = getActor();
		addTaskCast(actor, HEAL);
		doTask();
	}
}