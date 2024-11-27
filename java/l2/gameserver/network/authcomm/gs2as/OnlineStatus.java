package l2.gameserver.network.authcomm.gs2as;

import l2.gameserver.network.authcomm.SendablePacket;

public class OnlineStatus extends SendablePacket
{
	private final boolean _online;
	
	public OnlineStatus(boolean online)
	{
		_online = online;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(1);
		writeC(_online ? 1 : 0);
	}
}