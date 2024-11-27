package l2.gameserver.network.l2.s2c;

import java.util.Collections;
import java.util.Set;

public class ExMpccPartymasterList extends L2GameServerPacket
{
	private Set<String> _members = Collections.emptySet();
	
	public ExMpccPartymasterList(Set<String> s)
	{
		_members = s;
	}
	
	@Override
	protected void writeImpl()
	{
		writeEx(162);
		writeD(_members.size());
		for(String t : _members)
		{
			writeS(t);
		}
	}
}