package l2.gameserver.network.l2.c2s;

public class RequestChangeBookMarkSlot extends L2GameClientPacket
{
	private int slot_old;
	private int slot_new;
	
	@Override
	protected void readImpl()
	{
		slot_old = readD();
		slot_new = readD();
	}
	
	@Override
	protected void runImpl()
	{
	}
}