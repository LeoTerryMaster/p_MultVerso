package l2.gameserver.network.l2.c2s;

import l2.gameserver.network.l2.s2c.CharacterSelectionInfo;

public class GotoLobby extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		CharacterSelectionInfo cl = new CharacterSelectionInfo(getClient().getLogin(), getClient().getSessionKey().playOkID1);
		sendPacket(cl);
	}
}