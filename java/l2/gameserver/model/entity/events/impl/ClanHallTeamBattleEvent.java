package l2.gameserver.model.entity.events.impl;

import l2.commons.collections.CollectionUtils;
import l2.commons.collections.MultiValueSet;
import l2.gameserver.dao.SiegeClanDAO;
import l2.gameserver.dao.SiegePlayerDAO;
import l2.gameserver.model.Player;
import l2.gameserver.model.base.RestartType;
import l2.gameserver.model.entity.events.objects.CTBSiegeClanObject;
import l2.gameserver.model.entity.events.objects.CTBTeamObject;
import l2.gameserver.model.entity.events.objects.SiegeClanObject;
import l2.gameserver.model.entity.residence.ClanHall;
import l2.gameserver.model.pledge.Clan;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.PlaySound;
import l2.gameserver.network.l2.s2c.SystemMessage2;
import l2.gameserver.tables.ClanTable;
import l2.gameserver.utils.Location;

import java.util.List;

public class ClanHallTeamBattleEvent extends SiegeEvent<ClanHall, CTBSiegeClanObject>
{
	public static final String TRYOUT_PART = "tryout_part";
	public static final String CHALLENGER_RESTART_POINTS = "challenger_restart_points";
	public static final String FIRST_DOORS = "first_doors";
	public static final String SECOND_DOORS = "second_doors";
	public static final String NEXT_STEP = "next_step";
	
	public ClanHallTeamBattleEvent(MultiValueSet<String> set)
	{
		super(set);
	}
	
	@Override
	public void startEvent()
	{
		_oldOwner = getResidence().getOwner();
		List attackers = getObjects("attackers");
		if(attackers.isEmpty())
		{
			if(_oldOwner == null)
			{
				broadcastInZone2(new SystemMessage2(SystemMsg.THE_SIEGE_OF_S1_HAS_BEEN_CANCELED_DUE_TO_LACK_OF_INTEREST).addResidenceName(getResidence()));
			}
			else
			{
				broadcastInZone2(new SystemMessage2(SystemMsg.S1S_SIEGE_WAS_CANCELED_BECAUSE_THERE_WERE_NO_CLANS_THAT_PARTICIPATED).addResidenceName(getResidence()));
			}
			reCalcNextTime(false);
			return;
		}
		if(_oldOwner != null)
		{
			addObject("defenders", new SiegeClanObject("defenders", _oldOwner, 0));
		}
		SiegeClanDAO.getInstance().delete(getResidence());
		SiegePlayerDAO.getInstance().delete(getResidence());
		List teams = getObjects("tryout_part");
		for(int i = 0;i < 5;++i)
		{
			CTBTeamObject team = (CTBTeamObject) teams.get(i);
			team.setSiegeClan((CTBSiegeClanObject) CollectionUtils.safeGet(attackers, i));
		}
		broadcastTo(new SystemMessage2(SystemMsg.THE_SIEGE_TO_CONQUER_S1_HAS_BEGUN).addResidenceName(getResidence()), "attackers", "defenders");
		broadcastTo(SystemMsg.THE_TRYOUTS_ARE_ABOUT_TO_BEGIN, "attackers");
		super.startEvent();
	}
	
	public void nextStep()
	{
		broadcastTo(SystemMsg.THE_TRYOUTS_HAVE_BEGUN, "attackers", "defenders");
		updateParticles(true, "attackers", "defenders");
	}
	
	public void processStep(CTBTeamObject team)
	{
		if(team.getSiegeClan() != null)
		{
			CTBSiegeClanObject object = team.getSiegeClan();
			object.setEvent(false, this);
			teleportPlayers("spectators");
		}
		team.despawnObject(this);
		List<CTBTeamObject> teams = getObjects("tryout_part");
		boolean hasWinner = false;
		CTBTeamObject winnerTeam = null;
		for(CTBTeamObject t : teams)
		{
			if(!t.isParticle())
				continue;
			hasWinner = winnerTeam == null;
			winnerTeam = t;
		}
		if(!hasWinner)
		{
			return;
		}
		CTBSiegeClanObject clan = winnerTeam.getSiegeClan();
		if(clan != null)
		{
			getResidence().changeOwner(clan.getClan());
		}
		stopEvent(true);
	}
	
