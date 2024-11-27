package l2.gameserver.handler.chat;

import l2.gameserver.network.l2.components.ChatType;

public interface IChatHandler
{
	void say();
	
	ChatType getType();
}