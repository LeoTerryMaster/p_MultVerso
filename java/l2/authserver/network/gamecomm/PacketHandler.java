package l2.authserver.network.gamecomm;

import l2.authserver.network.gamecomm.gs2as.AuthRequest;
import l2.authserver.network.gamecomm.gs2as.ChangeAccessLevel;
import l2.authserver.network.gamecomm.gs2as.IGPwdCng;
import l2.authserver.network.gamecomm.gs2as.OnlineStatus;
import l2.authserver.network.gamecomm.gs2as.PingResponse;
import l2.authserver.network.gamecomm.gs2as.PlayerAuthRequest;
import l2.authserver.network.gamecomm.gs2as.PlayerInGame;
import l2.authserver.network.gamecomm.gs2as.PlayerLogout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

public class PacketHandler
{
	private static final Logger _log = LoggerFactory.getLogger(PacketHandler.class);
	
	public static ReceivablePacket handlePacket(GameServer gs, ByteBuffer buf)
	{
		ReceivablePacket packet = null;
		int id = buf.get() & 255;
		if(!gs.isAuthed())
		{
			switch(id)
			{
				case 0:
					packet = new AuthRequest();
					break;
				default:
					_log.error("Received unknown packet: " + Integer.toHexString(id));
			}
		}
		else
		{
			switch(id)
			{
				case 1:
					packet = new OnlineStatus();
					break;
				case 2:
					packet = new PlayerAuthRequest();
					break;
				case 3:
					packet = new PlayerInGame();
					break;
				case 4:
					packet = new PlayerLogout();
					break;
				case 17:
					packet = new ChangeAccessLevel();
					break;
				case 160:
					packet = new IGPwdCng();
					break;
				case 255:
					packet = new PingResponse();
					break;
				default:
					_log.error("Received unknown packet: " + Integer.toHexString(id));
			}
		}
		
		return packet;
	}
}