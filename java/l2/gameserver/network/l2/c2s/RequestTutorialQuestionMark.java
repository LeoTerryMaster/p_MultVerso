package l2.gameserver.network.l2.c2s;

import l2.gameserver.instancemanager.QuestManager;
import l2.gameserver.model.Player;
import l2.gameserver.model.quest.Quest;

public class RequestTutorialQuestionMark extends L2GameClientPacket
{
	int _number;
	
	@Override
	protected void readImpl()
	{
		_number = readD();
	}
	
	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
		{
			return;
		}
		Quest q = QuestManager.getQuest(255);
		if(q != null)
		{
			player.processQuestEvent(q.getName(), "QM" + _number, null);
		}
	}
}