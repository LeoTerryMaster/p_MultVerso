package l2.gameserver.network.l2.s2c;

import l2.gameserver.data.xml.holder.NpcHolder;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.base.TeamType;
import l2.gameserver.templates.npc.NpcTemplate;

public class NpcInfoPoly extends L2GameServerPacket
{
	private final Creature _obj;
	private final int _x;
	private final int _y;
	private final int _z;
	private final int _heading;
	private final int _npcId;
	private final boolean _isSummoned;
	private final boolean _isRunning;
	private final boolean _isInCombat;
	private final boolean _isAlikeDead;
	private final int _mAtkSpd;
	private final int _pAtkSpd;
	private final int _runSpd;
	private final int _walkSpd;
	private final int _swimRunSpd;
	private final int _swimWalkSpd;
	private final int _flRunSpd;
	private final int _flWalkSpd;
	private final int _flyRunSpd;
	private final int _flyWalkSpd;
	private final String _name;
	private final String _title;
	private final int _abnormalEffect;
	private final int _abnormalEffect2;
	private final double colRadius;
	private final double colHeight;
	private final TeamType _team;
	private int _rhand;
	private int _lhand;
	
	public NpcInfoPoly(Player cha)
	{
		_obj = cha;
		_npcId = cha.getPolyId();
		NpcTemplate template = NpcHolder.getInstance().getTemplate(_npcId);
		_rhand = 0;
		_lhand = 0;
		_isSummoned = false;
		colRadius = template.collisionRadius;
		colHeight = template.collisionHeight;
		_x = _obj.getX();
		_y = _obj.getY();
		_z = _obj.getZ();
		_rhand = template.rhand;
		_lhand = template.lhand;
		_heading = cha.getHeading();
		_mAtkSpd = cha.getMAtkSpd();
		_pAtkSpd = cha.getPAtkSpd();
		_runSpd = cha.getRunSpeed();
		_walkSpd = cha.getWalkSpeed();
		_flRunSpd = _flyRunSpd = _runSpd;
		_swimRunSpd = _flyRunSpd;
		_flWalkSpd = _flyWalkSpd = _walkSpd;
		_swimWalkSpd = _flyWalkSpd;
		_isRunning = cha.isRunning();
		_isInCombat = cha.isInCombat();
		_isAlikeDead = cha.isAlikeDead();
		_name = cha.getName();
		_title = cha.getTitle();
		_abnormalEffect = cha.getAbnormalEffect();
		_abnormalEffect2 = cha.getAbnormalEffect2();
		_team = cha.getTeam();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(22);
		writeD(_obj.getObjectId());
		writeD(_npcId + 1000000);
		writeD(0);
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeD(_heading);
		writeD(0);
		writeD(_mAtkSpd);
		writeD(_pAtkSpd);
		writeD(_runSpd);
		writeD(_walkSpd);
		writeD(_swimRunSpd);
		writeD(_swimWalkSpd);
		writeD(_flRunSpd);
		writeD(_flWalkSpd);
		writeD(_flyRunSpd);
		writeD(_flyWalkSpd);
		writeF(1.0);
		writeF(1.0);
		writeF(colRadius);
		writeF(colHeight);
		writeD(_rhand);
		writeD(0);
		writeD(_lhand);
		writeC(1);
		writeC(_isRunning ? 1 : 0);
		writeC(_isInCombat ? 1 : 0);
		writeC(_isAlikeDead ? 1 : 0);
		writeC(_isSummoned ? 2 : 0);
		writeS(_name);
		writeS(_title);
		writeD(0);
		writeD(0);
		writeD(0);
		writeD(_abnormalEffect);
		writeD(0);
		writeD(0);
		writeD(0);
		writeD(0);
		writeC(0);
		writeC(_team.ordinal());
		writeF(colRadius);
		writeF(colHeight);
		writeD(0);
		writeD(0);
	}
}