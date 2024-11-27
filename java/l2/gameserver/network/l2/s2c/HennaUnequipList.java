package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Player;
import l2.gameserver.templates.Henna;

import java.util.ArrayList;
import java.util.List;

public class HennaUnequipList extends L2GameServerPacket
{
	private final int _emptySlots;
	private final long _adena;
	private final List<Henna> availHenna = new ArrayList<>(3);
	
	public HennaUnequipList(Player player)
	{
		_adena = player.getAdena();
		_emptySlots = player.getHennaEmptySlots();
		for(int i = 1;i <= 3;++i)
		{
			if(player.getHenna(i) == null)
				continue;
			availHenna.add(player.getHenna(i));
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(229);
		writeD((int) _adena);
		writeD(_emptySlots);
		writeD(availHenna.size());
		for(Henna henna : availHenna)
		{
			writeD(henna.getSymbolId());
			writeD(henna.getDyeId());
			writeD((int) henna.getDrawCount());
			writeD((int) henna.getPrice());
			writeD(1);
		}
	}
}