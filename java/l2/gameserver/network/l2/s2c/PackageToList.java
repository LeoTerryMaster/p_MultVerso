package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Player;

import java.util.Collections;
import java.util.Map;

public class PackageToList extends L2GameServerPacket
{
	private Map<Integer, String> _characters = Collections.emptyMap();
	
	public PackageToList(Player player)
	{
		_characters = player.getAccountChars();
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(194);
		writeD(_characters.size());
		for(Map.Entry<Integer, String> entry : _characters.entrySet())
		{
			writeD(entry.getKey());
			writeS(entry.getValue());
		}
	}
}