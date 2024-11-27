package l2.gameserver.stats;

import l2.commons.lang.ArrayUtils;
import l2.gameserver.model.Creature;
import l2.gameserver.stats.funcs.Func;
import l2.gameserver.stats.funcs.FuncOwner;

public final class Calculator
{
	public final Stats _stat;
	public final Creature _character;
	private Func[] _functions;
	private double _base;
	private double _last;
	
	public Calculator(Stats stat, Creature character)
	{
		_stat = stat;
		_character = character;
		_functions = Func.EMPTY_FUNC_ARRAY;
	}
	
	public int size()
	{
		return _functions.length;
	}
	
	public void addFunc(Func f)
	{
		_functions = ArrayUtils.add(_functions, f);
		ArrayUtils.eqSort(_functions);
	}
	
	public void removeFunc(Func f)
	{
		_functions = ArrayUtils.remove(_functions, f);
		if(_functions.length == 0)
		{
			_functions = Func.EMPTY_FUNC_ARRAY;
		}
		else
		{
			ArrayUtils.eqSort(_functions);
		}
	}
	
	public void removeOwner(Object owner)
	{
		for(Func element : _functions)
		{
			if(element.owner != owner)
				continue;
			removeFunc(element);
		}
	}
	
	public void calc(Env env)
	{
		Func[] funcs = _functions;
		_base = env.value;
		boolean overrideLimits = false;
		for(Func func : funcs)
		{
			if(func == null)
				continue;
			if(func.owner instanceof FuncOwner)
			{
				if(!((FuncOwner) func.owner).isFuncEnabled())
					continue;
				if(((FuncOwner) func.owner).overrideLimits())
				{
					overrideLimits = true;
				}
			}
			if(func.getCondition() != null && !func.getCondition().test(env))
				continue;
			func.calc(env);
		}
		if(!overrideLimits)
		{
			env.value = _stat.validate(env.value);
		}
		if(env.value != _last)
		{
			double last = _last;
			_last = env.value;
		}
	}
	
	public Func[] getFunctions()
	{
		return _functions;
	}
	
	public double getBase()
	{
		return _base;
	}
	
	public double getLast()
	{
		return _last;
	}
}