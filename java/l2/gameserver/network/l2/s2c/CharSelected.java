package l2.gameserver.network.l2.s2c;

import l2.gameserver.GameTimeController;
import l2.gameserver.model.Player;
import l2.gameserver.utils.Location;

public class CharSelected extends L2GameServerPacket
{
	private final int _sessionId;
	private final int char_id;
	private final int clan_id;
	private final int sex;
	private final int race;
	private final int class_id;
	private final String _name;
	private final String _title;
	private final Location _loc;
	private final double curHp;
	private final double curMp;
	private final int _sp;
	private final int level;
	private final int karma;
	private final int _int;
	private final int _str;
	private final int _con;
	private final int _men;
	private final int _dex;
	private final int _wit;
	private final int _pk;
	private final long _exp;
	
	public CharSelected(Player cha, int sessionId)
	{
		_sessionId = sessionId;
		_name = cha.getName();
		char_id = cha.getObjectId();
		_title = cha.getTitle();
		clan_id = cha.getClanId();
		sex = cha.getSex();
		race = cha.getRace().ordinal();
		class_id = cha.getClassId().getId();
		_loc = cha.getLoc();
		curHp = cha.getCurrentHp();
		curMp = cha.getCurrentMp();
		_sp = cha.getIntSp();
		_exp = cha.getExp();
		level = cha.getLevel();
		karma = cha.getKarma();
		_pk = cha.getPkKills();
		_int = cha.getINT();
		_str = cha.getSTR();
		_con = cha.getCON();
		_men = cha.getMEN();
		_dex = cha.getDEX();
		_wit = cha.getWIT();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(21);
		writeS(_name);
		writeD(char_id);
		writeS(_title);
		writeD(_sessionId);
		writeD(clan_id);
		writeD(0);
		writeD(sex);
		writeD(race);
		writeD(class_id);
		writeD(1);
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z);
		writeF(curHp);
		writeF(curMp);
		writeD(_sp);
		writeQ(_exp);
		writeD(level);
		writeD(karma);
		writeD(_pk);
		writeD(_int);
		writeD(_str);
		writeD(_con);
		writeD(_men);
		writeD(_dex);
		writeD(_wit);
		for(int i = 0;i < 30;++i)
		{
			writeD(0);
		}
		writeF(0.0);
		writeF(0.0);
		writeD(GameTimeController.getInstance().getGameTime());
		writeD(0);
		writeD(0);
		writeC(0);
		writeH(0);
		writeH(0);
		writeD(0);
	}
}