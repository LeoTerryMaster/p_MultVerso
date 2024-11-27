package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Player;

import java.util.List;

public class ExCubeGameTeamList extends L2GameServerPacket
{
	List<Player> _bluePlayers;
	List<Player> _redPlayers;
	int _roomNumber;
	
	public ExCubeGameTeamList(List<Player> redPlayers, List<Player> bluePlayers, int roomNumber)
	{
		_redPlayers = redPlayers;
		_bluePlayers = bluePlayers;
		_roomNumber = roomNumber - 1;
	}
	
	@Override
	protected void writeImpl()
	{
		writeEx(151);
		writeD(0);
		writeD(_roomNumber);
		writeD(-1);
		writeD(_bluePlayers.size());
		for(Player player : _bluePlayers)
		{
			writeD(player.getObjectId());
			writeS(player.getName());
		}
		writeD(_redPlayers.size());
		for(Player player : _redPlayers)
		{
			writeD(player.getObjectId());
			writeS(player.getName());
		}
	}
}