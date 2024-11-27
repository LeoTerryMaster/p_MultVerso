package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Player;
import l2.gameserver.model.actor.instances.player.ShortCut;

public class ShortCutRegister extends ShortCutPacket
{
	private final ShortCutPacket.ShortcutInfo _shortcutInfo;
	
	public ShortCutRegister(Player player, ShortCut sc)
	{
		_shortcutInfo = convert(player, sc);
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(68);
		_shortcutInfo.write(this);
	}
}