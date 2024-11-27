package ai;

import l2.commons.util.Rnd;
import l2.gameserver.ai.Mystic;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.World;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.tables.SkillTable;

import java.util.Collections;
import java.util.List;

public class TriolsRevelation extends Mystic
{
	private static final int TriolSkillActivationChance = 3;
	private static final int SkillSearchRadius = 500;
	private static final Skill theSkill = SkillTable.getInstance().getInfo(4088, 8);
	
	public TriolsRevelation(NpcInstance actor)
	{
		super(actor);
		actor.startImmobilized();
		AI_TASK_ATTACK_DELAY = 1250;
	}
	
	@Override
	protected boolean thinkActive()
	{
		NpcInstance actor = getActor();
		if(actor.isDead() || !Rnd.chance(TriolSkillActivationChance))
		{
			return false;
		}
		List<Player> players = World.getAroundPlayers(actor, SkillSearchRadius, 200);
		if(players == null || players.isEmpty())
		{
			return false;
		}
		Collections.shuffle(players);
		for(Player player : players)
		{
			if(player.getEffectList().getEffectsCountForSkill(theSkill.getId()) >= 1)
				continue;
			theSkill.getEffects(actor, player, false, false);
			break;
		}
		return true;
	}
	
	@Override
	protected boolean randomWalk()
	{
		return false;
	}
}