package l2.gameserver.network.l2.c2s;

import l2.gameserver.model.Party;
import l2.gameserver.model.Player;
import l2.gameserver.network.l2.s2c.ExMPCCShowPartyMemberInfo;

public class RequestExMPCCShowPartyMembersInfo extends L2GameClientPacket
{
	private int _objectId;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null || !activeChar.isInParty() || !activeChar.getParty().isInCommandChannel())
		{
			return;
		}
		for(Party party : activeChar.getParty().getCommandChannel().getParties())
		{
			Player leader = party.getPartyLeader();
			if(leader == null || leader.getObjectId() != _objectId)
				continue;
			activeChar.sendPacket(new ExMPCCShowPartyMemberInfo(party));
			break;
		}
	}
}