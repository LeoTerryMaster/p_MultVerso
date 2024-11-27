package l2.authserver;

import l2.authserver.crypt.PasswordHash;
import l2.authserver.crypt.ScrambledKeyPair;
import l2.commons.configuration.ExProperties;
import l2.commons.util.Rnd;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.security.KeyPairGenerator;
import java.security.spec.RSAKeyGenParameterSpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Config
{
	public static final String LOGIN_CONFIGURATION_FILE = "config/authserver.properties";
	public static final String SERVER_NAMES_FILE = "config/servername.xml";
	public static final String PROXY_SERVERS_FILE = "config/proxyservers.xml";
	public static final Map<Integer, String> SERVER_NAMES;
	public static final long LOGIN_TIMEOUT = 60000;
	private static final Logger _log = LoggerFactory.getLogger(Config.class);
	public static String LOGIN_HOST;
	public static int PORT_LOGIN;
	public static String GAME_SERVER_LOGIN_HOST;
	public static int GAME_SERVER_LOGIN_PORT;
	public static long GAME_SERVER_PING_DELAY;
	public static int GAME_SERVER_PING_RETRY;
	public static String DATABASE_HOST;
	public static int DATABASE_PORT;
	public static String DATABASE_NAME;
	public static String DATABASE_USER;
	public static String DATABASE_PASS;
	public static int DATABASE_MAX_CONN;
	public static int DATABASE_TIMEOUT;
	public static String DEFAULT_PASSWORD_HASH;
	public static String LEGACY_PASSWORD_HASH;
	public static int LOGIN_BLOWFISH_KEYS;
	public static int LOGIN_RSA_KEYPAIRS;
	public static boolean ACCEPT_NEW_GAMESERVER;
	public static boolean AUTO_CREATE_ACCOUNTS;
	public static String ANAME_TEMPLATE;
	public static String APASSWD_TEMPLATE;
	public static int LOGIN_TRY_BEFORE_BAN;
	public static long LOGIN_TRY_TIMEOUT;
	public static long IP_BAN_TIME;
	public static Set<String> WHITE_IPS;
	public static PasswordHash DEFAULT_CRYPT;
	public static PasswordHash[] LEGACY_CRYPT;
	public static boolean LOGIN_LOG;
	public static ProxyServerConfig[] PROXY_SERVERS_CONFIGS;
	private static ScrambledKeyPair[] _keyPairs;
	private static byte[][] _blowfishKeys;
	
	static
	{
		SERVER_NAMES = new HashMap<>();
		WHITE_IPS = new HashSet<>();
	}
	
	private Config()
	{
	}
	
	public static final void load()
	{
		loadConfiguration();
		loadServerNames();
		loadServerProxies();
	}
	
	public static final void initCrypt() throws Throwable
	{
		DEFAULT_CRYPT = new PasswordHash(DEFAULT_PASSWORD_HASH);
		ArrayList<PasswordHash> legacy = new ArrayList<>();
		for(String method : LEGACY_PASSWORD_HASH.split(";"))
		{
			if(method.equalsIgnoreCase(DEFAULT_PASSWORD_HASH))
				continue;
			legacy.add(new PasswordHash(method));
		}
		LEGACY_CRYPT = legacy.toArray(new PasswordHash[legacy.size()]);
		_log.info("Loaded " + DEFAULT_PASSWORD_HASH + " as default crypt.");
		_keyPairs = new ScrambledKeyPair[LOGIN_RSA_KEYPAIRS];
		KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
		RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(1024, RSAKeyGenParameterSpec.F4);
		keygen.initialize(spec);
		int i;
		for(i = 0;i < _keyPairs.length;++i)
		{
			_keyPairs[i] = new ScrambledKeyPair(keygen.generateKeyPair());
		}
		_log.info("Cached " + _keyPairs.length + " KeyPairs for RSA communication");
		_blowfishKeys = new byte[LOGIN_BLOWFISH_KEYS][16];
		for(i = 0;i < _blowfishKeys.length;++i)
		{
			for(int j = 0;j < _blowfishKeys[i].length;++j)
			{
				_blowfishKeys[i][j] = (byte) (Rnd.get(255) + 1);
			}
		}
		_log.info("Stored " + _blowfishKeys.length + " keys for Blowfish communication");
	}
	
	public static final void loadServerNames()
	{
		SERVER_NAMES.clear();
		try
		{
			SAXReader reader = new SAXReader(true);
			Document document = reader.read(new File("config/servername.xml"));
			Element root = document.getRootElement();
			Iterator<Element> itr = root.elementIterator();
			while(itr.hasNext())
			{
				Element node = itr.next();
				if(!node.getName().equalsIgnoreCase("server"))
					continue;
				Integer id = Integer.valueOf(node.attributeValue("id"));
				String name = node.attributeValue("name");
				SERVER_NAMES.put(id, name);
			}
			_log.info("Loaded " + SERVER_NAMES.size() + " server names");
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
	}
	
	public static void loadServerProxies()
	{
		ArrayList<ProxyServerConfig> proxyServersConfigs = new ArrayList<>();
		try
		{
			SAXReader reader = new SAXReader(true);
			Document document = reader.read(new File("config/proxyservers.xml"));
			Element root = document.getRootElement();
			Iterator<Element> itr = root.elementIterator();
			while(itr.hasNext())
			{
				Element node = itr.next();
				if(!node.getName().equalsIgnoreCase("proxyServer"))
					continue;
				int origSrvId = Integer.parseInt(node.attributeValue("origId"));
				int proxySrvId = Integer.parseInt(node.attributeValue("proxyId"));
				String proxyHost = node.attributeValue("proxyHost");
				int proxyPort = Integer.parseInt(node.attributeValue("proxyPort"));
				ProxyServerConfig psc = new ProxyServerConfig(origSrvId, proxySrvId, proxyHost, proxyPort);
				proxyServersConfigs.add(psc);
			}
		}
		catch(Exception e)
		{
			_log.error("Can't load proxy server's config", e);
		}
		PROXY_SERVERS_CONFIGS = proxyServersConfigs.toArray(new ProxyServerConfig[proxyServersConfigs.size()]);
	}
	
	public static String DATABASE_DRIVER;
	public static String DATABASE_URL;
	public static String DATABASE_LOGIN;
	public static String DATABASE_PASSWORD;
	
	public static final void loadConfiguration()
	{
		ExProperties serverSettings = load("config/authserver.properties");
		LOGIN_HOST = serverSettings.getProperty("LoginserverHostname", "127.0.0.1");
		PORT_LOGIN = serverSettings.getProperty("LoginserverPort", 2106);
		GAME_SERVER_LOGIN_HOST = serverSettings.getProperty("LoginHost", "127.0.0.1");
		GAME_SERVER_LOGIN_PORT = serverSettings.getProperty("LoginPort", 9014);


		
		Config.DATABASE_DRIVER = serverSettings.getProperty("dataSourceClassName", "org.mariadb.jdbc.Driver");
		String databaseHost = serverSettings.getProperty("Database_Host", "localhost");
		int databasePort = serverSettings.getProperty("Maria_Port", 3306);
		String databaseName = serverSettings.getProperty("Data_Name", "l2auth");
		Config.DATABASE_URL = serverSettings.getProperty("URL", "jdbc:mariadb://" + databaseHost + ":" + databasePort + "/" + databaseName + "?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=UTC");
		Config.DATABASE_LOGIN = serverSettings.getProperty("Login", "root");
		Config.DATABASE_PASSWORD = serverSettings.getProperty("Password", "root");
		
		
		
		Config.DATABASE_PASSWORD = serverSettings.getProperty("Password", "root");
		
		
		DATABASE_MAX_CONN = serverSettings.getProperty("DatabaseMaxConnections", 8);
		DATABASE_TIMEOUT = serverSettings.getProperty("DatabaseConnectionTimeout", 30);
		LOGIN_BLOWFISH_KEYS = serverSettings.getProperty("BlowFishKeys", 20);
		LOGIN_RSA_KEYPAIRS = serverSettings.getProperty("RSAKeyPairs", 10);
		ACCEPT_NEW_GAMESERVER = serverSettings.getProperty("AcceptNewGameServer", true);
		DEFAULT_PASSWORD_HASH = serverSettings.getProperty("PasswordHash", "sha1");
		LEGACY_PASSWORD_HASH = serverSettings.getProperty("LegacyPasswordHash", "whirlpool2");
		AUTO_CREATE_ACCOUNTS = serverSettings.getProperty("AutoCreateAccounts", true);
		ANAME_TEMPLATE = serverSettings.getProperty("AccountTemplate", "[A-Za-z0-9]{4,14}");
		APASSWD_TEMPLATE = serverSettings.getProperty("PasswordTemplate", "[A-Za-z0-9]{4,16}");
		LOGIN_TRY_BEFORE_BAN = serverSettings.getProperty("LoginTryBeforeBan", 10);
		LOGIN_TRY_TIMEOUT = (long) serverSettings.getProperty("LoginTryTimeout", 5) * 1000;
		IP_BAN_TIME = (long) serverSettings.getProperty("IpBanTime", 300) * 1000;
		WHITE_IPS.addAll(Arrays.asList(serverSettings.getProperty("WhiteIpList", new String[] {"127.0.0.1"})));
		GAME_SERVER_PING_DELAY = (long) serverSettings.getProperty("GameServerPingDelay", 30) * 1000;
		GAME_SERVER_PING_RETRY = serverSettings.getProperty("GameServerPingRetry", 4);
		LOGIN_LOG = serverSettings.getProperty("LoginLog", true);
	}
	
	public static ExProperties load(String filename)
	{
		return load(new File(filename));
	}
	
	public static ExProperties load(File file)
	{
		ExProperties result = new ExProperties();
		try
		{
			result.load(file);
		}
		catch(IOException e)
		{
			_log.error("", e);
		}
		return result;
	}
	
	public static ScrambledKeyPair getScrambledRSAKeyPair()
	{
		return _keyPairs[Rnd.get(_keyPairs.length)];
	}
	
	public static byte[] getBlowfishKey()
	{
		return _blowfishKeys[Rnd.get(_blowfishKeys.length)];
	}
	
	public static class ProxyServerConfig
	{
		private final int _origServerId;
		private final int _proxyServerId;
		private final String _porxyHost;
		private final int _proxyPort;
		
		public ProxyServerConfig(int origServerId, int proxyServerId, String porxyHost, int proxyPort)
		{
			_origServerId = origServerId;
			_proxyServerId = proxyServerId;
			_porxyHost = porxyHost;
			_proxyPort = proxyPort;
		}
		
		public int getOrigServerId()
		{
			return _origServerId;
		}
		
		public int getProxyId()
		{
			return _proxyServerId;
		}
		
		public String getPorxyHost()
		{
			return _porxyHost;
		}
		
		public int getProxyPort()
		{
			return _proxyPort;
		}
	}
}