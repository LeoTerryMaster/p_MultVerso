package l2.authserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class IpBanManager
{
	private static final Logger _log = LoggerFactory.getLogger(IpBanManager.class);
	private static final IpBanManager _instance = new IpBanManager();
	private final Map<String, IpSession> ips = new HashMap<>();
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private final Lock readLock = lock.readLock();
	private final Lock writeLock = lock.writeLock();
	
	private IpBanManager()
	{
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable()
		{
			
			@Override
			public void run()
			{
				long currentMillis = System.currentTimeMillis();
				writeLock.lock();
				try
				{
					Iterator<IpSession> itr = ips.values().iterator();
					while(itr.hasNext())
					{
						IpSession session = itr.next();
						if(session.banExpire >= currentMillis || session.lastTry >= currentMillis - Config.LOGIN_TRY_TIMEOUT)
							continue;
						itr.remove();
					}
				}
				finally
				{
					writeLock.unlock();
				}
			}
		}, 1000, 1000);
	}
	
	public static final IpBanManager getInstance()
	{
		return _instance;
	}
	
	public boolean isIpBanned(String ip)
	{
		if(Config.WHITE_IPS.contains(ip))
		{
			return false;
		}
		readLock.lock();
		try
		{
			IpSession ipsession = ips.get(ip);
			if(ipsession == null)
			{
				boolean bl = false;
				return bl;
			}
			boolean bl = ipsession.banExpire > System.currentTimeMillis();
			return bl;
		}
		finally
		{
			readLock.unlock();
		}
	}
	
	public boolean tryLogin(String ip, boolean success)
	{
		if(Config.WHITE_IPS.contains(ip))
		{
			return true;
		}
		writeLock.lock();
		try
		{
			IpSession ipsession = ips.get(ip);
			if(ipsession == null)
			{
				ipsession = new IpSession();
				ips.put(ip, ipsession);
			}
			long currentMillis;
			if((currentMillis = System.currentTimeMillis()) - ipsession.lastTry < Config.LOGIN_TRY_TIMEOUT)
			{
				success = false;
			}
			if(success)
			{
				if(ipsession.tryCount > 0)
				{
					--ipsession.tryCount;
				}
			}
			else if(ipsession.tryCount < Config.LOGIN_TRY_BEFORE_BAN)
			{
				++ipsession.tryCount;
			}
			ipsession.lastTry = currentMillis;
			if(ipsession.tryCount == Config.LOGIN_TRY_BEFORE_BAN)
			{
				_log.warn("IpBanManager: " + ip + " banned for " + Config.IP_BAN_TIME / 1000 + " seconds.");
				ipsession.banExpire = currentMillis + Config.IP_BAN_TIME;
				boolean bl = false;
				return bl;
			}
			boolean bl = true;
			return bl;
		}
		finally
		{
			writeLock.unlock();
		}
	}
	
	private class IpSession
	{
		public int tryCount;
		public long lastTry;
		public long banExpire;
	}
}