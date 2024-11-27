package l2.gameserver.network.l2.c2s;

import l2.gameserver.instancemanager.QuestManager;
import l2.gameserver.model.Player;
import l2.gameserver.model.quest.Quest;

public class RequestTutorialClientEvent extends L2GameClientPacket
{
	int event;
	
	@Override
	protected void readImpl()
	{
		event = readD();
	}
	
	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
		{
			return;
		}
		Quest tutorial = QuestManager.getQuest(255);
		if(tutorial != null)
		{
			player.processQuestEvent(tutorial.getName(), "CE" + event, null);
		}
	}
}