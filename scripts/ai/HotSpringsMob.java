package ai;

import l2.commons.util.Rnd;
import l2.gameserver.Config;
import l2.gameserver.ai.Mystic;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Effect;
import l2.gameserver.model.Skill;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.tables.SkillTable;

import java.util.List;

public class HotSpringsMob extends Mystic
{
	private static final int[] AltDeBuffs = {4554, 4553, 4552, 4551};
	private static final int[] DeBuffs = {4554, 4552};
	
	public HotSpringsMob(NpcInstance actor)
	{
		super(actor);
	}
	
	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		NpcInstance actor = getActor();
		if(attacker != null && Rnd.chance(Config.ALT_HOT_SPIRIT_CHANCE_DEBUFF))
		{
			int[] debuffs = DeBuffs;
			if(Config.ALT_USE_HOT_SPIRIT_DEBUFF)
			{
				debuffs = AltDeBuffs;
			}
			int DeBuff = debuffs[Rnd.get(debuffs.length)];
			List effect = attacker.getEffectList().getEffectsBySkillId(DeBuff);
			if(effect != null)
			{
				int level = ((Effect) effect.get(0)).getSkill().getLevel();
				if(level < 10)
				{
					((Effect) effect.get(0)).exit();
					Skill skill = SkillTable.getInstance().getInfo(DeBuff, level + 1);
					skill.getEffects(actor, attacker, false, false);
				}
			}
			else
			{
				Skill skill = SkillTable.getInstance().getInfo(DeBuff, 1);
				if(skill != null)
				{
					skill.getEffects(actor, attacker, false, false);
				}
				else
				{
					System.out.println("Skill " + DeBuff + " is null, fix it.");
				}
			}
		}
		super.onEvtAttacked(attacker, damage);
	}
}