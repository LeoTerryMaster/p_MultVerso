package l2.gameserver.network.l2.s2c;

public class AcquireSkillDone extends L2GameServerPacket
{
	public static final L2GameServerPacket STATIC = new AcquireSkillDone();
	
	@Override
	protected void writeImpl()
	{
		writeC(37);
	}
}