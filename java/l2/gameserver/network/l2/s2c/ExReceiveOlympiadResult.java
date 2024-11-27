package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Player;
import l2.gameserver.model.entity.oly.Participant;

import java.util.ArrayList;

public class ExReceiveOlympiadResult extends L2GameServerPacket
{
	private final String _winner;
	private final int _winner_side;
	private final ArrayList<ExReceiveOlympiadResultRecord> _Red;
	private final ArrayList<ExReceiveOlympiadResultRecord> _Blue;
	
	public ExReceiveOlympiadResult(int winner_side, String winner)
	{
		_winner = winner;
		_winner_side = winner_side;
		_Red = new ArrayList();
		_Blue = new ArrayList();
	}
	
	public void add(int side, Player player, int dmg, int points, int delta)
	{
		if(side == Participant.SIDE_RED)
		{
			_Red.add(new ExReceiveOlympiadResultRecord(player, dmg, points, delta));
		}
		if(side == Participant.SIDE_BLUE)
		{
			_Blue.add(new ExReceiveOlympiadResultRecord(player, dmg, points, delta));
		}
	}
	
	@Override
	protected void writeImpl()
	{
		writeEx(212);
		writeD(1);
		if(_winner_side != 0)
		{
			writeD(0);
			writeS(_winner);
		}
		else
		{
			writeD(1);
			writeS("");
		}
		if(_winner_side == Participant.SIDE_RED)
		{
			writeD(1);
			writeD(_Red.size());
			for(ExReceiveOlympiadResultRecord orr : _Red)
			{
				writeS(orr.name);
				writeS(orr.clan);
				writeD(orr.crest_id);
				writeD(orr.class_id);
				writeD(orr.dmg);
				writeD(orr.points);
				writeD(orr.delta);
			}
			writeD(0);
			writeD(_Blue.size());
			for(ExReceiveOlympiadResultRecord orb : _Blue)
			{
				writeS(orb.name);
				writeS(orb.clan);
				writeD(orb.crest_id);
				writeD(orb.class_id);
				writeD(orb.dmg);
				writeD(orb.points);
				writeD(orb.delta);
			}
		}
		else
		{
			writeD(0);
			writeD(_Blue.size());
			for(ExReceiveOlympiadResultRecord orb : _Blue)
			{
				writeS(orb.name);
				writeS(orb.clan);
				writeD(orb.crest_id);
				writeD(orb.class_id);
				writeD(orb.dmg);
				writeD(orb.points);
				writeD(orb.delta);
			}
			writeD(1);
			writeD(_Red.size());
			for(ExReceiveOlympiadResultRecord orr : _Red)
			{
				writeS(orr.name);
				writeS(orr.clan);
				writeD(orr.crest_id);
				writeD(orr.class_id);
				writeD(orr.dmg);
				writeD(orr.points);
				writeD(orr.delta);
			}
		}
	}
	
	private class ExReceiveOlympiadResultRecord
	{
		String name;
		String clan;
		int class_id;
		int crest_id;
		int dmg;
		int points;
		int delta;
		
		public ExReceiveOlympiadResultRecord(Player player, int _dmg, int _points, int _delta)
		{
			name = player.getName();
			class_id = player.getClassId().getId();
			clan = player.getClan() != null ? player.getClan().getName() : "";
			crest_id = player.getClanId();
			dmg = _dmg;
			points = _points;
			delta = _delta;
		}
	}
}