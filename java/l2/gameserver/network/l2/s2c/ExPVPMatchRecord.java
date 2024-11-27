package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Player;
import l2.gameserver.model.base.TeamType;

import java.util.LinkedList;

public class ExPVPMatchRecord extends L2GameServerPacket
{
	private final PVPMatchAction _action;
	private final LinkedList<PVPMatchRecord> _red_records;
	private final LinkedList<PVPMatchRecord> _blue_records;
	private final TeamType _winner;
	private final int _blue;
	private final int _red;
	private int _blue_cnt;
	private int _red_cnt;
	
	public ExPVPMatchRecord(PVPMatchAction action, TeamType winner, int blue, int red)
	{
		_action = action;
		_red_records = new LinkedList();
		_blue_records = new LinkedList();
		_winner = winner;
		_blue = blue;
		_red = red;
	}
	
	public void addRecord(Player player, int kills, int dies)
	{
		if(player.getTeam() == TeamType.RED)
		{
			_red_records.add(new PVPMatchRecord(player.getName(), kills, dies));
			++_red_cnt;
		}
		else if(player.getTeam() == TeamType.BLUE)
		{
			_blue_records.add(new PVPMatchRecord(player.getName(), kills, dies));
			++_blue_cnt;
		}
	}
	
	@Override
	protected void writeImpl()
	{
		writeEx(126);
		writeD(_action.getVal());
		if(_winner == TeamType.RED)
		{
			writeD(2);
			writeD(1);
		}
		else if(_winner == TeamType.BLUE)
		{
			writeD(1);
			writeD(2);
		}
		else
		{
			writeD(0);
			writeD(0);
		}
		writeD(_blue);
		writeD(_red);
		if(_blue_cnt > 0)
		{
			writeD(_blue_records.size());
			for(PVPMatchRecord record : _blue_records)
			{
				writeS(record.name);
				writeD(record.kill);
				writeD(record.die);
			}
		}
		else
		{
			writeD(0);
		}
		if(_red_cnt > 0)
		{
			writeD(_red_records.size());
			for(PVPMatchRecord record : _red_records)
			{
				writeS(record.name);
				writeD(record.kill);
				writeD(record.die);
			}
		}
		else
		{
			writeD(0);
		}
	}
	
	public enum PVPMatchAction
	{
		INIT(0),
		UPDATE(1),
		DONE(2);
		
		private final int _val;
		
		PVPMatchAction(int val)
		{
			_val = val;
		}
		
		public int getVal()
		{
			return _val;
		}
	}
	
	private class PVPMatchRecord
	{
		public final String name;
		public final int kill;
		public final int die;
		
		public PVPMatchRecord(String _name, int _kill, int _die)
		{
			name = _name;
			kill = _kill;
			die = _die;
		}
	}
}