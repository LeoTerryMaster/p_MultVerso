package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Player;
import l2.gameserver.network.l2.components.NpcString;

public class ExSendUIEvent extends NpcStringContainer
{
	private final int _objectId;
	private final boolean _isHide;
	private final boolean _isIncrease;
	private final int _startTime;
	private final int _endTime;
	
	public ExSendUIEvent(Player player, boolean isHide, boolean isIncrease, int startTime, int endTime, String... params)
	{
		this(player, isHide, isIncrease, startTime, endTime, NpcString.NONE, params);
	}
	
	public ExSendUIEvent(Player player, boolean isHide, boolean isIncrease, int startTime, int endTime, NpcString npcString, String... params)
	{
		super(npcString, params);
		_objectId = player.getObjectId();
		_isHide = isHide;
		_isIncrease = isIncrease;
		_startTime = startTime;
		_endTime = endTime;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(254);
		writeH(142);
		writeD(_objectId);
		writeD(_isHide ? 1 : 0);
		writeD(0);
		writeD(0);
		writeS(_isIncrease ? "1" : "0");
		writeS(String.valueOf(_startTime / 60));
		writeS(String.valueOf(_startTime % 60));
		writeS(String.valueOf(_endTime / 60));
		writeS(String.valueOf(_endTime % 60));
		writeElements();
	}
}