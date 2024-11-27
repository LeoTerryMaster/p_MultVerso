package npc.model.residences.castle;

import l2.commons.util.Rnd;
import l2.gameserver.model.Player;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.templates.npc.NpcTemplate;
import l2.gameserver.utils.Location;

public class VenomTeleportCubicInstance extends NpcInstance
{
	public static final Location[] LOCS = {new Location(11913, -48851, -1088), new Location(11918, -49447, -1088)};
	
	public VenomTeleportCubicInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
		{
			return;
		}
		player.teleToLocation(LOCS[Rnd.get(LOCS.length)]);
	}
	
	@Override
	public void showChatWindow(Player player, int val, Object... arg)
	{
		showChatWindow(player, "residence2/castle/teleport_cube_benom001.htm");
	}
}