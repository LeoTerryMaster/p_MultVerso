package l2.gameserver.network.l2.s2c;

public class PetDelete extends L2GameServerPacket
{
	private final int _petId;
	private final int _petnum;
	
	public PetDelete(int petId, int petnum)
	{
		_petId = petId;
		_petnum = petnum;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(182);
		writeD(_petId);
		writeD(_petnum);
	}
}