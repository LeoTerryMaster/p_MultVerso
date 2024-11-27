package l2.gameserver.network.l2.c2s;

import l2.gameserver.instancemanager.QuestManager;
import l2.gameserver.model.Player;
import l2.gameserver.model.quest.Quest;

public class RequestTutorialPassCmdToServer extends L2GameClientPacket
{
	String _bypass;
	
	@Override
	protected void readImpl()
	{
		_bypass = readS();
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
			player.processQuestEvent(tutorial.getName(), _bypass, null);
		}
	}
}