package l2.gameserver.model.entity.events.impl;

import l2.commons.collections.MultiValueSet;
import l2.gameserver.dao.SiegeClanDAO;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.Zone;
import l2.gameserver.model.entity.events.objects.SiegeClanObject;
import l2.gameserver.model.entity.residence.ClanHall;
import l2.gameserver.model.pledge.Clan;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.PlaySound;
import l2.gameserver.network.l2.s2c.SystemMessage2;

public class ClanHallSiegeEvent extends SiegeEvent<ClanHall, SiegeClanObject>
{
	public static final String BOSS = "boss";
	
	public ClanHallSiegeEvent(MultiValueSet<String> set)
	{
		super(set);
	}
	
	@Override
	public void startEvent()
	{
		_oldOwner = getResidence().getOwner();
		if(_oldOwner != null)
		{
			getResidence().changeOwner(null);
			addObject("attackers", new SiegeClanObject("attackers", _oldOwner, 0));
		}
		if(getObjects("attackers").size() == 0)
		{
			broadcastInZone2(new SystemMessage2(SystemMsg.THE_SIEGE_OF_S1_HAS_BEEN_CANCELED_DUE_TO_LACK_OF_INTEREST).addResidenceName(getResidence()));
			reCalcNextTime(false);
			return;
		}
		SiegeClanDAO.getInstance().delete(getResidence());
		updateParticles(true, "attackers");
		broadcastTo(new SystemMessage2(SystemMsg.THE_SIEGE_TO_CONQUER_S1_HAS_BEGUN).addResidenceName(getResidence()), "attackers");
		super.startEvent();
	}
	
	@Override
	public void stopEvent(boolean step)
	{
		Clan newOwner = getResidence().getOwner();
		if(newOwner != null)
		{
			newOwner.broadcastToOnlineMembers(PlaySound.SIEGE_VICTORY);
			newOwner.incReputation(1700, false, toString());
			broadcastTo(new SystemMessage2(SystemMsg.S1_CLAN_HAS_DEFEATED_S2).addString(newOwner.getName()).addResidenceName(getResidence()), "attackers");
			broadcastTo(new SystemMessage2(SystemMsg.THE_SIEGE_OF_S1_IS_FINISHED).addResidenceName(getResidence()), "attackers");
		}
		else
		{
			broadcastTo(new SystemMessage2(SystemMsg.THE_SIEGE_OF_S1_HAS_ENDED_IN_A_DRAW).addResidenceName(getResidence()), "attackers");
		}
		updateParticles(false, "attackers");
		removeObjects("attackers");
		super.stopEvent(step);
		_oldOwner = null;
	}
	
	@Override
	public void setRegistrationOver(boolean b)
	{
		if(b)
		{
			broadcastTo(new SystemMessage2(SystemMsg.THE_DEADLINE_TO_REGISTER_FOR_THE_SIEGE_OF_S1_HAS_PASSED).addResidenceName(getResidence()), "attackers");
		}
		super.setRegistrationOver(b);
	}
	
	@Override
	public void processStep(Clan clan)
	{
		if(clan != null)
		{
			getResidence().changeOwner(clan);
		}
		stopEvent(true);
	}
	
	@Override
	public void loadSiegeClans()
	{
		addObjects("attackers", SiegeClanDAO.getInstance().load(getResidence(), "attackers"));
	}
	
	@Override
	public int getUserRelation(Player thisPlayer, int result)
	{
		return result;
	}
	
	@Override
	public int getRelation(Player thisPlayer, Player targetPlayer, int result)
	{
		return result;
	}
	
	@Override
	public boolean canResurrect(Player resurrectPlayer, Creature target, boolean force)
	{
		boolean playerInZone = resurrectPlayer.isInZone(Zone.ZoneType.SIEGE);
		boolean targetInZone = target.isInZone(Zone.ZoneType.SIEGE);
		if(!playerInZone && !targetInZone)
		{
			return true;
		}
		if(!targetInZone)
		{
			return false;
		}
		Player targetPlayer = target.getPlayer();
		ClanHallSiegeEvent siegeEvent = target.getEvent(ClanHallSiegeEvent.class);
		if(siegeEvent != this)
		{
			if(force)
			{
				targetPlayer.sendPacket(SystemMsg.IT_IS_NOT_POSSIBLE_TO_RESURRECT_IN_BATTLEFIELDS_WHERE_A_SIEGE_WAR_IS_TAKING_PLACE);
			}
			resurrectPlayer.sendPacket(force ? SystemMsg.IT_IS_NOT_POSSIBLE_TO_RESURRECT_IN_BATTLEFIELDS_WHERE_A_SIEGE_WAR_IS_TAKING_PLACE : SystemMsg.INVALID_TARGET);
			return false;
		}
		SiegeClanObject targetSiegeClan = siegeEvent.getSiegeClan("attackers", targetPlayer.getClan());
		if(targetSiegeClan.getFlag() == null)
		{
			if(force)
			{
				targetPlayer.sendPacket(SystemMsg.IF_A_BASE_CAMP_DOES_NOT_EXIST_RESURRECTION_IS_NOT_POSSIBLE);
			}
			resurrectPlayer.sendPacket(force ? SystemMsg.IF_A_BASE_CAMP_DOES_NOT_EXIST_RESURRECTION_IS_NOT_POSSIBLE : SystemMsg.INVALID_TARGET);
			return false;
		}
		if(force)
		{
			return true;
		}
		resurrectPlayer.sendPacket(SystemMsg.INVALID_TARGET);
		return false;
	}
}