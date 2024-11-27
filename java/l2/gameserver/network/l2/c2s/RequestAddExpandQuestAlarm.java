package l2.gameserver.network.l2.c2s;

import l2.gameserver.instancemanager.QuestManager;
import l2.gameserver.model.Player;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.network.l2.s2c.ExQuestNpcLogList;

public class RequestAddExpandQuestAlarm extends L2GameClientPacket
{
	private int _questId;
	
	@Override
	protected void readImpl() throws Exception
	{
		_questId = readD();
	}
	
	@Override
	protected void runImpl() throws Exception
	{
		Player player = getClient().getActiveChar();
		if(player == null)
		{
			return;
		}
		Quest quest = QuestManager.getQuest(_questId);
		if(quest == null)
		{
			return;
		}
		QuestState state = player.getQuestState(quest.getClass());
		if(state == null)
		{
			return;
		}
		player.sendPacket(new ExQuestNpcLogList(state));
	}
}