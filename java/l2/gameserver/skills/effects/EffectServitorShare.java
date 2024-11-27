package l2.gameserver.skills.effects;

import l2.gameserver.model.Effect;
import l2.gameserver.model.Summon;
import l2.gameserver.stats.Env;
import l2.gameserver.stats.Stats;
import l2.gameserver.stats.funcs.Func;
import l2.gameserver.stats.funcs.FuncTemplate;

public class EffectServitorShare extends Effect
{
	public EffectServitorShare(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	protected void onStart()
	{
		super.onStart();
		Summon target;
		if(_effector.isPlayer() && !_effector.isAlikeDead() && (target = _effector.getPet()) != null && !target.isAlikeDead())
		{
			target.addStatFuncs(getShareFuncs());
		}
	}
	
	@Override
	protected void onExit()
	{
		Summon target;
		if(_effector.isPlayer() && !_effector.isAlikeDead() && (target = _effector.getPet()) != null && !target.isAlikeDead())
		{
			target.removeStatsOwner(this);
		}
		super.onExit();
	}
	
	@Override
	public Func[] getStatFuncs()
	{
		return Func.EMPTY_FUNC_ARRAY;
	}
	
	private Func[] getShareFuncs()
	{
		FuncTemplate[] funcTemplates = getTemplate().getAttachedFuncs();
		Func[] funcs = new Func[funcTemplates.length];
		for(int i = 0;i < funcs.length;++i)
		{
			funcs[i] = new FuncShare(funcTemplates[i]._stat, funcTemplates[i]._order, this, funcTemplates[i]._value);
		}
		return funcs;
	}
	
	@Override
	protected boolean onActionTime()
	{
		return false;
	}
	
	public class FuncShare extends Func
	{
		public FuncShare(Stats stat, int order, Object owner, double value)
		{
			super(stat, order, owner, value);
		}
		
		@Override
		public void calc(Env env)
		{
			env.value += env.character.getPlayer().calcStat(stat, stat.getInit()) * value;
		}
	}
}