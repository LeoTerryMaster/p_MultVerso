package l2.gameserver.network.l2.c2s;

import l2.gameserver.model.Party;
import l2.gameserver.model.Player;

public class AnswerPartyLootModification extends L2GameClientPacket
{
	public int _answer;
	
	@Override
	protected void readImpl()
	{
		_answer = readD();
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		Party party = activeChar.getParty();
		if(party != null)
		{
			party.answerLootChangeRequest(activeChar, _answer == 1);
		}
	}
}