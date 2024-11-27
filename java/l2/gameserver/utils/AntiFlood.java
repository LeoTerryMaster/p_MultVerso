package l2.gameserver.utils;

import gnu.trove.TIntLongHashMap;
import gnu.trove.TIntLongIterator;

public class AntiFlood
{
	private final TIntLongHashMap _recentReceivers = new TIntLongHashMap();
	private long _lastSent;
	private String _lastText = "";
	private long _lastHeroTime;
	private long _lastTradeTime;
	private long _lastShoutTime;
	private long _lastMailTime;
	
	public boolean canTrade(String text)
	{
		long currentMillis = System.currentTimeMillis();
		if(currentMillis - _lastTradeTime < 5000)
		{
			return false;
		}
		_lastTradeTime = currentMillis;
		return true;
	}
	
	public boolean canShout(String text)
	{
		long currentMillis = System.currentTimeMillis();
		if(currentMillis - _lastShoutTime < 5000)
		{
			return false;
		}
		_lastShoutTime = currentMillis;
		return true;
	}
	
	public boolean canHero(String text)
	{
		long currentMillis = System.currentTimeMillis();
		if(currentMillis - _lastHeroTime < 10000)
		{
			return false;
		}
		_lastHeroTime = currentMillis;
		return true;
	}
	
	public boolean canMail()
	{
		long currentMillis = System.currentTimeMillis();
		if(currentMillis - _lastMailTime < 10000)
		{
			return false;
		}
		_lastMailTime = currentMillis;
		return true;
	}
	
	public boolean canTell(int charId, String text)
	{
		long lastSent;
		long currentMillis = System.currentTimeMillis();
		TIntLongIterator itr = _recentReceivers.iterator();
		int recent = 0;
		while(itr.hasNext())
		{
			itr.advance();
			lastSent = itr.value();
			if(currentMillis - lastSent < (text.equalsIgnoreCase(_lastText) ? 600000 : 60000))
			{
				++recent;
				continue;
			}
			itr.remove();
		}
		lastSent = _recentReceivers.put(charId, currentMillis);
		long delay = 333;
		if(recent > 3)
		{
			lastSent = _lastSent;
			delay = (long) (recent - 3) * 3333;
		}
		_lastText = text;
		_lastSent = currentMillis;
		return currentMillis - lastSent > delay;
	}
}