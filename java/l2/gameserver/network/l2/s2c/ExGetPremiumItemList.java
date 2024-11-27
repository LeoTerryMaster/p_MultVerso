package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Player;
import l2.gameserver.model.PremiumItem;

import java.util.Map;

public class ExGetPremiumItemList extends L2GameServerPacket
{
	private final int _objectId;
	private final Map<Integer, PremiumItem> _list;
	
	public ExGetPremiumItemList(Player activeChar)
	{
		_objectId = activeChar.getObjectId();
		_list = activeChar.getPremiumItemList();
	}
	
	@Override
	protected void writeImpl()
	{
		writeEx(134);
		if(!_list.isEmpty())
		{
			writeD(_list.size());
			for(Map.Entry<Integer, PremiumItem> entry : _list.entrySet())
			{
				writeD(entry.getKey());
				writeD(_objectId);
				writeD(entry.getValue().getItemId());
				writeQ(entry.getValue().getCount());
				writeD(0);
				writeS(entry.getValue().getSender());
			}
		}
	}
}