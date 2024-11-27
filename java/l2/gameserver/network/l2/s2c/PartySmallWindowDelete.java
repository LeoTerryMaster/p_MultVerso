package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Player;

public class PartySmallWindowDelete extends L2GameServerPacket
{
	private final int _objId;
	private final String _name;
	
	public PartySmallWindowDelete(Player member)
	{
		_objId = member.getObjectId();
		_name = member.getName();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(81);
		writeD(_objId);
		writeS(_name);
	}
}