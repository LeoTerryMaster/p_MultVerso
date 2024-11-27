package l2.gameserver.model;

import l2.commons.collections.LazyArrayList;
import l2.gameserver.Config;
import l2.gameserver.model.entity.Reflection;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.network.l2.s2c.L2GameServerPacket;
import l2.gameserver.utils.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

public class World
{
	public static final int MAP_MIN_X = Config.GEO_X_FIRST - 20 << 15;
	public static final int MAP_MAX_X = (Config.GEO_X_LAST - 20 + 1 << 15) - 1;
	public static final int MAP_MIN_Y = Config.GEO_Y_FIRST - 18 << 15;
	public static final int MAP_MAX_Y = (Config.GEO_Y_LAST - 18 + 1 << 15) - 1;
	public static final int MAP_MIN_Z = Config.MAP_MIN_Z;
	public static final int MAP_MAX_Z = Config.MAP_MAX_Z;
	public static final int WORLD_SIZE_X = Config.GEO_X_LAST - Config.GEO_X_FIRST + 1;
	public static final int WORLD_SIZE_Y = Config.GEO_Y_LAST - Config.GEO_Y_FIRST + 1;
	public static final int SHIFT_BY = Config.SHIFT_BY;
	public static final int SHIFT_BY_Z = Config.SHIFT_BY_Z;
	public static final int OFFSET_X = Math.abs(MAP_MIN_X >> SHIFT_BY);
	public static final int OFFSET_Y = Math.abs(MAP_MIN_Y >> SHIFT_BY);
	public static final int OFFSET_Z = Math.abs(MAP_MIN_Z >> SHIFT_BY_Z);
	private static final Logger _log = LoggerFactory.getLogger(World.class);
	private static final int REGIONS_X = (MAP_MAX_X >> SHIFT_BY) + OFFSET_X;
	private static final int REGIONS_Y = (MAP_MAX_Y >> SHIFT_BY) + OFFSET_Y;
	private static final int REGIONS_Z = (MAP_MAX_Z >> SHIFT_BY_Z) + OFFSET_Z;
	private static final WorldRegion[][][] _worldRegions = new WorldRegion[REGIONS_X + 1][REGIONS_Y + 1][REGIONS_Z + 1];
	
	public static void init()
	{
		_log.info("World Build: Creating regions: [" + (REGIONS_X + 1) + "][" + (REGIONS_Y + 1) + "][" + (REGIONS_Z + 1) + "].");
	}
	
	private static WorldRegion[][][] getRegions()
	{
		return _worldRegions;
	}
	
	private static int validX(int x)
	{
		if(x < 0)
		{
			x = 0;
		}
		else if(x > REGIONS_X)
		{
			x = REGIONS_X;
		}
		return x;
	}
	
	private static int validY(int y)
	{
		if(y < 0)
		{
			y = 0;
		}
		else if(y > REGIONS_Y)
		{
			y = REGIONS_Y;
		}
		return y;
	}
	
	private static int validZ(int z)
	{
		if(z < 0)
		{
			z = 0;
		}
		else if(z > REGIONS_Z)
		{
			z = REGIONS_Z;
		}
		return z;
	}
	
	public static int validCoordX(int x)
	{
		if(x < MAP_MIN_X)
		{
			x = MAP_MIN_X + 1;
		}
		else if(x > MAP_MAX_X)
		{
			x = MAP_MAX_X - 1;
		}
		return x;
	}
	
	public static int validCoordY(int y)
	{
		if(y < MAP_MIN_Y)
		{
			y = MAP_MIN_Y + 1;
		}
		else if(y > MAP_MAX_Y)
		{
			y = MAP_MAX_Y - 1;
		}
		return y;
	}
	
	public static int validCoordZ(int z)
	{
		if(z < MAP_MIN_Z)
		{
			z = MAP_MIN_Z + 1;
		}
		else if(z > MAP_MAX_Z)
		{
			z = MAP_MAX_Z - 1;
		}
		return z;
	}
	
