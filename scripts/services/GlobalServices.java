package services;

import l2.gameserver.model.Player;
import l2.gameserver.network.l2.s2c.SocialAction;
import l2.gameserver.scripts.Functions;

public class GlobalServices extends Functions
{
	public static boolean makeCustomHero(Player player, long customHeroDuration)
	{
		if(player.isHero() || customHeroDuration <= 0)
		{
			return false;
		}
		player.setCustomHero(true, customHeroDuration, true);
		player.broadcastPacket(new SocialAction(player.getObjectId(), 16));
		player.broadcastUserInfo(true);
		return true;
	}
}