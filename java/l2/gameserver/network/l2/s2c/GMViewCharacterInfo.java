package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Player;
import l2.gameserver.model.base.Element;
import l2.gameserver.model.items.Inventory;
import l2.gameserver.model.pledge.Alliance;
import l2.gameserver.model.pledge.Clan;
import l2.gameserver.utils.Location;

public class GMViewCharacterInfo extends L2GameServerPacket
{
	private final Location _loc;
	private final int[][] _inv;
	private final int obj_id;
	private final int _race;
	private final int _sex;
	private final int class_id;
	private final int pvp_flag;
	private final int karma;
	private final int level;
	private final int mount_type;
	private final int _str;
	private final int _con;
	private final int _dex;
	private final int _int;
	private final int _wit;
	private final int _men;
	private final int _sp;
	private final int curHp;
	private final int maxHp;
	private final int curMp;
	private final int maxMp;
	private final int curCp;
	private final int maxCp;
	private final int curLoad;
	private final int maxLoad;
	private final int rec_left;
	private final int rec_have;
	private final int _patk;
	private final int _patkspd;
	private final int _pdef;
	private final int evasion;
	private final int accuracy;
	private final int crit;
	private final int _matk;
	private final int _matkspd;
	private final int _mdef;
	private final int hair_style;
	private final int hair_color;
	private final int face;
	private final int gm_commands;
	private final int clan_id;
	private final int clan_crest_id;
	private final int ally_id;
	private final int title_color;
	private final int noble;
	private final int hero;
	private final int private_store;
	private final int name_color;
	private final int pk_kills;
	private final int pvp_kills;
	private final int _runSpd;
	private final int _walkSpd;
	private final int _swimSpd;
	private final int DwarvenCraftLevel;
	private final int running;
	private final int pledge_class;
	private final String _name;
	private final String title;
	private final long _exp;
	private final double move_speed;
	private final double attack_speed;
	private final double col_radius;
	private final double col_height;
	private final Element attackElement;
	
