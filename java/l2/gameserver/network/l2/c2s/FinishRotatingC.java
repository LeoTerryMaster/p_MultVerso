package l2.gameserver.network.l2.c2s;

import l2.gameserver.model.Player;
import l2.gameserver.network.l2.s2c.FinishRotating;

public class FinishRotatingC extends L2GameClientPacket
{
	private int _degree;
	private int _unknown;
	
	@Override
	protected void readImpl()
	{
		_degree = readD();
		_unknown = readD();
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		activeChar.broadcastPacket(new FinishRotating(activeChar, _degree, 0));
	}
}