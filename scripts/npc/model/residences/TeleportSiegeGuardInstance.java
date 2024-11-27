package npc.model.residences;

import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.entity.events.impl.SiegeEvent;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.pledge.Clan;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2.gameserver.templates.npc.NpcTemplate;

public class TeleportSiegeGuardInstance extends NpcInstance
{
	protected static final int COND_ALL_FALSE = 0;
	protected static final int BUSY_BECAUSE_SIEGE_NOT_INPROGRESS = 1;
	protected static final int COND_OWNER = 2;
	
	public TeleportSiegeGuardInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void showChatWindow(Player player, int val, Object... arg)
	{
		player.sendActionFailed();
		int condition = validateCondition(player);
		String filename = condition == 2 ? "castle/teleporter/" + getNpcId() + ".htm" : "castle/teleporter/castleteleporter-no.htm";
		NpcHtmlMessage html = new NpcHtmlMessage(player, this);
		html.setFile(filename);
		player.sendPacket(html);
	}
	
	@Override
	public boolean isAttackable(Creature attacker)
	{
		Player player = attacker.getPlayer();
		if(player == null)
		{
			return false;
		}
		SiegeEvent siegeEvent = getEvent(SiegeEvent.class);
		SiegeEvent siegeEvent2 = attacker.getEvent(SiegeEvent.class);
		Clan clan = player.getClan();
		if(siegeEvent == null)
		{
			return false;
		}
		return clan == null || siegeEvent != siegeEvent2 || siegeEvent.getSiegeClan("defenders", clan) == null;
	}
	
	protected int validateCondition(Player player)
	{
		if(getCastle() != null && getCastle().getId() > 0 && player.getClan() != null)
		{
			if(getCastle().getSiegeEvent().isInProgress())
			{
				return 1;
			}
			if(getCastle().getOwnerId() == player.getClanId())
			{
				return 2;
			}
		}
		return 0;
	}
	
	@Override
	public boolean isFearImmune()
	{
		return true;
	}
	
	@Override
	public boolean isParalyzeImmune()
	{
		return true;
	}
}