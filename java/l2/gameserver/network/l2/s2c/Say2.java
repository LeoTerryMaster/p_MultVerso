package l2.gameserver.network.l2.s2c;

import l2.gameserver.network.l2.components.ChatType;
import l2.gameserver.network.l2.components.NpcString;
import l2.gameserver.network.l2.components.SysString;
import l2.gameserver.network.l2.components.SystemMsg;

public class Say2 extends NpcStringContainer
{
	private final ChatType _type;
	private final int _objectId;
	private SysString _sysString;
	private SystemMsg _systemMsg;
	private String _charName;
	
	public Say2(int objectId, ChatType type, SysString st, SystemMsg sm)
	{
		super(NpcString.NONE);
		_objectId = objectId;
		_type = type;
		_sysString = st;
		_systemMsg = sm;
	}
	
	public Say2(int objectId, ChatType type, String charName, String text)
	{
		this(objectId, type, charName, NpcString.NONE, text);
	}
	
	public Say2(int objectId, ChatType type, String charName, NpcString npcString, String... params)
	{
		super(npcString, params);
		_objectId = objectId;
		_type = type;
		_charName = charName;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(74);
		writeD(_objectId);
		writeD(_type.ordinal());
		switch(_type)
		{
			case SYSTEM_MESSAGE:
			{
				writeD(_sysString.getId());
				writeD(_systemMsg.getId());
				break;
			}
			default:
			{
				writeS(_charName);
				writeElements();
			}
		}
	}
}