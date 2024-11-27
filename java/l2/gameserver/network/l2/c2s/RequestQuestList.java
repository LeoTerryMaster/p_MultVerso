package l2.gameserver.network.l2.c2s;

import l2.gameserver.network.l2.s2c.QuestList;

public class RequestQuestList extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		sendPacket(new QuestList(getClient().getActiveChar()));
	}
}