	@Override
	public void announce(int val)
	{
		int minute = val / 60;
		if(minute > 0)
		{
			broadcastTo(new SystemMessage2(SystemMsg.THE_CONTEST_WILL_BEGIN_IN_S1_MINUTES).addInteger(minute), "attackers", "defenders");
		}
		else
		{
			broadcastTo(new SystemMessage2(SystemMsg.THE_PRELIMINARY_MATCH_WILL_BEGIN_IN_S1_SECONDS).addInteger(val), "attackers", "defenders");
		}
	}
	
	@Override
	public void stopEvent(boolean step)
	{
		Clan newOwner = getResidence().getOwner();
		if(newOwner != null)
		{
			if(_oldOwner != newOwner)
			{
				newOwner.broadcastToOnlineMembers(PlaySound.SIEGE_VICTORY);
				newOwner.incReputation(1700, false, toString());
			}
			broadcastTo(new SystemMessage2(SystemMsg.S1_CLAN_HAS_DEFEATED_S2).addString(newOwner.getName()).addResidenceName(getResidence()), "attackers", "defenders");
			broadcastTo(new SystemMessage2(SystemMsg.THE_SIEGE_OF_S1_IS_FINISHED).addResidenceName(getResidence()), "attackers", "defenders");
		}
		else
		{
			broadcastTo(new SystemMessage2(SystemMsg.THE_PRELIMINARY_MATCH_OF_S1_HAS_ENDED_IN_A_DRAW).addResidenceName(getResidence()), "attackers");
		}
		updateParticles(false, "attackers", "defenders");
		removeObjects("defenders");
		removeObjects("attackers");
		super.stopEvent(step);
		_oldOwner = null;
	}
	
	@Override
	public void loadSiegeClans()
	{
		List<SiegeClanObject> siegeClanObjectList = SiegeClanDAO.getInstance().load(getResidence(), "attackers");
		addObjects("attackers", siegeClanObjectList);
		List<CTBSiegeClanObject> objects = getObjects("attackers");
		for(CTBSiegeClanObject clan : objects)
		{
			clan.select(getResidence());
		}
	}
	
	@Override
	public CTBSiegeClanObject newSiegeClan(String type, int clanId, long i, long date)
	{
		Clan clan = ClanTable.getInstance().getClan(clanId);
		return clan == null ? null : new CTBSiegeClanObject(type, clan, i, date);
	}
	
	@Override
	public boolean isParticle(Player player)
	{
		if(!isInProgress() || player.getClan() == null)
		{
			return false;
		}
		CTBSiegeClanObject object = getSiegeClan("attackers", player.getClan());
		return object != null && object.getPlayers().contains(player.getObjectId());
	}
	
	@Override
	public Location getRestartLoc(Player player, RestartType type)
	{
		if(!checkIfInZone(player))
		{
			return null;
		}
		Object attackerClan = getSiegeClan("attackers", player.getClan());
		Location loc = null;
		switch(type)
		{
			case TO_VILLAGE:
			{
				if(attackerClan == null || !checkIfInZone(player))
					break;
				List objectList = getObjects("attackers");
				List teleportList = getObjects("challenger_restart_points");
				int index = objectList.indexOf(attackerClan);
				loc = (Location) teleportList.get(index);
			}
		}
		return loc;
	}
	
	@Override
	public void action(String name, boolean start)
	{
		if(name.equalsIgnoreCase("next_step"))
		{
			nextStep();
		}
		else
		{
			super.action(name, start);
		}
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
}