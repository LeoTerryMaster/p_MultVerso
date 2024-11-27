package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Player;
import l2.gameserver.model.instances.DoorInstance;

public class DoorStatusUpdate extends L2GameServerPacket
{
	private final int _doorObjId;
	private final boolean _isClosed;
	private final int _dmg;
	private final boolean _isAttakable;
	private final int _doorStaticId;
	private final int _curHp;
	private final int _maxHp;
	
	public DoorStatusUpdate(DoorInstance door, Player player)
	{
		_doorObjId = door.getObjectId();
		_doorStaticId = door.getDoorId();
		_isClosed = !door.isOpen();
		_isAttakable = door.isAutoAttackable(player);
		_curHp = (int) door.getCurrentHp();
		_maxHp = door.getMaxHp();
		_dmg = door.getDamage();
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(77);
		writeD(_doorObjId);
		writeD(_isClosed);
		writeD(_dmg);
		writeD(_isAttakable);
		writeD(_doorStaticId);
		writeD(_maxHp);
		writeD(_curHp);
	}
}