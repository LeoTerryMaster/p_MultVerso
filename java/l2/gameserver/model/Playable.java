package l2.gameserver.model;

import l2.commons.lang.reference.HardReference;
import l2.commons.util.Rnd;
import l2.commons.util.concurrent.atomic.AtomicState;
import l2.gameserver.Config;
import l2.gameserver.ai.CtrlEvent;
import l2.gameserver.ai.CtrlIntention;
import l2.gameserver.cache.Msg;
import l2.gameserver.geodata.GeoEngine;
import l2.gameserver.model.base.TeamType;
import l2.gameserver.model.entity.events.GlobalEvent;
import l2.gameserver.model.entity.events.impl.DuelEvent;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.instances.StaticObjectInstance;
import l2.gameserver.model.items.Inventory;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.ExServerPrimitive;
import l2.gameserver.network.l2.s2c.Revive;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.skills.EffectType;
import l2.gameserver.stats.Stats;
import l2.gameserver.tables.SkillTable;
import l2.gameserver.templates.CharTemplate;
import l2.gameserver.templates.item.EtcItemTemplate;
import l2.gameserver.templates.item.WeaponTemplate;
import l2.gameserver.utils.Location;

import java.awt.*;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class Playable extends Creature
{
	protected final ReadWriteLock questLock = new ReentrantReadWriteLock();
	protected final Lock questRead = questLock.readLock();
	protected final Lock questWrite = questLock.writeLock();
	private final AtomicState _isSilentMoving = new AtomicState();
	private boolean _isPendingRevive;
	private long _nonAggroTime;
	
	public Playable(int objectId, CharTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public HardReference<? extends Playable> getRef()
	{
		return (HardReference<? extends Playable>) super.getRef();
	}
	
	public abstract Inventory getInventory();
	
	public abstract long getWearedMask();
	
	@Override
	public boolean checkPvP(Creature target, Skill skill)
	{
		Player player = getPlayer();
		if(isDead() || target == null || player == null || target == this || target == player || target == player.getPet() || player.getKarma() > 0)
		{
			return false;
		}
		if(skill != null)
		{
			if(skill.altUse())
			{
				return false;
			}
			if(skill.getTargetType() == Skill.SkillTargetType.TARGET_FEEDABLE_BEAST)
			{
				return false;
			}
			if(skill.getTargetType() == Skill.SkillTargetType.TARGET_UNLOCKABLE)
			{
				return false;
			}
			if(skill.getTargetType() == Skill.SkillTargetType.TARGET_CHEST)
			{
				return false;
			}
		}
		DuelEvent duelEvent;
		if((duelEvent = getEvent(DuelEvent.class)) != null && duelEvent == target.getEvent(DuelEvent.class))
		{
			return false;
		}
		if(isInZonePeace() && target.isInZonePeace())
		{
			return false;
		}
		if(isInZoneBattle() && target.isInZoneBattle())
		{
			return false;
		}
		if(isInZone(Zone.ZoneType.SIEGE) && target.isInZone(Zone.ZoneType.SIEGE))
		{
			return false;
		}
		if(isInZone(Zone.ZoneType.fun) && target.isInZone(Zone.ZoneType.fun))
		{
			return false;
		}
		if(skill == null || skill.isOffensive())
		{
			if(target.getKarma() > 0)
			{
				return false;
			}
			if(target.isPlayable())
			{
				return true;
			}
		}
		else if(target.getPvpFlag() > 0 || target.getKarma() > 0 || target.isMonster())
		{
			return true;
		}
		return false;
	}
	
	public boolean checkTarget(Creature target)
	{
		Player player = getPlayer();
		if(player == null)
		{
			return false;
		}
		if(target == null || target.isDead())
		{
			player.sendPacket(Msg.INVALID_TARGET);
			return false;
		}
		if(!isInRange(target, 2000))
		{
			player.sendPacket(Msg.YOUR_TARGET_IS_OUT_OF_RANGE);
			return false;
		}
		if(target.isDoor() && !target.isAttackable(this))
		{
			player.sendPacket(Msg.INVALID_TARGET);
			return false;
		}
		if(target.paralizeOnAttack(this))
		{
			if(Config.PARALIZE_ON_RAID_DIFF)
			{
				paralizeMe(target);
			}
			return false;
		}
		if(target.isInvisible() || getReflection() != target.getReflection() || !GeoEngine.canSeeTarget(this, target, false))
		{
			player.sendPacket(SystemMsg.CANNOT_SEE_TARGET);
			return false;
		}
		if(player.isInZone(Zone.ZoneType.epic) != target.isInZone(Zone.ZoneType.epic))
		{
			player.sendPacket(Msg.INVALID_TARGET);
			return false;
		}
		if(target.isPlayable())
		{
			if(isInZoneBattle() != target.isInZoneBattle())
			{
				player.sendPacket(Msg.INVALID_TARGET);
				return false;
			}
			if(isInZonePeace() || target.isInZonePeace())
			{
				player.sendPacket(Msg.YOU_MAY_NOT_ATTACK_THIS_TARGET_IN_A_PEACEFUL_ZONE);
				return false;
			}
			if(player.isOlyParticipant() && !player.isOlyCompetitionStarted())
			{
				return false;
			}
			if(target.isPlayer())
			{
				Player pcAttacker = target.getPlayer();
				if(player.isOlyParticipant())
				{
					if(pcAttacker.isOlyParticipant() && player.getOlyParticipant().getCompetition() != pcAttacker.getOlyParticipant().getCompetition())
					{
						return false;
					}
					if(player.isOlyCompetitionStarted() && player.getOlyParticipant() == pcAttacker.getOlyParticipant())
					{
						return false;
					}
					if(player.isLooseOlyCompetition())
					{
						return false;
					}
				}
			}
		}
		return true;
	}
	
	@Override
	public void setXYZ(int x, int y, int z, boolean MoveTask)
	{
		super.setXYZ(x, y, z, MoveTask);
		int dbgMove;
		Player player;
		if(MoveTask && isPlayable() && (dbgMove = (player = getPlayer()).getVarInt("debugMove", 0)) > 0)
		{
			Location loc = getLoc();
			ExServerPrimitive tracePkt = new ExServerPrimitive(loc.toXYZString(), loc.getX(), loc.getY(), (int) ((double) loc.getZ() + getColHeight() + 16.0));
			if(moveAction != null)
			{
				Color[] ccs = {Color.CYAN, Color.BLUE, Color.GREEN, Color.ORANGE, Color.MAGENTA, Color.RED, Color.YELLOW, Color.RED};
				Color c = ccs[System.identityHashCode(moveAction) % ccs.length];
				tracePkt.addPoint(String.format("%s|%08x", loc.toXYZString(), moveAction.hashCode()), c, true, loc.getX(), loc.getY(), loc.getZ());
			}
			else
			{
				tracePkt.addPoint(loc.toXYZString(), 16777215, true, loc.getX(), loc.getY(), loc.getZ());
			}
			player.sendPacket(tracePkt);
			if(dbgMove > 1)
			{
				player.broadcastPacketToOthers(tracePkt);
			}
		}
	}
	
	@Override
	public void doAttack(Creature target)
	{
		Player player = getPlayer();
		if(player == null)
		{
			return;
		}
		if(isAMuted() || isAttackingNow())
		{
			player.sendActionFailed();
			return;
		}
		if(player.isInObserverMode())
		{
			player.sendMessage(new CustomMessage("l2p.gameserver.model.L2Playable.OutOfControl.ObserverNoAttack", player));
			return;
		}
		if(!checkTarget(target))
		{
			getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
			player.sendActionFailed();
			return;
		}
		DuelEvent duelEvent = getEvent(DuelEvent.class);
		if(duelEvent != null && target.getEvent(DuelEvent.class) != duelEvent)
		{
			duelEvent.abortDuel(getPlayer());
		}
		WeaponTemplate weaponItem;
		if((weaponItem = getActiveWeaponItem()) != null && weaponItem.getItemType() == WeaponTemplate.WeaponType.BOW)
		{
			double bowMpConsume = weaponItem.getMpConsume();
			if(bowMpConsume > 0.0)
			{
				double chance = calcStat(Stats.MP_USE_BOW_CHANCE, 0.0, target, null);
				if(chance > 0.0 && Rnd.chance(chance))
				{
					bowMpConsume = calcStat(Stats.MP_USE_BOW, bowMpConsume, target, null);
				}
				if(_currentMp < bowMpConsume)
				{
					getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
					player.sendPacket(Msg.NOT_ENOUGH_MP);
					player.sendActionFailed();
					return;
				}
				reduceCurrentMp(bowMpConsume, null);
			}
			if(!player.checkAndEquipArrows())
			{
				getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
				player.sendPacket(Msg.YOU_HAVE_RUN_OUT_OF_ARROWS);
				player.sendActionFailed();
				return;
			}
		}
		super.doAttack(target);
	}
	
	public void doPurePk(Player killer)
	{
		int pkCountMulti = Math.max(killer.getPkKills() / 2, 1);
		killer.increaseKarma(Config.KARMA_MIN_KARMA * pkCountMulti);
	}
	
	@Override
	public void doCast(Skill skill, Creature target, boolean forceUse)
	{
		if(skill == null)
		{
			return;
		}
		DuelEvent duelEvent = getEvent(DuelEvent.class);
		if(duelEvent != null && target.getEvent(DuelEvent.class) != duelEvent)
		{
			duelEvent.abortDuel(getPlayer());
		}
		if(isInPeaceZone() && (skill.getTargetType() == Skill.SkillTargetType.TARGET_AREA || skill.getTargetType() == Skill.SkillTargetType.TARGET_AURA || skill.getTargetType() == Skill.SkillTargetType.TARGET_MULTIFACE || skill.getTargetType() == Skill.SkillTargetType.TARGET_MULTIFACE_AURA))
		{
			getPlayer().sendPacket(Msg.YOU_MAY_NOT_ATTACK_IN_A_PEACEFUL_ZONE);
			return;
		}
		if(skill.getSkillType() == Skill.SkillType.DEBUFF && skill.isMagic() && target.isNpc() && target.isInvul() && !target.isMonster())
		{
			getPlayer().sendPacket(Msg.INVALID_TARGET);
			return;
		}
		super.doCast(skill, target, forceUse);
	}
	
	@Override
	public void reduceCurrentHp(double damage, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp, boolean canReflect, boolean transferDamage, boolean isDot, boolean sendMessage)
	{
		if(attacker == null || isDead() || attacker.isDead() && !isDot)
		{
			return;
		}
		if(isDamageBlocked() && transferDamage)
		{
			return;
		}
		if(isDamageBlocked() && attacker != this)
		{
			if(sendMessage)
			{
				attacker.sendPacket(Msg.THE_ATTACK_HAS_BEEN_BLOCKED);
			}
			return;
		}
		if(attacker != this && attacker.isPlayable())
		{
			Player player = getPlayer();
			Player pcAttacker = attacker.getPlayer();
			if(pcAttacker != player && player.isOlyParticipant() && !player.isOlyCompetitionStarted())
			{
				if(sendMessage)
				{
					pcAttacker.sendPacket(Msg.INVALID_TARGET);
				}
				return;
			}
			if(isInZoneBattle() != attacker.isInZoneBattle())
			{
				if(sendMessage)
				{
					attacker.getPlayer().sendPacket(Msg.INVALID_TARGET);
				}
				return;
			}
			DuelEvent duelEvent = getEvent(DuelEvent.class);
			if(duelEvent != null && attacker.getEvent(DuelEvent.class) != duelEvent)
			{
				duelEvent.abortDuel(player);
			}
		}
		super.reduceCurrentHp(damage, attacker, skill, awake, standUp, directHp, canReflect, transferDamage, isDot, sendMessage);
	}
	
	@Override
	public int getPAtkSpd()
	{
		return Math.max((int) calcStat(Stats.POWER_ATTACK_SPEED, calcStat(Stats.ATK_BASE, _template.basePAtkSpd, null, null), null, null), 1);
	}
	
	@Override
	public int getPAtk(Creature target)
	{
		double init = getActiveWeaponInstance() == null ? (double) _template.basePAtk : 0.0;
		return (int) calcStat(Stats.POWER_ATTACK, init, target, null);
	}
	
	@Override
	public int getMAtk(Creature target, Skill skill)
	{
		if(skill != null && skill.getMatak() > 0)
		{
			return skill.getMatak();
		}
		double init = getActiveWeaponInstance() == null ? (double) _template.baseMAtk : 0.0;
		return (int) calcStat(Stats.MAGIC_ATTACK, init, target, skill);
	}
	
	@Override
	public boolean isAttackable(Creature attacker)
	{
		return isCtrlAttackable(attacker, true, false);
	}
	
	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return isCtrlAttackable(attacker, false, false);
	}
	
	public boolean isCtrlAttackable(Creature attacker, boolean force, boolean witchCtrl)
	{
		Player player = getPlayer();
		if(attacker == null || player == null || attacker == this || attacker == player && !force || isAlikeDead() || attacker.isAlikeDead())
		{
			return false;
		}
		if(isInvisible() || getReflection() != attacker.getReflection())
		{
			return false;
		}
		if(isInBoat())
		{
			return false;
		}
		if(attacker == getPet())
		{
			return false;
		}
		for(GlobalEvent e : getEvents())
		{
			if(e.checkForAttack(attacker, this, null, force) == null)
				continue;
			return false;
		}
		for(GlobalEvent e : player.getEvents())
		{
			if(!e.canAttack(this, attacker, null, force))
				continue;
			return true;
		}
		Player pcAttacker = attacker.getPlayer();
		if(pcAttacker != null && pcAttacker != player)
		{
			if(pcAttacker.isInBoat())
			{
				return false;
			}
			if(pcAttacker.isCursedWeaponEquipped() && player.getLevel() < 21 || player.isCursedWeaponEquipped() && pcAttacker.getLevel() < 21)
			{
				return false;
			}
			if(player.isInZone(Zone.ZoneType.epic) != pcAttacker.isInZone(Zone.ZoneType.epic))
			{
				return false;
			}
			if(player.isOlyParticipant())
			{
				if(pcAttacker.isOlyParticipant() && player.getOlyParticipant().getCompetition() != pcAttacker.getOlyParticipant().getCompetition())
				{
					return false;
				}
				if(player.isOlyCompetitionStarted() && player.getOlyParticipant() == pcAttacker.getOlyParticipant())
				{
					return false;
				}
				if(player.isLooseOlyCompetition())
				{
					return false;
				}
				if(player.getClan() != null && player.getClan() == pcAttacker.getClan())
				{
					return true;
				}
			}
			if(player.getTeam() != TeamType.NONE && player.getTeam() == pcAttacker.getTeam())
			{
				return false;
			}
			if(isInZonePeace())
			{
				return false;
			}
			if(!force && player.getParty() != null && player.getParty() == pcAttacker.getParty())
			{
				return false;
			}
			if(isInZoneBattle())
			{
				return true;
			}
			if(!force)
			{
				if(player.getClan() != null && player.getClan() == pcAttacker.getClan())
				{
					return false;
				}
				if(Config.ALLY_ALLOW_BUFF_DEBUFFS && player.getAlliance() != null && player.getAlliance() == pcAttacker.getAlliance())
				{
					return false;
				}
			}
			if(isInZone(Zone.ZoneType.SIEGE))
			{
				return true;
			}
			if(isInZone(Zone.ZoneType.fun))
			{
				return true;
			}
			if(pcAttacker.atMutualWarWith(player))
			{
				return true;
			}
			if(player.getKarma() > 0 || player.getPvpFlag() != 0)
			{
				return true;
			}
			if(witchCtrl && player.getPvpFlag() > 0)
			{
				return true;
			}
			return force;
		}
		return true;
	}
	
	@Override
	public int getKarma()
	{
		Player player = getPlayer();
		return player == null ? 0 : player.getKarma();
	}
	
	@Override
	public void callSkill(Skill skill, List<Creature> targets, boolean useActionSkills)
	{
		Player player = getPlayer();
		if(player == null)
		{
			return;
		}
		if(useActionSkills && !skill.altUse() && !skill.getSkillType().equals(Skill.SkillType.BEAST_FEED))
		{
			for(Creature target : targets)
			{
				if(target.isNpc())
				{
					if(skill.isOffensive())
					{
						if(target.paralizeOnAttack(player))
						{
							if(Config.PARALIZE_ON_RAID_DIFF)
							{
								paralizeMe(target);
							}
							return;
						}
						if(!skill.isAI())
						{
							int damage = skill.getEffectPoint() != 0 ? skill.getEffectPoint() : 1;
							target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, this, damage);
						}
					}
					target.getAI().notifyEvent(CtrlEvent.EVT_SEE_SPELL, skill, this);
				}
				else if(target.isPlayable() && target != getPet() && (!isSummon() && !isPet() || target != player))
				{
					int aggro = skill.getEffectPoint() != 0 ? skill.getEffectPoint() : Math.max(1, (int) skill.getPower());
					List<NpcInstance> npcs = World.getAroundNpc(target);
					for(NpcInstance npc : npcs)
					{
						if(npc.isDead() || !npc.isInRangeZ(this, 2000))
							continue;
						npc.getAI().notifyEvent(CtrlEvent.EVT_SEE_SPELL, skill, this);
						AggroList.AggroInfo ai = npc.getAggroList().get(target);
						if(ai == null)
							continue;
						if(!skill.isHandler() && npc.paralizeOnAttack(player))
						{
							if(Config.PARALIZE_ON_RAID_DIFF)
							{
								paralizeMe(npc);
							}
							return;
						}
						if(ai.hate < 100 || !GeoEngine.canSeeTarget(npc, target, false))
							continue;
						npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, this, ai.damage == 0 ? aggro / 2 : aggro);
					}
				}
				if(!checkPvP(target, skill))
					continue;
				startPvPFlag(target);
			}
		}
		super.callSkill(skill, targets, useActionSkills);
	}
	
	public void broadcastPickUpMsg(ItemInstance item)
	{
		Player player = getPlayer();
		if(item == null || player == null || player.isInvisible())
		{
			return;
		}
		if(item.isEquipable() && !(item.getTemplate() instanceof EtcItemTemplate))
		{
			SystemMessage msg;
			String player_name = player.getName();
			if(item.getEnchantLevel() > 0)
			{
				int msg_id = isPlayer() ? 1534 : 1536;
				msg = new SystemMessage(msg_id).addString(player_name).addNumber(item.getEnchantLevel()).addItemName(item.getItemId());
			}
			else
			{
				int msg_id = isPlayer() ? 1533 : 1536;
				msg = new SystemMessage(msg_id).addString(player_name).addItemName(item.getItemId());
			}
			player.broadcastPacket(msg);
		}
	}
	
	public void paralizeMe(Creature effector)
	{
		Skill revengeSkill = SkillTable.getInstance().getInfo(4515, 1);
		revengeSkill.getEffects(effector, this, false, false);
	}
	
	public boolean isPendingRevive()
	{
		return _isPendingRevive;
	}
	
	public final void setPendingRevive(boolean value)
	{
		_isPendingRevive = value;
	}
	
	public void doRevive()
	{
		if(!isTeleporting())
		{
			setPendingRevive(false);
			setNonAggroTime(System.currentTimeMillis() + Config.NONAGGRO_TIME_ONTELEPORT);
			if(isSalvation())
			{
				for(Effect e : getEffectList().getAllEffects())
				{
					if(e.getEffectType() != EffectType.Salvation)
						continue;
					e.exit();
					break;
				}
				setCurrentHp(getMaxHp(), true);
				setCurrentMp(getMaxMp());
				setCurrentCp(getMaxCp());
			}
			else
			{
				if(Config.RESPAWN_RESTORE_HP >= 0.0)
				{
					setCurrentHp((double) getMaxHp() * Config.RESPAWN_RESTORE_HP, true);
				}
				if(Config.RESPAWN_RESTORE_MP >= 0.0)
				{
					setCurrentMp((double) getMaxMp() * Config.RESPAWN_RESTORE_MP);
				}
				if(isPlayer() && Config.RESPAWN_RESTORE_CP >= 0.0)
				{
					setCurrentCp((double) getMaxCp() * Config.RESPAWN_RESTORE_CP, true);
				}
			}
			broadcastPacket(new Revive(this));
		}
		else
		{
			setPendingRevive(true);
		}
	}
	
	public abstract void doPickupItem(GameObject object);
	
	public void sitDown(StaticObjectInstance throne)
	{
	}
	
	public void standUp()
	{
	}
	
	public long getNonAggroTime()
	{
		return _nonAggroTime;
	}
	
	public void setNonAggroTime(long time)
	{
		_nonAggroTime = time;
	}
	
	public boolean startSilentMoving()
	{
		return _isSilentMoving.getAndSet(true);
	}
	
	public boolean stopSilentMoving()
	{
		return _isSilentMoving.setAndGet(false);
	}
	
	public boolean isSilentMoving()
	{
		return _isSilentMoving.get();
	}
	
	public boolean isInCombatZone()
	{
		return isInZoneBattle();
	}
	
	public boolean isInPeaceZone()
	{
		return isInZonePeace();
	}
	
	@Override
	public boolean isInZoneBattle()
	{
		return super.isInZoneBattle();
	}
	
	public boolean isOnSiegeField()
	{
		return isInZone(Zone.ZoneType.SIEGE);
	}
	
	public boolean isInSSQZone()
	{
		return isInZone(Zone.ZoneType.ssq_zone);
	}
	
	public boolean isInDangerArea()
	{
		return isInZone(Zone.ZoneType.damage) || isInZone(Zone.ZoneType.swamp) || isInZone(Zone.ZoneType.poison) || isInZone(Zone.ZoneType.instant_skill);
	}
	
	public int getMaxLoad()
	{
		return 0;
	}
	
	public int getInventoryLimit()
	{
		return 0;
	}
	
	@Override
	public boolean isPlayable()
	{
		return true;
	}
}