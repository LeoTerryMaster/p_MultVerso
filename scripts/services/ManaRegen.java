package services;

import l2.gameserver.cache.Msg;
import l2.gameserver.model.Player;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.scripts.Functions;

public class ManaRegen extends Functions
{
	private static final int ADENA = 57;
	private static final long PRICE = 5;
	
	public void DoManaRegen()
	{
		Player player = getSelf();
		long mp = (long) Math.floor((double) player.getMaxMp() - player.getCurrentMp());
		long fullCost = mp * 5;
		if(fullCost <= 0)
		{
			player.sendPacket(Msg.NOTHING_HAPPENED);
			return;
		}
		if(getItemCount(player, 57) >= fullCost)
		{
			removeItem(player, 57, fullCost);
			player.sendPacket(new SystemMessage(1068).addNumber(mp));
			player.setCurrentMp((double) player.getMaxMp());
		}
		else
		{
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
		}
	}
}