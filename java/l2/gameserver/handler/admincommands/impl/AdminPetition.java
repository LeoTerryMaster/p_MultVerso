package l2.gameserver.handler.admincommands.impl;

import l2.gameserver.handler.admincommands.IAdminCommandHandler;
import l2.gameserver.instancemanager.PetitionManager;
import l2.gameserver.model.GameObject;
import l2.gameserver.model.Player;
import l2.gameserver.network.l2.s2c.SystemMessage;
import org.apache.commons.lang3.math.NumberUtils;

public class AdminPetition implements IAdminCommandHandler
{
	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		if(!activeChar.getPlayerAccess().CanEditChar)
		{
			return false;
		}
		int petitionId = NumberUtils.toInt(wordList.length > 1 ? wordList[1] : "-1", -1);
		Commands command = (Commands) comm;
		switch(command)
		{
			case admin_view_petitions:
			{
				PetitionManager.getInstance().sendPendingPetitionList(activeChar);
				break;
			}
			case admin_view_petition:
			{
				PetitionManager.getInstance().viewPetition(activeChar, petitionId);
				break;
			}
			case admin_accept_petition:
			{
				if(petitionId < 0)
				{
					activeChar.sendMessage("Usage: //accept_petition id");
					return false;
				}
				if(PetitionManager.getInstance().isPlayerInConsultation(activeChar))
				{
					activeChar.sendPacket(new SystemMessage(390));
					return true;
				}
				if(PetitionManager.getInstance().isPetitionInProcess(petitionId))
				{
					activeChar.sendPacket(new SystemMessage(407));
					return true;
				}
				if(PetitionManager.getInstance().acceptPetition(activeChar, petitionId))
					break;
				activeChar.sendPacket(new SystemMessage(388));
				break;
			}
			case admin_reject_petition:
			{
				if(petitionId < 0)
				{
					activeChar.sendMessage("Usage: //accept_petition id");
					return false;
				}
				if(!PetitionManager.getInstance().rejectPetition(activeChar, petitionId))
				{
					activeChar.sendPacket(new SystemMessage(393));
				}
				PetitionManager.getInstance().sendPendingPetitionList(activeChar);
				break;
			}
			case admin_reset_petitions:
			{
				if(PetitionManager.getInstance().isPetitionInProcess())
				{
					activeChar.sendPacket(new SystemMessage(407));
					return false;
				}
				PetitionManager.getInstance().clearPendingPetitions();
				PetitionManager.getInstance().sendPendingPetitionList(activeChar);
				break;
			}
			case admin_force_peti:
			{
				if(fullString.length() < 11)
				{
					activeChar.sendMessage("Usage: //force_peti text");
					return false;
				}
				try
				{
					GameObject targetChar = activeChar.getTarget();
					if(targetChar == null || !(targetChar instanceof Player))
					{
						activeChar.sendPacket(new SystemMessage(109));
						return false;
					}
					Player targetPlayer = (Player) targetChar;
					petitionId = PetitionManager.getInstance().submitPetition(targetPlayer, fullString.substring(10), 9);
					PetitionManager.getInstance().acceptPetition(activeChar, petitionId);
					break;
				}
				catch(StringIndexOutOfBoundsException e)
				{
					activeChar.sendMessage("Usage: //force_peti text");
					return false;
				}
			}
		}
		return true;
	}
	
	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}
	
	private enum Commands
	{
		admin_view_petitions,
		admin_view_petition,
		admin_accept_petition,
		admin_reject_petition,
		admin_reset_petitions,
		admin_force_peti;
	}
}