package l2.gameserver.network.l2.s2c;

public class TutorialCloseHtml extends L2GameServerPacket
{
	public static final L2GameServerPacket STATIC = new TutorialCloseHtml();
	
	@Override
	protected final void writeImpl()
	{
		writeC(163);
	}
}