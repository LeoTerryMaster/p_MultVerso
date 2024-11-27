package l2.gameserver.templates.moveroute;

import l2.gameserver.network.l2.components.ChatType;
import l2.gameserver.utils.Location;

public class MoveNode extends Location
{
	private static final long serialVersionUID = 8291528118019681063L;
	private final String _msgAddr;
	private final ChatType _chatType;
	private final long _delay;
	private final int _socialId;
	
	public MoveNode(int x, int y, int z, String msgAddr, int socialId, long delay, ChatType chatType)
	{
		super(x, y, z);
		_msgAddr = msgAddr;
		_socialId = socialId;
		_delay = delay;
		_chatType = chatType;
	}
	
	public String getNpcMsgAddress()
	{
		return _msgAddr;
	}
	
	public long getDelay()
	{
		return _delay;
	}
	
	public int getSocialId()
	{
		return _socialId;
	}
	
	public ChatType getChatType()
	{
		return _chatType;
	}
}