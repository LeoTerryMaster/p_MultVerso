package l2.gameserver.skills.skillclasses;

import l2.gameserver.ThreadPoolManager;
import l2.gameserver.cache.Msg;
import l2.gameserver.dao.EffectsDAO;
import l2.gameserver.data.xml.holder.NpcHolder;
import l2.gameserver.idfactory.IdFactory;
import l2.gameserver.model.Creature;
import l2.gameserver.model.GameObjectTasks;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.World;
import l2.gameserver.model.base.Experience;
import l2.gameserver.model.entity.events.impl.SiegeEvent;
import l2.gameserver.model.instances.MerchantInstance;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.instances.SummonInstance;
import l2.gameserver.model.instances.TrapInstance;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.stats.Stats;
import l2.gameserver.stats.funcs.FuncAdd;
import l2.gameserver.tables.SkillTable;
import l2.gameserver.templates.StatsSet;
import l2.gameserver.templates.npc.NpcTemplate;
import l2.gameserver.utils.Location;
import l2.gameserver.utils.NpcUtils;

import java.util.List;

public class Summon extends Skill
{
	private final SummonType _summonType;
	private final double _expPenalty;
	private final int _itemConsumeIdInTime;
	private final int _itemConsumeCountInTime;
	private final int _itemConsumeDelay;
	private final int _lifeTime;
	private final int _minRadius;
	
