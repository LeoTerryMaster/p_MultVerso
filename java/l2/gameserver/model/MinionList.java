package l2.gameserver.model;

import l2.gameserver.data.xml.holder.NpcHolder;
import l2.gameserver.idfactory.IdFactory;
import l2.gameserver.model.instances.MinionInstance;
import l2.gameserver.model.instances.MonsterInstance;
import l2.gameserver.templates.npc.MinionData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MinionList
{
	private final Set<MinionData> _minionData;
	private final Set<MinionInstance> _minions;
	private final Lock lock;
	private final MonsterInstance _master;
	
	public MinionList(MonsterInstance master)
	{
		_master = master;
		_minions = new HashSet<>();
		_minionData = new HashSet<>();
		_minionData.addAll(_master.getTemplate().getMinionData());
		lock = new ReentrantLock();
	}
	
	public boolean addMinion(MinionData m)
	{
		lock.lock();
		try
		{
			boolean bl = _minionData.add(m);
			return bl;
		}
		finally
		{
			lock.unlock();
		}
	}
	
	public boolean addMinion(MinionInstance m)
	{
		lock.lock();
		try
		{
			boolean bl = _minions.add(m);
			return bl;
		}
		finally
		{
			lock.unlock();
		}
	}
	
	public boolean hasAliveMinions()
	{
		lock.lock();
		try
		{
			for(MinionInstance m : _minions)
			{
				if(!m.isVisible() || m.isDead())
					continue;
				boolean bl = true;
				return bl;
			}
		}
		finally
		{
			lock.unlock();
		}
		return false;
	}
	
	public boolean hasMinions()
	{
		return _minionData.size() > 0;
	}
	
	public List<MinionInstance> getAliveMinions()
	{
		ArrayList<MinionInstance> result = new ArrayList<>(_minions.size());
		lock.lock();
		try
		{
			for(MinionInstance m : _minions)
			{
				if(!m.isVisible() || m.isDead())
					continue;
				result.add(m);
			}
		}
		finally
		{
			lock.unlock();
		}
		return result;
	}
	
	public void spawnMinions()
	{
		lock.lock();
		try
		{
			for(MinionData minion : _minionData)
			{
				int minionId = minion.getMinionId();
				int minionCount = minion.getAmount();
				for(MinionInstance m : _minions)
				{
					if(m.getNpcId() == minionId)
					{
						--minionCount;
					}
					if(!m.isDead() && m.isVisible())
						continue;
					m.refreshID();
					m.stopDecay();
					_master.spawnMinion(m);
				}
				for(int i = 0;i < minionCount;++i)
				{
					MinionInstance m = new MinionInstance(IdFactory.getInstance().getNextId(), NpcHolder.getInstance().getTemplate(minionId));
					m.setLeader(_master);
					_master.spawnMinion(m);
					_minions.add(m);
				}
			}
		}
		finally
		{
			lock.unlock();
		}
	}
	
	public void unspawnMinions()
	{
		lock.lock();
		try
		{
			for(MinionInstance m : _minions)
			{
				m.decayMe();
			}
		}
		finally
		{
			lock.unlock();
		}
	}
	
	public void deleteMinions()
	{
		lock.lock();
		try
		{
			for(MinionInstance m : _minions)
			{
				m.deleteMe();
			}
			_minions.clear();
		}
		finally
		{
			lock.unlock();
		}
	}
}