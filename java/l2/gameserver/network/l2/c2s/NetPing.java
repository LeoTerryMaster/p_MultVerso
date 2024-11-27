package l2.gameserver.network.l2.c2s;

import l2.gameserver.network.l2.GameClient;

public class NetPing extends L2GameClientPacket
{
	public static final int MIN_CLIP_RANGE = 1433;
	public static final int MAX_CLIP_RANGE = 6144;
	private int _timestamp;
	private int _clippingRange;
	private int _fps;
	
	@Override
	protected void runImpl()
	{
		GameClient client = getClient();
		if(client.getRevision() == 0)
		{
			client.closeNow(false);
		}
		else
		{
			client.onPing(_timestamp, _fps, Math.max(1433, Math.min(_clippingRange, 6144)));
		}
	}
	
	@Override
	protected void readImpl()
	{
		_timestamp = readD();
		_fps = readD();
		_clippingRange = readD();
	}
}