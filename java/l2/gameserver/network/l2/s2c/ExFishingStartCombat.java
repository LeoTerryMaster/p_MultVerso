package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Creature;

public class ExFishingStartCombat extends L2GameServerPacket
{
	private final int char_obj_id;
	int _time;
	int _hp;
	int _lureType;
	int _deceptiveMode;
	int _mode;
	
	public ExFishingStartCombat(Creature character, int time, int hp, int mode, int lureType, int deceptiveMode)
	{
		char_obj_id = character.getObjectId();
		_time = time;
		_hp = hp;
		_mode = mode;
		_lureType = lureType;
		_deceptiveMode = deceptiveMode;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeEx(21);
		writeD(char_obj_id);
		writeD(_time);
		writeD(_hp);
		writeC(_mode);
		writeC(_lureType);
		writeC(_deceptiveMode);
	}
}