	private static int regionX(int x)
	{
		return (x >> SHIFT_BY) + OFFSET_X;
	}
	
	private static int regionY(int y)
	{
		return (y >> SHIFT_BY) + OFFSET_Y;
	}
	
	private static int regionZ(int z)
	{
		return (z >> SHIFT_BY_Z) + OFFSET_Z;
	}
	
	static boolean isNeighbour(int x1, int y1, int z1, int x2, int y2, int z2)
	{
		return x1 <= x2 + 1 && x1 >= x2 - 1 && y1 <= y2 + 1 && y1 >= y2 - 1 && z1 <= z2 + 1 && z1 >= z2 - 1;
	}
	
	public static WorldRegion getRegion(Location loc)
	{
		return getRegion(validX(regionX(loc.x)), validY(regionY(loc.y)), validZ(regionZ(loc.z)));
	}
	
	public static WorldRegion getRegion(GameObject obj)
	{
		return getRegion(validX(regionX(obj.getX())), validY(regionY(obj.getY())), validZ(regionZ(obj.getZ())));
	}
	
	private static WorldRegion getRegion(int x, int y, int z)
	{
		WorldRegion[][][] regions = getRegions();
		WorldRegion region = regions[x][y][z];
		if(region == null)
		{
			WorldRegion[][][] arrworldRegion = regions;
			synchronized(arrworldRegion)
			{
				region = regions[x][y][z];
				if(region == null)
				{
					WorldRegion worldRegion = new WorldRegion(x, y, z);
					regions[x][y][z] = worldRegion;
					region = worldRegion;
				}
			}
		}
		return region;
	}
	
	public static Player getPlayer(String name)
	{
		return GameObjectsStorage.getPlayer(name);
	}
	
	public static Player getPlayer(int objId)
	{
		return GameObjectsStorage.getPlayer(objId);
	}
	
	public static void addVisibleObject(GameObject object, Creature dropper)
	{
		if(object == null || !object.isVisible() || object.isInObserverMode())
		{
			return;
		}
		WorldRegion region = getRegion(object);
		WorldRegion currentRegion = object.getCurrentRegion();
		if(currentRegion == region)
		{
			return;
		}
		if(currentRegion == null)
		{
			object.setCurrentRegion(region);
			region.addObject(object);
			for(int x = validX(region.getX() - 1);x <= validX(region.getX() + 1);++x)
			{
				for(int y = validY(region.getY() - 1);y <= validY(region.getY() + 1);++y)
				{
					for(int z = validZ(region.getZ() - 1);z <= validZ(region.getZ() + 1);++z)
					{
						getRegion(x, y, z).addToPlayers(object, dropper);
					}
				}
			}
		}
		else
		{
			currentRegion.removeObject(object);
			object.setCurrentRegion(region);
			region.addObject(object);
			int z;
			int x;
			int y;
			for(x = validX(currentRegion.getX() - 1);x <= validX(currentRegion.getX() + 1);++x)
			{
				for(y = validY(currentRegion.getY() - 1);y <= validY(currentRegion.getY() + 1);++y)
				{
					for(z = validZ(currentRegion.getZ() - 1);z <= validZ(currentRegion.getZ() + 1);++z)
					{
						if(isNeighbour(region.getX(), region.getY(), region.getZ(), x, y, z))
							continue;
						getRegion(x, y, z).removeFromPlayers(object);
					}
				}
			}
			for(x = validX(region.getX() - 1);x <= validX(region.getX() + 1);++x)
			{
				for(y = validY(region.getY() - 1);y <= validY(region.getY() + 1);++y)
				{
					for(z = validZ(region.getZ() - 1);z <= validZ(region.getZ() + 1);++z)
					{
						if(isNeighbour(currentRegion.getX(), currentRegion.getY(), currentRegion.getZ(), x, y, z))
							continue;
						getRegion(x, y, z).addToPlayers(object, dropper);
					}
				}
			}
		}
	}
	
