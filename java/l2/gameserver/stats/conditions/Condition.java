package l2.gameserver.stats.conditions;

import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.stats.Env;

public abstract class Condition
{
	public static final Condition[] EMPTY_ARRAY = new Condition[0];
	private SystemMsg _message;
	
	public final SystemMsg getSystemMsg()
	{
		return _message;
	}
	
	public final void setSystemMsg(int msgId)
	{
		_message = SystemMsg.valueOf(msgId);
	}
	
	public final boolean test(Env env)
	{
		return testImpl(env);
	}
	
	protected abstract boolean testImpl(Env env);
}