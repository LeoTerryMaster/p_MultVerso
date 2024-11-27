package l2.gameserver.model;

import l2.commons.text.StrTable;
import l2.gameserver.Config;
import l2.gameserver.model.instances.MonsterInstance;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.instances.PetInstance;
import l2.gameserver.model.items.ItemInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class GameObjectsStorage
{
	private static final Logger _log = LoggerFactory.getLogger(GameObjectsStorage.class);
	private static final int STORAGE_PLAYERS = 0;
	private static final int STORAGE_SUMMONS = 1;
	private static final int STORAGE_NPCS = 2;
	private static final int STORAGE_OTHER = 30;
	private static final int STORAGE_NONE = 31;
	private static final GameObjectArray[] storages = new GameObjectArray[31];
	private static long offline_refresh;
	private static int offline_count;
	
	static
	{
		storages[0] = new GameObjectArray("PLAYERS", Config.MAXIMUM_ONLINE_USERS, 1);
		storages[1] = new GameObjectArray("SUMMONS", Config.MAXIMUM_ONLINE_USERS, 1);
		storages[2] = new GameObjectArray("NPCS", 60000 * Config.RATE_MOB_SPAWN, 5000);
		storages[30] = new GameObjectArray("OTHER", 2000, 1000);
		offline_refresh = 0;
		offline_count = 0;
	}
	
	private static GameObjectArray<Player> getStoragePlayers()
	{
		return storages[0];
	}
	
	private static GameObjectArray<Playable> getStorageSummons()
	{
		return storages[1];
	}
	
	private static GameObjectArray<NpcInstance> getStorageNpcs()
	{
		return storages[2];
	}
	
	private static int selectStorageID(GameObject o)
	{
		if(o.isNpc())
		{
			return 2;
		}
		if(o.isPlayable())
		{
			return o.isPlayer() ? 0 : 1;
		}
		return 30;
	}
	
	public static GameObject get(long storedId)
	{
		int STORAGE_ID;
		if(storedId == 0 || (STORAGE_ID = getStorageID(storedId)) == 31)
		{
			return null;
		}
		GameObject result = storages[STORAGE_ID].get(getStoredIndex(storedId));
		return result != null && result.getObjectId() == getStoredObjectId(storedId) ? result : null;
	}
	
	public static GameObject get(Long storedId)
	{
		int STORAGE_ID;
		if(storedId == null || storedId == 0 || (STORAGE_ID = getStorageID(storedId)) == 31)
		{
			return null;
		}
		GameObject result = storages[STORAGE_ID].get(getStoredIndex(storedId));
		return result != null && result.getObjectId() == getStoredObjectId(storedId) ? result : null;
	}
	
	public static boolean isStored(long storedId)
	{
		int STORAGE_ID;
		if(storedId == 0 || (STORAGE_ID = getStorageID(storedId)) == 31)
		{
			return false;
		}
		GameObject o = storages[STORAGE_ID].get(getStoredIndex(storedId));
		return o != null && o.getObjectId() == getStoredObjectId(storedId);
	}
	
	public static NpcInstance getAsNpc(long storedId)
	{
		return (NpcInstance) get(storedId);
	}
	
	public static NpcInstance getAsNpc(Long storedId)
	{
		return (NpcInstance) get(storedId);
	}
	
	public static Player getAsPlayer(long storedId)
	{
		return (Player) get(storedId);
	}
	
	public static Playable getAsPlayable(long storedId)
	{
		return (Playable) get(storedId);
	}
	
	public static Creature getAsCharacter(long storedId)
	{
		return (Creature) get(storedId);
	}
	
	public static MonsterInstance getAsMonster(long storedId)
	{
		return (MonsterInstance) get(storedId);
	}
	
	public static PetInstance getAsPet(long storedId)
	{
		return (PetInstance) get(storedId);
	}
	
	public static ItemInstance getAsItem(long storedId)
	{
		return (ItemInstance) get(storedId);
	}
	
	public static boolean contains(long storedId)
	{
		return get(storedId) != null;
	}
	
	public static Player getPlayer(String name)
	{
		return getStoragePlayers().findByName(name);
	}
	
	public static Player getPlayer(int objId)
	{
		return getStoragePlayers().findByObjectId(objId);
	}
	
	public static List<Player> getAllPlayers()
	{
		return getStoragePlayers().getAll();
	}
	
	public static Iterable<Player> getAllPlayersForIterate()
	{
		return getStoragePlayers();
	}
	
	public static int getAllPlayersCount()
	{
		return getStoragePlayers().getRealSize();
	}
	
	public static int getAllObjectsCount()
	{
		int result = 0;
		for(GameObjectArray storage : storages)
		{
			if(storage == null)
				continue;
			result += storage.getRealSize();
		}
		return result;
	}
	
	public static List<GameObject> getAllObjects()
	{
		ArrayList<GameObject> result = new ArrayList<>(getAllObjectsCount());
		for(GameObjectArray storage : storages)
		{
			if(storage == null)
				continue;
			storage.getAll(result);
		}
		return result;
	}
	
	public static GameObject findObject(int objId)
	{
		for(GameObjectArray storage : storages)
		{
			if(storage == null)
				continue;
			Object e = storage.findByObjectId(objId);
			GameObject result = (GameObject) e;
			if(e == null)
				continue;
			return result;
		}
		return null;
	}
	
	public static int getAllOfflineCount()
	{
		if(!Config.SERVICES_OFFLINE_TRADE_ALLOW)
		{
			return 0;
		}
		long now = System.currentTimeMillis();
		if(now > offline_refresh)
		{
			offline_refresh = now + 10000;
			offline_count = 0;
			for(Player player : getStoragePlayers())
			{
				if(!player.isInOfflineMode())
					continue;
				++offline_count;
			}
		}
		return offline_count;
	}
	
	public static List<NpcInstance> getAllNpcs()
	{
		return getStorageNpcs().getAll();
	}
	
	public static Iterable<NpcInstance> getAllNpcsForIterate()
	{
		return getStorageNpcs();
	}
	
	public static NpcInstance getByNpcId(int npc_id)
	{
		NpcInstance result = null;
		for(NpcInstance temp : getStorageNpcs())
		{
			if(npc_id != temp.getNpcId())
				continue;
			if(!temp.isDead())
			{
				return temp;
			}
			result = temp;
		}
		return result;
	}
	
	public static List<NpcInstance> getAllByNpcId(int npc_id, boolean justAlive)
	{
		ArrayList<NpcInstance> result = new ArrayList<>();
		for(NpcInstance temp : getStorageNpcs())
		{
			if(temp.getTemplate() == null || npc_id != temp.getTemplate().getNpcId() || justAlive && temp.isDead())
				continue;
			result.add(temp);
		}
		return result;
	}
	
	public static List<NpcInstance> getAllByNpcId(int[] npc_ids, boolean justAlive)
	{
		ArrayList<NpcInstance> result = new ArrayList<>();
		for(NpcInstance temp : getStorageNpcs())
		{
			if(justAlive && temp.isDead())
				continue;
			for(int npc_id : npc_ids)
			{
				if(npc_id != temp.getNpcId())
					continue;
				result.add(temp);
			}
		}
		return result;
	}
	
	public static NpcInstance getNpc(String s)
	{
		List<NpcInstance> npcs = getStorageNpcs().findAllByName(s);
		if(npcs.size() == 0)
		{
			return null;
		}
		for(NpcInstance temp : npcs)
		{
			if(temp.isDead())
				continue;
			return temp;
		}
		if(npcs.size() > 0)
		{
			return npcs.remove(npcs.size() - 1);
		}
		return null;
	}
	
	public static NpcInstance getNpc(int objId)
	{
		return getStorageNpcs().findByObjectId(objId);
	}
	
	public static long put(GameObject o)
	{
		int STORAGE_ID = selectStorageID(o);
		return (long) o.getObjectId() & 0xFFFFFFFFL | ((long) STORAGE_ID & 31) << 32 | ((long) storages[STORAGE_ID].add(o) & 0xFFFFFFFFL) << 37;
	}
	
	public static long putDummy(GameObject o)
	{
		return objIdNoStore(o.getObjectId());
	}
	
	public static long objIdNoStore(int objId)
	{
		return (long) objId & 0xFFFFFFFFL | 133143986176L;
	}
	
	public static long refreshId(Creature o)
	{
		return (long) o.getObjectId() & 0xFFFFFFFFL | o.getStoredId() >> 32 << 32;
	}
	
	public static GameObject remove(long storedId)
	{
		int STORAGE_ID = getStorageID(storedId);
		return STORAGE_ID == 31 ? null : storages[STORAGE_ID].remove(getStoredIndex(storedId), getStoredObjectId(storedId));
	}
	
	private static int getStorageID(long storedId)
	{
		return (int) (storedId >> 32) & 31;
	}
	
	private static int getStoredIndex(long storedId)
	{
		return (int) (storedId >> 37);
	}
	
	public static int getStoredObjectId(long storedId)
	{
		return (int) storedId;
	}
	
	public static StrTable getStats()
	{
		StrTable table = new StrTable("L2 Objects Storage Stats");
		for(int i = 0;i < storages.length;++i)
		{
			GameObjectArray storage = storages[i];
			if(storage == null)
				continue;
			GameObjectArray gameObjectArray = storage;
			synchronized(gameObjectArray)
			{
				table.set(i, "Name", storage.name);
				table.set(i, "Size / Real", "" + storage.size() + " / " + storage.getRealSize());
				table.set(i, "Capacity / init", "" + storage.capacity() + " / " + storage.initCapacity);
				continue;
			}
		}
		return table;
	}
}