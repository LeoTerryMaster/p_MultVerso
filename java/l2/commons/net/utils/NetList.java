package l2.commons.net.utils;

import java.util.ArrayList;
import java.util.Iterator;

public final class NetList extends ArrayList<Net>
{
	private static final long serialVersionUID = 4266033257195615387L;
	
	public boolean isInRange(String address)
	{
		for(Net net : this)
		{
			if(!net.isInRange(address))
				continue;
			return true;
		}
		return false;
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		Iterator itr = iterator();
		while(itr.hasNext())
		{
			sb.append(itr.next());
			if(!itr.hasNext())
				continue;
			sb.append(',');
		}
		return sb.toString();
	}
}