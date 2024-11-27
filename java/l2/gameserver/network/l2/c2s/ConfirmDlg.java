package l2.gameserver.network.l2.c2s;

import l2.gameserver.listener.actor.player.OnAnswerListener;
import l2.gameserver.model.Player;
import org.apache.commons.lang3.tuple.Pair;

public class ConfirmDlg extends L2GameClientPacket
{
	private int _answer;
	private int _requestId;
	
	@Override
	protected void readImpl()
	{
		readD();
		_answer = readD();
		_requestId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		Pair<Integer, OnAnswerListener> entry = activeChar.getAskListener(true);
		if(entry == null || entry.getKey() != _requestId)
		{
			return;
		}
		OnAnswerListener listener = entry.getValue();
		if(_answer == 1)
		{
			listener.sayYes();
		}
		else
		{
			listener.sayNo();
		}
	}
}