package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Player;

public class HennaInfo extends L2GameServerPacket
{
	private final Henna[] _hennas = new Henna[3];
	private final int _str;
	private final int _con;
	private final int _dex;
	private final int _int;
	private final int _wit;
	private final int _men;
	private final int slots;
	private int _count;
	
	public HennaInfo(Player player)
	{
		for(int i = 0;i < 3;++i)
		{
			l2.gameserver.templates.Henna h = player.getHenna(i + 1);
			if(h == null)
				continue;
			_hennas[_count++] = new Henna(h.getSymbolId(), h.isForThisClass(player));
		}
		_str = player.getHennaStatSTR();
		_con = player.getHennaStatCON();
		_dex = player.getHennaStatDEX();
		_int = player.getHennaStatINT();
		_wit = player.getHennaStatWIT();
		_men = player.getHennaStatMEN();
		slots = player.getLevel() < 40 ? 2 : 3;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(228);
		writeC(_int);
		writeC(_str);
		writeC(_con);
		writeC(_men);
		writeC(_dex);
		writeC(_wit);
		writeD(slots);
		writeD(_count);
		for(int i = 0;i < _count;++i)
		{
			writeD(_hennas[i]._symbolId);
			writeD(_hennas[i]._valid ? _hennas[i]._symbolId : 0);
		}
	}
	
	private static class Henna
	{
		private final int _symbolId;
		private final boolean _valid;
		
		public Henna(int sy, boolean valid)
		{
			_symbolId = sy;
			_valid = valid;
		}
	}
}