package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.entity.oly.CompetitionType;
import l2.gameserver.model.entity.oly.Stadium;

import java.util.ArrayList;

public class ExReceiveOlympiadGameList extends L2GameServerPacket
{
	private final ArrayList<GameRec> _games = new ArrayList();
	
	public void add(Stadium sid, CompetitionType _type, int _state, String p0, String p1)
	{
		_games.add(new GameRec(sid, _type, _state, p0, p1));
	}
	
	@Override
	protected void writeImpl()
	{
		writeEx(212);
		writeD(0);
		writeD(_games.size());
		writeD(0);
		for(GameRec gr : _games)
		{
			writeD(gr.stadium_id);
			writeD(gr.type);
			writeD(gr.state);
			writeS(gr.player0name);
			writeS(gr.player1name);
		}
	}
	
	private class GameRec
	{
		int stadium_id;
		int type;
		int state;
		String player0name;
		String player1name;
		
		public GameRec(Stadium sid, CompetitionType _type, int _state, String p0, String p1)
		{
			stadium_id = sid.getStadiumId();
			type = _type.getTypeIdx();
			state = _state;
			player0name = p0;
			player1name = p1;
		}
	}
}