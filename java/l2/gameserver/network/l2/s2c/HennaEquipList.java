package l2.gameserver.network.l2.s2c;

import l2.gameserver.data.xml.holder.HennaHolder;
import l2.gameserver.model.Player;
import l2.gameserver.templates.Henna;

import java.util.ArrayList;
import java.util.List;

public class HennaEquipList extends L2GameServerPacket
{
	private final int _emptySlots;
	private final long _adena;
	private final List<Henna> _hennas = new ArrayList<>();
	
	public HennaEquipList(Player player)
	{
		_adena = player.getAdena();
		_emptySlots = player.getHennaEmptySlots();
		List<Henna> list = HennaHolder.getInstance().generateList(player);
		for(Henna element : list)
		{
			if(player.getInventory().getItemByItemId(element.getDyeId()) == null)
				continue;
			_hennas.add(element);
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(226);
		writeD((int) _adena);
		writeD(_emptySlots);
		if(_hennas.size() != 0)
		{
			writeD(_hennas.size());
			for(Henna henna : _hennas)
			{
				writeD(henna.getSymbolId());
				writeD(henna.getDyeId());
				writeD((int) henna.getDrawCount());
				writeD((int) henna.getPrice());
				writeD(1);
			}
		}
		else
		{
			writeD(1);
			writeD(0);
			writeD(0);
			writeD(0);
			writeD(0);
			writeD(0);
		}
	}
}