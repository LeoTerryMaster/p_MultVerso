package l2.commons.net.nio.impl;

public interface IClientFactory<T extends MMOClient>
{
	T create(MMOConnection<T> con);
}