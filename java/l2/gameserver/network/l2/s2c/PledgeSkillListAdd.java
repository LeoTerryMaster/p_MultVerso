package l2.gameserver.network.l2.s2c;

public class PledgeSkillListAdd extends L2GameServerPacket
{
	private final int _skillId;
	private final int _skillLevel;
	
	public PledgeSkillListAdd(int skillId, int skillLevel)
	{
		_skillId = skillId;
		_skillLevel = skillLevel;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeEx(58);
		writeD(_skillId);
		writeD(_skillLevel);
	}
}