package l2.gameserver.model.entity.events.impl;

import l2.commons.collections.MultiValueSet;
import l2.commons.dao.JdbcEntityState;
import l2.gameserver.Config;
import l2.gameserver.dao.SiegeClanDAO;
import l2.gameserver.instancemanager.PlayerMessageStack;
import l2.gameserver.model.Player;
import l2.gameserver.model.entity.events.actions.StartStopAction;
import l2.gameserver.model.entity.events.objects.AuctionSiegeClanObject;
import l2.gameserver.model.entity.events.objects.SiegeClanObject;
import l2.gameserver.model.entity.residence.ClanHall;
import l2.gameserver.model.pledge.Clan;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.SystemMessage2;
import l2.gameserver.tables.ClanTable;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class ClanHallAuctionEvent extends SiegeEvent<ClanHall, AuctionSiegeClanObject>
{
	private final Calendar _endSiegeDate = Calendar.getInstance();
	
	public ClanHallAuctionEvent(MultiValueSet<String> set)
	{
		super(set);
	}
	
	@Override
	public void reCalcNextTime(boolean onStart)
	{
		clearActions();
		_onTimeActions.clear();
		Clan owner = getResidence().getOwner();
		_endSiegeDate.setTimeInMillis(0);
		if(getResidence().getAuctionLength() == 0 && owner == null)
		{
			getResidence().getSiegeDate().setTimeInMillis(System.currentTimeMillis());
			getResidence().getSiegeDate().set(7, 2);
			getResidence().getSiegeDate().set(11, 15);
			getResidence().getSiegeDate().set(12, 0);
			getResidence().getSiegeDate().set(13, 0);
			getResidence().getSiegeDate().set(14, 0);
			getResidence().setAuctionLength(Config.CLNHALL_REWARD_CYCLE / 24);
			getResidence().setAuctionMinBid(getResidence().getBaseMinBid());
			getResidence().setJdbcState(JdbcEntityState.UPDATED);
			getResidence().update();
			_onTimeActions.clear();
			addOnTimeAction(0, new StartStopAction("event", true));
			addOnTimeAction(getResidence().getAuctionLength() * 86400, new StartStopAction("event", false));
			_endSiegeDate.setTimeInMillis(getResidence().getSiegeDate().getTimeInMillis() + (long) getResidence().getAuctionLength() * 86400000);
			registerActions();
		}
		else if(getResidence().getAuctionLength() != 0 || owner == null)
		{
			long endDate = getResidence().getSiegeDate().getTimeInMillis() + (long) getResidence().getAuctionLength() * 86400000;
			if(endDate <= System.currentTimeMillis())
			{
				getResidence().getSiegeDate().setTimeInMillis(System.currentTimeMillis() + 60000);
				_endSiegeDate.setTimeInMillis(System.currentTimeMillis() + 60000);
				_onTimeActions.clear();
				addOnTimeAction(0, new StartStopAction("event", true));
				addOnTimeAction(1, new StartStopAction("event", false));
				registerActions();
			}
			else
			{
				_endSiegeDate.setTimeInMillis(getResidence().getSiegeDate().getTimeInMillis() + (long) getResidence().getAuctionLength() * 86400000);
				_onTimeActions.clear();
				addOnTimeAction(0, new StartStopAction("event", true));
				addOnTimeAction((int) getEndSiegeForCH(), new StartStopAction("event", false));
				registerActions();
			}
		}
	}
	
	@Override
	public void stopEvent(boolean step)
	{
		List<AuctionSiegeClanObject> siegeClanObjects = removeObjects("attackers");
		AuctionSiegeClanObject[] clans = siegeClanObjects.toArray(new AuctionSiegeClanObject[siegeClanObjects.size()]);
		Arrays.sort(clans, SiegeClanObject.SiegeClanComparatorImpl.getInstance());
		Clan oldOwner = getResidence().getOwner();
		AuctionSiegeClanObject winnerSiegeClan;
		AuctionSiegeClanObject auctionSiegeClanObject = winnerSiegeClan = clans.length > 0 ? clans[0] : null;
		if(winnerSiegeClan != null)
		{
			SystemMessage2 msg = new SystemMessage2(SystemMsg.THE_CLAN_HALL_WHICH_WAS_PUT_UP_FOR_AUCTION_HAS_BEEN_AWARDED_TO_S1_CLAN).addString(winnerSiegeClan.getClan().getName());
			for(AuctionSiegeClanObject siegeClan : siegeClanObjects)
			{
				try
				{
					Player player = siegeClan.getClan().getLeader().getPlayer();
					if(player != null)
					{
						player.sendPacket(msg);
					}
					else
					{
						PlayerMessageStack.getInstance().mailto(siegeClan.getClan().getLeaderId(), msg);
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				if(siegeClan == winnerSiegeClan)
					continue;
				long returnBid = siegeClan.getParam() - (long) ((double) siegeClan.getParam() * 0.1);
				siegeClan.getClan().getWarehouse().addItem(Config.CH_BID_CURRENCY_ITEM_ID, returnBid);
			}
			SiegeClanDAO.getInstance().delete(getResidence());
			if(oldOwner != null)
			{
				oldOwner.getWarehouse().addItem(Config.CH_BID_CURRENCY_ITEM_ID, getResidence().getDeposit());
			}
			getResidence().setAuctionLength(0);
			getResidence().setAuctionMinBid(0);
			getResidence().setAuctionDescription("");
			getResidence().getSiegeDate().setTimeInMillis(0);
			getResidence().getLastSiegeDate().setTimeInMillis(0);
			getResidence().getOwnDate().setTimeInMillis(System.currentTimeMillis());
			getResidence().setJdbcState(JdbcEntityState.UPDATED);
			getResidence().changeOwner(winnerSiegeClan.getClan());
			getResidence().startCycleTask();
		}
		else if(oldOwner != null)
		{
			Player player = oldOwner.getLeader().getPlayer();
			if(player != null)
			{
				player.sendPacket(SystemMsg.THE_CLAN_HALL_WHICH_HAD_BEEN_PUT_UP_FOR_AUCTION_WAS_NOT_SOLD_AND_THEREFORE_HAS_BEEN_RELISTED);
			}
			else
			{
				PlayerMessageStack.getInstance().mailto(oldOwner.getLeaderId(), SystemMsg.THE_CLAN_HALL_WHICH_HAD_BEEN_PUT_UP_FOR_AUCTION_WAS_NOT_SOLD_AND_THEREFORE_HAS_BEEN_RELISTED.packet(null));
			}
		}
		else
		{
			getResidence().setAuctionLength(0);
			getResidence().setAuctionMinBid(0);
			getResidence().setAuctionDescription("");
			getResidence().getSiegeDate().setTimeInMillis(0);
			getResidence().getLastSiegeDate().setTimeInMillis(0);
			getResidence().getOwnDate().setTimeInMillis(0);
			getResidence().setJdbcState(JdbcEntityState.UPDATED);
		}
		super.stopEvent(step);
	}
	
	@Override
	public boolean isParticle(Player player)
	{
		return false;
	}
	
	@Override
	public AuctionSiegeClanObject newSiegeClan(String type, int clanId, long param, long date)
	{
		Clan clan = ClanTable.getInstance().getClan(clanId);
		return clan == null ? null : new AuctionSiegeClanObject(type, clan, param, date);
	}
	
	public long getEndSiegeForCH()
	{
		long start_date_msec = getResidence().getSiegeDate().getTimeInMillis();
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(getResidence().getSiegeDate().getTimeInMillis());
		cal.set(7, 2);
		cal.set(11, 15);
		cal.set(12, 0);
		cal.set(13, 0);
		cal.set(14, 0);
		long end_date = cal.getTimeInMillis() + (long) getResidence().getAuctionLength() * 86400000;
		return (end_date - start_date_msec) / 1000;
	}
	
	public Calendar getEndSiegeDate()
	{
		return _endSiegeDate;
	}
}