	public static void removeVisibleObject(GameObject object)
	{
		if(object == null || object.isVisible() || object.isInObserverMode())
		{
			return;
		}
		WorldRegion currentRegion = object.getCurrentRegion();
		if(currentRegion == null)
		{
			return;
		}
		object.setCurrentRegion(null);
		currentRegion.removeObject(object);
		for(int x = validX(currentRegion.getX() - 1);x <= validX(currentRegion.getX() + 1);++x)
		{
			for(int y = validY(currentRegion.getY() - 1);y <= validY(currentRegion.getY() + 1);++y)
			{
				for(int z = validZ(currentRegion.getZ() - 1);z <= validZ(currentRegion.getZ() + 1);++z)
				{
					getRegion(x, y, z).removeFromPlayers(object);
				}
			}
		}
	}
	
	public static GameObject getAroundObjectById(GameObject object, int objId)
	{
		WorldRegion currentRegion = object.getCurrentRegion();
		if(currentRegion == null)
		{
			return null;
		}
		for(int x = validX(currentRegion.getX() - 1);x <= validX(currentRegion.getX() + 1);++x)
		{
			for(int y = validY(currentRegion.getY() - 1);y <= validY(currentRegion.getY() + 1);++y)
			{
				for(int z = validZ(currentRegion.getZ() - 1);z <= validZ(currentRegion.getZ() + 1);++z)
				{
					for(GameObject obj : getRegion(x, y, z))
					{
						if(obj.getObjectId() != objId)
							continue;
						return obj;
					}
				}
			}
		}
		return null;
	}
	
	public static List<GameObject> getAroundObjects(GameObject object)
	{
		WorldRegion currentRegion = object.getCurrentRegion();
		if(currentRegion == null)
		{
			return Collections.emptyList();
		}
		int oid = object.getObjectId();
		int rid = object.getReflectionId();
		LazyArrayList<GameObject> result = new LazyArrayList<>(128);
		for(int x = validX(currentRegion.getX() - 1);x <= validX(currentRegion.getX() + 1);++x)
		{
			for(int y = validY(currentRegion.getY() - 1);y <= validY(currentRegion.getY() + 1);++y)
			{
				for(int z = validZ(currentRegion.getZ() - 1);z <= validZ(currentRegion.getZ() + 1);++z)
				{
					for(GameObject obj : getRegion(x, y, z))
					{
						if(obj.getObjectId() == oid || obj.getReflectionId() != rid)
							continue;
						result.add(obj);
					}
				}
			}
		}
		return result;
	}
	
	public static List<GameObject> getAroundObjects(GameObject object, int radius, int height)
	{
		WorldRegion currentRegion = object.getCurrentRegion();
		if(currentRegion == null)
		{
			return Collections.emptyList();
		}
		int oid = object.getObjectId();
		int rid = object.getReflectionId();
		int ox = object.getX();
		int oy = object.getY();
		int oz = object.getZ();
		int sqrad = radius * radius;
		LazyArrayList<GameObject> result = new LazyArrayList<>(128);
		for(int x = validX(currentRegion.getX() - 1);x <= validX(currentRegion.getX() + 1);++x)
		{
			for(int y = validY(currentRegion.getY() - 1);y <= validY(currentRegion.getY() + 1);++y)
			{
				for(int z = validZ(currentRegion.getZ() - 1);z <= validZ(currentRegion.getZ() + 1);++z)
				{
					for(GameObject obj : getRegion(x, y, z))
					{
						int dy;
						int dx;
						if(obj.getObjectId() == oid || obj.getReflectionId() != rid || Math.abs(obj.getZ() - oz) > height || (dx = Math.abs(obj.getX() - ox)) > radius || (dy = Math.abs(obj.getY() - oy)) > radius || dx * dx + dy * dy > sqrad)
							continue;
						result.add(obj);
					}
				}
			}
		}
		return result;
	}
	
