package l2.gameserver.network.l2.c2s;

import l2.gameserver.data.xml.holder.HennaHolder;
import l2.gameserver.model.Player;
import l2.gameserver.network.l2.s2c.HennaUnequipInfo;
import l2.gameserver.templates.Henna;

public class RequestHennaUnequipInfo extends L2GameClientPacket
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
		Henna henna = HennaHolder.getInstance().getHenna(_symbolId);
		if(henna != null)
		{
			player.sendPacket(new HennaUnequipInfo(henna, player));
		}
	}
}