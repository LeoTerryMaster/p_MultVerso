package l2.authserver.accounts;

import l2.authserver.ThreadPoolManager;
import l2.authserver.network.l2.SessionKey;
import l2.commons.threading.RunnableImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SessionManager
{
	private static final Logger _log = LoggerFactory.getLogger(SessionManager.class);
	private static final SessionManager _instance = new SessionManager();
	private final Map<SessionKey, Session> sessions = new HashMap<>();
	private final Lock lock = new ReentrantLock();
	
	private SessionManager()
	{
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new RunnableImpl()
		{
			
			@Override
			public void runImpl()
			{
				lock.lock();
				try
				{
					long currentMillis = System.currentTimeMillis();
					Iterator<Session> itr = sessions.values().iterator();
					while(itr.hasNext())
					{
						Session session = itr.next();
						if(session.getExpireTime() >= currentMillis)
							continue;
						itr.remove();
					}
				}
				finally
				{
					lock.unlock();
				}
			}
		}, 30000, 30000);
	}
	
	public static final SessionManager getInstance()
	{
		return _instance;
	}
	
	public Session openSession(Account account)
	{
		lock.lock();
		try
		{
			Session session = new Session(account);
			sessions.put(session.getSessionKey(), session);
			Session session2 = session;
			return session2;
		}
		finally
		{
			lock.unlock();
		}
	}
	
	public Session closeSession(SessionKey skey)
	{
		lock.lock();
		try
		{
			Session session = sessions.remove(skey);
			return session;
		}
		finally
		{
			lock.unlock();
		}
	}
	
	public Session getSessionByName(String name)
	{
		for(Session session : sessions.values())
		{
			if(!session.account.getLogin().equalsIgnoreCase(name))
				continue;
			return session;
		}
		return null;
	}
	
	public final class Session
	{
		private final Account account;
		private final SessionKey skey;
		private final long expireTime;
		
		private Session(Account account)
		{
			this.account = account;
			skey = SessionKey.create();
			expireTime = System.currentTimeMillis() + 60000;
		}
		
		public SessionKey getSessionKey()
		{
			return skey;
		}
		
		public Account getAccount()
		{
			return account;
		}
		
		public long getExpireTime()
		{
			return expireTime;
		}
	}
}