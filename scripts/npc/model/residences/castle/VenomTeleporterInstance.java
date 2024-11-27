package npc.model.residences.castle;

import l2.gameserver.model.Player;
import l2.gameserver.model.entity.residence.Castle;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.templates.npc.NpcTemplate;

public class VenomTeleporterInstance extends NpcInstance
{
	public VenomTeleporterInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		Castle castle = getCastle();
		if(castle.getSiegeEvent().isInProgress())
		{
			showChatWindow(player, "residence2/castle/rune_massymore_teleporter002.htm");
		}
		else
		{
			player.teleToLocation(12589, -49044, -3008);
		}
	}
	
	@Override
	public void showChatWindow(Player player, int val, Object... arg)
	{
		showChatWindow(player, "residence2/castle/rune_massymore_teleporter001.htm");
	}
}