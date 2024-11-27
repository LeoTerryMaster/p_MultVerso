package l2.gameserver.network.l2.c2s;

import l2.gameserver.instancemanager.QuestManager;
import l2.gameserver.model.Player;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;

public class RequestQuestAbort extends L2GameClientPacket
{
	private int _questID;
	
	@Override
	protected void readImpl()
	{
		_questID = readD();
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		Quest quest = QuestManager.getQuest(_questID);
		if(activeChar == null || quest == null)
		{
			return;
		}
		if(!quest.canAbortByPacket())
		{
			return;
		}
		QuestState qs = activeChar.getQuestState(quest.getClass());
		if(qs != null && !qs.isCompleted())
		{
			qs.abortQuest();
		}
	}
}