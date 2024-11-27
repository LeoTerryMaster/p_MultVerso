package l2.gameserver.network.l2.s2c;

import l2.gameserver.Config;
import l2.gameserver.data.xml.holder.NpcHolder;
import l2.gameserver.instancemanager.CursedWeaponsManager;
import l2.gameserver.model.Player;
import l2.gameserver.model.base.TeamType;
import l2.gameserver.model.entity.events.GlobalEvent;
import l2.gameserver.model.items.Inventory;
import l2.gameserver.model.matching.MatchingRoom;
import l2.gameserver.model.pledge.Alliance;
import l2.gameserver.model.pledge.Clan;
import l2.gameserver.skills.effects.EffectCubic;
import l2.gameserver.utils.Location;

public class UserInfo extends L2GameServerPacket
{
	private final boolean can_writeImpl;
	private final boolean partyRoom;
	private final int _runSpd;
	private final int _walkSpd;
	private final int _swimRunSpd;
	private final int _swimWalkSpd;
	private final int _flRunSpd;
	private final int _flWalkSpd;
	private final int _flyRunSpd;
	private final int _flyWalkSpd;
	private final double move_speed;
	private final double attack_speed;
	private final double col_radius;
	private final double col_height;
	private final int[][] _inv;
	private final Location _loc;
	private final Location _fishLoc;
	private final int obj_id;
	private final int vehicle_obj_id;
	private final int _race;
	private final int sex;
	private final int base_class;
	private final int level;
	private final int curCp;
	private final int maxCp;
	private final int _enchant;
	private final int _pAtkRange;
	private final long _exp;
	private final int curHp;
	private final int maxHp;
	private final int curMp;
	private final int maxMp;
	private final int curLoad;
	private final int maxLoad;
	private final int rec_left;
	private final int rec_have;
	private final int _str;
	private final int _con;
	private final int _dex;
	private final int _int;
	private final int _wit;
	private final int _men;
	private final int _sp;
	private final int ClanPrivs;
	private final int InventoryLimit;
	private final int _patk;
	private final int _patkspd;
	private final int _pdef;
	private final int evasion;
	private final int accuracy;
	private final int crit;
	private final int _matk;
	private final int _matkspd;
	private final int _mdef;
	private final int pvp_flag;
	private final int karma;
	private final int hair_style;
	private final int hair_color;
	private final int face;
	private final int gm_commands;
	private final int clan_crest_id;
	private final int ally_crest_id;
	private final int large_clan_crest_id;
	private final int private_store;
	private final int can_crystalize;
	private final int pk_kills;
	private final int pvp_kills;
	private final int class_id;
	private final int agathion;
	private final int _abnormalEffect;
	private final int _abnormalEffect2;
	private final int noble;
	private final int hero;
	private final int mount_id;
	private final int cw_level;
	private final int name_color;
	private final int running;
	private final int pledge_class;
	private final int pledge_type;
	private final int title_color;
	private final int transformation;
	private final int mount_type;
	private final String _name;
	private final EffectCubic[] cubics;
	private final boolean isFlying;
	private final TeamType _team;
	private int _relation;
	private int clan_id;
	private int ally_id;
	private String title;
	
