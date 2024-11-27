package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Effect;

public class ShortBuffStatusUpdate extends L2GameServerPacket
{
	int _skillId;
	int _skillLevel;
	int _skillDuration;
	
	public ShortBuffStatusUpdate(Effect effect)
	{
		_skillId = effect.getSkill().getDisplayId();
		_skillLevel = effect.getSkill().getDisplayLevel();
		_skillDuration = effect.getTimeLeft();
	}
	
	public ShortBuffStatusUpdate()
	{
		_skillId = 0;
		_skillLevel = 0;
		_skillDuration = 0;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(244);
		writeD(_skillId);
		writeD(_skillLevel);
		writeD(_skillDuration);
	}
}