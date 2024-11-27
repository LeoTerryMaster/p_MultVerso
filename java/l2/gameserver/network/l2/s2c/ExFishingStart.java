package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Creature;
import l2.gameserver.utils.Location;

public class ExFishingStart extends L2GameServerPacket
{
	private final int _charObjId;
	private final Location _loc;
	private final int _fishType;
	private final boolean _isNightLure;
	
	public ExFishingStart(Creature character, int fishType, Location loc, boolean isNightLure)
	{
		_charObjId = character.getObjectId();
		_fishType = fishType;
		_loc = loc;
		_isNightLure = isNightLure;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeEx(19);
		writeD(_charObjId);
		writeD(_fishType);
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z);
		writeC(_isNightLure ? 1 : 0);
		writeC(1);
	}
}