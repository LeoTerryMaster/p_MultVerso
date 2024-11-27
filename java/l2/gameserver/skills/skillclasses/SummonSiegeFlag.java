package l2.gameserver.skills.skillclasses;

import l2.gameserver.data.xml.holder.NpcHolder;
import l2.gameserver.geodata.GeoEngine;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.Zone;
import l2.gameserver.model.entity.events.impl.SiegeEvent;
import l2.gameserver.model.entity.events.objects.SiegeClanObject;
import l2.gameserver.model.entity.events.objects.ZoneObject;
import l2.gameserver.model.instances.residences.SiegeFlagInstance;
import l2.gameserver.model.pledge.Clan;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.SystemMessage2;
import l2.gameserver.stats.Stats;
import l2.gameserver.stats.funcs.FuncMul;
import l2.gameserver.templates.StatsSet;

import java.util.List;

public class SummonSiegeFlag extends Skill
{
	private final FlagType _flagType;
	private final double _advancedMult;
	
	public SummonSiegeFlag(StatsSet set)
	{
		super(set);
		_flagType = set.getEnum("flagType", FlagType.class);
		_advancedMult = set.getDouble("advancedMultiplier", 1.0);
	}
	
	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		if(!activeChar.isPlayer())
		{
			return false;
		}
		if(!super.checkCondition(activeChar, target, forceUse, dontMove, first))
		{
			return false;
		}
		Player player = (Player) activeChar;
		if(player.getClan() == null || !player.isClanLeader())
		{
			return false;
		}
		switch(_flagType)
		{
			case DESTROY:
			{
				break;
			}
			case OUTPOST:
			case NORMAL:
			case ADVANCED:
			{
				if(player.isInZone(Zone.ZoneType.RESIDENCE))
				{
					player.sendPacket(SystemMsg.YOU_CANNOT_SET_UP_A_BASE_HERE, new SystemMessage2(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
					return false;
				}
				SiegeEvent siegeEvent = activeChar.getEvent(SiegeEvent.class);
				if(siegeEvent == null)
				{
					player.sendPacket(SystemMsg.YOU_CANNOT_SET_UP_A_BASE_HERE, new SystemMessage2(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
					return false;
				}
				boolean inZone = false;
				List<ZoneObject> zones = siegeEvent.getObjects("flag_zones");
				for(ZoneObject zone : zones)
				{
					if(!player.isInZone(zone.getZone()))
						continue;
					inZone = true;
				}
				if(!inZone)
				{
					player.sendPacket(SystemMsg.YOU_CANNOT_SET_UP_A_BASE_HERE, new SystemMessage2(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
					return false;
				}
				SiegeClanObject siegeClan = siegeEvent.getSiegeClan("attackers", player.getClan());
				if(siegeClan == null)
				{
					player.sendPacket(SystemMsg.YOU_CANNOT_SUMMON_THE_ENCAMPMENT_BECAUSE_YOU_ARE_NOT_A_MEMBER_OF_THE_SIEGE_CLAN_INVOLVED_IN_THE_CASTLE__FORTRESS__HIDEOUT_SIEGE, new SystemMessage2(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
					return false;
				}
				if(siegeClan.getFlag() == null)
					break;
				player.sendPacket(SystemMsg.AN_OUTPOST_OR_HEADQUARTERS_CANNOT_BE_BUILT_BECAUSE_ONE_ALREADY_EXISTS, new SystemMessage2(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
				return false;
			}
		}
		return true;
	}
	
	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		Player player = (Player) activeChar;
		Clan clan = player.getClan();
		if(clan == null || !player.isClanLeader())
		{
			return;
		}
		SiegeEvent siegeEvent = activeChar.getEvent(SiegeEvent.class);
		if(siegeEvent == null)
		{
			return;
		}
		SiegeClanObject siegeClan = siegeEvent.getSiegeClan("attackers", clan);
		if(siegeClan == null)
		{
			return;
		}
		switch(_flagType)
		{
			case DESTROY:
			{
				siegeClan.deleteFlag();
				break;
			}
			default:
			{
				if(siegeClan.getFlag() != null)
				{
					return;
				}
				SiegeFlagInstance flag = (SiegeFlagInstance) NpcHolder.getInstance().getTemplate(_flagType == FlagType.OUTPOST ? 36590 : 35062).getNewInstance();
				flag.setClan(siegeClan);
				flag.addEvent(siegeEvent);
				if(_flagType == FlagType.ADVANCED)
				{
					flag.addStatFunc(new FuncMul(Stats.MAX_HP, 80, flag, _advancedMult));
				}
				flag.setCurrentHpMp(flag.getMaxHp(), flag.getMaxMp(), true);
				flag.setHeading(player.getHeading());
				int x = (int) ((double) player.getX() + 100.0 * Math.cos(player.headingToRadians(player.getHeading() - 32768)));
				int y = (int) ((double) player.getY() + 100.0 * Math.sin(player.headingToRadians(player.getHeading() - 32768)));
				flag.spawnMe(GeoEngine.moveCheck(player.getX(), player.getY(), player.getZ(), x, y, player.getGeoIndex()));
				siegeClan.setFlag(flag);
			}
		}
	}
	
	public enum FlagType
	{
		DESTROY,
		NORMAL,
		ADVANCED,
		OUTPOST;
	}
}