	public static List<Creature> getAroundCharacters(GameObject object)
	{
		WorldRegion currentRegion = object.getCurrentRegion();
		if(currentRegion == null)
		{
			return Collections.emptyList();
		}
		int oid = object.getObjectId();
		int rid = object.getReflectionId();
		LazyArrayList<Creature> result = new LazyArrayList<>(64);
		for(int x = validX(currentRegion.getX() - 1);x <= validX(currentRegion.getX() + 1);++x)
		{
			for(int y = validY(currentRegion.getY() - 1);y <= validY(currentRegion.getY() + 1);++y)
			{
				for(int z = validZ(currentRegion.getZ() - 1);z <= validZ(currentRegion.getZ() + 1);++z)
				{
					for(GameObject obj : getRegion(x, y, z))
					{
						if(!obj.isCreature() || obj.getObjectId() == oid || obj.getReflectionId() != rid)
							continue;
						result.add((Creature) obj);
					}
				}
			}
		}
		return result;
	}
	
	public static List<Creature> getAroundCharacters(GameObject object, int radius, int height)
	{
		WorldRegion currentRegion = object.getCurrentRegion();
		if(currentRegion == null)
		{
			return Collections.emptyList();
		}
		int oid = object.getObjectId();
		int rid = object.getReflectionId();
		int ox = object.getX();
		int oy = object.getY();
		int oz = object.getZ();
		int sqrad = radius * radius;
		LazyArrayList<Creature> result = new LazyArrayList<>(64);
		for(int x = validX(currentRegion.getX() - 1);x <= validX(currentRegion.getX() + 1);++x)
		{
			for(int y = validY(currentRegion.getY() - 1);y <= validY(currentRegion.getY() + 1);++y)
			{
				for(int z = validZ(currentRegion.getZ() - 1);z <= validZ(currentRegion.getZ() + 1);++z)
				{
					for(GameObject obj : getRegion(x, y, z))
					{
						int dy;
						int dx;
						if(!obj.isCreature() || obj.getObjectId() == oid || obj.getReflectionId() != rid || Math.abs(obj.getZ() - oz) > height || (dx = Math.abs(obj.getX() - ox)) > radius || (dy = Math.abs(obj.getY() - oy)) > radius || dx * dx + dy * dy > sqrad)
							continue;
						result.add((Creature) obj);
					}
				}
			}
		}
		return result;
	}
	
	public static List<NpcInstance> getAroundNpc(GameObject object)
	{
		WorldRegion currentRegion = object.getCurrentRegion();
		if(currentRegion == null)
		{
			return Collections.emptyList();
		}
		int oid = object.getObjectId();
		int rid = object.getReflectionId();
		LazyArrayList<NpcInstance> result = new LazyArrayList<>(64);
		for(int x = validX(currentRegion.getX() - 1);x <= validX(currentRegion.getX() + 1);++x)
		{
			for(int y = validY(currentRegion.getY() - 1);y <= validY(currentRegion.getY() + 1);++y)
			{
				for(int z = validZ(currentRegion.getZ() - 1);z <= validZ(currentRegion.getZ() + 1);++z)
				{
					for(GameObject obj : getRegion(x, y, z))
					{
						if(!obj.isNpc() || obj.getObjectId() == oid || obj.getReflectionId() != rid)
							continue;
						result.add((NpcInstance) obj);
					}
				}
			}
		}
		return result;
	}
	
