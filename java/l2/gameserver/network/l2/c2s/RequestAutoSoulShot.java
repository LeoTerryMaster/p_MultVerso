package l2.gameserver.network.l2.c2s;

import l2.gameserver.handler.items.IItemHandler;
import l2.gameserver.model.Player;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.network.l2.s2c.ExAutoSoulShot;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.utils.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestAutoSoulShot extends L2GameClientPacket
{
	private static final Logger LOG = LoggerFactory.getLogger(RequestAutoSoulShot.class);
	private int _itemId;
	private boolean _type;
	
	@Override
	protected void readImpl()
	{
		_itemId = readD();
		_type = readD() == 1;
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		if(activeChar.getPrivateStoreType() != 0 || activeChar.isDead())
		{
			return;
		}
		ItemInstance item = activeChar.getInventory().getItemByItemId(_itemId);
		if(item == null)
		{
			return;
		}
		if(!item.getTemplate().isShotItem())
		{
			return;
		}
		if(!item.getTemplate().testCondition(activeChar, item, false))
		{
			String msg = "Player " + activeChar.getName() + " trying illegal item use, ban this player!";
			Log.add(msg, "illegal-actions");
			LOG.warn(msg);
			return;
		}
		if(_type)
		{
			activeChar.addAutoSoulShot(_itemId);
			activeChar.sendPacket(new ExAutoSoulShot(_itemId, true));
			activeChar.sendPacket(new SystemMessage(1433).addString(item.getName()));
			IItemHandler handler = item.getTemplate().getHandler();
			handler.useItem(activeChar, item, false);
			return;
		}
		activeChar.removeAutoSoulShot(_itemId);
		activeChar.sendPacket(new ExAutoSoulShot(_itemId, false));
		activeChar.sendPacket(new SystemMessage(1434).addString(item.getName()));
	}
}