package l2.gameserver.stats.funcs;

import l2.gameserver.stats.Stats;
import l2.gameserver.stats.conditions.Condition;

public final class FuncTemplate
{
	public static final FuncTemplate[] EMPTY_ARRAY = new FuncTemplate[0];
	public Condition _applyCond;
	public EFunction _func;
	public Stats _stat;
	public int _order;
	public double _value;
	
	public FuncTemplate(Condition applyCond, String func, Stats stat, int order, double value)
	{
		_applyCond = applyCond;
		_stat = stat;
		_order = order;
		_value = value;
		_func = EFunction.VALUES_BY_LOWER_NAME.get(func.toLowerCase());
		if(_func == null)
		{
			throw new RuntimeException("Unknown function " + func);
		}
	}
	
	public FuncTemplate(Condition applyCond, EFunction func, Stats stat, int order, double value)
	{
		_applyCond = applyCond;
		_stat = stat;
		_order = order;
		_value = value;
		_func = func;
	}
	
	public Func getFunc(Object owner)
	{
		Func f = _func.create(_stat, _order, owner, _value);
		if(_applyCond != null)
		{
			f.setCondition(_applyCond);
		}
		return f;
	}
}