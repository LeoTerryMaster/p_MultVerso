package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Creature;

public class NickNameChanged extends L2GameServerPacket
{
	private final int objectId;
	private final String title;
	
	public NickNameChanged(Creature cha)
	{
		objectId = cha.getObjectId();
		title = cha.getTitle();
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(204);
		writeD(objectId);
		writeS(title);
	}
}