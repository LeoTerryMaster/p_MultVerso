package l2.gameserver.skills.effects;

import l2.commons.util.Rnd;
import l2.gameserver.model.AggroList;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Effect;
import l2.gameserver.model.instances.MonsterInstance;
import l2.gameserver.stats.Env;

import java.util.List;

public class EffectRandomHate extends Effect
{
	public EffectRandomHate(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public boolean checkCondition()
	{
		return getEffected().isMonster() && Rnd.chance(_template.chance(100));
	}
	
	@Override
	public void onStart()
	{
		MonsterInstance monster = (MonsterInstance) getEffected();
		Creature mostHated = monster.getAggroList().getMostHated();
		if(mostHated == null)
		{
			return;
		}
		AggroList.AggroInfo mostAggroInfo = monster.getAggroList().get(mostHated);
		List<Creature> hateList = monster.getAggroList().getHateList(monster.getAggroRange());
		hateList.remove(mostHated);
		if(!hateList.isEmpty())
		{
			AggroList.AggroInfo newAggroInfo = monster.getAggroList().get(hateList.get(Rnd.get(hateList.size())));
			int oldHate = newAggroInfo.hate;
			newAggroInfo.hate = mostAggroInfo.hate;
			mostAggroInfo.hate = oldHate;
		}
	}
	
	@Override
	public boolean isHidden()
	{
		return true;
	}
	
	@Override
	protected boolean onActionTime()
	{
		return false;
	}
}