	public GMViewCharacterInfo(Player cha)
	{
		_loc = cha.getLoc();
		obj_id = cha.getObjectId();
		_name = cha.getName();
		_race = cha.getRace().ordinal();
		_sex = cha.getSex();
		class_id = cha.getClassId().getId();
		level = cha.getLevel();
		_exp = cha.getExp();
		_str = cha.getSTR();
		_dex = cha.getDEX();
		_con = cha.getCON();
		_int = cha.getINT();
		_wit = cha.getWIT();
		_men = cha.getMEN();
		curHp = (int) cha.getCurrentHp();
		maxHp = cha.getMaxHp();
		curMp = (int) cha.getCurrentMp();
		maxMp = cha.getMaxMp();
		_sp = cha.getIntSp();
		curLoad = cha.getCurrentLoad();
		maxLoad = cha.getMaxLoad();
		_patk = cha.getPAtk(null);
		_patkspd = cha.getPAtkSpd();
		_pdef = cha.getPDef(null);
		evasion = cha.getEvasionRate(null);
		accuracy = cha.getAccuracy();
		crit = cha.getCriticalHit(null, null);
		_matk = cha.getMAtk(null, null);
		_matkspd = cha.getMAtkSpd();
		_mdef = cha.getMDef(null, null);
		pvp_flag = cha.getPvpFlag();
		karma = cha.getKarma();
		_runSpd = cha.getRunSpeed();
		_walkSpd = cha.getWalkSpeed();
		_swimSpd = cha.getSwimSpeed();
		move_speed = cha.getMovementSpeedMultiplier();
		attack_speed = cha.getAttackSpeedMultiplier();
		mount_type = cha.getMountType();
		col_radius = cha.getColRadius();
		col_height = cha.getColHeight();
		hair_style = cha.getHairStyle();
		hair_color = cha.getHairColor();
		face = cha.getFace();
		gm_commands = cha.isGM() ? 1 : 0;
		title = cha.getTitle();
		Clan clan = cha.getClan();
		Alliance alliance = clan == null ? null : clan.getAlliance();
		clan_id = clan == null ? 0 : clan.getClanId();
		clan_crest_id = clan == null ? 0 : clan.getCrestId();
		ally_id = alliance == null ? 0 : alliance.getAllyId();
		private_store = cha.isInObserverMode() ? 7 : cha.getPrivateStoreType();
		DwarvenCraftLevel = Math.max(cha.getSkillLevel(1320), 0);
		pk_kills = cha.getPkKills();
		pvp_kills = cha.getPvpKills();
		rec_left = cha.getGivableRec();
		rec_have = cha.getReceivedRec();
		curCp = (int) cha.getCurrentCp();
		maxCp = cha.getMaxCp();
		running = cha.isRunning() ? 1 : 0;
		pledge_class = cha.getPledgeClass();
		noble = cha.isNoble() ? 1 : 0;
		hero = cha.isHero() ? 1 : 0;
		name_color = cha.getNameColor();
		title_color = cha.getTitleColor();
		attackElement = cha.getAttackElement();
		_inv = new int[17][3];
		for(int PAPERDOLL_ID : Inventory.PAPERDOLL_ORDER)
		{
			_inv[PAPERDOLL_ID][0] = cha.getInventory().getPaperdollObjectId(PAPERDOLL_ID);
			_inv[PAPERDOLL_ID][1] = cha.getInventory().getPaperdollItemId(PAPERDOLL_ID);
			_inv[PAPERDOLL_ID][2] = cha.getInventory().getPaperdollAugmentationId(PAPERDOLL_ID);
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(143);
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z);
		writeD(_loc.h);
		writeD(obj_id);
		writeS(_name);
		writeD(_race);
		writeD(_sex);
		writeD(class_id);
		writeD(level);
		writeQ(_exp);
		writeD(_str);
		writeD(_dex);
		writeD(_con);
		writeD(_int);
		writeD(_wit);
		writeD(_men);
		writeD(maxHp);
		writeD(curHp);
		writeD(maxMp);
		writeD(curMp);
		writeD(_sp);
		writeD(curLoad);
		writeD(maxLoad);
		writeD(pk_kills);
		for(int PAPERDOLL_ID : Inventory.PAPERDOLL_ORDER)
		{
			writeD(_inv[PAPERDOLL_ID][0]);
		}
		for(int PAPERDOLL_ID : Inventory.PAPERDOLL_ORDER)
		{
			writeD(_inv[PAPERDOLL_ID][1]);
		}
		for(int PAPERDOLL_ID : Inventory.PAPERDOLL_ORDER)
		{
			writeD(_inv[PAPERDOLL_ID][2]);
		}
		writeD(_patk);
		writeD(_patkspd);
		writeD(_pdef);
		writeD(evasion);
		writeD(accuracy);
		writeD(crit);
		writeD(_matk);
		writeD(_matkspd);
		writeD(_patkspd);
		writeD(_mdef);
		writeD(pvp_flag);
		writeD(karma);
		writeD(_runSpd);
		writeD(_walkSpd);
		writeD(_swimSpd);
		writeD(_swimSpd);
		writeD(_runSpd);
		writeD(_walkSpd);
		writeD(_runSpd);
		writeD(_walkSpd);
		writeF(move_speed);
		writeF(attack_speed);
		writeF(col_radius);
		writeF(col_height);
		writeD(hair_style);
		writeD(hair_color);
		writeD(face);
		writeD(gm_commands);
		writeS(title);
		writeD(clan_id);
		writeD(clan_crest_id);
		writeD(ally_id);
		writeC(mount_type);
		writeC(private_store);
		writeC(DwarvenCraftLevel);
		writeD(pk_kills);
		writeD(pvp_kills);
		writeH(rec_left);
		writeH(rec_have);
		writeD(class_id);
		writeD(0);
		writeD(maxCp);
		writeD(curCp);
		writeC(running);
		writeC(321);
		writeD(pledge_class);
		writeC(noble);
		writeC(hero);
		writeD(name_color);
		writeD(title_color);
	}
}