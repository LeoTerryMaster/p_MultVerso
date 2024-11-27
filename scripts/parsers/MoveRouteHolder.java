package parsers;

import l2.commons.data.xml.AbstractHolder;
import l2.gameserver.templates.moveroute.MoveRoute;

import java.util.HashMap;
import java.util.Map;

public class MoveRouteHolder extends AbstractHolder
{
	private static MoveRouteHolder INSTANCE;
	private final Map<String, MoveRoute> _routes = new HashMap<>();
	
	private MoveRouteHolder()
	{
	}
	
	public static MoveRouteHolder getInstance()
	{
		if(INSTANCE == null)
		{
			INSTANCE = new MoveRouteHolder();
			MoveRouteParser.getInstance();
		}
		return INSTANCE;
	}
	
	public void addRoute(MoveRoute route)
	{
		if(route.getNodes().isEmpty())
		{
			_log.warn("Route \"" + route.getName() + "\" is empty.");
		}
		_routes.put(route.getName(), route);
	}
	
	public MoveRoute getRoute(String name)
	{
		return _routes.get(name);
	}
	
	@Override
	public int size()
	{
		return _routes.size();
	}
	
	@Override
	public void clear()
	{
		_routes.clear();
	}
}