package l2.gameserver.network.l2.s2c;

public class ShowTownMap extends L2GameServerPacket
{
	String _texture;
	int _x;
	int _y;
	
	public ShowTownMap(String texture, int x, int y)
	{
		_texture = texture;
		_x = x;
		_y = y;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(222);
		writeS(_texture);
		writeD(_x);
		writeD(_y);
	}
}