package l2.gameserver.network.authcomm.as2gs;

import l2.gameserver.network.authcomm.AuthServerCommunication;
import l2.gameserver.network.authcomm.ReceivablePacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginServerFail extends ReceivablePacket
{
	private static final Logger _log = LoggerFactory.getLogger(LoginServerFail.class);
	private static final String[] reasons = {"none", "IP banned", "IP reserved", "wrong hexid", "ID reserved", "no free ID", "not authed", "already logged in"};
	private int _reason;
	
	public String getReason()
	{
		return reasons[_reason];
	}
	
	@Override
	protected void readImpl()
	{
		_reason = readC();
	}
	
	@Override
	protected void runImpl()
	{
		_log.warn("Authserver registration failed! Reason: " + getReason());
		AuthServerCommunication.getInstance().restart();
	}
}