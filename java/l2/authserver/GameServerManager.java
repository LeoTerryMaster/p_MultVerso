package l2.authserver;

import l2.authserver.database.L2DatabaseFactory;
import l2.authserver.network.gamecomm.GameServer;
import l2.authserver.network.gamecomm.ProxyServer;
import l2.commons.dbutils.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class GameServerManager
{
	private static final Logger LOG = LoggerFactory.getLogger(GameServerManager.class);
	private static final GameServerManager INSTANCE = new GameServerManager();
	private final Map<Integer, GameServer> _gameServers = new TreeMap<>();
	private final Map<Integer, List<ProxyServer>> _gameServerProxys = new TreeMap<>();
	private final Map<Integer, ProxyServer> _proxyServers = new TreeMap<>();
	private final ReadWriteLock _lock = new ReentrantReadWriteLock();
	private final Lock _readLock = _lock.readLock();
	private final Lock _writeLock = _lock.writeLock();
	
	public GameServerManager()
	{
		loadGameServers();
		LOG.info("Loaded " + _gameServers.size() + " registered GameServer(s).");
		loadProxyServers();
		LOG.info("Loaded " + _proxyServers.size() + " proxy server(s).");
	}
	
	public static final GameServerManager getInstance()
	{
		return INSTANCE;
	}
	
	private void loadGameServers()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT server_id FROM gameservers");
			rset = statement.executeQuery();
			while(rset.next())
			{
				int id = rset.getInt("server_id");
				for(Config.ProxyServerConfig psc : Config.PROXY_SERVERS_CONFIGS)
				{
					if(psc.getProxyId() != id)
						continue;
					LOG.warn("Server with id " + id + " collides with proxy server.");
				}
				GameServer gs = new GameServer(id);
				_gameServers.put(id, gs);
			}
		}
		catch(Exception e)
		{
			LOG.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}
	
	private void loadProxyServers()
	{
		for(Config.ProxyServerConfig psc : Config.PROXY_SERVERS_CONFIGS)
		{
			if(_gameServers.containsKey(psc.getProxyId()))
			{
				LOG.warn("Won't load collided proxy with id " + psc.getProxyId() + ".");
				continue;
			}
			ProxyServer ps = new ProxyServer(psc.getOrigServerId(), psc.getProxyId());
			try
			{
				InetAddress inetAddress = InetAddress.getByName(psc.getPorxyHost());
				ps.setProxyAddr(inetAddress);
			}
			catch(UnknownHostException e)
			{
				LOG.error("Can't load proxy", e);
				continue;
			}
			ps.setProxyPort(psc.getProxyPort());
			List<ProxyServer> proxyList = _gameServerProxys.get(ps.getOrigServerId());
			if(proxyList == null)
			{
				proxyList = new LinkedList<>();
				_gameServerProxys.put(ps.getOrigServerId(), proxyList);
			}
			proxyList.add(ps);
			_proxyServers.put(ps.getProxyServerId(), ps);
		}
	}
	
	public List<ProxyServer> getProxyServersList(int gameServerId)
	{
		List<ProxyServer> result = _gameServerProxys.get(gameServerId);
		return result != null ? result : Collections.emptyList();
	}
	
	public ProxyServer getProxyServerById(int proxyServerId)
	{
		return _proxyServers.get(proxyServerId);
	}
	
	public GameServer[] getGameServers()
	{
		_readLock.lock();
		try
		{
			GameServer[] arrgameServer = _gameServers.values().toArray(new GameServer[_gameServers.size()]);
			return arrgameServer;
		}
		finally
		{
			_readLock.unlock();
		}
	}
	
	public GameServer getGameServerById(int id)
	{
		_readLock.lock();
		try
		{
			GameServer gameServer = _gameServers.get(id);
			return gameServer;
		}
		finally
		{
			_readLock.unlock();
		}
	}
	
	public boolean registerGameServer(GameServer gs)
	{
		if(!Config.ACCEPT_NEW_GAMESERVER)
		{
			return false;
		}
		_writeLock.lock();
		try
		{
			int id = 1;
			while(id++ < 127)
			{
				GameServer pgs = _gameServers.get(id);
				if(!_proxyServers.containsKey(id) && pgs != null)
					continue;
				_gameServers.put(id, gs);
				gs.setId(id);
				boolean bl = true;
				return bl;
			}
		}
		finally
		{
			_writeLock.unlock();
		}
		return false;
	}
	
	public boolean registerGameServer(int id, GameServer gs)
	{
		_writeLock.lock();
		try
		{
			GameServer pgs = _gameServers.get(id);
			if(!Config.ACCEPT_NEW_GAMESERVER && pgs == null)
			{
				boolean bl = false;
				return bl;
			}
			if(pgs == null || !pgs.isAuthed())
			{
				_gameServers.put(id, gs);
				gs.setId(id);
				boolean bl = true;
				return bl;
			}
		}
		finally
		{
			_writeLock.unlock();
		}
		return false;
	}
}