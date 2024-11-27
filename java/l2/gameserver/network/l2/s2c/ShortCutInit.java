package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Player;
import l2.gameserver.model.actor.instances.player.ShortCut;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ShortCutInit extends ShortCutPacket
{
	private List<ShortCutPacket.ShortcutInfo> _shortCuts = Collections.emptyList();
	
	public ShortCutInit(Player pl)
	{
		Collection<ShortCut> shortCuts = pl.getAllShortCuts();
		_shortCuts = new ArrayList<>(shortCuts.size());
		for(ShortCut shortCut : shortCuts)
		{
			_shortCuts.add(convert(pl, shortCut));
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(69);
		writeD(_shortCuts.size());
		for(ShortCutPacket.ShortcutInfo sc : _shortCuts)
		{
			sc.write(this);
		}
	}
}