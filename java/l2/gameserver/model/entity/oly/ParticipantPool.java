package l2.gameserver.model.entity.oly;

import l2.gameserver.Config;
import l2.gameserver.model.GameObjectsStorage;
import l2.gameserver.model.Player;
import l2.gameserver.network.l2.s2c.L2GameServerPacket;
import l2.gameserver.utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

public class ParticipantPool
{
	private static final Logger _log = LoggerFactory.getLogger(ParticipantPool.class);
	private static ParticipantPool _instance;
	private Map<CompetitionType, ArrayList<EntryRec>> _pools;
	
	private ParticipantPool()
	{
	}
	
	public static final ParticipantPool getInstance()
	{
		if(_instance == null)
		{
			_instance = new ParticipantPool();
		}
		return _instance;
	}
	
	public void AllocatePools()
	{
		_pools = new HashMap<>();
		for(CompetitionType type : CompetitionType.values())
		{
			_pools.put(type, new ArrayList());
		}
		_log.info("OlyParticipantPool: Allocated " + _pools.size() + " particiant pools.");
	}
	
	public void FreePools()
	{
		if(_pools != null)
		{
			_pools.clear();
		}
		_log.info("OlyParticipantPool: pools cleared.");
	}
	
	public boolean isEnough(CompetitionType type, int cls_id)
	{
		switch(type)
		{
			case CLASS_FREE:
			{
				return _pools.get(type).size() >= Config.OLY_MIN_CF_START;
			}
			case TEAM_CLASS_FREE:
			{
				return _pools.get(type).size() >= Config.OLY_MIN_TB_START;
			}
			case CLASS_INDIVIDUAL:
			{
				int cnt = 0;
				for(EntryRec er : _pools.get(type))
				{
					if(er.cls_id != cls_id)
						continue;
					++cnt;
				}
				return cnt >= Config.OLY_MIN_CB_START;
			}
		}
		return false;
	}
	
	public int getNearestIndex(CompetitionType type, int idx, int cls_id)
	{
		ArrayList<EntryRec> pool = _pools.get(type);
		EntryRec base_rec = pool.get(idx);
		if(base_rec == null)
		{
			return -1;
		}
		int ndelta = Integer.MAX_VALUE;
		int nidx = Integer.MIN_VALUE;
		int delta;
		int i;
		EntryRec pr;
		for(i = 0;i < idx;++i)
		{
			pr = pool.get(i);
			if(pr == null || type == CompetitionType.CLASS_INDIVIDUAL && cls_id > 0 && cls_id != pr.cls_id || (delta = Math.abs(base_rec.average - pr.average)) >= ndelta)
				continue;
			nidx = i;
			ndelta = delta;
		}
		for(i = idx + 1;i < pool.size();++i)
		{
			pr = pool.get(i);
			if(pr == null || type == CompetitionType.CLASS_INDIVIDUAL && cls_id > 0 && cls_id != pr.cls_id || (delta = Math.abs(base_rec.average - pr.average)) >= ndelta)
				continue;
			nidx = i;
			ndelta = delta;
		}
		return nidx;
	}
	
	public void createEntry(CompetitionType type, Player[] players)
	{
		if(players == null || players.length == 0)
		{
			return;
		}
		ArrayList<EntryRec> pool = _pools.get(type);
		synchronized(pool)
		{
			_pools.get(type).add(new EntryRec(players));
		}
	}
	
	public Player[][] retrieveEntrys(CompetitionType type, int cls_id)
	{
		cleadInvalidEntrys(type);
		ArrayList<EntryRec> pool = _pools.get(type);
		Player[][] ret;
		ArrayList<EntryRec> arrayList = pool;
		synchronized(arrayList)
		{
			long oldest_time = Long.MIN_VALUE;
			int oldest_idx = -1;
			for(int i = 0;i < pool.size();++i)
			{
				EntryRec pr = pool.get(i);
				if(pr == null || type == CompetitionType.CLASS_INDIVIDUAL && cls_id > 0 && cls_id != pr.cls_id || pr.reg_time <= oldest_time)
					continue;
				oldest_idx = i;
				oldest_time = pr.reg_time;
			}
			if(oldest_idx < 0)
			{
				return null;
			}
			int pair_idx = getNearestIndex(type, oldest_idx, cls_id);
			if(pair_idx < 0)
			{
				return null;
			}
			ret = new Player[][] {Util.GetPlayersFromStoredIds(pool.remove(oldest_idx).sids), Util.GetPlayersFromStoredIds(pool.remove(pair_idx).sids)};
			pool.trimToSize();
		}
		return ret;
	}
	
	public boolean removeEntryByPlayer(CompetitionType type, Player player)
	{
		ArrayList<EntryRec> pool;
		long psid = player.getStoredId();
		ArrayList<EntryRec> arrayList = pool = _pools.get(type);
		synchronized(arrayList)
		{
			for(int i = 0;i < pool.size();++i)
			{
				EntryRec pr = pool.get(i);
				if(pr == null)
					continue;
				for(long sid : pr.sids)
				{
					if(sid != psid)
						continue;
					pool.remove(i);
					return true;
				}
			}
		}
		return false;
	}
	
	public CompetitionType getCompTypeOf(Player player)
	{
		long psid = player.getStoredId();
		for(Map.Entry<CompetitionType, ArrayList<EntryRec>> e : _pools.entrySet())
		{
			ArrayList<EntryRec> pool = e.getValue();
			for(int i = 0;i < pool.size();++i)
			{
				EntryRec pr = pool.get(i);
				if(pr == null)
					continue;
				for(long sid : pr.sids)
				{
					if(sid != psid)
						continue;
					return e.getKey();
				}
			}
		}
		return null;
	}
	
