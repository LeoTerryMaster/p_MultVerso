package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Summon;
import l2.gameserver.model.base.TeamType;
import l2.gameserver.tables.PetDataTable;
import l2.gameserver.utils.Location;

public class PetInfo extends L2GameServerPacket
{
	private final int _runSpd;
	private final int _walkSpd;
	private final int MAtkSpd;
	private final int PAtkSpd;
	private final int pvp_flag;
	private final int karma;
	private final int rideable;
	private final int _type;
	private final int obj_id;
	private final int npc_id;
	private final int runing;
	private final int incombat;
	private final int dead;
	private final int _sp;
	private final int level;
	private final int _abnormalEffect;
	private final int _abnormalEffect2;
	private final int curFed;
	private final int maxFed;
	private final int curHp;
	private final int maxHp;
	private final int curMp;
	private final int maxMp;
	private final int curLoad;
	private final int maxLoad;
	private final int PAtk;
	private final int PDef;
	private final int MAtk;
	private final int MDef;
	private final int Accuracy;
	private final int Evasion;
	private final int Crit;
	private final int sps;
	private final int ss;
	private final int type;
	private final Location _loc;
	private final double col_redius;
	private final double col_height;
	private final long exp;
	private final long exp_this_lvl;
	private final long exp_next_lvl;
	private final String _name;
	private final String title;
	private final TeamType _team;
	private int _showSpawnAnimation;
	
	public PetInfo(Summon summon)
	{
		_type = summon.getSummonType();
		obj_id = summon.getObjectId();
		npc_id = summon.getTemplate().npcId;
		_loc = summon.getLoc();
		MAtkSpd = summon.getMAtkSpd();
		PAtkSpd = summon.getPAtkSpd();
		_runSpd = summon.getRunSpeed();
		_walkSpd = summon.getWalkSpeed();
		col_redius = summon.getColRadius();
		col_height = summon.getColHeight();
		runing = summon.isRunning() ? 1 : 0;
		incombat = summon.isInCombat() ? 1 : 0;
		dead = summon.isAlikeDead() ? 1 : 0;
		_name = summon.getName().equalsIgnoreCase(summon.getTemplate().name) ? "" : summon.getName();
		title = summon.getTitle();
		pvp_flag = summon.getPvpFlag();
		karma = summon.getKarma();
		curFed = summon.getCurrentFed();
		maxFed = summon.getMaxFed();
		curHp = (int) summon.getCurrentHp();
		maxHp = summon.getMaxHp();
		curMp = (int) summon.getCurrentMp();
		maxMp = summon.getMaxMp();
		_sp = summon.getSp();
		level = summon.getLevel();
		exp = summon.getExp();
		exp_this_lvl = summon.getExpForThisLevel();
		exp_next_lvl = summon.getExpForNextLevel();
		curLoad = summon.isPet() ? summon.getInventory().getTotalWeight() : 0;
		maxLoad = summon.getMaxLoad();
		PAtk = summon.getPAtk(null);
		PDef = summon.getPDef(null);
		MAtk = summon.getMAtk(null, null);
		MDef = summon.getMDef(null, null);
		Accuracy = summon.getAccuracy();
		Evasion = summon.getEvasionRate(null);
		Crit = summon.getCriticalHit(null, null);
		_abnormalEffect = summon.getAbnormalEffect();
		_abnormalEffect2 = summon.getAbnormalEffect2();
		rideable = summon.getPlayer().getTransformation() != 0 ? 0 : PetDataTable.isMountable(npc_id) ? 1 : 0;
		_team = summon.getTeam();
		ss = summon.getSoulshotConsumeCount();
		sps = summon.getSpiritshotConsumeCount();
		_showSpawnAnimation = summon.getSpawnAnimation();
		type = summon.getFormId();
	}
	
	public PetInfo update()
	{
		_showSpawnAnimation = 1;
		return this;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(177);
		writeD(_type);
		writeD(obj_id);
		writeD(npc_id + 1000000);
		writeD(0);
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z);
		writeD(_loc.h);
		writeD(0);
		writeD(MAtkSpd);
		writeD(PAtkSpd);
		writeD(_runSpd);
		writeD(_walkSpd);
		writeD(_runSpd);
		writeD(_walkSpd);
		writeD(_runSpd);
		writeD(_walkSpd);
		writeD(_runSpd);
		writeD(_walkSpd);
		writeF(1.0);
		writeF(1.0);
		writeF(col_redius);
		writeF(col_height);
		writeD(0);
		writeD(0);
		writeD(0);
		writeC(1);
		writeC(runing);
		writeC(incombat);
		writeC(dead);
		writeC(_showSpawnAnimation);
		writeS(_name);
		writeS(title);
		writeD(1);
		writeD(pvp_flag);
		writeD(karma);
		writeD(curFed);
		writeD(maxFed);
		writeD(curHp);
		writeD(maxHp);
		writeD(curMp);
		writeD(maxMp);
		writeD(_sp);
		writeD(level);
		writeQ(exp);
		writeQ(exp_this_lvl);
		writeQ(exp_next_lvl);
		writeD(curLoad);
		writeD(maxLoad);
		writeD(PAtk);
		writeD(PDef);
		writeD(MAtk);
		writeD(MDef);
		writeD(Accuracy);
		writeD(Evasion);
		writeD(Crit);
		writeD(_runSpd);
		writeD(PAtkSpd);
		writeD(MAtkSpd);
		writeD(_abnormalEffect);
		writeH(rideable);
		writeC(0);
		writeH(0);
		writeC(_team.ordinal());
		writeD(ss);
		writeD(sps);
	}
}