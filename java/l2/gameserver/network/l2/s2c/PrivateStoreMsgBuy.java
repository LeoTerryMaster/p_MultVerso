package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Player;
import org.apache.commons.lang3.StringUtils;

public class PrivateStoreMsgBuy extends L2GameServerPacket
{
	private final int _objId;
	private final String _name;
	
	public PrivateStoreMsgBuy(Player player)
	{
		_objId = player.getObjectId();
		_name = StringUtils.defaultString(player.getBuyStoreName());
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(185);
		writeD(_objId);
		writeS(_name);
	}
}