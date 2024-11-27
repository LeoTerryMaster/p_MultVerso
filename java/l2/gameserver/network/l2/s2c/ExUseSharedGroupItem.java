package l2.gameserver.network.l2.s2c;

import l2.gameserver.skills.TimeStamp;

public class ExUseSharedGroupItem extends L2GameServerPacket
{
	private final int _itemId;
	private final int _grpId;
	private final int _remainedTime;
	private final int _totalTime;
	
	public ExUseSharedGroupItem(int grpId, TimeStamp timeStamp)
	{
		_grpId = grpId;
		_itemId = timeStamp.getId();
		_remainedTime = (int) (timeStamp.getReuseCurrent() / 1000);
		_totalTime = (int) (timeStamp.getReuseBasic() / 1000);
	}
	
	@Override
	protected final void writeImpl()
	{
		writeEx(73);
		writeD(_itemId);
		writeD(_grpId);
		writeD(_remainedTime);
		writeD(_totalTime);
	}
}