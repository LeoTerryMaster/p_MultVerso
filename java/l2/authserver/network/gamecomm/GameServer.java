package l2.authserver.network.gamecomm;

import l2.authserver.Config;
import org.apache.log4j.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;

public class GameServer
{
	private static final Logger _log = Logger.getLogger(GameServer.class);
	private final AtomicInteger _port = new AtomicInteger(0);
	private int _id;
	private String _internalHost;
	private String _externalHost;
	private InetAddress _internalAddr;
	private InetAddress _externalAddr;
	private volatile int[] _ports = {7777};
	private int _serverType;
	private int _ageLimit;
	private int _protocol;
	private boolean _isOnline;
	private boolean _isPvp;
	private boolean _isShowingBrackets;
	private boolean _isGmOnly;
	private int _maxPlayers;
	private GameServerConnection _conn;
	private boolean _isAuthed;
	private volatile int _playersIngame;
	
	public GameServer(GameServerConnection conn)
	{
		_conn = conn;
	}
	
	public GameServer(int id)
	{
		_id = id;
	}
	
	public int getId()
	{
		return _id;
	}
	
	public void setId(int id)
	{
		_id = id;
	}
	
	public boolean isAuthed()
	{
		return _isAuthed;
	}
	
	public void setAuthed(boolean isAuthed)
	{
		_isAuthed = isAuthed;
	}
	
	public GameServerConnection getConnection()
	{
		return _conn;
	}
	
	public void setConnection(GameServerConnection conn)
	{
		_conn = conn;
	}
	
	public InetAddress getInternalHost() throws UnknownHostException
	{
		if(_internalAddr != null)
		{
			return _internalAddr;
		}
		_internalAddr = InetAddress.getByName(_internalHost);
		return _internalAddr;
	}
	
	public void setInternalHost(String internalHost)
	{
		if(internalHost.equals("*"))
		{
			internalHost = getConnection().getIpAddress();
		}
		_internalHost = internalHost;
		_internalAddr = null;
	}
	
	public InetAddress getExternalHost() throws UnknownHostException
	{
		if(_externalAddr != null)
		{
			return _externalAddr;
		}
		_externalAddr = InetAddress.getByName(_externalHost);
		return _externalAddr;
	}
	
	public void setExternalHost(String externalHost)
	{
		if(externalHost.equals("*"))
		{
			externalHost = getConnection().getIpAddress();
		}
		_externalHost = externalHost;
		_externalAddr = null;
	}
	
	public int getPort()
	{
		int[] ports = _ports;
		return ports[(_port.incrementAndGet() & Integer.MAX_VALUE) % ports.length];
	}
	
	public void setPorts(int[] ports)
	{
		_ports = ports;
	}
	
	public int getMaxPlayers()
	{
		return _maxPlayers;
	}
	
	public void setMaxPlayers(int maxPlayers)
	{
		_maxPlayers = maxPlayers;
	}
	
	public int getOnline()
	{
		return _playersIngame;
	}
	
	public void addAccount(String account)
	{
		++_playersIngame;
	}
	
	public void removeAccount(String account)
	{
		--_playersIngame;
	}
	
	public void setDown()
	{
		setAuthed(false);
		setConnection(null);
		setOnline(false);
	}
	
	public String getName()
	{
		return Config.SERVER_NAMES.get(getId());
	}
	
	public void sendPacket(SendablePacket packet)
	{
		GameServerConnection conn = getConnection();
		if(conn != null)
		{
			conn.sendPacket(packet);
		}
	}
	
	public int getServerType()
	{
		return _serverType;
	}
	
	public void setServerType(int serverType)
	{
		_serverType = serverType;
	}
	
	public boolean isOnline()
	{
		return _isOnline;
	}
	
	public void setOnline(boolean online)
	{
		_isOnline = online;
	}
	
	public boolean isPvp()
	{
		return _isPvp;
	}
	
	public void setPvp(boolean pvp)
	{
		_isPvp = pvp;
	}
	
	public boolean isShowingBrackets()
	{
		return _isShowingBrackets;
	}
	
	public void setShowingBrackets(boolean showingBrackets)
	{
		_isShowingBrackets = showingBrackets;
	}
	
	public boolean isGmOnly()
	{
		return _isGmOnly;
	}
	
	public void setGmOnly(boolean gmOnly)
	{
		_isGmOnly = gmOnly;
	}
	
	public int getAgeLimit()
	{
		return _ageLimit;
	}
	
	public void setAgeLimit(int ageLimit)
	{
		_ageLimit = ageLimit;
	}
	
	public void setProtocol(int protocol)
	{
		_protocol = protocol;
	}
}