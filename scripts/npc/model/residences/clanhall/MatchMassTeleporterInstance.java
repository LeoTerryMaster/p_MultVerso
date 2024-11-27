package npc.model.residences.clanhall;

import l2.gameserver.model.Player;
import l2.gameserver.model.World;
import l2.gameserver.model.entity.events.impl.ClanHallTeamBattleEvent;
import l2.gameserver.model.entity.events.objects.CTBTeamObject;
import l2.gameserver.model.entity.residence.ClanHall;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.templates.npc.NpcTemplate;
import l2.gameserver.utils.Location;

import java.util.List;

public class MatchMassTeleporterInstance extends NpcInstance
{
	private final int _flagId;
	private long _timeout;
	
	public MatchMassTeleporterInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		_flagId = template.getAIParams().getInteger("flag");
	}
	
	@Override
	public void showChatWindow(Player player, int val, Object... arg)
	{
		ClanHall clanHall = getClanHall();
		ClanHallTeamBattleEvent siegeEvent = clanHall.getSiegeEvent();
		if(_timeout > System.currentTimeMillis())
		{
			showChatWindow(player, "residence2/clanhall/agit_mass_teleporter001.htm");
			return;
		}
		if(isInActingRange(player))
		{
			_timeout = System.currentTimeMillis() + 60000;
			List locs = siegeEvent.getObjects("tryout_part");
			CTBTeamObject object = (CTBTeamObject) locs.get(_flagId);
			if(object.getFlag() != null)
			{
				for(Player aroundPlayer : World.getAroundPlayers(this, 400, 100))
				{
					aroundPlayer.teleToLocation(Location.findPointToStay(object.getFlag(), 100, 125));
				}
			}
		}
	}
}