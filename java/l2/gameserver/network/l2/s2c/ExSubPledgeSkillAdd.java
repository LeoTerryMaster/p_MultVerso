package l2.gameserver.network.l2.s2c;

public class ExSubPledgeSkillAdd extends L2GameServerPacket
{
	private final int _type;
	private final int _id;
	private final int _level;
	
	public ExSubPledgeSkillAdd(int type, int id, int level)
	{
		_type = type;
		_id = id;
		_level = level;
	}
	
	@Override
	protected void writeImpl()
	{
		writeEx(118);
		writeD(_type);
		writeD(_id);
		writeD(_level);
	}
}