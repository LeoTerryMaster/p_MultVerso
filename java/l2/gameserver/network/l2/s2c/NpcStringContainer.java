package l2.gameserver.network.l2.s2c;

import l2.gameserver.network.l2.components.NpcString;

public abstract class NpcStringContainer extends L2GameServerPacket
{
	private final NpcString _npcString;
	private final String[] _parameters = new String[5];
	
	protected NpcStringContainer(NpcString npcString, String... arg)
	{
		_npcString = npcString;
		System.arraycopy(arg, 0, _parameters, 0, arg.length);
	}
	
	protected void writeElements()
	{
		for(String st : _parameters)
		{
			writeS(st);
		}
	}
}