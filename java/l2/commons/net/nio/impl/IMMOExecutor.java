package l2.commons.net.nio.impl;

public interface IMMOExecutor<T extends MMOClient>
{
	void execute(Runnable r);
}