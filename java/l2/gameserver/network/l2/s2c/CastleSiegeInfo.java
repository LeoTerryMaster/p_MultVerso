package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Player;
import l2.gameserver.model.entity.events.impl.CastleSiegeEvent;
import l2.gameserver.model.entity.residence.Castle;
import l2.gameserver.model.entity.residence.ClanHall;
import l2.gameserver.model.entity.residence.Residence;
import l2.gameserver.model.pledge.Alliance;
import l2.gameserver.model.pledge.Clan;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Calendar;

public class CastleSiegeInfo extends L2GameServerPacket
{
	private final int _id;
	private final int _ownerObjectId;
	private long _startTime;
	private int _allyId;
	private boolean _isLeader;
	private String _ownerName = "NPC";
	private String _leaderName = "";
	private String _allyName = "";
	private int[] _nextTimeMillis = ArrayUtils.EMPTY_INT_ARRAY;
	
	public CastleSiegeInfo(Castle castle, Player player)
	{
		this((Residence) castle, player);
		CastleSiegeEvent siegeEvent = castle.getSiegeEvent();
		long siegeTimeMillis = castle.getSiegeDate().getTimeInMillis();
		if(siegeTimeMillis == 0)
		{
			_nextTimeMillis = siegeEvent.getNextSiegeTimes();
		}
		else
		{
			_startTime = (int) (siegeTimeMillis / 1000);
		}
	}
	
	public CastleSiegeInfo(ClanHall ch, Player player)
	{
		this((Residence) ch, player);
		_startTime = (int) (ch.getSiegeDate().getTimeInMillis() / 1000);
	}
	
	protected CastleSiegeInfo(Residence residence, Player player)
	{
		_id = residence.getId();
		_ownerObjectId = residence.getOwnerId();
		Clan owner = residence.getOwner();
		if(owner != null)
		{
			_isLeader = owner.getLeaderId(0) == player.getObjectId();
			_ownerName = owner.getName();
			_leaderName = owner.getLeaderName(0);
			Alliance ally = owner.getAlliance();
			if(ally != null)
			{
				_allyId = ally.getAllyId();
				_allyName = ally.getAllyName();
			}
		}
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(201);
		writeD(_id);
		writeD(_isLeader ? 1 : 0);
		writeD(_ownerObjectId);
		writeS(_ownerName);
		writeS(_leaderName);
		writeD(_allyId);
		writeS(_allyName);
		writeD((int) (Calendar.getInstance().getTimeInMillis() / 1000));
		writeD((int) _startTime);
		if(_startTime == 0)
		{
			writeDD(_nextTimeMillis, true);
		}
	}
}