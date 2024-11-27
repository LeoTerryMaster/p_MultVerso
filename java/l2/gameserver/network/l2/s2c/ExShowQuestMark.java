package l2.gameserver.network.l2.s2c;

public class ExShowQuestMark extends L2GameServerPacket
{
	private final int _questId;
	
	public ExShowQuestMark(int questId)
	{
		_questId = questId;
	}
	
	@Override
	protected void writeImpl()
	{
		writeEx(26);
		writeD(_questId);
	}
}