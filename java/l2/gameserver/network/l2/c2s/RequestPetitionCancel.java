package l2.gameserver.network.l2.c2s;

import l2.gameserver.Config;
import l2.gameserver.instancemanager.PetitionManager;
import l2.gameserver.model.Player;
import l2.gameserver.network.l2.components.ChatType;
import l2.gameserver.network.l2.s2c.Say2;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.tables.GmListTable;

public final class RequestPetitionCancel extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		if(PetitionManager.getInstance().isPlayerInConsultation(activeChar))
		{
			if(activeChar.isGM())
			{
				PetitionManager.getInstance().endActivePetition(activeChar);
			}
			else
			{
				activeChar.sendPacket(new SystemMessage(407));
			}
		}
		else if(PetitionManager.getInstance().isPlayerPetitionPending(activeChar))
		{
			if(PetitionManager.getInstance().cancelActivePetition(activeChar))
			{
				int numRemaining = Config.MAX_PETITIONS_PER_PLAYER - PetitionManager.getInstance().getPlayerTotalPetitionCount(activeChar);
				activeChar.sendPacket(new SystemMessage(736).addString(String.valueOf(numRemaining)));
				String msgContent = activeChar.getName() + " has canceled a pending petition.";
				GmListTable.broadcastToGMs(new Say2(activeChar.getObjectId(), ChatType.HERO_VOICE, "Petition System", msgContent));
			}
			else
			{
				activeChar.sendPacket(new SystemMessage(393));
			}
		}
		else
		{
			activeChar.sendPacket(new SystemMessage(738));
		}
	}
}