	public static List<NpcInstance> getAroundNpc(GameObject object, int radius, int height)
	{
		WorldRegion currentRegion = object.getCurrentRegion();
		if(currentRegion == null)
		{
			return Collections.emptyList();
		}
		int oid = object.getObjectId();
		int rid = object.getReflectionId();
		int ox = object.getX();
		int oy = object.getY();
		int oz = object.getZ();
		int sqrad = radius * radius;
		LazyArrayList<NpcInstance> result = new LazyArrayList<>(64);
		for(int x = validX(currentRegion.getX() - 1);x <= validX(currentRegion.getX() + 1);++x)
		{
			for(int y = validY(currentRegion.getY() - 1);y <= validY(currentRegion.getY() + 1);++y)
			{
				for(int z = validZ(currentRegion.getZ() - 1);z <= validZ(currentRegion.getZ() + 1);++z)
				{
					for(GameObject obj : getRegion(x, y, z))
					{
						int dy;
						int dx;
						if(!obj.isNpc() || obj.getObjectId() == oid || obj.getReflectionId() != rid || Math.abs(obj.getZ() - oz) > height || (dx = Math.abs(obj.getX() - ox)) > radius || (dy = Math.abs(obj.getY() - oy)) > radius || dx * dx + dy * dy > sqrad)
							continue;
						result.add((NpcInstance) obj);
					}
				}
			}
		}
		return result;
	}
	
	public static List<Playable> getAroundPlayables(GameObject object)
	{
		WorldRegion currentRegion = object.getCurrentRegion();
		if(currentRegion == null)
		{
			return Collections.emptyList();
		}
		int oid = object.getObjectId();
		int rid = object.getReflectionId();
		LazyArrayList<Playable> result = new LazyArrayList<>(64);
		for(int x = validX(currentRegion.getX() - 1);x <= validX(currentRegion.getX() + 1);++x)
		{
			for(int y = validY(currentRegion.getY() - 1);y <= validY(currentRegion.getY() + 1);++y)
			{
				for(int z = validZ(currentRegion.getZ() - 1);z <= validZ(currentRegion.getZ() + 1);++z)
				{
					for(GameObject obj : getRegion(x, y, z))
					{
						if(!obj.isPlayable() || obj.getObjectId() == oid || obj.getReflectionId() != rid)
							continue;
						result.add((Playable) obj);
					}
				}
			}
		}
		return result;
	}
	
	public static List<Playable> getAroundPlayables(GameObject object, int radius, int height)
	{
		WorldRegion currentRegion = object.getCurrentRegion();
		if(currentRegion == null)
		{
			return Collections.emptyList();
		}
		int oid = object.getObjectId();
		int rid = object.getReflectionId();
		int ox = object.getX();
		int oy = object.getY();
		int oz = object.getZ();
		int sqrad = radius * radius;
		LazyArrayList<Playable> result = new LazyArrayList<>(64);
		for(int x = validX(currentRegion.getX() - 1);x <= validX(currentRegion.getX() + 1);++x)
		{
			for(int y = validY(currentRegion.getY() - 1);y <= validY(currentRegion.getY() + 1);++y)
			{
				for(int z = validZ(currentRegion.getZ() - 1);z <= validZ(currentRegion.getZ() + 1);++z)
				{
					for(GameObject obj : getRegion(x, y, z))
					{
						int dy;
						int dx;
						if(!obj.isPlayable() || obj.getObjectId() == oid || obj.getReflectionId() != rid || Math.abs(obj.getZ() - oz) > height || (dx = Math.abs(obj.getX() - ox)) > radius || (dy = Math.abs(obj.getY() - oy)) > radius || dx * dx + dy * dy > sqrad)
							continue;
						result.add((Playable) obj);
					}
				}
			}
		}
		return result;
	}
	
	public static List<Player> getAroundPlayers(GameObject object)
	{
		WorldRegion currentRegion = object.getCurrentRegion();
		if(currentRegion == null)
		{
			return Collections.emptyList();
		}
		int oid = object.getObjectId();
		int rid = object.getReflectionId();
		LazyArrayList<Player> result = new LazyArrayList<>(64);
		for(int x = validX(currentRegion.getX() - 1);x <= validX(currentRegion.getX() + 1);++x)
		{
			for(int y = validY(currentRegion.getY() - 1);y <= validY(currentRegion.getY() + 1);++y)
			{
				for(int z = validZ(currentRegion.getZ() - 1);z <= validZ(currentRegion.getZ() + 1);++z)
				{
					for(GameObject obj : getRegion(x, y, z))
					{
						if(!obj.isPlayer() || obj.getObjectId() == oid || obj.getReflectionId() != rid)
							continue;
						result.add((Player) obj);
					}
				}
			}
		}
		return result;
	}
	
