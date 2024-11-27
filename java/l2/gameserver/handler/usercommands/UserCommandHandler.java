package l2.gameserver.handler.usercommands;

import gnu.trove.TIntObjectHashMap;
import l2.commons.data.xml.AbstractHolder;
import l2.gameserver.handler.usercommands.impl.ClanPenalty;
import l2.gameserver.handler.usercommands.impl.ClanWarsList;
import l2.gameserver.handler.usercommands.impl.CommandChannel;
import l2.gameserver.handler.usercommands.impl.Escape;
import l2.gameserver.handler.usercommands.impl.InstanceZone;
import l2.gameserver.handler.usercommands.impl.LocCommand;
import l2.gameserver.handler.usercommands.impl.OlympiadStat;
import l2.gameserver.handler.usercommands.impl.PartyInfo;
import l2.gameserver.handler.usercommands.impl.Time;

public class UserCommandHandler extends AbstractHolder
{
	private static final UserCommandHandler _instance = new UserCommandHandler();
	private final TIntObjectHashMap<IUserCommandHandler> _datatable = new TIntObjectHashMap();
	
	private UserCommandHandler()
	{
		registerUserCommandHandler(new ClanWarsList());
		registerUserCommandHandler(new ClanPenalty());
		registerUserCommandHandler(new CommandChannel());
		registerUserCommandHandler(new Escape());
		registerUserCommandHandler(new LocCommand());
		registerUserCommandHandler(new OlympiadStat());
		registerUserCommandHandler(new PartyInfo());
		registerUserCommandHandler(new InstanceZone());
		registerUserCommandHandler(new Time());
	}
	
	public static UserCommandHandler getInstance()
	{
		return _instance;
	}
	
	public void registerUserCommandHandler(IUserCommandHandler handler)
	{
		int[] ids = handler.getUserCommandList();
		for(int element : ids)
		{
			_datatable.put(element, handler);
		}
	}
	
	public IUserCommandHandler getUserCommandHandler(int userCommand)
	{
		return _datatable.get(userCommand);
	}
	
	@Override
	public int size()
	{
		return _datatable.size();
	}
	
	@Override
	public void clear()
	{
		_datatable.clear();
	}
}