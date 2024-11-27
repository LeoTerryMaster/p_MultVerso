package l2.gameserver.taskmanager;

import l2.commons.lang.reference.HardReference;
import l2.commons.threading.RunnableImpl;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.model.Spawner;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.utils.Util;

import java.util.ArrayList;

public class SpawnTaskManager
{
	private static SpawnTaskManager _instance;
	private final Object spawnTasks_lock = new Object();
	private SpawnTask[] _spawnTasks = new SpawnTask[500];
	private int _spawnTasksSize;
	
	public SpawnTaskManager()
	{
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new SpawnScheduler(), 2000, 2000);
	}
	
	public static SpawnTaskManager getInstance()
	{
		if(_instance == null)
		{
			_instance = new SpawnTaskManager();
		}
		return _instance;
	}
	
	public void addSpawnTask(NpcInstance actor, long interval)
	{
		removeObject(actor);
		addObject(new SpawnTask(actor, System.currentTimeMillis() + interval));
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder("============= SpawnTask Manager Report ============\n\r");
		sb.append("Tasks count: ").append(_spawnTasksSize).append("\n\r");
		sb.append("Tasks dump:\n\r");
		long current = System.currentTimeMillis();
		for(SpawnTask container : _spawnTasks)
		{
			if(container == null)
				continue;
			sb.append("Class/Name: ").append(container.getClass().getSimpleName()).append('/').append(container.getActor());
			sb.append(" spawn timer: ").append(Util.formatTime((int) ((container.endtime - current) / 1000))).append("\n\r");
		}
		return sb.toString();
	}
	
	private void addObject(SpawnTask decay)
	{
		Object object = spawnTasks_lock;
		synchronized(object)
		{
			if(_spawnTasksSize >= _spawnTasks.length)
			{
				SpawnTask[] temp = new SpawnTask[_spawnTasks.length * 2];
				for(int i = 0;i < _spawnTasksSize;++i)
				{
					temp[i] = _spawnTasks[i];
				}
				_spawnTasks = temp;
			}
			_spawnTasks[_spawnTasksSize] = decay;
			++_spawnTasksSize;
		}
	}
	
	public void removeObject(NpcInstance actor)
	{
		Object object = spawnTasks_lock;
		synchronized(object)
		{
			if(_spawnTasksSize > 1)
			{
				int k = -1;
				for(int i = 0;i < _spawnTasksSize;++i)
				{
					if(_spawnTasks[i].getActor() != actor)
						continue;
					k = i;
				}
				if(k > -1)
				{
					_spawnTasks[k] = _spawnTasks[_spawnTasksSize - 1];
					_spawnTasks[_spawnTasksSize - 1] = null;
					--_spawnTasksSize;
				}
			}
			else if(_spawnTasksSize == 1 && _spawnTasks[0].getActor() == actor)
			{
				_spawnTasks[0] = null;
				_spawnTasksSize = 0;
			}
		}
	}
	
	private class SpawnTask
	{
		private final HardReference<NpcInstance> _npcRef;
		public long endtime;
		
		SpawnTask(NpcInstance cha, long delay)
		{
			_npcRef = cha.getRef();
			endtime = delay;
		}
		
		public NpcInstance getActor()
		{
			return _npcRef.get();
		}
	}
	
	public class SpawnScheduler extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			if(_spawnTasksSize > 0)
			{
				try
				{
					ArrayList<NpcInstance> works = new ArrayList<>();
					synchronized(spawnTasks_lock)
					{
						long current = System.currentTimeMillis();
						int size = _spawnTasksSize;
						for(int i = size - 1;i >= 0;--i)
						{
							try
							{
								SpawnTask container = _spawnTasks[i];
								if(container != null && container.endtime > 0 && current > container.endtime)
								{
									NpcInstance actor = container.getActor();
									if(actor != null && actor.getSpawn() != null)
									{
										works.add(actor);
									}
									container.endtime = -1;
								}
								if(container != null && container.getActor() != null && container.endtime >= 0)
									continue;
								if(i == _spawnTasksSize - 1)
								{
									_spawnTasks[i] = null;
								}
								else
								{
									_spawnTasks[i] = _spawnTasks[_spawnTasksSize - 1];
									_spawnTasks[_spawnTasksSize - 1] = null;
								}
								if(_spawnTasksSize <= 0)
									continue;
								_spawnTasksSize--;
								continue;
							}
							catch(Exception e)
							{
								_log.error("", e);
							}
						}
					}
					for(NpcInstance work : works)
					{
						Spawner spawn = work.getSpawn();
						if(spawn == null)
							continue;
						spawn.decreaseScheduledCount();
						if(!spawn.isDoRespawn())
							continue;
						spawn.respawnNpc(work);
					}
				}
				catch(Exception e)
				{
					_log.error("", e);
				}
			}
		}
	}
}