	public static List<Player> getAroundPlayers(GameObject object, int radius, int height)
	{
		WorldRegion currentRegion = object.getCurrentRegion();
		if(currentRegion == null)
		{
			return Collections.emptyList();
		}
		int oid = object.getObjectId();
		int rid = object.getReflectionId();
		int ox = object.getX();
		int oy = object.getY();
		int oz = object.getZ();
		int sqrad = radius * radius;
		LazyArrayList<Player> result = new LazyArrayList<>(64);
		for(int x = validX(currentRegion.getX() - 1);x <= validX(currentRegion.getX() + 1);++x)
		{
			for(int y = validY(currentRegion.getY() - 1);y <= validY(currentRegion.getY() + 1);++y)
			{
				for(int z = validZ(currentRegion.getZ() - 1);z <= validZ(currentRegion.getZ() + 1);++z)
				{
					for(GameObject obj : getRegion(x, y, z))
					{
						int dy;
						int dx;
						if(!obj.isPlayer() || obj.getObjectId() == oid || obj.getReflectionId() != rid || Math.abs(obj.getZ() - oz) > height || (dx = Math.abs(obj.getX() - ox)) > radius || (dy = Math.abs(obj.getY() - oy)) > radius || dx * dx + dy * dy > sqrad)
							continue;
						result.add((Player) obj);
					}
				}
			}
		}
		return result;
	}
	
	public static boolean isNeighborsEmpty(WorldRegion region)
	{
		for(int x = validX(region.getX() - 1);x <= validX(region.getX() + 1);++x)
		{
			for(int y = validY(region.getY() - 1);y <= validY(region.getY() + 1);++y)
			{
				for(int z = validZ(region.getZ() - 1);z <= validZ(region.getZ() + 1);++z)
				{
					if(getRegion(x, y, z).isEmpty())
						continue;
					return false;
				}
			}
		}
		return true;
	}
	
	public static void activate(WorldRegion currentRegion)
	{
		for(int x = validX(currentRegion.getX() - 1);x <= validX(currentRegion.getX() + 1);++x)
		{
			for(int y = validY(currentRegion.getY() - 1);y <= validY(currentRegion.getY() + 1);++y)
			{
				for(int z = validZ(currentRegion.getZ() - 1);z <= validZ(currentRegion.getZ() + 1);++z)
				{
					getRegion(x, y, z).setActive(true);
				}
			}
		}
	}
	
	public static void deactivate(WorldRegion currentRegion)
	{
		for(int x = validX(currentRegion.getX() - 1);x <= validX(currentRegion.getX() + 1);++x)
		{
			for(int y = validY(currentRegion.getY() - 1);y <= validY(currentRegion.getY() + 1);++y)
			{
				for(int z = validZ(currentRegion.getZ() - 1);z <= validZ(currentRegion.getZ() + 1);++z)
				{
					if(!isNeighborsEmpty(getRegion(x, y, z)))
						continue;
					getRegion(x, y, z).setActive(false);
				}
			}
		}
	}
	
	public static void showObjectsToPlayer(Player player)
	{
		WorldRegion currentRegion;
		WorldRegion worldRegion = currentRegion = player.isInObserverMode() ? player.getObserverRegion() : player.getCurrentRegion();
		if(currentRegion == null)
		{
			return;
		}
		int oid = player.getObjectId();
		int rid = player.getReflectionId();
		for(int x = validX(currentRegion.getX() - 1);x <= validX(currentRegion.getX() + 1);++x)
		{
			for(int y = validY(currentRegion.getY() - 1);y <= validY(currentRegion.getY() + 1);++y)
			{
				for(int z = validZ(currentRegion.getZ() - 1);z <= validZ(currentRegion.getZ() + 1);++z)
				{
					for(GameObject obj : getRegion(x, y, z))
					{
						if(obj.getObjectId() == oid || obj.getReflectionId() != rid)
							continue;
						player.sendPacket(player.addVisibleObject(obj, null));
					}
				}
			}
		}
	}
	
