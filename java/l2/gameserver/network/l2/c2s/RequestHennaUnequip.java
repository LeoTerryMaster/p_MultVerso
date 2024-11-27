package l2.gameserver.network.l2.c2s;

import l2.gameserver.model.Player;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.templates.Henna;

public class RequestHennaUnequip extends L2GameClientPacket
{
	private int _symbolId;
	
	@Override
	protected void readImpl()
	{
		_symbolId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
		{
			return;
		}
		for(int i = 1;i <= 3;++i)
		{
			Henna henna = player.getHenna(i);
			if(henna == null || henna.getSymbolId() != _symbolId)
				continue;
			long price = henna.getPrice() / 5;
			if(player.getAdena() < price)
			{
				player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
				break;
			}
			player.reduceAdena(price);
			player.removeHenna(i);
			player.sendPacket(SystemMsg.THE_SYMBOL_HAS_BEEN_DELETED);
			break;
		}
	}
}