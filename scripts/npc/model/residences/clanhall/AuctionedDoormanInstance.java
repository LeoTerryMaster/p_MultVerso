package npc.model.residences.clanhall;

import l2.gameserver.model.Player;
import l2.gameserver.model.entity.residence.ClanHall;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.pledge.Clan;
import l2.gameserver.model.pledge.Privilege;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2.gameserver.templates.npc.NpcTemplate;
import l2.gameserver.utils.ReflectionUtils;
import org.apache.commons.lang3.ArrayUtils;

public class AuctionedDoormanInstance extends NpcInstance
{
	private final int[] _doors;
	private final boolean _elite;
	
	public AuctionedDoormanInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		_doors = template.getAIParams().getIntegerArray("doors", ArrayUtils.EMPTY_INT_ARRAY);
		_elite = template.getAIParams().getBool("elite", false);
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
		{
			return;
		}
		ClanHall clanHall = getClanHall();
		if(command.equalsIgnoreCase("openDoors"))
		{
			if(player.hasPrivilege(Privilege.CH_ENTER_EXIT) && player.getClan().getHasHideout() == clanHall.getId())
			{
				for(int d : _doors)
				{
					ReflectionUtils.getDoor(d).openMe();
				}
				showChatWindow(player, "residence2/clanhall/agitafterdooropen.htm");
			}
			else
			{
				showChatWindow(player, "residence2/clanhall/noAuthority.htm");
			}
		}
		else if(command.equalsIgnoreCase("closeDoors"))
		{
			if(player.hasPrivilege(Privilege.CH_ENTER_EXIT) && player.getClan().getHasHideout() == clanHall.getId())
			{
				for(int d : _doors)
				{
					ReflectionUtils.getDoor(d).closeMe(player, true);
				}
				showChatWindow(player, "residence2/clanhall/agitafterdoorclose.htm");
			}
			else
			{
				showChatWindow(player, "residence2/clanhall/noAuthority.htm");
			}
		}
		else if(command.equalsIgnoreCase("banish"))
		{
			if(player.hasPrivilege(Privilege.CH_DISMISS))
			{
				clanHall.banishForeigner();
				showChatWindow(player, "residence2/clanhall/agitafterbanish.htm");
			}
			else
			{
				showChatWindow(player, "residence2/clanhall/noAuthority.htm");
			}
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
	
	@Override
	public void showChatWindow(Player player, int val, Object... arg)
	{
		ClanHall clanHall = getClanHall();
		if(clanHall != null)
		{
			Clan ownerClan = clanHall.getOwner();
			if(ownerClan != null)
			{
				Clan playerClan = player.getClan();
				if(playerClan != null && playerClan == ownerClan)
				{
					showChatWindow(player, _elite ? "residence2/clanhall/WyvernAgitJanitorHi.htm" : "residence2/clanhall/AgitJanitorHi.htm", "%owner%", playerClan.getName());
				}
				else
				{
					NpcHtmlMessage html = new NpcHtmlMessage(player, this);
					html.setFile("residence2/clanhall/defaultAgitInfo.htm");
					html.replace("<?my_owner_name?>", ownerClan.getLeaderName());
					html.replace("<?my_pledge_name?>", ownerClan.getName());
					player.sendPacket(html);
				}
			}
			else
			{
				showChatWindow(player, "residence2/clanhall/noAgitInfo.htm");
			}
		}
	}
	
	@Override
	protected boolean canInteractWithKarmaPlayer()
	{
		return true;
	}
}