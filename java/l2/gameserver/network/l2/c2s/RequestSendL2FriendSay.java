package l2.gameserver.network.l2.c2s;

import l2.commons.lang.ArrayUtils;
import l2.gameserver.Config;
import l2.gameserver.model.Player;
import l2.gameserver.model.World;
import l2.gameserver.model.chat.ChatFilters;
import l2.gameserver.model.chat.chatfilter.ChatFilter;
import l2.gameserver.network.l2.components.ChatType;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.L2FriendSay;
import l2.gameserver.utils.Log;

public class RequestSendL2FriendSay extends L2GameClientPacket
{
	private String _message;
	private String _reciever;
	
	@Override
	protected void readImpl()
	{
		_message = readS(2048);
		_reciever = readS(16);
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		Player targetPlayer = World.getPlayer(_reciever);
		if(targetPlayer == null)
		{
			activeChar.sendPacket(SystemMsg.THAT_PLAYER_IS_NOT_ONLINE);
			return;
		}
		if(targetPlayer.isBlockAll())
		{
			activeChar.sendPacket(SystemMsg.THAT_PERSON_IS_IN_MESSAGE_REFUSAL_MODE);
			return;
		}
		if(!activeChar.getPlayerAccess().CanAnnounce)
		{
			block5:
			for(ChatFilter f : ChatFilters.getinstance().getFilters())
			{
				if(!f.isMatch(activeChar, ChatType.L2FRIEND, _message, targetPlayer))
					continue;
				switch(f.getAction())
				{
					case 1:
					{
						activeChar.updateNoChannel((long) Integer.parseInt(f.getValue()) * 1000);
						break block5;
					}
					case 2:
					{
						activeChar.sendMessage(new CustomMessage(f.getValue(), activeChar));
						return;
					}
					case 3:
					{
						_message = f.getValue();
						break block5;
					}
					default:
					{
						continue block5;
					}
				}
			}
		}
		if(activeChar.getNoChannel() > 0 && ArrayUtils.contains(Config.BAN_CHANNEL_LIST, ChatType.L2FRIEND))
		{
			if(activeChar.getNoChannelRemained() > 0)
			{
				long timeRemained = activeChar.getNoChannelRemained() / 60000 + 1;
				activeChar.sendMessage(new CustomMessage("common.ChatBanned", activeChar).addNumber(timeRemained));
				return;
			}
			activeChar.updateNoChannel(0);
		}
		if(!activeChar.getFriendList().getList().containsKey(targetPlayer.getObjectId()))
		{
			return;
		}
		if(activeChar.canTalkWith(targetPlayer))
		{
			L2FriendSay frm = new L2FriendSay(activeChar.getName(), _reciever, _message);
			targetPlayer.sendPacket(frm);
			Log.LogChat("FRIENDTELL", activeChar.getName(), _reciever, _message, 0);
		}
	}
}