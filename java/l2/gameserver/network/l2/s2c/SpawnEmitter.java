package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Player;
import l2.gameserver.model.instances.NpcInstance;

public class SpawnEmitter extends L2GameServerPacket
{
	private final int _monsterObjId;
	private final int _playerObjId;
	
	public SpawnEmitter(NpcInstance monster, Player player)
	{
		_playerObjId = player.getObjectId();
		_monsterObjId = monster.getObjectId();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeEx(93);
		writeD(_monsterObjId);
		writeD(_playerObjId);
		writeD(0);
	}
}