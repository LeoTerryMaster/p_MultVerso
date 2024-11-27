package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Creature;

public class MagicAndSkillList extends L2GameServerPacket
{
	private final int _chaId;
	private final int _unk1;
	private final int _unk2;
	
	public MagicAndSkillList(Creature cha, int unk1, int unk2)
	{
		_chaId = cha.getObjectId();
		_unk1 = unk1;
		_unk2 = unk2;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(64);
		writeD(_chaId);
		writeD(_unk1);
		writeD(_unk2);
	}
}