	public static void removeObjectsFromPlayer(Player player)
	{
		WorldRegion currentRegion;
		WorldRegion worldRegion = currentRegion = player.isInObserverMode() ? player.getObserverRegion() : player.getCurrentRegion();
		if(currentRegion == null)
		{
			return;
		}
		int oid = player.getObjectId();
		int rid = player.getReflectionId();
		for(int x = validX(currentRegion.getX() - 1);x <= validX(currentRegion.getX() + 1);++x)
		{
			for(int y = validY(currentRegion.getY() - 1);y <= validY(currentRegion.getY() + 1);++y)
			{
				for(int z = validZ(currentRegion.getZ() - 1);z <= validZ(currentRegion.getZ() + 1);++z)
				{
					for(GameObject obj : getRegion(x, y, z))
					{
						if(obj.getObjectId() == oid || obj.getReflectionId() != rid)
							continue;
						player.sendPacket(player.removeVisibleObject(obj, null));
					}
				}
			}
		}
	}
	
	public static void removeObjectFromPlayers(GameObject object)
	{
		WorldRegion currentRegion = object.getCurrentRegion();
		if(currentRegion == null)
		{
			return;
		}
		int oid = object.getObjectId();
		int rid = object.getReflectionId();
		List<L2GameServerPacket> d = null;
		for(int x = validX(currentRegion.getX() - 1);x <= validX(currentRegion.getX() + 1);++x)
		{
			for(int y = validY(currentRegion.getY() - 1);y <= validY(currentRegion.getY() + 1);++y)
			{
				for(int z = validZ(currentRegion.getZ() - 1);z <= validZ(currentRegion.getZ() + 1);++z)
				{
					for(GameObject obj : getRegion(x, y, z))
					{
						if(!obj.isPlayer() || obj.getObjectId() == oid || obj.getReflectionId() != rid)
							continue;
						Player p = (Player) obj;
						p.sendPacket(p.removeVisibleObject(object, d == null ? object.deletePacketList() : d));
					}
				}
			}
		}
	}
	
	static void addZone(Zone zone)
	{
		Reflection reflection = zone.getReflection();
		Territory territory = zone.getTerritory();
		if(territory == null)
		{
			_log.info("World: zone - " + zone.getName() + " not has territory.");
			return;
		}
		for(int x = validX(regionX(territory.getXmin()));x <= validX(regionX(territory.getXmax()));++x)
		{
			for(int y = validY(regionY(territory.getYmin()));y <= validY(regionY(territory.getYmax()));++y)
			{
				for(int z = validZ(regionZ(territory.getZmin()));z <= validZ(regionZ(territory.getZmax()));++z)
				{
					WorldRegion region = getRegion(x, y, z);
					region.addZone(zone);
					for(GameObject obj : region)
					{
						if(!obj.isCreature() || obj.getReflection() != reflection)
							continue;
						((Creature) obj).updateZones();
					}
				}
			}
		}
	}
	
	static void removeZone(Zone zone)
	{
		Reflection reflection = zone.getReflection();
		Territory territory = zone.getTerritory();
		if(territory == null)
		{
			_log.info("World: zone - " + zone.getName() + " not has territory.");
			return;
		}
		for(int x = validX(regionX(territory.getXmin()));x <= validX(regionX(territory.getXmax()));++x)
		{
			for(int y = validY(regionY(territory.getYmin()));y <= validY(regionY(territory.getYmax()));++y)
			{
				for(int z = validZ(regionZ(territory.getZmin()));z <= validZ(regionZ(territory.getZmax()));++z)
				{
					WorldRegion region = getRegion(x, y, z);
					region.removeZone(zone);
					for(GameObject obj : region)
					{
						if(!obj.isCreature() || obj.getReflection() != reflection)
							continue;
						((Creature) obj).updateZones();
					}
				}
			}
		}
	}
	
