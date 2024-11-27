package l2.gameserver.network.l2.s2c;

public class TutorialShowHtml extends L2GameServerPacket
{
	private final String _html;
	
	public TutorialShowHtml(String html)
	{
		_html = html;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(160);
		writeS(_html);
	}
}