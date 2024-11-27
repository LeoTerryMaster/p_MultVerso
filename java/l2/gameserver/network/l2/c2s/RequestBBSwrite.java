package l2.gameserver.network.l2.c2s;

import l2.gameserver.Config;
import l2.gameserver.handler.bbs.CommunityBoardManager;
import l2.gameserver.handler.bbs.ICommunityBoardHandler;
import l2.gameserver.model.Player;
import l2.gameserver.network.l2.s2c.SystemMessage;

public class RequestBBSwrite extends L2GameClientPacket
{
	private String _url;
	private String _arg1;
	private String _arg2;
	private String _arg3;
	private String _arg4;
	private String _arg5;
	
	@Override
	public void readImpl()
	{
		_url = readS();
		_arg1 = readS();
		_arg2 = readS();
		_arg3 = readS();
		_arg4 = readS();
		_arg5 = readS();
	}
	
	@Override
	public void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		ICommunityBoardHandler handler = CommunityBoardManager.getInstance().getCommunityHandler(_url);
		if(handler != null)
		{
			if(!Config.COMMUNITYBOARD_ENABLED)
			{
				activeChar.sendPacket(new SystemMessage(938));
			}
			else
			{
				handler.onWriteCommand(activeChar, _url, _arg1, _arg2, _arg3, _arg4, _arg5);
			}
		}
	}
}