	public static void getZones(List<Zone> inside, Location loc, Reflection reflection)
	{
		WorldRegion region = getRegion(loc);
		Zone[] zones = region.getZones();
		if(zones.length == 0)
		{
			return;
		}
		for(Zone zone : zones)
		{
			if(!zone.checkIfInZone(loc.x, loc.y, loc.z, reflection))
				continue;
			inside.add(zone);
		}
	}
	
	public static boolean isWater(Location loc, Reflection reflection)
	{
		return getWater(loc, reflection) != null;
	}
	
	public static Zone getWater(Location loc, Reflection reflection)
	{
		WorldRegion region = getRegion(loc);
		Zone[] zones = region.getZones();
		if(zones.length == 0)
		{
			return null;
		}
		for(Zone zone : zones)
		{
			if(zone == null || zone.getType() != Zone.ZoneType.water || !zone.checkIfInZone(loc.x, loc.y, loc.z, reflection))
				continue;
			return zone;
		}
		return null;
	}
	
	public static void broadcast(L2GameServerPacket... packets)
	{
		for(Player player : GameObjectsStorage.getAllPlayersForIterate())
		{
			player.sendPacket(packets);
		}
	}
	
	public static int[] getStats()
	{
		int[] ret = new int[32];
		for(int x = 0;x <= REGIONS_X;++x)
		{
			for(int y = 0;y <= REGIONS_Y;++y)
			{
				for(int z = 0;z <= REGIONS_Z;++z)
				{
					int[] arrn = ret;
					arrn[0] = arrn[0] + 1;
					WorldRegion region = _worldRegions[x][y][z];
					if(region != null)
					{
						if(region.isActive())
						{
							int[] arrn2 = ret;
							arrn2[1] = arrn2[1] + 1;
						}
						else
						{
							int[] arrn3 = ret;
							arrn3[2] = arrn3[2] + 1;
						}
						for(GameObject obj : region)
						{
							int[] arrn4 = ret;
							arrn4[10] = arrn4[10] + 1;
							if(obj.isCreature())
							{
								int[] arrn5 = ret;
								arrn5[11] = arrn5[11] + 1;
								if(obj.isPlayer())
								{
									int[] arrn6 = ret;
									arrn6[12] = arrn6[12] + 1;
									Player p = (Player) obj;
									if(!p.isInOfflineMode())
										continue;
									int[] arrn7 = ret;
									arrn7[13] = arrn7[13] + 1;
									continue;
								}
								if(obj.isNpc())
								{
									int[] arrn8 = ret;
									arrn8[14] = arrn8[14] + 1;
									if(obj.isMonster())
									{
										int[] arrn9 = ret;
										arrn9[16] = arrn9[16] + 1;
										if(obj.isMinion())
										{
											int[] arrn10 = ret;
											arrn10[17] = arrn10[17] + 1;
										}
									}
									NpcInstance npc;
									if(!(npc = (NpcInstance) obj).hasAI() || !npc.getAI().isActive())
										continue;
									int[] arrn11 = ret;
									arrn11[15] = arrn11[15] + 1;
									continue;
								}
								if(obj.isPlayable())
								{
									int[] arrn12 = ret;
									arrn12[18] = arrn12[18] + 1;
									continue;
								}
								if(!obj.isDoor())
									continue;
								int[] arrn13 = ret;
								arrn13[19] = arrn13[19] + 1;
								continue;
							}
							if(!obj.isItem())
								continue;
							int[] arrn14 = ret;
							arrn14[20] = arrn14[20] + 1;
						}
						continue;
					}
					int[] arrn15 = ret;
					arrn15[3] = arrn15[3] + 1;
				}
			}
		}
		return ret;
	}
}