package l2.gameserver.utils;

import l2.commons.geometry.Polygon;
import l2.commons.util.Rnd;
import l2.gameserver.Config;
import l2.gameserver.geodata.GeoEngine;
import l2.gameserver.model.World;
import l2.gameserver.templates.spawn.SpawnRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpawnMesh extends Polygon implements SpawnRange
{
	private static final Logger LOG = LoggerFactory.getLogger(SpawnMesh.class);
	
	@Override
	public Location getRandomLoc(int geoIndex)
	{
		Location loc = new Location(0, 0, 0);
		int width = getXmax() - getXmin();
		int height = getYmax() - getYmin();
		int dropZ = getZmin() + (getZmax() - getZmin()) / 2;
		int maxAttempts = Math.max(2048, (height >> 4) * (width >> 4));
		int attempts = 0;
		block0:
		do
		{
			loc.setX(Rnd.get(getXmin(), getXmax()));
			loc.setY(Rnd.get(getYmin(), getYmax()));
			loc.setZ(dropZ);
			if(!isInside(loc.getX(), loc.getY()))
				continue;
			int tempz = GeoEngine.getHeight(loc, geoIndex);
			if(getZmin() != getZmax() ? tempz < getZmin() || tempz > getZmax() : tempz < getZmin() - Config.MAX_Z_DIFF || tempz > getZmin() + Config.MAX_Z_DIFF)
				continue;
			loc.setZ(tempz);
			int geoX = loc.getX() - World.MAP_MIN_X >> 4;
			int geoY = loc.getY() - World.MAP_MIN_Y >> 4;
			for(int gx = geoX - 1;gx <= geoX + 1;++gx)
			{
				for(int gy = geoY - 1;gy <= geoY + 1;++gy)
				{
					if(GeoEngine.NgetNSWE(gx, gy, tempz, geoIndex) != 15)
						continue block0;
				}
			}
			return loc;
		}
		while(attempts++ < maxAttempts);
		if(Config.ALT_DEBUG_ENABLED)
		{
			LOG.warn("Cant find suitable point in " + this + " z[" + getZmin() + " " + getZmax() + "] last: " + loc);
		}
		return loc;
	}
}