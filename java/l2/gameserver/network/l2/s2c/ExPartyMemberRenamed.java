package l2.gameserver.network.l2.s2c;

public class ExPartyMemberRenamed extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeEx(166);
	}
}