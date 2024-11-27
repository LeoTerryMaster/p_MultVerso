package l2.gameserver.model.chat.chatfilter;

import l2.gameserver.network.l2.components.ChatType;

public class ChatMsg
{
	public final ChatType chatType;
	public final int recipient;
	public final int msgHashcode;
	public final int time;
	
	public ChatMsg(ChatType chatType, int recipient, int msgHashcode, int time)
	{
		this.chatType = chatType;
		this.recipient = recipient;
		this.msgHashcode = msgHashcode;
		this.time = time;
	}
}