package l2.gameserver;

import l2.commons.net.nio.impl.SelectorThread;
import l2.commons.time.cron.SchedulingPattern;
import l2.gameserver.data.xml.holder.ResidenceHolder;
import l2.gameserver.database.DatabaseFactory;
import l2.gameserver.instancemanager.CoupleManager;
import l2.gameserver.instancemanager.CursedWeaponsManager;
import l2.gameserver.instancemanager.games.FishingChampionShipManager;
import l2.gameserver.model.GameObjectsStorage;
import l2.gameserver.model.Player;
import l2.gameserver.model.entity.SevenSigns;
import l2.gameserver.model.entity.SevenSignsFestival.SevenSignsFestival;
import l2.gameserver.model.entity.oly.HeroController;
import l2.gameserver.model.entity.oly.NoblesController;
import l2.gameserver.model.entity.oly.OlyController;
import l2.gameserver.model.entity.oly.ParticipantPool;
import l2.gameserver.model.entity.oly.StadiumPool;
import l2.gameserver.model.entity.residence.Residence;
import l2.gameserver.network.authcomm.AuthServerCommunication;
import l2.gameserver.network.l2.GameClient;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.scripts.Scripts;
import l2.gameserver.utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

public class Shutdown extends Thread
{
	public static final int SHUTDOWN = 0;
	public static final int RESTART = 2;
	public static final int NONE = -1;
	private static final Logger _log = LoggerFactory.getLogger(Shutdown.class);
	private static final Shutdown _instance = new Shutdown();
	private Timer counter;
	private int shutdownMode;
	private int shutdownCounter;
	
	private Shutdown()
	{
		setName(getClass().getSimpleName());
		setDaemon(true);
		shutdownMode = -1;
	}
	
	public static final Shutdown getInstance()
	{
		return _instance;
	}
	
	public int getSeconds()
	{
		return shutdownMode == -1 ? -1 : shutdownCounter;
	}
	
	public int getMode()
	{
		return shutdownMode;
	}
	
	public synchronized void schedule(int seconds, int shutdownMode)
	{
		if(seconds < 0)
		{
			return;
		}
		if(counter != null)
		{
			counter.cancel();
		}
		this.shutdownMode = shutdownMode;
		shutdownCounter = seconds;
		_log.info("Scheduled server " + (shutdownMode == 0 ? "shutdown" : "restart") + " in " + Util.formatTime(seconds) + ".");
		counter = new Timer("ShutdownCounter", true);
		counter.scheduleAtFixedRate(new ShutdownCounter(), 0, 1000);
	}
	
	public void schedule(String time, int shutdownMode)
	{
		SchedulingPattern cronTime;
		try
		{
			cronTime = new SchedulingPattern(time);
		}
		catch(SchedulingPattern.InvalidPatternException e)
		{
			return;
		}
		int seconds = (int) (cronTime.next(System.currentTimeMillis()) / 1000 - System.currentTimeMillis() / 1000);
		schedule(seconds, shutdownMode);
	}
	
	public synchronized void cancel()
	{
		shutdownMode = -1;
		if(counter != null)
		{
			counter.cancel();
		}
		counter = null;
	}
	
	@Override
	public void run()
	{
		System.out.println("Shutting down LS/GS communication...");
		AuthServerCommunication.getInstance().shutdown();
		System.out.println("Shutting down scripts...");
		Scripts.getInstance().shutdown();
		System.out.println("Disconnecting players...");
		disconnectAllPlayers();
		System.out.println("Saving data...");
		saveData();
		NoblesController.getInstance().SaveNobleses();
		if(Config.OLY_ENABLED)
		{
			try
			{
				ParticipantPool.getInstance().FreePools();
				StadiumPool.getInstance().FreeStadiums();
				OlyController.getInstance().shutdown();
				System.out.println("Olympiad System: Oly cleaned and data saved!");
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		try
		{
			System.out.println("Shutting down thread pool...");
			ThreadPoolManager.getInstance().shutdown();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		System.out.println("Shutting down selector...");
		if(GameServer.getInstance() != null)
		{
			for(SelectorThread<GameClient> st : GameServer.getInstance().getSelectorThreads())
			{
				try
				{
					st.shutdown();
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		try
		{
			System.out.println("Shutting down database communication...");
			DatabaseFactory.getInstance().close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		System.out.println("Shutdown finished.");
	}
	
	private void saveData()
	{
		try
		{
			if(!SevenSigns.getInstance().isSealValidationPeriod())
			{
				SevenSignsFestival.getInstance().saveFestivalData(false);
				System.out.println("SevenSignsFestival: Data saved.");
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		try
		{
			SevenSigns.getInstance().saveSevenSignsData(0, true);
			System.out.println("SevenSigns: Data saved.");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		if(Config.ALLOW_WEDDING)
		{
			try
			{
				CoupleManager.getInstance().store();
				System.out.println("CoupleManager: Data saved.");
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		try
		{
			FishingChampionShipManager.getInstance().shutdown();
			System.out.println("FishingChampionShipManager: Data saved.");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		try
		{
			HeroController.getInstance().saveHeroes();
			System.out.println("Hero: Data saved.");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		try
		{
			Collection<Residence> residences = ResidenceHolder.getInstance().getResidences();
			for(Residence residence : residences)
			{
				residence.update();
			}
			System.out.println("Residences: Data saved.");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		if(Config.ALLOW_CURSED_WEAPONS)
		{
			try
			{
				CursedWeaponsManager.getInstance().saveData();
				System.out.println("CursedWeaponsManager: Data saved,");
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	private void disconnectAllPlayers()
	{
		for(Player player : GameObjectsStorage.getAllPlayersForIterate())
		{
			try
			{
				player.logout();
			}
			catch(Exception e)
			{
				System.out.println("Error while disconnecting: " + player + "!");
				e.printStackTrace();
			}
		}
	}
	
	private class ShutdownCounter extends TimerTask
	{
		@Override
		public void run()
		{
			block:
			switch(shutdownCounter)
			{
				case 60:
				case 120:
				case 180:
				case 240:
				case 300:
				case 600:
				case 900:
				case 1800:
				{
					switch(shutdownMode)
					{
						case 0:
						{
							Announcements.getInstance().announceByCustomMessage("THE_SERVER_WILL_BE_COMING_DOWN_IN_S1_MINUTES", new String[] {String.valueOf(shutdownCounter / 60)});
							break block;
						}
						case 2:
						{
							Announcements.getInstance().announceByCustomMessage("THE_SERVER_WILL_BE_COMING_RESTARTED_IN_S1_MINUTES", new String[] {String.valueOf(shutdownCounter / 60)});
						}
					}
					break;
				}
				case 5:
				case 10:
				case 20:
				case 30:
				{
					Announcements.getInstance().announceToAll(new SystemMessage(1).addNumber(shutdownCounter));
					break;
				}
				case 0:
				{
					switch(shutdownMode)
					{
						case 0:
						{
							Runtime.getRuntime().exit(0);
							break;
						}
						case 2:
						{
							Runtime.getRuntime().exit(2);
						}
					}
					cancel();
					return;
				}
			}
			shutdownCounter--;
		}
	}
}