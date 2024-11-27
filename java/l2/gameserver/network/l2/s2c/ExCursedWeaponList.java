package l2.gameserver.network.l2.s2c;

import l2.gameserver.instancemanager.CursedWeaponsManager;

public class ExCursedWeaponList extends L2GameServerPacket
{
	private final int[] cursedWeapon_ids = CursedWeaponsManager.getInstance().getCursedWeaponsIds();
	
	@Override
	protected final void writeImpl()
	{
		writeEx(69);
		writeDD(cursedWeapon_ids, true);
	}
}