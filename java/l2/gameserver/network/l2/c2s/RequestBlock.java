package l2.gameserver.network.l2.c2s;

import l2.gameserver.cache.Msg;
import l2.gameserver.model.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class RequestBlock extends L2GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(RequestBlock.class);
	private static final int BLOCK = 0;
	private static final int UNBLOCK = 1;
	private static final int BLOCKLIST = 2;
	private static final int ALLBLOCK = 3;
	private static final int ALLUNBLOCK = 4;
	private Integer _type;
	private String targetName;
	
	@Override
	protected void readImpl()
	{
		_type = readD();
		if(_type == 0 || _type == 1)
		{
			targetName = readS(16);
		}
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		switch(_type)
		{
			case 0:
			{
				activeChar.addToBlockList(targetName);
				break;
			}
			case 1:
			{
				activeChar.removeFromBlockList(targetName);
				break;
			}
			case 2:
			{
				Collection<String> blockList = activeChar.getBlockList();
				if(blockList == null)
					break;
				activeChar.sendPacket(Msg._IGNORE_LIST_);
				for(String name : blockList)
				{
					activeChar.sendMessage(name);
				}
				activeChar.sendPacket(Msg.__EQUALS__);
				break;
			}
			case 3:
			{
				activeChar.setBlockAll(true);
				activeChar.sendPacket(Msg.YOU_ARE_NOW_BLOCKING_EVERYTHING);
				activeChar.sendEtcStatusUpdate();
				break;
			}
			case 4:
			{
				activeChar.setBlockAll(false);
				activeChar.sendPacket(Msg.YOU_ARE_NO_LONGER_BLOCKING_EVERYTHING);
				activeChar.sendEtcStatusUpdate();
				break;
			}
			default:
			{
				_log.info("Unknown 0x0a block type: " + _type);
			}
		}
	}
}