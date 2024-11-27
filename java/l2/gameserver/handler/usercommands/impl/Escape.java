package l2.gameserver.handler.usercommands.impl;

import l2.gameserver.handler.usercommands.IUserCommandHandler;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.base.TeamType;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.scripts.Scripts;
import l2.gameserver.tables.SkillTable;

public class Escape implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS = {52};
	
	@Override
	public boolean useUserCommand(int id, Player activeChar)
	{
		if(id != COMMAND_IDS[0])
		{
			return false;
		}
		if(activeChar.isMovementDisabled() || activeChar.isOlyParticipant())
		{
			return false;
		}
		if(activeChar.getTeleMode() != 0 || !activeChar.getPlayerAccess().UseTeleport || isEventParticipant(activeChar))
		{
			activeChar.sendMessage(new CustomMessage("common.TryLater", activeChar));
			return false;
		}
		if(activeChar.isInDuel() || activeChar.getTeam() != TeamType.NONE)
		{
			activeChar.sendMessage(new CustomMessage("common.RecallInDuel", activeChar));
			return false;
		}
		activeChar.abortAttack(true, true);
		activeChar.abortCast(true, true);
		activeChar.stopMove();
		Skill skill = activeChar.getPlayerAccess().FastUnstuck ? SkillTable.getInstance().getInfo(1050, 2) : SkillTable.getInstance().getInfo(2099, 1);
		if(skill != null && skill.checkCondition(activeChar, activeChar, false, false, true))
		{
			activeChar.getAI().Cast(skill, activeChar, false, true);
		}
		return true;
	}
	
	private boolean isEventParticipant(Player player)
	{
		return ((Boolean) Scripts.getInstance().callScripts(player, "events.TvT2.PvPEvent", "isEventPartisipant")).booleanValue();
	}
	
	@Override
	public final int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}