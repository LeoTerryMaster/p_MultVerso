package l2.gameserver.network.l2.c2s;

import l2.gameserver.cache.ItemInfoCache;
import l2.gameserver.model.items.ItemInfo;
import l2.gameserver.network.l2.s2c.ActionFail;
import l2.gameserver.network.l2.s2c.ExRpItemLink;

public class RequestExRqItemLink extends L2GameClientPacket
{
	private int _objectId;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		ItemInfo item = ItemInfoCache.getInstance().get(_objectId);
		if(item == null)
		{
			sendPacket(ActionFail.STATIC);
		}
		else
		{
			sendPacket(new ExRpItemLink(item));
		}
	}
}