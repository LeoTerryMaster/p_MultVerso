package l2.gameserver.templates.spawn;

import l2.gameserver.utils.Location;

public interface SpawnRange
{
	Location getRandomLoc(int ref);
}