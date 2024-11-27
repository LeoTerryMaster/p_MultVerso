package handler.admincommands;

import l2.gameserver.model.Creature;
import l2.gameserver.model.GameObject;
import l2.gameserver.model.Player;
import l2.gameserver.model.base.TeamType;
import l2.gameserver.network.l2.components.SystemMsg;

public class AdminTeam extends ScriptAdminCommand
{
	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		TeamType team = TeamType.NONE;
		if(wordList.length >= 2)
		{
			for(TeamType t : TeamType.values())
			{
				if(!wordList[1].equalsIgnoreCase(t.name()))
					continue;
				team = t;
			}
		}
		GameObject object = activeChar.getTarget();
		if(object == null || !object.isCreature())
		{
			activeChar.sendPacket(SystemMsg.INVALID_TARGET);
			return false;
		}
		((Creature) object).setTeam(team);
		return true;
	}
	
	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}
	
	enum Commands
	{
		admin_setteam;
	}
}