package l2.authserver;

import l2.authserver.database.L2DatabaseFactory;
import l2.authserver.network.gamecomm.GameServerCommunication;
import l2.authserver.network.l2.L2LoginClient;
import l2.authserver.network.l2.L2LoginPacketHandler;
import l2.authserver.network.l2.SelectorHelper;
import l2.commons.net.nio.impl.SelectorConfig;
import l2.commons.net.nio.impl.SelectorThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.InetAddress;
import java.net.ServerSocket;

public class AuthServer
{
	private static final Logger _log = LoggerFactory.getLogger(AuthServer.class);
	private static AuthServer authServer;
	private final GameServerCommunication _gameServerListener;
	private final SelectorThread<L2LoginClient> _selectorThread;
	
	public AuthServer() throws Throwable
	{
		Config.initCrypt();
		GameServerManager.getInstance();
		L2LoginPacketHandler loginPacketHandler = new L2LoginPacketHandler();
		SelectorHelper sh = new SelectorHelper();
		SelectorConfig sc = new SelectorConfig();
		_selectorThread = new SelectorThread(sc, loginPacketHandler, sh, sh, sh);
		_gameServerListener = GameServerCommunication.getInstance();
		_gameServerListener.openServerSocket(Config.GAME_SERVER_LOGIN_HOST.equals("*") ? null : InetAddress.getByName(Config.GAME_SERVER_LOGIN_HOST), Config.GAME_SERVER_LOGIN_PORT);
		_gameServerListener.start();
		_log.info("Listening for gameservers on " + Config.GAME_SERVER_LOGIN_HOST + ":" + Config.GAME_SERVER_LOGIN_PORT);
		_selectorThread.openServerSocket(Config.LOGIN_HOST.equals("*") ? null : InetAddress.getByName(Config.LOGIN_HOST), Config.PORT_LOGIN);
		_selectorThread.start();
		_log.info("Listening for clients on " + Config.LOGIN_HOST + ":" + Config.PORT_LOGIN);
	}
	
	public static AuthServer getInstance()
	{
		return authServer;
	}
	
	public static void checkFreePorts() throws Throwable
	{
		ServerSocket ss = null;
		try
		{
			ss = Config.LOGIN_HOST.equalsIgnoreCase("*") ? new ServerSocket(Config.PORT_LOGIN) : new ServerSocket(Config.PORT_LOGIN, 50, InetAddress.getByName(Config.LOGIN_HOST));
		}
		finally
		{
			if(ss != null)
			{
				try
				{
					ss.close();
				}
				catch(Exception e)
				{
				}
			}
		}
	}
	
	public static void main(String[] args) throws Throwable
	{
		new File("./log/").mkdir();
		Config.load();
		checkFreePorts();
		L2DatabaseFactory.getInstance().getConnection().close();
		authServer = new AuthServer();
	}
	
	public GameServerCommunication getGameServerListener()
	{
		return _gameServerListener;
	}
}