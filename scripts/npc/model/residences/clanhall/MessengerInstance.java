package npc.model.residences.clanhall;

import l2.gameserver.model.Player;
import l2.gameserver.model.entity.events.impl.ClanHallSiegeEvent;
import l2.gameserver.model.entity.residence.ClanHall;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.network.l2.s2c.CastleSiegeInfo;
import l2.gameserver.templates.npc.NpcTemplate;

public class MessengerInstance extends NpcInstance
{
	private final String _siegeDialog;
	private final String _ownerDialog;
	
	public MessengerInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		_siegeDialog = template.getAIParams().getString("siege_dialog");
		_ownerDialog = template.getAIParams().getString("owner_dialog");
	}
	
	@Override
	public void showChatWindow(Player player, int val, Object... arg)
	{
		ClanHall clanHall = getClanHall();
		ClanHallSiegeEvent siegeEvent = clanHall.getSiegeEvent();
		if(clanHall.getOwner() != null && clanHall.getOwner() == player.getClan())
		{
			showChatWindow(player, _ownerDialog);
		}
		else if(siegeEvent.isInProgress())
		{
			showChatWindow(player, _siegeDialog);
		}
		else
		{
			player.sendPacket(new CastleSiegeInfo(clanHall, player));
		}
	}
}