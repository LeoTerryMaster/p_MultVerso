package l2.gameserver;

import l2.commons.listener.Listener;
import l2.commons.listener.ListenerList;
import l2.commons.threading.RunnableImpl;
import l2.gameserver.listener.GameListener;
import l2.gameserver.listener.game.OnDayNightChangeListener;
import l2.gameserver.listener.game.OnStartListener;
import l2.gameserver.model.GameObjectsStorage;
import l2.gameserver.model.Player;
import l2.gameserver.network.l2.s2c.ClientSetTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;

public class GameTimeController
{
	public static final int TICKS_PER_SECOND = 10;
	public static final int MILLIS_IN_TICK = 100;
	private static final Logger _log = LoggerFactory.getLogger(GameTimeController.class);
	private static final GameTimeController _instance = new GameTimeController();
	private final long _gameStartTime;
	private final GameTimeListenerList listenerEngine;
	private final Runnable _dayChangeNotify;
	
	private GameTimeController()
	{
		listenerEngine = new GameTimeListenerList();
		_dayChangeNotify = new CheckSunState();
		_gameStartTime = getDayStartTime();
		GameServer.getInstance().addListener(new OnStartListenerImpl());
		StringBuilder msg = new StringBuilder();
		msg.append("GameTimeController: initialized.").append(" ");
		msg.append("Current time is ");
		msg.append(getGameHour()).append(":");
		if(getGameMin() < 10)
		{
			msg.append("0");
		}
		msg.append(getGameMin());
		msg.append(" in the ");
		if(isNowNight())
		{
			msg.append("night");
		}
		else
		{
			msg.append("day");
		}
		msg.append(".");
		_log.info(msg.toString());
		long nightStart = 0;
		while(_gameStartTime + nightStart < System.currentTimeMillis())
		{
			nightStart += 14400000;
		}
		long dayStart = 3600000;
		while(_gameStartTime + dayStart < System.currentTimeMillis())
		{
			dayStart += 14400000;
		}
		nightStart -= System.currentTimeMillis() - _gameStartTime;
		dayStart -= System.currentTimeMillis() - _gameStartTime;
		ThreadPoolManager.getInstance().scheduleAtFixedRate(_dayChangeNotify, nightStart, 14400000);
		ThreadPoolManager.getInstance().scheduleAtFixedRate(_dayChangeNotify, dayStart, 14400000);
	}
	
	public static final GameTimeController getInstance()
	{
		return _instance;
	}
	
	private long getDayStartTime()
	{
		Calendar dayStart = Calendar.getInstance();
		int HOUR_OF_DAY = dayStart.get(11);
		dayStart.add(11, (-HOUR_OF_DAY + 1) % 4);
		dayStart.set(12, 0);
		dayStart.set(13, 0);
		dayStart.set(14, 0);
		return dayStart.getTimeInMillis();
	}
	
	public boolean isNowNight()
	{
		return getGameHour() < 6;
	}
	
	public int getGameTime()
	{
		return getGameTicks() / 100;
	}
	
	public int getGameHour()
	{
		return getGameTime() / 60 % 24;
	}
	
	public int getGameMin()
	{
		return getGameTime() % 60;
	}
	
	public int getGameTicks()
	{
		return (int) ((System.currentTimeMillis() - _gameStartTime) / 100);
	}
	
	public GameTimeListenerList getListenerEngine()
	{
		return listenerEngine;
	}
	
	public <T extends GameListener> boolean addListener(T listener)
	{
		return listenerEngine.add(listener);
	}
	
	public <T extends GameListener> boolean removeListener(T listener)
	{
		return listenerEngine.remove(listener);
	}
	
	protected class GameTimeListenerList extends ListenerList<GameServer>
	{
		public void onDay()
		{
			for(Listener listener : getListeners())
			{
				try
				{
					if(!OnDayNightChangeListener.class.isInstance(listener))
						continue;
					((OnDayNightChangeListener) listener).onDay();
				}
				catch(Exception e)
				{
					_log.warn("Exception during day change", e);
				}
			}
		}
		
		public void onNight()
		{
			for(Listener listener : getListeners())
			{
				try
				{
					if(!OnDayNightChangeListener.class.isInstance(listener))
						continue;
					((OnDayNightChangeListener) listener).onNight();
				}
				catch(Exception e)
				{
					_log.warn("Exception during night change", e);
				}
			}
		}
	}
	
	public class CheckSunState extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			if(isNowNight())
			{
				getInstance().getListenerEngine().onNight();
			}
			else
			{
				getInstance().getListenerEngine().onDay();
			}
			ThreadPoolManager.getInstance().execute(new RunnableImpl()
			{
				
				@Override
				public void runImpl() throws Exception
				{
					for(Player player : GameObjectsStorage.getAllPlayersForIterate())
					{
						player.checkDayNightMessages();
						player.sendPacket(new ClientSetTime());
					}
				}
			});
		}
	}
	
	private class OnStartListenerImpl implements OnStartListener
	{
		@Override
		public void onStart()
		{
			ThreadPoolManager.getInstance().execute(_dayChangeNotify);
		}
	}
}