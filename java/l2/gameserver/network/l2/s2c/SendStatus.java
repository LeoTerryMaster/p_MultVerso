package l2.gameserver.network.l2.s2c;

import l2.gameserver.Config;
import l2.gameserver.model.GameObjectsStorage;
import l2.gameserver.model.Player;

public final class SendStatus extends L2GameServerPacket
{
	private static final long MIN_UPDATE_PERIOD = 30000;
	private static int online_players;
	private static int max_online_players;
	private static int online_priv_store;
	private static long last_update;
	
	public SendStatus()
	{
		int i = 0;
		int j = 0;
		for(Player player : GameObjectsStorage.getAllPlayersForIterate())
		{
			if(player == null)
				continue;
			++i;
			if(!player.isInStoreMode() || Config.SENDSTATUS_TRADE_JUST_OFFLINE && !player.isInOfflineMode())
				continue;
			++j;
		}
		online_players = (int) ((double) i * Config.MUL_PLAYERS_ONLINE);
		online_priv_store = (int) Math.floor((double) j * Config.SENDSTATUS_TRADE_MOD);
		max_online_players = Math.max(max_online_players, online_players);
	}
	
	@Override
	protected final void writeImpl()
	{
		if(System.currentTimeMillis() - last_update < 30000)
		{
			return;
		}
		last_update = System.currentTimeMillis();
		writeC(0);
		writeD(1);
		writeD(max_online_players);
		writeD(online_players);
		writeD(online_players);
		writeD(online_priv_store);
		writeH(48);
		writeH(44);
		writeH(53);
		writeH(49);
		writeH(48);
		writeH(44);
		writeH(55);
		writeH(55);
		writeH(55);
		writeH(53);
		writeH(56);
		writeH(44);
		writeH(54);
		writeH(53);
		writeH(48);
		writeD(54);
		writeD(119);
		writeD(183);
		writeQ(159);
		writeD(0);
		writeH(65);
		writeH(117);
		writeH(103);
		writeH(32);
		writeH(50);
		writeH(57);
		writeH(32);
		writeH(50);
		writeH(48);
		writeH(48);
		writeD(57);
		writeH(48);
		writeH(50);
		writeH(58);
		writeH(52);
		writeH(48);
		writeH(58);
		writeH(52);
		writeD(51);
		writeD(87);
		writeC(17);
		writeC(93);
		writeC(31);
		writeC(96);
	}
}