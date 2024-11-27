package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Player;
import org.apache.commons.lang3.StringUtils;

public class PrivateStoreMsgSell extends L2GameServerPacket
{
	private final int _objId;
	private final String _name;
	
	public PrivateStoreMsgSell(Player player)
	{
		_objId = player.getObjectId();
		_name = StringUtils.defaultString(player.getSellStoreName());
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(156);
		writeD(_objId);
		writeS(_name);
	}
}