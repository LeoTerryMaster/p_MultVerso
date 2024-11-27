package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Player;

public class PartySmallWindowAdd extends L2GameServerPacket
{
	private final PartySmallWindowAll.PartySmallWindowMemberInfo member;
	private final int objectId;
	
	public PartySmallWindowAdd(Player player, Player member)
	{
		objectId = player.getObjectId();
		this.member = new PartySmallWindowAll.PartySmallWindowMemberInfo(member);
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(79);
		writeD(objectId);
		writeD(0);
		writeD(member._id);
		writeS(member._name);
		writeD(member.curCp);
		writeD(member.maxCp);
		writeD(member.curHp);
		writeD(member.maxHp);
		writeD(member.curMp);
		writeD(member.maxMp);
		writeD(member.level);
		writeD(member.class_id);
		writeD(0);
		writeD(member.race_id);
	}
}