	public UserInfo(Player player)
	{
		if(player.getTransformationName() != null)
		{
			_name = player.getTransformationName();
			title = player.getTransformationTitle() != null ? player.getTransformationTitle() : "";
			clan_crest_id = 0;
			ally_crest_id = 0;
			large_clan_crest_id = 0;
			cw_level = CursedWeaponsManager.getInstance().getLevel(player.getCursedWeaponEquippedId());
		}
		else
		{
			_name = player.getName();
			Clan clan = player.getClan();
			Alliance alliance = clan == null ? null : clan.getAlliance();
			clan_id = clan == null ? 0 : clan.getClanId();
			clan_crest_id = clan == null ? 0 : clan.getCrestId();
			large_clan_crest_id = clan == null ? 0 : clan.getCrestLargeId();
			ally_id = alliance == null ? 0 : alliance.getAllyId();
			ally_crest_id = alliance == null ? 0 : alliance.getAllyCrestId();
			cw_level = 0;
			title = player.getTitle();
		}
		if(player.getPlayerAccess().GodMode && player.isInvisible())
		{
			title = title + "(Invisible)";
		}
		if(player.isPolymorphed())
		{
			title = NpcHolder.getInstance().getTemplate(player.getPolyId()) != null ? title + " - " + NpcHolder.getInstance().getTemplate(player.getPolyId()).name : title + " - Polymorphed";
		}
		if(player.isMounted())
		{
			_enchant = 0;
			mount_id = player.getMountNpcId() + 1000000;
			mount_type = player.getMountType();
		}
		else
		{
			_enchant = player.getEnchantEffect();
			mount_id = 0;
			mount_type = 0;
		}
		_pAtkRange = player.getPhysicalAttackRange();
		move_speed = player.getMovementSpeedMultiplier();
		_runSpd = (int) ((double) player.getRunSpeed() / move_speed);
		_walkSpd = (int) ((double) player.getWalkSpeed() / move_speed);
		_flRunSpd = 0;
		_flWalkSpd = 0;
		if(player.isFlying())
		{
			_flyRunSpd = _runSpd;
			_flyWalkSpd = _walkSpd;
		}
		else
		{
			_flyRunSpd = 0;
			_flyWalkSpd = 0;
		}
		_swimRunSpd = player.getSwimSpeed();
		_swimWalkSpd = player.getSwimSpeed();
		_inv = new int[17][3];
		for(int PAPERDOLL_ID : Inventory.PAPERDOLL_ORDER)
		{
			_inv[PAPERDOLL_ID][0] = player.getInventory().getPaperdollObjectId(PAPERDOLL_ID);
			_inv[PAPERDOLL_ID][1] = player.getInventory().getPaperdollItemId(PAPERDOLL_ID);
			_inv[PAPERDOLL_ID][2] = player.getInventory().getPaperdollAugmentationId(PAPERDOLL_ID);
		}
		_relation = player.isClanLeader() ? 64 : 0;
		for(GlobalEvent e : player.getEvents())
		{
			_relation = e.getUserRelation(player, _relation);
		}
		_loc = player.getLoc();
		obj_id = player.getObjectId();
		vehicle_obj_id = player.isInBoat() ? player.getBoat().getObjectId() : 0;
		_race = player.getRace().ordinal();
		sex = player.getSex();
		base_class = player.getBaseClassId();
		level = player.getLevel();
		_exp = player.getExp();
		_str = player.getSTR();
		_dex = player.getDEX();
		_con = player.getCON();
		_int = player.getINT();
		_wit = player.getWIT();
		_men = player.getMEN();
		curHp = (int) player.getCurrentHp();
		maxHp = player.getMaxHp();
		curMp = (int) player.getCurrentMp();
		maxMp = player.getMaxMp();
		curLoad = player.getCurrentLoad();
		maxLoad = player.getMaxLoad();
		_sp = player.getIntSp();
		_patk = player.getPAtk(null);
		_patkspd = player.getPAtkSpd();
		_pdef = player.getPDef(null);
		evasion = player.getEvasionRate(null);
		accuracy = player.getAccuracy();
		crit = player.getCriticalHit(null, null);
		_matk = player.getMAtk(null, null);
		_matkspd = player.getMAtkSpd();
		_mdef = player.getMDef(null, null);
		pvp_flag = player.getPvpFlag();
		karma = player.getKarma();
		attack_speed = player.getAttackSpeedMultiplier();
		col_radius = player.getColRadius();
		col_height = player.getColHeight();
		hair_style = player.getHairStyle();
		hair_color = player.getHairColor();
		face = player.getFace();
		gm_commands = player.isGM() || player.getPlayerAccess().CanUseGMCommand ? 1 : 0;
		clan_id = player.getClanId();
		ally_id = player.getAllyId();
		private_store = player.getPrivateStoreType();
		can_crystalize = player.getSkillLevel(248) > 0 ? 1 : 0;
		pk_kills = player.getPkKills();
		pvp_kills = player.getPvpKills();
		cubics = player.getCubics().toArray(new EffectCubic[player.getCubics().size()]);
		_abnormalEffect = player.getAbnormalEffect();
		_abnormalEffect2 = player.getAbnormalEffect2();
		ClanPrivs = player.getClanPrivileges();
		rec_left = player.getGivableRec();
		rec_have = player.getReceivedRec();
		InventoryLimit = player.getInventoryLimit();
		class_id = player.getClassId().getId();
		maxCp = player.getMaxCp();
		curCp = (int) player.getCurrentCp();
		_team = player.getTeam();
		noble = player.isNoble() || player.isGM() && Config.GM_HERO_AURA ? 1 : 0;
		hero = player.isHero() || player.isGM() && Config.GM_HERO_AURA ? 1 : 0;
		_fishLoc = player.getFishLoc();
		name_color = player.getNameColor();
		running = player.isRunning() ? 1 : 0;
		pledge_class = player.getPledgeClass();
		pledge_type = player.getPledgeType();
		title_color = player.getTitleColor();
		transformation = player.getTransformation();
		agathion = player.getAgathionId();
		partyRoom = player.getMatchingRoom() != null && player.getMatchingRoom().getType() == MatchingRoom.PARTY_MATCHING && player.getMatchingRoom().getLeader() == player;
		isFlying = player.isInFlyingTransform();
		can_writeImpl = true;
	}
	
	@Override
	protected final void writeImpl()
	{
		if(!can_writeImpl)
		{
			return;
		}
		writeC(4);
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z + Config.CLIENT_Z_SHIFT);
		writeD(_loc.h);
		writeD(obj_id);
		writeS(_name);
		writeD(_race);
		writeD(sex);
		writeD(base_class);
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
		writeD(_pAtkRange);
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
		writeD(_swimRunSpd);
		writeD(_swimWalkSpd);
		writeD(_flRunSpd);
		writeD(_flWalkSpd);
		writeD(_flyRunSpd);
		writeD(_flyWalkSpd);
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
		writeD(ally_crest_id);
		writeD(_relation);
		writeC(mount_type);
		writeC(private_store);
		writeC(can_crystalize);
		writeD(pk_kills);
		writeD(pvp_kills);
		writeH(cubics.length);
		for(EffectCubic cubic : cubics)
		{
			writeH(cubic == null ? 0 : cubic.getId());
		}
		writeC(partyRoom ? 1 : 0);
		writeD(_abnormalEffect);
		writeC(isFlying ? 2 : 0);
		writeD(ClanPrivs);
		writeH(rec_left);
		writeH(rec_have);
		writeD(mount_id);
		writeH(InventoryLimit);
		writeD(class_id);
		writeD(0);
		writeD(maxCp);
		writeD(curCp);
		writeC(_enchant);
		writeC(_team.ordinal());
		writeD(large_clan_crest_id);
		writeC(noble);
		writeC(hero);
		writeC(0);
		writeD(_fishLoc.x);
		writeD(_fishLoc.y);
		writeD(_fishLoc.z);
		writeD(name_color);
		writeC(running);
		writeD(pledge_class);
		writeD(pledge_type);
		writeD(title_color);
		writeD(cw_level);
	}
}