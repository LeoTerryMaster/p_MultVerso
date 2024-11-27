package l2.gameserver.network.telnet;

import java.util.Set;

public interface TelnetCommandHolder
{
	Set<TelnetCommand> getCommands();
}