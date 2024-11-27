package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Playable;
import l2.gameserver.model.Player;

public class RelationChanged extends L2GameServerPacket
{
	public static final int RELATION_PARTY1 = 1;
	public static final int RELATION_PARTY2 = 2;
	public static final int RELATION_PARTY3 = 4;
	public static final int RELATION_PARTY4 = 8;
	public static final int RELATION_PARTYLEADER = 16;
	public static final int RELATION_HAS_PARTY = 32;
	public static final int RELATION_CLAN_MEMBER = 64;
	public static final int RELATION_LEADER = 128;
	public static final int RELATION_INSIEGE = 512;
	public static final int RELATION_ATTACKER = 1024;
	public static final int RELATION_ALLY = 2048;
	public static final int RELATION_ENEMY = 4096;
	public static final int RELATION_MUTUAL_WAR = 32768;
	public static final int RELATION_1SIDED_WAR = 65536;
	private final int _charObjId;
	private final boolean _isAutoAttackable;
	private final int _relation;
	private final int _karma;
	private final int _pvpFlag;
	
	protected RelationChanged(Playable cha, boolean isAutoAttackable, int relation)
	{
		_isAutoAttackable = isAutoAttackable;
		_relation = relation;
		_charObjId = cha.getObjectId();
		_karma = cha.getKarma();
		_pvpFlag = cha.getPvpFlag();
	}
	
	public static L2GameServerPacket create(Player sendTo, Playable targetPlayable, Player activeChar)
	{
		if(sendTo == null || targetPlayable == null || activeChar == null)
		{
			return null;
		}
		Player targetPlayer = targetPlayable.getPlayer();
		int relation = targetPlayer == null ? 0 : targetPlayer.getRelation(activeChar);
		return new RelationChanged(targetPlayable, targetPlayable.isAutoAttackable(activeChar), relation);
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(206);
		writeD(_charObjId);
		writeD(_relation);
		writeD(_isAutoAttackable);
		writeD(_karma);
		writeD(_pvpFlag);
	}
}