package l2.gameserver.model;

import l2.commons.geometry.Point3D;
import l2.commons.geometry.Shape;
import l2.commons.util.Rnd;
import l2.gameserver.geodata.GeoEngine;
import l2.gameserver.templates.spawn.SpawnRange;
import l2.gameserver.utils.Location;

import java.util.ArrayList;
import java.util.List;

public class Territory implements Shape, SpawnRange
{
	protected final Point3D max = new Point3D();
	protected final Point3D min = new Point3D();
	private final List<Shape> include = new ArrayList<>(1);
	private final List<Shape> exclude = new ArrayList<>(1);
	
	public static Location getRandomLoc(Territory territory)
	{
		return getRandomLoc(territory, 0);
	}
	
	public static Location getRandomLoc(Territory territory, int geoIndex)
	{
		Location pos = new Location();
		List<Shape> territories = territory.getTerritories();
		block0:
		for(int i = 0;i < 100;++i)
		{
			Shape shape = territories.get(Rnd.get(territories.size()));
			pos.x = Rnd.get(shape.getXmin(), shape.getXmax());
			pos.y = Rnd.get(shape.getYmin(), shape.getYmax());
			pos.z = shape.getZmin() + (shape.getZmax() - shape.getZmin()) / 2;
			if(!territory.isInside(pos.x, pos.y))
				continue;
			int tempz = GeoEngine.getHeight(pos, geoIndex);
			if(shape.getZmin() != shape.getZmax() ? tempz < shape.getZmin() || tempz > shape.getZmax() : tempz < shape.getZmin() - 200 || tempz > shape.getZmin() + 200)
				continue;
			pos.z = tempz;
			int geoX = pos.x - World.MAP_MIN_X >> 4;
			int geoY = pos.y - World.MAP_MIN_Y >> 4;
			for(int x = geoX - 1;x <= geoX + 1;++x)
			{
				for(int y = geoY - 1;y <= geoY + 1;++y)
				{
					if(GeoEngine.NgetNSWE(x, y, tempz, geoIndex) != 15)
						continue block0;
				}
			}
			return pos;
		}
		return pos;
	}
	
	public Territory add(Shape shape)
	{
		if(include.isEmpty())
		{
			max.x = shape.getXmax();
			max.y = shape.getYmax();
			max.z = shape.getZmax();
			min.x = shape.getXmin();
			min.y = shape.getYmin();
			min.z = shape.getZmin();
		}
		else
		{
			max.x = Math.max(max.x, shape.getXmax());
			max.y = Math.max(max.y, shape.getYmax());
			max.z = Math.max(max.z, shape.getZmax());
			min.x = Math.min(min.x, shape.getXmin());
			min.y = Math.min(min.y, shape.getYmin());
			min.z = Math.min(min.z, shape.getZmin());
		}
		include.add(shape);
		return this;
	}
	
	public Territory addBanned(Shape shape)
	{
		exclude.add(shape);
		return this;
	}
	
	public List<Shape> getTerritories()
	{
		return include;
	}
	
	public List<Shape> getBannedTerritories()
	{
		return exclude;
	}
	
	@Override
	public boolean isInside(int x, int y)
	{
		for(int i = 0;i < include.size();++i)
		{
			Shape shape = include.get(i);
			if(!shape.isInside(x, y))
				continue;
			return !isExcluded(x, y);
		}
		return false;
	}
	
	@Override
	public boolean isInside(int x, int y, int z)
	{
		if(x < min.x || x > max.x || y < min.y || y > max.y || z < min.z || z > max.z)
		{
			return false;
		}
		for(int i = 0;i < include.size();++i)
		{
			Shape shape = include.get(i);
			if(!shape.isInside(x, y, z))
				continue;
			return !isExcluded(x, y, z);
		}
		return false;
	}
	
	public boolean isExcluded(int x, int y)
	{
		for(int i = 0;i < exclude.size();++i)
		{
			Shape shape = exclude.get(i);
			if(!shape.isInside(x, y))
				continue;
			return true;
		}
		return false;
	}
	
	public boolean isExcluded(int x, int y, int z)
	{
		for(int i = 0;i < exclude.size();++i)
		{
			Shape shape = exclude.get(i);
			if(!shape.isInside(x, y, z))
				continue;
			return true;
		}
		return false;
	}
	
	@Override
	public int getXmax()
	{
		return max.x;
	}
	
	@Override
	public int getXmin()
	{
		return min.x;
	}
	
	@Override
	public int getYmax()
	{
		return max.y;
	}
	
	@Override
	public int getYmin()
	{
		return min.y;
	}
	
	@Override
	public int getZmax()
	{
		return max.z;
	}
	
	@Override
	public int getZmin()
	{
		return min.z;
	}
	
	@Override
	public Location getRandomLoc(int geoIndex)
	{
		return getRandomLoc(this, geoIndex);
	}
}