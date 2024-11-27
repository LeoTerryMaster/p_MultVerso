package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Player;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.LinkedList;
import java.util.List;

public class ExPVPMatchCCRecord extends L2GameServerPacket
{
	private final List<Pair<String, Integer>> _result;
	private final PVPMatchCCAction _action;
	private int _len;
	
	public ExPVPMatchCCRecord(PVPMatchCCAction action)
	{
		_action = action;
		_len = 0;
		_result = new LinkedList<>();
	}
	
	public void addPlayer(Player player, int points)
	{
		++_len;
		_result.add((Pair<String, Integer>) new ImmutablePair(player.getName(), points));
	}
	
	@Override
	public void writeImpl()
	{
		writeEx(137);
		writeD(_action.getVal());
		writeD(_len);
		for(Pair<String, Integer> p : _result)
		{
			writeS(p.getLeft());
			writeD(p.getRight());
		}
	}
	
	public enum PVPMatchCCAction
	{
		INIT(0),
		UPDATE(1),
		DONE(2);
		
		private final int _val;
		
		PVPMatchCCAction(int val)
		{
			_val = val;
		}
		
		public int getVal()
		{
			return _val;
		}
	}
}