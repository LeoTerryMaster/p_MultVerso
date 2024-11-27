package l2.gameserver.instancemanager;

import gnu.trove.TIntObjectHashMap;
import l2.gameserver.data.xml.holder.DoorHolder;
import l2.gameserver.data.xml.holder.InstantZoneHolder;
import l2.gameserver.data.xml.holder.ZoneHolder;
import l2.gameserver.model.entity.Reflection;
import l2.gameserver.utils.Location;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReflectionManager
{
	public static final Reflection DEFAULT = Reflection.createReflection(0);
	public static final Reflection GIRAN_HARBOR = Reflection.createReflection(-1);
	public static final Reflection JAIL = Reflection.createReflection(-2);
	private static final ReflectionManager _instance = new ReflectionManager();
	private final TIntObjectHashMap<Reflection> _reflections = new TIntObjectHashMap();
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private final Lock readLock = lock.readLock();
	private final Lock writeLock = lock.writeLock();
	
	private ReflectionManager()
	{
		add(DEFAULT);
		add(GIRAN_HARBOR);
		add(JAIL);
		DEFAULT.init(DoorHolder.getInstance().getDoors(), ZoneHolder.getInstance().getZones());
		GIRAN_HARBOR.fillSpawns(InstantZoneHolder.getInstance().getInstantZone(10).getSpawnsInfo());
		JAIL.setCoreLoc(new Location(-114648, -249384, -2984));
	}
	
	public static ReflectionManager getInstance()
	{
		return _instance;
	}
	
	public Reflection get(int id)
	{
		readLock.lock();
		try
		{
			Reflection reflection = _reflections.get(id);
			return reflection;
		}
		finally
		{
			readLock.unlock();
		}
	}
	
	public Reflection add(Reflection ref)
	{
		writeLock.lock();
		try
		{
			Reflection reflection = _reflections.put(ref.getId(), ref);
			return reflection;
		}
		finally
		{
			writeLock.unlock();
		}
	}
	
	public Reflection remove(Reflection ref)
	{
		writeLock.lock();
		try
		{
			Reflection reflection = _reflections.remove(ref.getId());
			return reflection;
		}
		finally
		{
			writeLock.unlock();
		}
	}
	
	public Reflection[] getAll()
	{
		readLock.lock();
		try
		{
			Reflection[] arrreflection = (Reflection[]) _reflections.getValues((Object[]) new Reflection[_reflections.size()]);
			return arrreflection;
		}
		finally
		{
			readLock.unlock();
		}
	}
	
	public int getCountByIzId(int izId)
	{
		readLock.lock();
		try
		{
			int i = 0;
			for(Reflection r : getAll())
			{
				if(r.getInstancedZoneId() != izId)
					continue;
				++i;
			}
			int n = i;
			return n;
		}
		finally
		{
			readLock.unlock();
		}
	}
	
	public int size()
	{
		return _reflections.size();
	}
}