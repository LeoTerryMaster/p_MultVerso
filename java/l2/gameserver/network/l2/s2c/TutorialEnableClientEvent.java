package l2.gameserver.network.l2.s2c;

public class TutorialEnableClientEvent extends L2GameServerPacket
{
	private final int _event;
	
	public TutorialEnableClientEvent(int event)
	{
		_event = event;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(162);
		writeD(_event);
	}
}