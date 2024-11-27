package l2.gameserver.utils;

import l2.gameserver.instancemanager.ServerVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

public class GameStats
{
	private static final Logger _log = LoggerFactory.getLogger(GameStats.class);
	private static final AtomicLong _updatePlayerBase = new AtomicLong(0);
	private static final AtomicLong _playerEnterGameCounter = new AtomicLong(0);
	private static final AtomicLong _taxSum = new AtomicLong(0);
	private static final AtomicLong _rouletteSum;
	private static final AtomicLong _adenaSum;
	private static long _taxLastUpdate;
	private static long _rouletteLastUpdate;
	
	static
	{
		_rouletteSum = new AtomicLong(0);
		_adenaSum = new AtomicLong(0);
		_taxSum.set(ServerVariables.getLong("taxsum", 0));
		_rouletteSum.set(ServerVariables.getLong("rouletteSum", 0));
	}
	
	public static void increaseUpdatePlayerBase()
	{
		_updatePlayerBase.incrementAndGet();
	}
	
	public static long getUpdatePlayerBase()
	{
		return _updatePlayerBase.get();
	}
	
	public static void incrementPlayerEnterGame()
	{
		_playerEnterGameCounter.incrementAndGet();
	}
	
	public static long getPlayerEnterGame()
	{
		return _playerEnterGameCounter.get();
	}
	
	public static void addTax(long sum)
	{
		long taxSum = _taxSum.addAndGet(sum);
		if(System.currentTimeMillis() - _taxLastUpdate < 10000)
		{
			return;
		}
		_taxLastUpdate = System.currentTimeMillis();
		ServerVariables.set("taxsum", taxSum);
	}
	
	public static void addRoulette(long sum)
	{
		long rouletteSum = _rouletteSum.addAndGet(sum);
		if(System.currentTimeMillis() - _rouletteLastUpdate < 10000)
		{
			return;
		}
		_rouletteLastUpdate = System.currentTimeMillis();
		ServerVariables.set("rouletteSum", rouletteSum);
	}
	
	public static long getTaxSum()
	{
		return _taxSum.get();
	}
	
	public static long getRouletteSum()
	{
		return _rouletteSum.get();
	}
	
	public static void addAdena(long sum)
	{
		_adenaSum.addAndGet(sum);
	}
	
	public static long getAdena()
	{
		return _adenaSum.get();
	}
}