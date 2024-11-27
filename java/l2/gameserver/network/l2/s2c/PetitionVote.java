package l2.gameserver.network.l2.s2c;

public class PetitionVote extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(246);
	}
}