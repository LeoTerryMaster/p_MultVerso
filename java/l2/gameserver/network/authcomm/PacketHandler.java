package l2.gameserver.network.authcomm;

import l2.gameserver.network.authcomm.as2gs.AuthResponse;
import l2.gameserver.network.authcomm.as2gs.KickPlayer;
import l2.gameserver.network.authcomm.as2gs.LoginServerFail;
import l2.gameserver.network.authcomm.as2gs.NotifyPwdCngResult;
import l2.gameserver.network.authcomm.as2gs.PingRequest;
import l2.gameserver.network.authcomm.as2gs.PlayerAuthResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

public class PacketHandler
{
	private static final Logger _log = LoggerFactory.getLogger(PacketHandler.class);
	
	public static ReceivablePacket handlePacket(ByteBuffer buf)
	{
		ReceivablePacket packet = null;
		int id = buf.get() & 255;
		switch(id)
		{
			case 0:
			{
				packet = new AuthResponse();
				break;
			}
			case 1:
			{
				packet = new LoginServerFail();
				break;
			}
			case 2:
			{
				packet = new PlayerAuthResponse();
				break;
			}
			case 3:
			{
				packet = new KickPlayer();
				break;
			}
			case 161:
			{
				packet = new NotifyPwdCngResult();
				break;
			}
			case 255:
			{
				packet = new PingRequest();
				break;
			}
			default:
			{
				_log.error("Received unknown packet: " + Integer.toHexString(id));
			}
		}
		return packet;
	}
}