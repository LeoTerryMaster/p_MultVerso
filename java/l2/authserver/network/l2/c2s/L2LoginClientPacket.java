package l2.authserver.network.l2.c2s;

import l2.authserver.network.l2.L2LoginClient;
import l2.commons.net.nio.impl.ReceivablePacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class L2LoginClientPacket extends ReceivablePacket<L2LoginClient>
{
	private static final Logger _log = LoggerFactory.getLogger(L2LoginClientPacket.class);
	
	@Override
	protected final boolean read()
	{
		try
		{
			readImpl();
			return true;
		}
		catch(Exception e)
		{
			_log.error("", e);
			return false;
		}
	}
	
	@Override
	public void run()
	{
		try
		{
			runImpl();
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
	}
	
	protected abstract void readImpl();
	
	protected abstract void runImpl() throws Exception;
}