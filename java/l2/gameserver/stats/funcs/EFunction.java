package l2.gameserver.stats.funcs;

import l2.gameserver.stats.Stats;

import java.util.HashMap;
import java.util.Map;

public enum EFunction
{
	Set
			{
				@Override
				public Func create(Stats stat, int order, Object owner, double value)
				{
					return new FuncSet(stat, order, owner, value);
				}
			},
	Add
			{
				@Override
				public Func create(Stats stat, int order, Object owner, double value)
				{
					return new FuncAdd(stat, order, owner, value);
				}
			},
	Sub
			{
				@Override
				public Func create(Stats stat, int order, Object owner, double value)
				{
					return new FuncSub(stat, order, owner, value);
				}
			},
	Mul
			{
				@Override
				public Func create(Stats stat, int order, Object owner, double value)
				{
					return new FuncMul(stat, order, owner, value);
				}
			},
	Div
			{
				@Override
				public Func create(Stats stat, int order, Object owner, double value)
				{
					return new FuncDiv(stat, order, owner, value);
				}
			},
	Enchant
			{
				@Override
				public Func create(Stats stat, int order, Object owner, double value)
				{
					return new FuncEnchant(stat, order, owner, value);
				}
			};
	
	public static final EFunction[] VALUES;
	public static final Map<String, EFunction> VALUES_BY_LOWER_NAME;
	
	static
	{
		VALUES = EFunction.values();
		VALUES_BY_LOWER_NAME = new HashMap<>();
		for(EFunction eFunc : VALUES)
		{
			VALUES_BY_LOWER_NAME.put(eFunc.name().toLowerCase(), eFunc);
		}
	}
	
	public abstract Func create(Stats stat, int order, Object owner, double value);
}