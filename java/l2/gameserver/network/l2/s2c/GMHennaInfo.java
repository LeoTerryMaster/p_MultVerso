package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Player;
import l2.gameserver.templates.Henna;

public class GMHennaInfo extends L2GameServerPacket
{
	private final Henna[] _hennas = new Henna[3];
	private final int _count;
	private final int _str;
	private final int _con;
	private final int _dex;
	private final int _int;
	private final int _wit;
	private final int _men;
	
	public GMHennaInfo(Player cha)
	{
		_str = cha.getHennaStatSTR();
		_con = cha.getHennaStatCON();
		_dex = cha.getHennaStatDEX();
		_int = cha.getHennaStatINT();
		_wit = cha.getHennaStatWIT();
		_men = cha.getHennaStatMEN();
		int j = 0;
		for(int i = 0;i < 3;++i)
		{
			Henna h = cha.getHenna(i + 1);
			if(h == null)
				continue;
			_hennas[j++] = h;
		}
		_count = j;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(234);
		writeC(_int);
		writeC(_str);
		writeC(_con);
		writeC(_men);
		writeC(_dex);
		writeC(_wit);
		writeD(3);
		writeD(_count);
		for(int i = 0;i < _count;++i)
		{
			writeD(_hennas[i].getSymbolId());
			writeD(_hennas[i].getSymbolId());
		}
	}
}