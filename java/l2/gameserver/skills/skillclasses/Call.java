package l2.gameserver.skills.skillclasses;

import l2.gameserver.cache.Msg;
import l2.gameserver.instancemanager.ReflectionManager;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.Zone;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.templates.StatsSet;
import l2.gameserver.utils.Location;

import java.util.List;

public class Call extends Skill
{
	final boolean _party;
	final int _requestWithCrystal;
	
	public Call(StatsSet set)
	{
		super(set);
		_party = set.getBool("party", false);
		_requestWithCrystal = set.getInteger("requestWithCrystal", -1);
	}
	
	public static SystemMessage canSummonHere(Player activeChar)
	{
		if(activeChar.isAlikeDead() || activeChar.isOlyParticipant() || activeChar.isInObserverMode() || activeChar.isFlying() || activeChar.isFestivalParticipant())
		{
			return Msg.NOTHING_HAPPENED;
		}
		if(activeChar.isInZoneBattle() || activeChar.isInZone(Zone.ZoneType.SIEGE) || activeChar.isInZone(Zone.ZoneType.no_restart) || activeChar.isInZone(Zone.ZoneType.no_summon) || activeChar.isInBoat() || activeChar.getReflection() != ReflectionManager.DEFAULT || activeChar.isInZone(Zone.ZoneType.fun))
		{
			return Msg.YOU_MAY_NOT_SUMMON_FROM_YOUR_CURRENT_LOCATION;
		}
		if(activeChar.isInStoreMode() || activeChar.isProcessingRequest())
		{
			return Msg.YOU_CANNOT_SUMMON_DURING_A_TRADE_OR_WHILE_USING_THE_PRIVATE_SHOPS;
		}
		return null;
	}
	
	public static SystemMessage canBeSummoned(Creature target)
	{
		if(target == null || !target.isPlayer() || target.isFlying() || target.isInObserverMode() || target.getPlayer().isFestivalParticipant() || !target.getPlayer().getPlayerAccess().UseTeleport)
		{
			return Msg.INVALID_TARGET;
		}
		if(target.isOlyParticipant())
		{
			return Msg.YOU_CANNOT_SUMMON_PLAYERS_WHO_ARE_CURRENTLY_PARTICIPATING_IN_THE_GRAND_OLYMPIAD;
		}
		if(target.isInZoneBattle() || target.isInZone(Zone.ZoneType.SIEGE) || target.isInZone(Zone.ZoneType.no_restart) || target.isInZone(Zone.ZoneType.no_summon) || target.getReflection() != ReflectionManager.DEFAULT || target.isInBoat() || target.isInZone(Zone.ZoneType.fun))
		{
			return Msg.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING;
		}
		if(target.isAlikeDead())
		{
			return new SystemMessage(1844).addString(target.getName());
		}
		if(target.getPvpFlag() != 0 || target.isInCombat())
		{
			return new SystemMessage(1843).addString(target.getName());
		}
		Player pTarget = (Player) target;
		if(pTarget.getPrivateStoreType() != 0 || pTarget.isProcessingRequest())
		{
			return new SystemMessage(1898).addString(target.getName());
		}
		return null;
	}
	
	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		if(activeChar.isPlayer())
		{
			if(_party && ((Player) activeChar).getParty() == null)
			{
				return false;
			}
			SystemMessage msg = canSummonHere((Player) activeChar);
			if(msg != null)
			{
				activeChar.sendPacket(msg);
				return false;
			}
			if(!_party)
			{
				if(activeChar == target)
				{
					return false;
				}
				msg = canBeSummoned(target);
				if(msg != null)
				{
					activeChar.sendPacket(msg);
					return false;
				}
			}
		}
		return super.checkCondition(activeChar, target, forceUse, dontMove, first);
	}
	
	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		if(!activeChar.isPlayer())
		{
			return;
		}
		SystemMessage msg = canSummonHere((Player) activeChar);
		if(msg != null)
		{
			activeChar.sendPacket(msg);
			return;
		}
		if(_party)
		{
			if(((Player) activeChar).getParty() != null)
			{
				for(Player target : ((Player) activeChar).getParty().getPartyMembers())
				{
					if(target.equals(activeChar) || canBeSummoned(target) != null)
						continue;
					if(_requestWithCrystal >= 0)
					{
						target.summonCharacterRequest(activeChar, Location.findPointToStay(activeChar, 100, 150), _requestWithCrystal);
					}
					else
					{
						target.stopMove();
						target.teleToLocation(Location.findPointToStay(activeChar, 100, 150), activeChar.getGeoIndex());
					}
					getEffects(activeChar, target, getActivateRate() > 0, false);
				}
			}
			if(isSSPossible())
			{
				activeChar.unChargeShots(isMagic());
			}
			return;
		}
		for(Creature target : targets)
		{
			if(target == null || canBeSummoned(target) != null)
				continue;
			if(_requestWithCrystal >= 0)
			{
				((Player) target).summonCharacterRequest(activeChar, Location.findAroundPosition(activeChar, 100, 150), _requestWithCrystal);
			}
			else
			{
				target.stopMove();
				target.teleToLocation(Location.findPointToStay(activeChar, 100, 150), activeChar.getGeoIndex());
			}
			getEffects(activeChar, target, getActivateRate() > 0, false);
		}
		if(isSSPossible())
		{
			activeChar.unChargeShots(isMagic());
		}
	}
}