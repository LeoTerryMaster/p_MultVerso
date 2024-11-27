package l2.gameserver.network.l2.c2s;

import l2.gameserver.model.Player;
import l2.gameserver.network.l2.s2c.StartRotating;

public class StartRotatingC extends L2GameClientPacket
{
	private int _degree;
	private int _side;
	
	@Override
	protected void readImpl()
	{
		_degree = readD();
		_side = readD();
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		activeChar.setHeading(_degree);
		activeChar.broadcastPacket(new StartRotating(activeChar, _degree, _side, 0));
	}
}