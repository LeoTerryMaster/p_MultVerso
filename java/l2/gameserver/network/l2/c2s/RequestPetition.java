package l2.gameserver.network.l2.c2s;

import l2.gameserver.instancemanager.PetitionManager;
import l2.gameserver.model.Player;

public final class RequestPetition extends L2GameClientPacket
{
	private String _content;
	private int _type;
	
	@Override
	protected void readImpl()
	{
		_content = readS();
		_type = readD();
	}
	
	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
		{
			return;
		}
		PetitionManager.getInstance().handle(player, _type, _content);
	}
}