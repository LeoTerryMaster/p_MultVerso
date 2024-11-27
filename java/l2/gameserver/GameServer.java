package l2.gameserver;

import l2.commons.lang.StatsUtils;
import l2.commons.listener.Listener;
import l2.commons.listener.ListenerList;
import l2.commons.net.nio.impl.SelectorThread;
import l2.commons.versioning.Version;
import l2.gameserver.cache.CrestCache;
import l2.gameserver.dao.CharacterDAO;
import l2.gameserver.dao.ItemsDAO;
import l2.gameserver.data.BoatHolder;
import l2.gameserver.data.xml.Parsers;
import l2.gameserver.data.xml.holder.EventHolder;
import l2.gameserver.data.xml.holder.ResidenceHolder;
import l2.gameserver.data.xml.holder.StaticObjectHolder;
import l2.gameserver.database.DatabaseFactory;
import l2.gameserver.geodata.GeoEngine;
import l2.gameserver.handler.admincommands.AdminCommandHandler;
import l2.gameserver.handler.items.ItemHandler;
import l2.gameserver.handler.usercommands.UserCommandHandler;
import l2.gameserver.handler.voicecommands.VoicedCommandHandler;
import l2.gameserver.idfactory.IdFactory;
import l2.gameserver.instancemanager.CastleManorManager;
import l2.gameserver.instancemanager.CoupleManager;
import l2.gameserver.instancemanager.CursedWeaponsManager;
import l2.gameserver.instancemanager.DimensionalRiftManager;
import l2.gameserver.instancemanager.PetitionManager;
import l2.gameserver.instancemanager.PlayerMessageStack;
import l2.gameserver.instancemanager.RaidBossSpawnManager;
import l2.gameserver.instancemanager.SpawnManager;
import l2.gameserver.instancemanager.games.FishingChampionShipManager;
import l2.gameserver.instancemanager.games.LotteryManager;
import l2.gameserver.listener.GameListener;
import l2.gameserver.listener.game.OnShutdownListener;
import l2.gameserver.listener.game.OnStartListener;
import l2.gameserver.model.World;
import l2.gameserver.model.entity.MonsterRace;
import l2.gameserver.model.entity.SevenSigns;
import l2.gameserver.model.entity.SevenSignsFestival.SevenSignsFestival;
import l2.gameserver.model.entity.oly.HeroController;
import l2.gameserver.model.entity.oly.NoblesController;
import l2.gameserver.model.entity.oly.OlyController;
import l2.gameserver.network.authcomm.AuthServerCommunication;
import l2.gameserver.network.l2.CGMHelper;
import l2.gameserver.network.l2.GameClient;
import l2.gameserver.network.l2.GamePacketHandler;
import l2.gameserver.network.telnet.TelnetServer;
import l2.gameserver.scripts.Scripts;
import l2.gameserver.tables.CharTemplateTable;
import l2.gameserver.tables.ClanTable;
import l2.gameserver.tables.LevelUpTable;
import l2.gameserver.tables.PetSkillsTable;
import l2.gameserver.tables.SkillTreeTable;
import l2.gameserver.taskmanager.ItemsAutoDestroy;
import l2.gameserver.taskmanager.L2TopRuManager;
import l2.gameserver.taskmanager.TaskManager;
import l2.gameserver.taskmanager.tasks.RestoreOfflineTraders;
import l2.gameserver.utils.Strings;
import net.sf.ehcache.CacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.InetAddress;
import java.net.ServerSocket;

public class GameServer
{
	public static final int AUTH_SERVER_PROTOCOL = 2;
	private static final Logger _log = LoggerFactory.getLogger(GameServer.class);
	public static GameServer _instance;
	private final SelectorThread<GameClient>[] _selectorThreads;
	private final GameServerListenerList _listeners;
	private final Version version;
	private final long _serverStartTimeMillis;
	private TelnetServer statusServer;
	
