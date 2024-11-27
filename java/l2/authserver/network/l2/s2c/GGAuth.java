package l2.authserver.network.l2.s2c;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class GGAuth extends L2LoginServerPacket
{
	public static int SKIP_GG_AUTH_REQUEST = 11;
	static Logger _log = LoggerFactory.getLogger(GGAuth.class);
	private final int _response;
	
	public GGAuth(int response)
	{
		_response = response;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(11);
		writeD(_response);
		writeD(0);
		writeD(0);
		writeD(0);
		writeD(0);
	}
}