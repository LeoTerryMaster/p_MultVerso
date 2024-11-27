package l2.gameserver.tables;

import l2.gameserver.Config;
import l2.gameserver.cache.Msg;
import l2.gameserver.model.GameObjectsStorage;
import l2.gameserver.model.Player;
import l2.gameserver.network.l2.s2c.L2GameServerPacket;
import l2.gameserver.network.l2.s2c.SystemMessage;

import java.util.ArrayList;
import java.util.List;

public class GmListTable
{
	public static List<Player> getAllGMs()
	{
		ArrayList<Player> gmList = new ArrayList<>();
		for(Player player : GameObjectsStorage.getAllPlayersForIterate())
		{
			if(!player.isGM())
				continue;
			gmList.add(player);
		}
		return gmList;
	}
	
	public static List<Player> getAllVisibleGMs()
	{
		ArrayList<Player> gmList = new ArrayList<>();
		for(Player player : GameObjectsStorage.getAllPlayersForIterate())
		{
			if(!player.isGM() || player.isInvisible())
				continue;
			gmList.add(player);
		}
		return gmList;
	}
	
	public static void sendListToPlayer(Player player)
	{
		List<Player> gmList;
		List<Player> list = gmList = Config.HIDE_GM_STATUS ? getAllVisibleGMs() : getAllGMs();
		if(gmList.isEmpty())
		{
			player.sendPacket(Msg.THERE_ARE_NOT_ANY_GMS_THAT_ARE_PROVIDING_CUSTOMER_SERVICE_CURRENTLY);
			return;
		}
		player.sendPacket(Msg._GM_LIST_);
		for(Player gm : gmList)
		{
			player.sendPacket(new SystemMessage(704).addString(gm.getName()));
		}
	}
	
	public static void broadcastToGMs(L2GameServerPacket packet)
	{
		for(Player gm : getAllGMs())
		{
			gm.sendPacket(packet);
		}
	}
	
	public static void broadcastMessageToGMs(String message)
	{
		for(Player gm : getAllGMs())
		{
			gm.sendMessage(message);
		}
	}
}