	public GameServer() throws Exception
	{
		_instance = this;
		_serverStartTimeMillis = System.currentTimeMillis();
		_listeners = new GameServerListenerList();
		new File("./log/").mkdir();
		version = new Version(GameServer.class);
		_log.info("=================================================");
		_log.info("Revision: ................ " + version.getRevisionNumber());
		_log.info("Build date: .............. " + version.getBuildDate());
		_log.info("Compiler version: ........ " + version.getBuildJdk());
		_log.info("=================================================");
		Config.load();
		checkFreePorts();
		DatabaseFactory.getInstance().getConnection().close();
		IdFactory _idFactory = IdFactory.getInstance();
		if(!_idFactory.isInitialized())
		{
			_log.error("Could not read object IDs from DB. Please Check Your Data.");
			throw new Exception("Could not initialize the ID factory");
		}
		CacheManager.getInstance();
		ThreadPoolManager.getInstance();
		Scripts.getInstance();
		GeoEngine.load();
		Strings.reload();
		GameTimeController.getInstance();
		World.init();
		Parsers.parseAll();
		ItemsDAO.getInstance();
		CrestCache.getInstance();
		CharacterDAO.getInstance();
		ClanTable.getInstance();
		SkillTreeTable.getInstance();
		CharTemplateTable.getInstance();
		LevelUpTable.getInstance();
		PetSkillsTable.getInstance();
		SpawnManager.getInstance().spawnAll();
		BoatHolder.getInstance().spawnAll();
		StaticObjectHolder.getInstance().spawnAll();
		RaidBossSpawnManager.getInstance();
		Scripts.getInstance().init();
		DimensionalRiftManager.getInstance();
		Announcements.getInstance();
		LotteryManager.getInstance();
		PlayerMessageStack.getInstance();
		if(Config.AUTODESTROY_ITEM_AFTER > 0)
		{
			ItemsAutoDestroy.getInstance();
		}
		MonsterRace.getInstance();
		SevenSigns.getInstance();
		SevenSignsFestival.getInstance();
		SevenSigns.getInstance().updateFestivalScore();
		NoblesController.getInstance();
		if(Config.OLY_ENABLED)
		{
			OlyController.getInstance();
			HeroController.getInstance();
		}
		PetitionManager.getInstance();
		if(!Config.ALLOW_WEDDING)
		{
			CoupleManager.getInstance();
			_log.info("CoupleManager initialized");
		}
		ItemHandler.getInstance();
		AdminCommandHandler.getInstance().log();
		UserCommandHandler.getInstance().log();
		VoicedCommandHandler.getInstance().log();
		TaskManager.getInstance();
		_log.info("=[Events]=========================================");
		ResidenceHolder.getInstance().callInit();
		EventHolder.getInstance().callInit();
		_log.info("==================================================");
		CastleManorManager.getInstance();
		Runtime.getRuntime().addShutdownHook(Shutdown.getInstance());
		_log.info("IdFactory: Free ObjectID's remaining: " + IdFactory.getInstance().size());
		CoupleManager.getInstance();
		CursedWeaponsManager.getInstance();
		if(Config.ALT_FISH_CHAMPIONSHIP_ENABLED)
		{
			FishingChampionShipManager.getInstance();
		}
		L2TopRuManager.getInstance();
		Shutdown.getInstance().schedule(Config.RESTART_AT_TIME, 2);
		_log.info("GameServer Started");
		_log.info("Maximum Numbers of Connected Players: " + Config.MAXIMUM_ONLINE_USERS);
		GamePacketHandler gph = new GamePacketHandler();
		InetAddress serverAddr = Config.GAMESERVER_HOSTNAME.equalsIgnoreCase("*") ? null : InetAddress.getByName(Config.GAMESERVER_HOSTNAME);
		_selectorThreads = new SelectorThread[Config.PORTS_GAME.length];
		for(int i = 0;i < Config.PORTS_GAME.length;++i)
		{
			_selectorThreads[i] = new SelectorThread(Config.SELECTOR_CONFIG, gph, gph, gph, null);
			_selectorThreads[i].openServerSocket(serverAddr, Config.PORTS_GAME[i]);
			_selectorThreads[i].start();
		}
		AuthServerCommunication.getInstance().start();
		if(Config.SERVICES_OFFLINE_TRADE_RESTORE_AFTER_RESTART)
		{
			ThreadPoolManager.getInstance().schedule(new RestoreOfflineTraders(), 30000);
		}
		getListeners().onStart();
		if(Config.IS_TELNET_ENABLED)
		{
			statusServer = new TelnetServer();
		}
		else
		{
			_log.info("Telnet server is currently disabled.");
		}
		CGMHelper.getInstance();
		_log.info("=================================================");
		String memUsage = new StringBuilder().append(StatsUtils.getMemUsage()).toString();
		for(String line : memUsage.split("\n"))
		{
			_log.info(line);
		}
		_log.info("=================================================");
	}
	
	public static GameServer getInstance()
	{
		return _instance;
	}
	
	public static void checkFreePorts()
	{
		boolean binded = false;
		while(!binded)
		{
			for(int PORT_GAME : Config.PORTS_GAME)
			{
				try
				{
					ServerSocket ss = Config.GAMESERVER_HOSTNAME.equalsIgnoreCase("*") ? new ServerSocket(PORT_GAME) : new ServerSocket(PORT_GAME, 50, InetAddress.getByName(Config.GAMESERVER_HOSTNAME));
					ss.close();
					binded = true;
				}
				catch(Exception e)
				{
					_log.warn("Port " + PORT_GAME + " is allready binded. Please free it and restart server.");
					binded = false;
					try
					{
						Thread.sleep(1000);
					}
					catch(InterruptedException e2)
					{
						
					}
				}
			}
		}
	}
	
	public static void main(String[] args) throws Exception
	{
		new GameServer();
	}
	
	public SelectorThread<GameClient>[] getSelectorThreads()
	{
		return _selectorThreads;
	}
	
	public long getServerStartTime()
	{
		return _serverStartTimeMillis;
	}
	
	public GameServerListenerList getListeners()
	{
		return _listeners;
	}
	
	public <T extends GameListener> boolean addListener(T listener)
	{
		return _listeners.add(listener);
	}
	
	public <T extends GameListener> boolean removeListener(T listener)
	{
		return _listeners.remove(listener);
	}
	
	public Version getVersion()
	{
		return version;
	}
	
	public TelnetServer getStatusServer()
	{
		return statusServer;
	}
	
	public class GameServerListenerList extends ListenerList<GameServer>
	{
		public void onStart()
		{
			for(Listener listener : getListeners())
			{
				if(!OnStartListener.class.isInstance(listener))
					continue;
				((OnStartListener) listener).onStart();
			}
		}
		
		public void onShutdown()
		{
			for(Listener listener : getListeners())
			{
				if(!OnShutdownListener.class.isInstance(listener))
					continue;
				((OnShutdownListener) listener).onShutdown();
			}
		}
	}
}