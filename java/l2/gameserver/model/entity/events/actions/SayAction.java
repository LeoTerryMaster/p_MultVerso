package l2.gameserver.model.entity.events.actions;

import l2.gameserver.model.Player;
import l2.gameserver.model.entity.events.EventAction;
import l2.gameserver.model.entity.events.GlobalEvent;
import l2.gameserver.network.l2.components.ChatType;
import l2.gameserver.network.l2.components.NpcString;
import l2.gameserver.network.l2.components.SysString;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.Say2;

import java.util.List;

public class SayAction implements EventAction
{
	private final int _range;
	private final ChatType _chatType;
	private String _how;
	private NpcString _text;
	private SysString _sysString;
	private SystemMsg _systemMsg;
	
	protected SayAction(int range, ChatType type)
	{
		_range = range;
		_chatType = type;
	}
	
	public SayAction(int range, ChatType type, SysString sysString, SystemMsg systemMsg)
	{
		this(range, type);
		_sysString = sysString;
		_systemMsg = systemMsg;
	}
	
	public SayAction(int range, ChatType type, String how, NpcString string)
	{
		this(range, type);
		_text = string;
		_how = how;
	}
	
	@Override
	public void call(GlobalEvent event)
	{
		List<Player> players = event.broadcastPlayers(_range);
		for(Player player : players)
		{
			packet(player);
		}
	}
	
	private void packet(Player player)
	{
		if(player == null)
		{
			return;
		}
		Say2 packet = _sysString != null ? new Say2(0, _chatType, _sysString, _systemMsg) : new Say2(0, _chatType, _how, _text);
		player.sendPacket(packet);
	}
}