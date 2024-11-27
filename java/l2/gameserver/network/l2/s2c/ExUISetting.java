package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Player;

public class ExUISetting extends L2GameServerPacket
{
	private final byte[] data;
	
	public ExUISetting(Player player)
	{
		data = player.getKeyBindings();
	}
	
	@Override
	protected void writeImpl()
	{
		writeEx(112);
		writeD(data.length);
		writeB(data);
	}
}