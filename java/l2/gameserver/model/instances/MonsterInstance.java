package l2.gameserver.model.instances;

import gnu.trove.TIntHashSet;
import l2.commons.threading.RunnableImpl;
import l2.commons.util.Rnd;
import l2.gameserver.Config;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.cache.Msg;
import l2.gameserver.instancemanager.CursedWeaponsManager;
import l2.gameserver.model.*;
import l2.gameserver.model.base.Experience;
import l2.gameserver.model.base.TeamType;
import l2.gameserver.model.entity.Reflection;
import l2.gameserver.model.pledge.Clan;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestEventType;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.model.reward.RewardItem;
import l2.gameserver.model.reward.RewardList;
import l2.gameserver.model.reward.RewardType;
import l2.gameserver.network.l2.s2c.SocialAction;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.stats.Stats;
import l2.gameserver.tables.SkillTable;
import l2.gameserver.templates.npc.Faction;
import l2.gameserver.templates.npc.NpcTemplate;
import l2.gameserver.utils.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MonsterInstance extends NpcInstance
{
	private static final int MONSTER_MAINTENANCE_INTERVAL = 1000;
	private final MinionList minionList;
	private final Lock harvestLock = new ReentrantLock();
	private final Lock absorbLock = new ReentrantLock();
	private final Lock sweepLock = new ReentrantLock();
	private final double MIN_DISTANCE_FOR_USE_UD = 200.0;
	private final double MIN_DISTANCE_FOR_CANCEL_UD = 50.0;
	private final double UD_USE_CHANCE = 30.0;
	private ScheduledFuture<?> minionMaintainTask;
	private boolean _isSeeded;
	private int _seederId;
	private boolean _altSeed;
	private RewardItem _harvestItem;
	private int overhitAttackerId;
	private double _overhitDamage;
	private TIntHashSet _absorbersIds;
	private boolean _isSpoiled;
	private int spoilerId;
	private List<RewardItem> _sweepItems;
	private int _isChampion;
	
	public MonsterInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		minionList = new MinionList(this);
	}
	
	@Override
	public boolean isMovementDisabled()
	{
		return getNpcId() == 18344 || getNpcId() == 18345 || super.isMovementDisabled();
	}
	
	@Override
	public boolean isLethalImmune()
	{
		return getMaxHp() >= 50000 || _isChampion > 0 || getNpcId() == 22215 || getNpcId() == 22216 || getNpcId() == 22217 || super.isLethalImmune();
	}
	
	@Override
	public boolean isFearImmune()
	{
		return _isChampion > 0 || super.isFearImmune();
	}
	
	@Override
	public boolean isParalyzeImmune()
	{
		return _isChampion > 0 || super.isParalyzeImmune();
	}
	
	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return !attacker.isMonster();
	}
	
	public int getChampion()
	{
		return _isChampion;
	}
	
	public void setChampion(int level)
	{
		if(level == 0)
		{
			removeSkillById(4407);
			_isChampion = 0;
		}
		else
		{
			addSkill(SkillTable.getInstance().getInfo(4407, level));
			_isChampion = level;
		}
	}
	
	public void setChampion()
	{
		if(getReflection().canChampions() && canChampion())
		{
			double random = Rnd.nextDouble();
			if(Config.ALT_CHAMPION_CHANCE2 / 100.0 >= random)
			{
				setChampion(2);
			}
			else if((Config.ALT_CHAMPION_CHANCE1 + Config.ALT_CHAMPION_CHANCE2) / 100.0 >= random)
			{
				setChampion(1);
			}
			else
			{
				setChampion(0);
			}
		}
		else
		{
			setChampion(0);
		}
	}
	
	public boolean canChampion()
	{
		return getTemplate().rewardExp > 0 && getTemplate().level >= Config.ALT_CHAMPION_MIN_LEVEL && getTemplate().level <= Config.ALT_CHAMPION_TOP_LEVEL;
	}
	
	@Override
	public TeamType getTeam()
	{
		return getChampion() == 2 ? TeamType.RED : getChampion() == 1 ? TeamType.BLUE : TeamType.NONE;
	}
	
	@Override
	protected void onSpawn()
	{
		super.onSpawn();
		setCurrentHpMp(getMaxHp(), getMaxMp(), true);
		if(getMinionList().hasMinions())
		{
			if(minionMaintainTask != null)
			{
				minionMaintainTask.cancel(false);
				minionMaintainTask = null;
			}
			minionMaintainTask = ThreadPoolManager.getInstance().schedule(new MinionMaintainTask(), 1000);
		}
	}
	
	@Override
	protected void onDespawn()
	{
		setOverhitDamage(0.0);
		setOverhitAttacker(null);
		clearSweep();
		clearHarvest();
		clearAbsorbers();
		super.onDespawn();
	}
	
	@Override
	public MinionList getMinionList()
	{
		return minionList;
	}
	
	public Location getMinionPosition()
	{
		return Location.findPointToStay(this, 100, 150);
	}
	
	public void notifyMinionDied(MinionInstance minion)
	{
	}
	
	public void spawnMinion(MonsterInstance minion)
	{
		minion.setReflection(getReflection());
		if(getChampion() == 2)
		{
			minion.setChampion(1);
		}
		else
		{
			minion.setChampion(0);
		}
		minion.setHeading(getHeading());
		minion.setCurrentHpMp(minion.getMaxHp(), minion.getMaxMp(), true);
		minion.spawnMe(getMinionPosition());
	}
	
	@Override
	public boolean hasMinions()
	{
		return getMinionList().hasMinions();
	}
	
	@Override
	public void setReflection(Reflection reflection)
	{
		super.setReflection(reflection);
		if(hasMinions())
		{
			for(MinionInstance m : getMinionList().getAliveMinions())
			{
				m.setReflection(reflection);
			}
		}
	}
	
	@Override
	protected void onDelete()
	{
		if(minionMaintainTask != null)
		{
			minionMaintainTask.cancel(false);
			minionMaintainTask = null;
		}
		getMinionList().deleteMinions();
		super.onDelete();
	}
	
	@Override
	protected void onDeath(Creature killer)
	{
		if(minionMaintainTask != null)
		{
			minionMaintainTask.cancel(false);
			minionMaintainTask = null;
		}
		calculateRewards(killer);
		super.onDeath(killer);
	}
	
	@Override
	protected void onReduceCurrentHp(double damage, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp)
	{
		if(skill != null && skill.isOverhit())
		{
			double overhitDmg = (getCurrentHp() - damage) * -1.0;
			if(overhitDmg <= 0.0)
			{
				setOverhitDamage(0.0);
				setOverhitAttacker(null);
			}
			else
			{
				setOverhitDamage(overhitDmg);
				setOverhitAttacker(attacker);
			}
		}
		super.onReduceCurrentHp(damage, attacker, skill, awake, standUp, directHp);
	}
	
	private int getPlayersDamage(Iterable<Player> players, Map<Playable, AggroList.HateInfo> hateMap)
	{
		int damage = 0;
		for(Player player : players)
		{
			AggroList.HateInfo hateInfo;
			if(player.isDead() || (hateInfo = hateMap.get(player)) == null || hateInfo.damage <= 1)
				continue;
			damage += hateInfo.damage;
			Summon playerSummon = player.getPet();
			AggroList.HateInfo summonHateInfo;
			if(playerSummon == null || (summonHateInfo = hateMap.get(playerSummon)) == null)
				continue;
			damage += summonHateInfo.damage;
		}
		return damage;
	}
	
	private Creature getTopDamager(Iterable<? extends PlayerGroup> playerGroups, Map<Playable, AggroList.HateInfo> hateMap)
	{
		if(hateMap.isEmpty())
		{
			return null;
		}
		int topDamage = Integer.MIN_VALUE;
		PlayerGroup topPlayerGroup = null;
		for(PlayerGroup playerGroup : playerGroups)
		{
			int playerGroupDamage;
			if(playerGroup == topPlayerGroup || (playerGroupDamage = getPlayersDamage(playerGroup, hateMap)) <= topDamage)
				continue;
			topDamage = playerGroupDamage;
			topPlayerGroup = playerGroup;
		}
		if(topPlayerGroup == null)
		{
			return null;
		}
		if(topPlayerGroup instanceof Player)
		{
			return (Player) topPlayerGroup;
		}
		if(topPlayerGroup instanceof Party)
		{
			Party party = (Party) topPlayerGroup;
			return getTopDamager(party.getPartyMembers(), hateMap);
		}
		if(topPlayerGroup instanceof CommandChannel)
		{
			CommandChannel commandChannel = (CommandChannel) topPlayerGroup;
			return getTopDamager(commandChannel.getParties(), hateMap);
		}
		return null;
	}
	
	private Creature getTopDamager(Map<Playable, AggroList.HateInfo> hateMap)
	{
		HashSet<PlayerGroup> players = new HashSet<>();
		for(Playable playable : hateMap.keySet())
		{
			if(!(playable instanceof Player))
				continue;
			players.add(((Player) playable).getPlayerGroup());
		}
		return getTopDamager(players, hateMap);
	}
	
	protected Creature getTopDamager()
	{
		return getTopDamager(getAggroList().getPlayableMap());
	}
	
	public void calculateRewards(Creature lastAttacker)
	{
		Creature topDamager = getTopDamager();
		if(lastAttacker == null || !lastAttacker.isPlayable())
		{
			lastAttacker = topDamager;
		}
		if(lastAttacker == null || !lastAttacker.isPlayable())
		{
			return;
		}
		Player killer = lastAttacker.getPlayer();
		if(killer == null)
		{
			return;
		}
		Map<Playable, AggroList.HateInfo> aggroMap = getAggroList().getPlayableMap();
		Quest[] quests = getTemplate().getEventQuests(QuestEventType.MOB_KILLED_WITH_QUEST);
		if(quests != null && quests.length > 0)
		{
			ArrayList<Player> players = null;
			if(isRaid() && Config.ALT_NO_LASTHIT)
			{
				players = new ArrayList<>();
				for(Playable pl : aggroMap.keySet())
				{
					if(pl.isDead() || !isInRangeZ(pl, (long) Config.ALT_PARTY_DISTRIBUTION_RANGE) && !killer.isInRangeZ(pl, (long) Config.ALT_PARTY_DISTRIBUTION_RANGE) || players.contains(pl.getPlayer()))
						continue;
					players.add(pl.getPlayer());
				}
			}
			else if(killer.getParty() != null)
			{
				players = new ArrayList(killer.getParty().getMemberCount());
				for(Player pl : killer.getParty().getPartyMembers())
				{
					if(pl.isDead() || !isInRangeZ(pl, (long) Config.ALT_PARTY_DISTRIBUTION_RANGE) && !killer.isInRangeZ(pl, (long) Config.ALT_PARTY_DISTRIBUTION_RANGE))
						continue;
					players.add(pl);
				}
			}
			for(Quest quest : quests)
			{
				Player toReward = killer;
				if(quest.getParty() != 0 && players != null)
				{
					if(isRaid() || quest.getParty() == 2)
					{
						for(Player pl : players)
						{
							QuestState qs2 = pl.getQuestState(quest.getName());
							if(qs2 == null || qs2.isCompleted())
								continue;
							quest.notifyKill(this, qs2);
						}
						toReward = null;
					}
					else
					{
						ArrayList<Player> interested = new ArrayList<>(players.size());
						for(Player pl2 : players)
						{
							QuestState qs3 = pl2.getQuestState(quest.getName());
							if(qs3 == null || qs3.isCompleted())
								continue;
							interested.add(pl2);
						}
						if(interested.isEmpty())
							continue;
						toReward = interested.get(Rnd.get(interested.size()));
						if(toReward == null)
						{
							toReward = killer;
						}
					}
				}
				QuestState qs;
				if(toReward == null || (qs = toReward.getQuestState(quest.getName())) == null || qs.isCompleted())
					continue;
				quest.notifyKill(this, qs);
			}
		}
		HashMap<Player, RewardInfo> rewards = new HashMap<>();
		for(AggroList.HateInfo info : aggroMap.values())
		{
			if(info.damage <= 1)
				continue;
			Playable attacker = (Playable) info.attacker;
			Player player = attacker.getPlayer();
			RewardInfo reward = rewards.get(player);
			if(reward == null)
			{
				rewards.put(player, new RewardInfo(player, info.damage));
				continue;
			}
			reward.addDamage(info.damage);
		}
		Player[] attackers = rewards.keySet().toArray(new Player[rewards.size()]);
		double[] xpsp = new double[2];
		for(Player attacker : attackers)
		{
			RewardInfo reward;
			if(attacker.isDead() || (reward = rewards.get(attacker)) == null)
				continue;
			Party party = attacker.getParty();
			int maxHp = getMaxHp();
			xpsp[0] = 0.0;
			xpsp[1] = 0.0;
			if(party == null)
			{
				int damage = Math.min(reward._dmg, maxHp);
				if(damage > 0)
				{
					if(isInRangeZ(attacker, (long) Config.ALT_PARTY_DISTRIBUTION_RANGE))
					{
						xpsp = calculateExpAndSp(attacker.getLevel(), damage);
					}
					xpsp[0] = applyOverhit(killer, xpsp[0]);
					attacker.addExpAndCheckBonus(this, xpsp[0], xpsp[1]);
				}
				rewards.remove(attacker);
				continue;
			}
			int partyDmg = 0;
			int partyMaxLevel = 1;
			ArrayList<Player> rewardedMembers = new ArrayList<>();
			for(Player partyMember : party.getPartyMembers())
			{
				RewardInfo ai = rewards.remove(partyMember);
				if(partyMember.isDead() || !isInRangeZ(partyMember, (long) Config.ALT_PARTY_DISTRIBUTION_RANGE))
					continue;
				if(ai != null)
				{
					partyDmg += ai._dmg;
				}
				rewardedMembers.add(partyMember);
				if(partyMember.getLevel() <= partyMaxLevel)
					continue;
				partyMaxLevel = partyMember.getLevel();
			}
			if((partyDmg = Math.min(partyDmg, maxHp)) <= 0)
				continue;
			xpsp = calculateExpAndSp(partyMaxLevel, partyDmg);
			double partyMul = (double) partyDmg / (double) maxHp;
			double[] arrd = xpsp;
			arrd[0] = arrd[0] * partyMul;
			double[] arrd2 = xpsp;
			arrd2[1] = arrd2[1] * partyMul;
			xpsp[0] = applyOverhit(killer, xpsp[0]);
			party.distributeXpAndSp(xpsp[0], xpsp[1], rewardedMembers, lastAttacker, this);
		}
		CursedWeaponsManager.getInstance().dropAttackable(this, killer);
		if(topDamager == null || !topDamager.isPlayable())
		{
			return;
		}
		for(Map.Entry entry : getTemplate().getRewards().entrySet())
		{
			rollRewards(entry, lastAttacker, topDamager);
		}
		if(getChampion() > 0 && Config.ALT_CHAMPION_DROP_ITEM_ID > 0 && Math.abs(getLevel() - topDamager.getLevel()) < 9 && Rnd.chance(Config.ALT_CHAMPION_DROP_CHANCE) && topDamager.getPlayer() != null)
		{
			dropItem(topDamager.getPlayer(), Config.ALT_CHAMPION_DROP_ITEM_ID, Config.ALT_CHAMPION_DROP_COUNT);
		}
	}
	
	@Override
	public void onRandomAnimation()
	{
		if(System.currentTimeMillis() - _lastSocialAction > 10000)
		{
			broadcastPacket(new SocialAction(getObjectId(), 1));
			_lastSocialAction = System.currentTimeMillis();
		}
	}
	
	@Override
	public void startRandomAnimation()
	{
	}
	
	@Override
	public int getKarma()
	{
		return 0;
	}
	
	public void addAbsorber(Player attacker)
	{
		if(attacker == null)
		{
			return;
		}
		if(getCurrentHpPercents() > 50.0)
		{
			return;
		}
		absorbLock.lock();
		try
		{
			if(_absorbersIds == null)
			{
				_absorbersIds = new TIntHashSet();
			}
			_absorbersIds.add(attacker.getObjectId());
		}
		finally
		{
			absorbLock.unlock();
		}
	}
	
	public boolean isAbsorbed(Player player)
	{
		absorbLock.lock();
		try
		{
			if(_absorbersIds == null)
			{
				boolean bl = false;
				return bl;
			}
			if(!_absorbersIds.contains(player.getObjectId()))
			{
				boolean bl = false;
				return bl;
			}
		}
		finally
		{
			absorbLock.unlock();
		}
		return true;
	}
	
	public void clearAbsorbers()
	{
		absorbLock.lock();
		try
		{
			if(_absorbersIds != null)
			{
				_absorbersIds.clear();
			}
		}
		finally
		{
			absorbLock.unlock();
		}
	}
	
	public RewardItem takeHarvest()
	{
		harvestLock.lock();
		try
		{
			RewardItem harvest = _harvestItem;
			clearHarvest();
			RewardItem rewardItem = harvest;
			return rewardItem;
		}
		finally
		{
			harvestLock.unlock();
		}
	}
	
	public void clearHarvest()
	{
		harvestLock.lock();
		try
		{
			_harvestItem = null;
			_altSeed = false;
			_seederId = 0;
			_isSeeded = false;
		}
		finally
		{
			harvestLock.unlock();
		}
	}
	
	public boolean setSeeded(Player player, int seedId, boolean altSeed)
	{
		harvestLock.lock();
		try
		{
			if(isSeeded())
			{
				boolean bl = false;
				return bl;
			}
			_isSeeded = true;
			_altSeed = altSeed;
			_seederId = player.getObjectId();
			_harvestItem = new RewardItem(Manor.getInstance().getCropType(seedId));
			if(getTemplate().rateHp > 1.0)
			{
				_harvestItem.count = Rnd.get(Math.round(getTemplate().rateHp), Math.round(1.5 * getTemplate().rateHp));
			}
		}
		finally
		{
			harvestLock.unlock();
		}
		return true;
	}
	
	public boolean isSeeded(Player player)
	{
		return isSeeded() && _seederId == player.getObjectId() && getDeadTime() < 20000;
	}
	
	public boolean isSeeded()
	{
		return _isSeeded;
	}
	
	public boolean isSpoiled()
	{
		return _isSpoiled;
	}
	
	public boolean isSpoiled(Player player)
	{
		if(!isSpoiled())
		{
			return false;
		}
		if(player.getObjectId() == spoilerId && getDeadTime() < 20000)
		{
			return true;
		}
		if(player.isInParty())
		{
			for(Player pm : player.getParty().getPartyMembers())
			{
				if(pm.getObjectId() != spoilerId || getDistance(pm) >= (double) Config.ALT_PARTY_DISTRIBUTION_RANGE)
					continue;
				return true;
			}
		}
		return false;
	}
	
	public boolean setSpoiled(Player player)
	{
		sweepLock.lock();
		try
		{
			if(isSpoiled())
			{
				boolean bl = false;
				return bl;
			}
			_isSpoiled = true;
			spoilerId = player.getObjectId();
		}
		finally
		{
			sweepLock.unlock();
		}
		return true;
	}
	
	public boolean isSweepActive()
	{
		sweepLock.lock();
		try
		{
			boolean bl = _sweepItems != null && _sweepItems.size() > 0;
			return bl;
		}
		finally
		{
			sweepLock.unlock();
		}
	}
	
	public List<RewardItem> takeSweep()
	{
		sweepLock.lock();
		try
		{
			List<RewardItem> sweep = _sweepItems;
			clearSweep();
			List<RewardItem> list = sweep;
			return list;
		}
		finally
		{
			sweepLock.unlock();
		}
	}
	
	public void clearSweep()
	{
		sweepLock.lock();
		try
		{
			_isSpoiled = false;
			spoilerId = 0;
			_sweepItems = null;
		}
		finally
		{
			sweepLock.unlock();
		}
	}
	
	public void rollRewards(Map.Entry<RewardType, RewardList> entry, Creature lastAttacker, Creature topDamager)
	{
		RewardType type = entry.getKey();
		RewardList list = entry.getValue();
		if(type == RewardType.SWEEP && !isSpoiled())
		{
			return;
		}
		Creature activeChar = type == RewardType.SWEEP ? lastAttacker : topDamager;
		Player activePlayer = activeChar.getPlayer();
		if(activePlayer == null)
		{
			return;
		}
		int diff = calculateLevelDiffForDrop(topDamager.getLevel());
		double mod = calcStat(Stats.ITEM_REWARD_MULTIPLIER, 1.0, activeChar, null);
		mod *= Experience.penaltyModifier(diff, 9.0);
		List<RewardItem> rewardItems = list.roll(activePlayer, mod, this instanceof RaidBossInstance);
		switch(type)
		{
			case SWEEP:
			{
				_sweepItems = rewardItems;
				break;
			}
			default:
			{
				for(RewardItem drop : rewardItems)
				{
					if(isSeeded() && !_altSeed && !drop.isAdena && !drop.isSealStone)
						continue;
					dropItem(activePlayer, drop.itemId, drop.count);
				}
			}
		}
	}
	
	private double[] calculateExpAndSp(int level, long damage)
	{
		int diff = level - getLevel();
		double xp = getExpReward() * damage / (long) getMaxHp();
		double sp = getSpReward() * damage / (long) getMaxHp();
		if(Config.EXP_SP_DIFF_LIMIT != 0 && Math.abs(diff) > Config.EXP_SP_DIFF_LIMIT)
		{
			xp = 0.0;
			sp = 0.0;
		}
		if(diff > 5)
		{
			double mod = Math.pow(0.83, diff - 5);
			xp *= mod;
			sp *= mod;
		}
		xp = Math.max(0.0, xp);
		sp = Math.max(0.0, sp);
		return new double[] {xp, sp};
	}
	
	private double applyOverhit(Player killer, double xp)
	{
		if(xp > 0.0 && killer.getObjectId() == overhitAttackerId)
		{
			int overHitExp = calculateOverhitExp(xp);
			killer.sendPacket(Msg.OVER_HIT, new SystemMessage(362).addNumber(overHitExp));
			xp += (double) overHitExp;
		}
		return xp;
	}
	
	@Override
	public void setOverhitAttacker(Creature attacker)
	{
		overhitAttackerId = attacker == null ? 0 : attacker.getObjectId();
	}
	
	public double getOverhitDamage()
	{
		return _overhitDamage;
	}
	
	@Override
	public void setOverhitDamage(double damage)
	{
		_overhitDamage = damage;
	}
	
	public int calculateOverhitExp(double normalExp)
	{
		double overhitPercentage = getOverhitDamage() * 100.0 / (double) getMaxHp();
		if(overhitPercentage > 25.0)
		{
			overhitPercentage = 25.0;
		}
		double overhitExp = overhitPercentage / 100.0 * normalExp;
		setOverhitAttacker(null);
		setOverhitDamage(0.0);
		return (int) Math.round(overhitExp);
	}
	
	@Override
	public boolean isAggressive()
	{
		return (Config.ALT_CHAMPION_CAN_BE_AGGRO || getChampion() == 0) && super.isAggressive();
	}
	
	@Override
	public Faction getFaction()
	{
		return Config.ALT_CHAMPION_CAN_BE_SOCIAL || getChampion() == 0 ? super.getFaction() : Faction.NONE;
	}
	
	@Override
	public void reduceCurrentHp(double i, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp, boolean canReflect, boolean transferDamage, boolean isDot, boolean sendMessage)
	{
		checkUD(attacker, i);
		super.reduceCurrentHp(i, attacker, skill, awake, standUp, directHp, canReflect, transferDamage, isDot, sendMessage);
	}
	
	private void checkUD(Creature attacker, double damage)
	{
		if((double) getTemplate().baseAtkRange > 200.0 || getLevel() < 20 || getLevel() > 78 || attacker.getLevel() - getLevel() > 9 || getLevel() - attacker.getLevel() > 9)
		{
			return;
		}
		if(isMinion() || getMinionList() != null || isRaid() || this instanceof ReflectionBossInstance || this instanceof ChestInstance || getChampion() > 0)
		{
			return;
		}
		int skillLvl = 1;
		if(getLevel() >= 41 || getLevel() <= 60)
		{
			skillLvl = 2;
		}
		else if(getLevel() > 60)
		{
			skillLvl = 3;
		}
		double distance = getDistance(attacker);
		int skillId = 5044;
		if(distance <= 50.0)
		{
			if(getEffectList() != null && getEffectList().getEffectsBySkillId(skillId) != null)
			{
				for(Effect e : getEffectList().getEffectsBySkillId(skillId))
				{
					e.exit();
				}
			}
		}
		else if(distance >= 200.0)
		{
			double chance = 30.0 / ((double) getMaxHp() / damage);
			if(Rnd.chance(chance))
			{
				Skill skill = SkillTable.getInstance().getInfo(skillId, skillLvl);
				if(skill != null)
				{
					skill.getEffects(this, this, false, false);
				}
			}
		}
	}
	
	@Override
	public boolean isMonster()
	{
		return true;
	}
	
	@Override
	public Clan getClan()
	{
		return null;
	}
	
	@Override
	public boolean isInvul()
	{
		return _isInvul;
	}
	
	protected static final class RewardInfo
	{
		protected Creature _attacker;
		protected int _dmg;
		
		public RewardInfo(Creature attacker, int dmg)
		{
			_attacker = attacker;
			_dmg = dmg;
		}
		
		public void addDamage(int dmg)
		{
			if(dmg < 0)
			{
				dmg = 0;
			}
			_dmg += dmg;
		}
		
		@Override
		public int hashCode()
		{
			return _attacker.getObjectId();
		}
	}
	
	public class MinionMaintainTask extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			if(isDead())
			{
				return;
			}
			getMinionList().spawnMinions();
		}
	}
}