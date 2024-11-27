package l2.gameserver.network.l2.s2c;

public class CharacterCreateSuccess extends L2GameServerPacket
{
	public static final L2GameServerPacket STATIC = new CharacterCreateSuccess();
	
	@Override
	protected final void writeImpl()
	{
		writeC(25);
		writeD(1);
	}
}