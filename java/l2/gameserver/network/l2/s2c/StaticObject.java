package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Player;
import l2.gameserver.model.instances.DoorInstance;
import l2.gameserver.model.instances.StaticObjectInstance;

public class StaticObject extends L2GameServerPacket
{
	private final int _staticObjectId;
	private final int _objectId;
	private final int _type;
	private final int _isTargetable;
	private final int _meshIndex;
	private final int _isClosed;
	private final int _isEnemy;
	private final int _maxHp;
	private final int _currentHp;
	private final int _showHp;
	private final int _damageGrade;
	
	public StaticObject(StaticObjectInstance obj)
	{
		_staticObjectId = obj.getUId();
		_objectId = obj.getObjectId();
		_type = 0;
		_isTargetable = 1;
		_meshIndex = obj.getMeshIndex();
		_isClosed = 0;
		_isEnemy = 0;
		_maxHp = 0;
		_currentHp = 0;
		_showHp = 0;
		_damageGrade = 0;
	}
	
	public StaticObject(DoorInstance door, Player player)
	{
		_staticObjectId = door.getDoorId();
		_objectId = door.getObjectId();
		_type = 1;
		_isTargetable = door.getTemplate().isTargetable() ? 1 : 0;
		_meshIndex = 1;
		_isClosed = door.isOpen() ? 0 : 1;
		_isEnemy = door.isAutoAttackable(player) ? 1 : 0;
		_currentHp = (int) door.getCurrentHp();
		_maxHp = door.getMaxHp();
		_showHp = door.isHPVisible() ? 1 : 0;
		_damageGrade = door.getDamage();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(153);
		writeD(_staticObjectId);
		writeD(_objectId);
	}
}