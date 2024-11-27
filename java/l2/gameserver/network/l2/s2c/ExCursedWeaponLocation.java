package l2.gameserver.network.l2.s2c;

import l2.gameserver.utils.Location;

import java.util.List;

public class ExCursedWeaponLocation extends L2GameServerPacket
{
	private final List<CursedWeaponInfo> _cursedWeaponInfo;
	
	public ExCursedWeaponLocation(List<CursedWeaponInfo> cursedWeaponInfo)
	{
		_cursedWeaponInfo = cursedWeaponInfo;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeEx(70);
		if(_cursedWeaponInfo.isEmpty())
		{
			writeD(0);
		}
		else
		{
			writeD(_cursedWeaponInfo.size());
			for(CursedWeaponInfo w : _cursedWeaponInfo)
			{
				writeD(w._id);
				writeD(w._status);
				writeD(w._pos.x);
				writeD(w._pos.y);
				writeD(w._pos.z);
			}
		}
	}
	
	public static class CursedWeaponInfo
	{
		public Location _pos;
		public int _id;
		public int _status;
		
		public CursedWeaponInfo(Location p, int ID, int status)
		{
			_pos = p;
			_id = ID;
			_status = status;
		}
	}
}