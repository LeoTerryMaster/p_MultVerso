package l2.gameserver.network.l2.c2s;

public class PetitionVote extends L2GameClientPacket
{
	private int _type;
	private int _unk1;
	private String _petitionText;
	
	@Override
	protected void runImpl()
	{
	}
	
	@Override
	protected void readImpl()
	{
		_type = readD();
		_unk1 = readD();
		_petitionText = readS(4096);
	}
}