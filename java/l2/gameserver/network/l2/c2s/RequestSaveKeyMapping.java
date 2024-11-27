package l2.gameserver.network.l2.c2s;

import l2.gameserver.model.Player;
import l2.gameserver.network.l2.s2c.ExUISetting;

public class RequestSaveKeyMapping extends L2GameClientPacket
{
	private byte[] _data;
	
	@Override
	protected void readImpl()
	{
		int length = readD();
		if(length > _buf.remaining() || length > 32767 || length < 0)
		{
			_data = null;
			return;
		}
		_data = new byte[length];
		readB(_data);
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null || _data == null)
		{
			return;
		}
		activeChar.setKeyBindings(_data);
		activeChar.sendPacket(new ExUISetting(activeChar));
	}
}