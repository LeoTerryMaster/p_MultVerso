package l2.gameserver.network.l2.s2c;

public class Snoop extends L2GameServerPacket
{
	private final int _convoID;
	private final String _name;
	private final int _type;
	private final int _fStringId;
	private final String _speaker;
	private final String[] _params;
	
	public Snoop(int id, String name, int type, String speaker, String msg, int fStringId, String... params)
	{
		_convoID = id;
		_name = name;
		_type = type;
		_speaker = speaker;
		_fStringId = fStringId;
		_params = params;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(213);
		writeD(_convoID);
		writeS(_name);
		writeD(0);
		writeD(_type);
		writeS(_speaker);
		for(String param : _params)
		{
			writeS(param);
		}
	}
}