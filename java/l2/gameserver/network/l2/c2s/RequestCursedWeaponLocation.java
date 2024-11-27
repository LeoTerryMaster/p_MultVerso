package l2.gameserver.network.l2.c2s;

import l2.gameserver.instancemanager.CursedWeaponsManager;
import l2.gameserver.model.CursedWeapon;
import l2.gameserver.model.Player;
import l2.gameserver.network.l2.s2c.ExCursedWeaponLocation;
import l2.gameserver.utils.Location;

import java.util.ArrayList;

public class RequestCursedWeaponLocation extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		ArrayList<ExCursedWeaponLocation.CursedWeaponInfo> list = new ArrayList<>();
		for(CursedWeapon cw : CursedWeaponsManager.getInstance().getCursedWeapons())
		{
			Location pos = cw.getWorldPosition();
			if(pos == null)
				continue;
			list.add(new ExCursedWeaponLocation.CursedWeaponInfo(pos, cw.getItemId(), cw.isActivated() ? 1 : 0));
		}
		activeChar.sendPacket(new ExCursedWeaponLocation(list));
	}
}