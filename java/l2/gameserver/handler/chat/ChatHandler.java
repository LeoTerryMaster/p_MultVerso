package l2.gameserver.handler.chat;

import l2.commons.data.xml.AbstractHolder;
import l2.gameserver.network.l2.components.ChatType;

public class ChatHandler extends AbstractHolder
{
	private static final ChatHandler _instance = new ChatHandler();
	private final IChatHandler[] _handlers = new IChatHandler[ChatType.VALUES.length];
	
	private ChatHandler()
	{
	}
	
	public static ChatHandler getInstance()
	{
		return _instance;
	}
	
	public void register(IChatHandler chatHandler)
	{
		_handlers[chatHandler.getType().ordinal()] = chatHandler;
	}
	
	public IChatHandler getHandler(ChatType type)
	{
		return _handlers[type.ordinal()];
	}
	
	@Override
	public int size()
	{
		return _handlers.length;
	}
	
	@Override
	public void clear()
	{
	}
}