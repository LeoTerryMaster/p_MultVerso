package l2.gameserver.model.chat;

import l2.commons.data.xml.AbstractHolder;
import l2.gameserver.model.chat.chatfilter.ChatFilter;
import org.apache.commons.lang3.ArrayUtils;

public class ChatFilters extends AbstractHolder
{
	private static final ChatFilters _instance = new ChatFilters();
	private ChatFilter[] filters = new ChatFilter[0];
	
	private ChatFilters()
	{
	}
	
	public static final ChatFilters getinstance()
	{
		return _instance;
	}
	
	public ChatFilter[] getFilters()
	{
		return filters;
	}
	
	public void add(ChatFilter f)
	{
		filters = ArrayUtils.add(filters, f);
	}
	
	@Override
	public void log()
	{
		info(String.format("loaded %d filter(s).", size()));
	}
	
	@Override
	public int size()
	{
		return filters.length;
	}
	
	@Override
	public void clear()
	{
		filters = new ChatFilter[0];
	}
}