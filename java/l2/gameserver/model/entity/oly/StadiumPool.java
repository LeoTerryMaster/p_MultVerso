package l2.gameserver.model.entity.oly;

import l2.gameserver.utils.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;

public class StadiumPool
{
	public static final int REFLECTION_COUNT = 22;
	private static final Logger _log = LoggerFactory.getLogger(StadiumPool.class);
	private static final StadiumTemplate[] OLY_STADIA_TEMPLATES;
	private static StadiumPool _instance;
	
	static
	{
		OLY_STADIA_TEMPLATES = new StadiumTemplate[] {new StadiumTemplate(147, new Location(-20814, -21189, -3030))};
	}
	
	private final ArrayDeque<Stadium> _freeStadiums = new ArrayDeque();
	private final Stadium[] _allStadiums = new Stadium[22];
	
	private StadiumPool()
	{
	}
	
	public static final StadiumPool getInstance()
	{
		if(_instance == null)
		{
			_instance = new StadiumPool();
		}
		return _instance;
	}
	
	public Stadium[] getAllStadiums()
	{
		return _allStadiums;
	}
	
	public void AllocateStadiums()
	{
		int cnt = 0;
		for(int i = 0;i < 22 / OLY_STADIA_TEMPLATES.length;++i)
		{
			for(StadiumTemplate st : OLY_STADIA_TEMPLATES)
			{
				Stadium stadium;
				_allStadiums[cnt] = stadium = new Stadium(cnt, st.zid, st.oloc);
				_freeStadiums.addLast(stadium);
				++cnt;
			}
		}
		_log.info("OlyStadiumPool: allocated " + cnt + " stadiums.");
	}
	
	public void FreeStadiums()
	{
		for(int i = 0;i < _allStadiums.length;++i)
		{
			if(_allStadiums[i] == null)
				continue;
			_allStadiums[i].collapse();
			_allStadiums[i] = null;
		}
		_freeStadiums.clear();
		_log.info("OlyStadiumPool: stadiums cleared.");
	}
	
	public boolean isStadiumAvailable()
	{
		return _freeStadiums.size() > 0;
	}
	
	public synchronized Stadium pollStadium()
	{
		Stadium stadium = _freeStadiums.pollFirst();
		if(!stadium.isFree())
		{
			_log.warn("Poll used stadium");
			Thread.dumpStack();
			stadium = _freeStadiums.pollFirst();
		}
		stadium.setFree(false);
		return stadium;
	}
	
	public synchronized void putStadium(Stadium stadium)
	{
		if(stadium.isFree())
		{
			_log.warn("Put free stadium");
			Thread.dumpStack();
		}
		stadium.clear();
		stadium.setFree(true);
		_freeStadiums.addFirst(stadium);
	}
	
	public Stadium getStadium(int id)
	{
		return _allStadiums[id];
	}
	
	private static class StadiumTemplate
	{
		public Location[] plocs;
		public Location[] blocs;
		public Location oloc;
		public int zid;
		
		public StadiumTemplate(int _zid, Location ol)
		{
			oloc = ol;
			zid = _zid;
		}
	}
}