package l2.gameserver.network.l2.c2s;

import l2.gameserver.model.Player;
import l2.gameserver.network.l2.s2c.ManagePledgePower;

public class RequestPledgePower extends L2GameClientPacket
{
	private int _rank;
	private int _action;
	private int _privs;
	
	@Override
	protected void readImpl()
	{
		_rank = readD();
		_action = readD();
		if(_action == 2)
		{
			_privs = readD();
		}
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		if(_action == 2)
		{
			if(_rank < 1 || _rank > 9)
			{
				return;
			}
			if(activeChar.getClan() != null && (activeChar.getClanPrivileges() & 16) == 16)
			{
				if(_rank == 9)
				{
					_privs = (_privs & 8) + (_privs & 1024) + (_privs & 32768) + (_privs & 2048) + (_privs & 262144);
				}
				activeChar.getClan().setRankPrivs(_rank, _privs);
				activeChar.getClan().updatePrivsForRank(_rank);
			}
		}
		else if(activeChar.getClan() != null)
		{
			activeChar.sendPacket(new ManagePledgePower(activeChar, _action, _rank));
		}
		else
		{
			activeChar.sendActionFailed();
		}
	}
}