	public boolean isRegistred(Player player)
	{
		if(!OlyController.getInstance().isRegAllowed())
		{
			return false;
		}
		for(CompetitionType type : _pools.keySet())
		{
			if(!isRegistred(type, player))
				continue;
			return true;
		}
		return false;
	}
	
	public boolean isHWIDRegistred(String hwid)
	{
		if(!OlyController.getInstance().isRegAllowed())
		{
			return false;
		}
		LinkedList<EntryRec> recs = new LinkedList<>();
		for(Map.Entry<CompetitionType, ArrayList<EntryRec>> e : _pools.entrySet())
		{
			ArrayList<EntryRec> entryRecs;
			ArrayList<EntryRec> arrayList = entryRecs = e.getValue();
			synchronized(arrayList)
			{
				recs.addAll(entryRecs);
			}
		}
		for(EntryRec er : recs)
		{
			for(int sidx = 0;sidx < er.sids.length;++sidx)
			{
				Player player = GameObjectsStorage.getAsPlayer(er.sids[sidx]);
				if(player == null || player.getNetConnection() == null || player.getNetConnection().getHwid() == null || !hwid.equalsIgnoreCase(player.getNetConnection().getHwid()))
					continue;
				return true;
			}
		}
		return false;
	}
	
	public boolean isIPRegistred(String ip)
	{
		if(!OlyController.getInstance().isRegAllowed())
		{
			return false;
		}
		LinkedList<EntryRec> recs = new LinkedList<>();
		for(Map.Entry<CompetitionType, ArrayList<EntryRec>> e : _pools.entrySet())
		{
			ArrayList<EntryRec> entryRecs;
			ArrayList<EntryRec> arrayList = entryRecs = e.getValue();
			synchronized(arrayList)
			{
				recs.addAll(entryRecs);
			}
		}
		for(EntryRec er : recs)
		{
			for(int sidx = 0;sidx < er.sids.length;++sidx)
			{
				Player player = GameObjectsStorage.getAsPlayer(er.sids[sidx]);
				if(player == null || player.getNetConnection() == null || player.getNetConnection().getIpAddr() == null || player.getNetConnection().getIpAddr() == "?.?.?.?" || !ip.equalsIgnoreCase(player.getNetConnection().getIpAddr()))
					continue;
				return true;
			}
		}
		return false;
	}
	
	public boolean isRegistred(CompetitionType type, Player player)
	{
		if(!OlyController.getInstance().isRegAllowed())
		{
			return false;
		}
		long psid = player.getStoredId();
		ArrayList<EntryRec> pool = _pools.get(type);
		for(EntryRec pr : pool)
		{
			if(pr == null)
				continue;
			for(long sid : pr.sids)
			{
				if(sid != psid)
					continue;
				return true;
			}
		}
		return false;
	}
	
	public void broadcastToEntrys(CompetitionType type, L2GameServerPacket gsp, int cls_id)
	{
		ArrayList<EntryRec> pool = _pools.get(type);
		for(EntryRec pr : pool)
		{
			if(pr == null)
				continue;
			for(long sid : pr.sids)
			{
				Player player = GameObjectsStorage.getAsPlayer(sid);
				if(player == null || cls_id > 0 && player.getClassId().getId() != cls_id)
					continue;
				player.sendPacket(gsp);
			}
		}
	}
	
	private void cleadInvalidEntrys(CompetitionType type)
	{
		ArrayList<EntryRec> pool;
		ArrayList<EntryRec> arrayList = pool = _pools.get(type);
		synchronized(arrayList)
		{
			ArrayList<Integer> invalid_entrys = new ArrayList<>();
			for(int i = 0;i < pool.size();++i)
			{
				if(isValidEntry(pool.get(i)))
					continue;
				invalid_entrys.add(i);
			}
			Iterator i = invalid_entrys.iterator();
			while(i.hasNext())
			{
				int i2 = (Integer) i.next();
				pool.remove(i2);
			}
		}
	}
	
	public void onLogout(Player player)
	{
		if(!OlyController.getInstance().isRegAllowed())
		{
			return;
		}
		CompetitionType ctype = getInstance().getCompTypeOf(player);
		if(ctype != null)
		{
			removeEntryByPlayer(ctype, player);
		}
	}
	
	private boolean isValidEntry(EntryRec pr)
	{
		return true;
	}
	
	public int getParticipantCount()
	{
		int result = 0;
		for(Map.Entry<CompetitionType, ArrayList<EntryRec>> e : _pools.entrySet())
		{
			ArrayList<EntryRec> pool = e.getValue();
			for(int i = 0;i < pool.size();++i)
			{
				EntryRec pr = pool.get(i);
				if(pr == null)
					continue;
				result += pr.sids.length;
			}
		}
		return result;
	}
	
	private class EntryRec
	{
		long[] sids;
		int average;
		long reg_time;
		int cls_id;
		
		public EntryRec(Player[] players)
		{
			sids = new long[players.length];
			cls_id = players[0].getClassId().getId();
			int sum = 0;
			for(int i = 0;i < players.length;++i)
			{
				sids[i] = players[i].getStoredId();
				sum += Math.max(0, NoblesController.getInstance().getPointsOf(players[i].getObjectId()));
				OlyController.getInstance().incPartCount();
			}
			average = sum / players.length;
			reg_time = System.currentTimeMillis();
		}
	}
}