	public Summon(StatsSet set)
	{
		super(set);
		_summonType = Enum.valueOf(SummonType.class, set.getString("summonType", "PET").toUpperCase());
		_expPenalty = set.getDouble("expPenalty", 0.0);
		_itemConsumeIdInTime = set.getInteger("itemConsumeIdInTime", 0);
		_itemConsumeCountInTime = set.getInteger("itemConsumeCountInTime", 0);
		_itemConsumeDelay = set.getInteger("itemConsumeDelay", 240) * 1000;
		_lifeTime = set.getInteger("lifeTime", 1200) * 1000;
		_minRadius = set.getInteger("minRadius", 0);
	}
	
	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		Player player = activeChar.getPlayer();
		if(player == null)
		{
			return false;
		}
		switch(_summonType)
		{
			case TRAP:
			{
				if(!player.isInZonePeace())
					break;
				activeChar.sendPacket(Msg.YOU_MAY_NOT_ATTACK_IN_A_PEACEFUL_ZONE);
				return false;
			}
			case PET:
			case SIEGE_SUMMON:
			{
				if(player.isProcessingRequest())
				{
					player.sendPacket(Msg.PETS_AND_SERVITORS_ARE_NOT_AVAILABLE_AT_THIS_TIME);
					return false;
				}
				if(player.getPet() == null && !player.isMounted())
					break;
				player.sendPacket(Msg.YOU_ALREADY_HAVE_A_PET);
				return false;
			}
			case MERCHANT:
			{
				if(!player.isProcessingRequest())
					break;
				player.sendPacket(Msg.PETS_AND_SERVITORS_ARE_NOT_AVAILABLE_AT_THIS_TIME);
				return false;
			}
			case AGATHION:
			{
				if(player.getAgathionId() > 0 && _npcId != 0)
				{
					player.sendPacket(SystemMsg.AN_AGATHION_HAS_ALREADY_BEEN_SUMMONED);
					return false;
				}
			}
			case NPC:
			{
				if(_minRadius <= 0)
					break;
				for(NpcInstance npc : World.getAroundNpc(player, _minRadius, 200))
				{
					if(npc == null || npc.getNpcId() != getNpcId())
						continue;
					player.sendPacket(new SystemMessage(SystemMsg.SINCE_S1_ALREADY_EXISTS_NEARBY_YOU_CANNOT_SUMMON_IT_AGAIN).addName(npc));
					return false;
				}
				break;
			}
		}
		return super.checkCondition(activeChar, target, forceUse, dontMove, first);
	}
	
	@Override
	public void useSkill(Creature caster, List<Creature> targets)
	{
		Player activeChar = caster.getPlayer();
		switch(_summonType)
		{
			case AGATHION:
			{
				activeChar.setAgathion(getNpcId());
				break;
			}
			case TRAP:
			{
				Skill trapSkill = getFirstAddedSkill();
				if(activeChar.getTrapsCount() >= 5)
				{
					activeChar.destroyFirstTrap();
				}
				TrapInstance trap = new TrapInstance(IdFactory.getInstance().getNextId(), NpcHolder.getInstance().getTemplate(getNpcId()), activeChar, trapSkill);
				activeChar.addTrap(trap);
				trap.spawnMe();
				break;
			}
			case PET:
			case SIEGE_SUMMON:
			{
				Location loc = null;
				if(_targetType == Skill.SkillTargetType.TARGET_CORPSE)
				{
					for(Creature target : targets)
					{
						if(target == null || !target.isDead())
							continue;
						activeChar.getAI().setAttackTarget(null);
						loc = target.getLoc();
						if(target.isNpc())
						{
							((NpcInstance) target).endDecayTask();
							continue;
						}
						if(target.isSummon())
						{
							((SummonInstance) target).endDecayTask();
							continue;
						}
						return;
					}
				}
				if(activeChar.getPet() != null || activeChar.isMounted())
				{
					return;
				}
				NpcTemplate summonTemplate = NpcHolder.getInstance().getTemplate(getNpcId());
				SummonInstance summon = new SummonInstance(IdFactory.getInstance().getNextId(), summonTemplate, activeChar, _lifeTime, _itemConsumeIdInTime, _itemConsumeCountInTime, _itemConsumeDelay, this);
				activeChar.setPet(summon);
				summon.setExpPenalty(_expPenalty);
				summon.setExp(Experience.LEVEL[Math.min(summon.getLevel(), Experience.LEVEL.length - 1)]);
				summon.setHeading(activeChar.getHeading());
				summon.setReflection(activeChar.getReflection());
				summon.spawnMe(loc == null ? Location.findAroundPosition(activeChar, 50, 70) : loc);
				summon.setRunning();
				summon.setFollowMode(true);
				if(summon.getSkillLevel(4140) > 0)
				{
					summon.altUseSkill(SkillTable.getInstance().getInfo(4140, summon.getSkillLevel(4140)), activeChar);
				}
				if(summon.getName().equalsIgnoreCase("Shadow"))
				{
					summon.addStatFunc(new FuncAdd(Stats.ABSORB_DAMAGE_PERCENT, 64, this, 15.0));
				}
				EffectsDAO.getInstance().restoreEffects(summon);
				if(activeChar.isOlyParticipant())
				{
					summon.getEffectList().stopAllEffects();
				}
				summon.setCurrentHpMp(summon.getMaxHp(), summon.getMaxMp(), false);
				if(_summonType != SummonType.SIEGE_SUMMON)
					break;
				SiegeEvent siegeEvent = activeChar.getEvent(SiegeEvent.class);
				siegeEvent.addSiegeSummon(summon);
				break;
			}
			case MERCHANT:
			{
				if(activeChar.getPet() != null || activeChar.isMounted())
				{
					return;
				}
				NpcTemplate merchantTemplate = NpcHolder.getInstance().getTemplate(getNpcId());
				MerchantInstance merchant = new MerchantInstance(IdFactory.getInstance().getNextId(), merchantTemplate);
				merchant.setCurrentHp(merchant.getMaxHp(), false);
				merchant.setCurrentMp(merchant.getMaxMp());
				merchant.setHeading(activeChar.getHeading());
				merchant.setReflection(activeChar.getReflection());
				merchant.spawnMe(activeChar.getLoc());
				ThreadPoolManager.getInstance().schedule(new GameObjectTasks.DeleteTask(merchant), _lifeTime);
				break;
			}
			case NPC:
			{
				NpcUtils.spawnSingle(getNpcId(), activeChar.getLoc(), activeChar.getReflection(), (long) _lifeTime, activeChar.getName());
			}
		}
		if(isSSPossible())
		{
			caster.unChargeShots(isMagic());
		}
	}
	
	@Override
	public boolean isOffensive()
	{
		return _targetType == Skill.SkillTargetType.TARGET_CORPSE;
	}
	
	private enum SummonType
	{
		PET,
		SIEGE_SUMMON,
		AGATHION,
		TRAP,
		MERCHANT,
		NPC;
	}
}