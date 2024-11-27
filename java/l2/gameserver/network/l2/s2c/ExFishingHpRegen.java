package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Creature;

public class ExFishingHpRegen extends L2GameServerPacket
{
	private final int _time;
	private final int _fishHP;
	private final int _HPmode;
	private final int _Anim;
	private final int _GoodUse;
	private final int _Penalty;
	private final int _hpBarColor;
	private final int char_obj_id;
	
	public ExFishingHpRegen(Creature character, int time, int fishHP, int HPmode, int GoodUse, int anim, int penalty, int hpBarColor)
	{
		char_obj_id = character.getObjectId();
		_time = time;
		_fishHP = fishHP;
		_HPmode = HPmode;
		_GoodUse = GoodUse;
		_Anim = anim;
		_Penalty = penalty;
		_hpBarColor = hpBarColor;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeEx(22);
		writeD(char_obj_id);
		writeD(_time);
		writeD(_fishHP);
		writeC(_HPmode);
		writeC(_GoodUse);
		writeC(_Anim);
		writeD(_Penalty);
		writeC(_hpBarColor);
	}
}