package l2.gameserver.stats.funcs;

public interface FuncOwner
{
	boolean isFuncEnabled();
	
	boolean overrideLimits();
}