package l2.gameserver.network.l2.c2s;

import l2.gameserver.Config;
import l2.gameserver.handler.bbs.CommunityBoardManager;
import l2.gameserver.handler.bbs.ICommunityBoardHandler;
import l2.gameserver.model.Player;
import l2.gameserver.network.l2.s2c.SystemMessage;

public class RequestShowBoard extends L2GameClientPacket
{
	private int _unknown;
	
	@Override
	public void readImpl()
	{
		_unknown = readD();
	}
	
	@Override
	public void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		if(Config.COMMUNITYBOARD_ENABLED)
		{
			ICommunityBoardHandler handler = CommunityBoardManager.getInstance().getCommunityHandler(Config.BBS_DEFAULT);
			if(handler != null)
			{
				handler.onBypassCommand(activeChar, Config.BBS_DEFAULT);
			}
		}
		else
		{
			activeChar.sendPacket(new SystemMessage(938));
		}
	}
}