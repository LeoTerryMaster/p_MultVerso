package npc.model.residences.clanhall;

import l2.gameserver.dao.SiegeClanDAO;
import l2.gameserver.model.Player;
import l2.gameserver.model.entity.events.impl.ClanHallMiniGameEvent;
import l2.gameserver.model.entity.events.objects.CMGSiegeClanObject;
import l2.gameserver.model.entity.events.objects.SiegeClanObject;
import l2.gameserver.model.entity.residence.ClanHall;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.pledge.Clan;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2.gameserver.templates.npc.NpcTemplate;
import l2.gameserver.utils.ItemFunctions;
import l2.gameserver.utils.TimeUtils;

public class RainbowMessengerInstance extends NpcInstance
{
	public static final int ITEM_ID = 8034;
	
	public RainbowMessengerInstance(int objectId, NpcTemplate template)
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
		ClanHall clanHall = getClanHall();
		ClanHallMiniGameEvent miniGameEvent = clanHall.getSiegeEvent();
		if(command.equalsIgnoreCase("register"))
		{
			if(miniGameEvent.isRegistrationOver())
			{
				showChatWindow(player, "residence2/clanhall/messenger_yetti014.htm");
				return;
			}
			Clan clan = player.getClan();
			if(clan == null || clan.getLevel() < 3 || clan.getAllSize() <= 5)
			{
				showChatWindow(player, "residence2/clanhall/messenger_yetti011.htm");
				return;
			}
			if(clan.getLeaderId() != player.getObjectId())
			{
				showChatWindow(player, "residence2/clanhall/messenger_yetti010.htm");
				return;
			}
			if(clan.getHasHideout() > 0)
			{
				showChatWindow(player, "residence2/clanhall/messenger_yetti012.htm");
				return;
			}
			if(miniGameEvent.getSiegeClan("attackers", clan) != null)
			{
				showChatWindow(player, "residence2/clanhall/messenger_yetti013.htm");
				return;
			}
			long count = player.getInventory().getCountOf(8034);
			if(count == 0)
			{
				showChatWindow(player, "residence2/clanhall/messenger_yetti008.htm");
			}
			else
			{
				if(!player.consumeItem(8034, count))
				{
					return;
				}
				CMGSiegeClanObject siegeClanObject = new CMGSiegeClanObject("attackers", clan, count);
				miniGameEvent.addObject("attackers", siegeClanObject);
				SiegeClanDAO.getInstance().insert(clanHall, siegeClanObject);
				showChatWindow(player, "residence2/clanhall/messenger_yetti009.htm");
			}
		}
		else if(command.equalsIgnoreCase("cancel"))
		{
			if(miniGameEvent.isRegistrationOver())
			{
				showChatWindow(player, "residence2/clanhall/messenger_yetti017.htm");
				return;
			}
			Clan clan = player.getClan();
			if(clan == null || clan.getLevel() < 3)
			{
				showChatWindow(player, "residence2/clanhall/messenger_yetti011.htm");
				return;
			}
			if(clan.getLeaderId() != player.getObjectId())
			{
				showChatWindow(player, "residence2/clanhall/messenger_yetti010.htm");
				return;
			}
			SiegeClanObject siegeClanObject = miniGameEvent.getSiegeClan("attackers", clan);
			if(siegeClanObject == null)
			{
				showChatWindow(player, "residence2/clanhall/messenger_yetti016.htm");
			}
			else
			{
				miniGameEvent.removeObject("attackers", siegeClanObject);
				SiegeClanDAO.getInstance().delete(clanHall, siegeClanObject);
				ItemFunctions.addItem(player, 8034, siegeClanObject.getParam() / 2, true);
				showChatWindow(player, "residence2/clanhall/messenger_yetti005.htm");
			}
		}
		else if(command.equalsIgnoreCase("refund"))
		{
			if(miniGameEvent.isRegistrationOver())
			{
				showChatWindow(player, "residence2/clanhall/messenger_yetti010.htm");
				return;
			}
			Clan clan = player.getClan();
			if(clan == null || clan.getLevel() < 3)
			{
				showChatWindow(player, "residence2/clanhall/messenger_yetti011.htm");
				return;
			}
			if(clan.getLeaderId() != player.getObjectId())
			{
				showChatWindow(player, "residence2/clanhall/messenger_yetti010.htm");
				return;
			}
			SiegeClanObject siegeClanObject = miniGameEvent.getSiegeClan("refund", clan);
			if(siegeClanObject == null)
			{
				showChatWindow(player, "residence2/clanhall/messenger_yetti020.htm");
			}
			else
			{
				miniGameEvent.removeObject("refund", siegeClanObject);
				SiegeClanDAO.getInstance().delete(clanHall, siegeClanObject);
				ItemFunctions.addItem(player, 8034, siegeClanObject.getParam(), true);
				showChatWindow(player, "residence2/clanhall/messenger_yetti019.htm");
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
		Clan clan = clanHall.getOwner();
		NpcHtmlMessage msg = new NpcHtmlMessage(player, this);
		if(clan != null)
		{
			msg.setFile("residence2/clanhall/messenger_yetti001.htm");
			msg.replace("%owner_name%", clan.getName());
		}
		else
		{
			msg.setFile("residence2/clanhall/messenger_yetti001a.htm");
		}
		msg.replace("%siege_date%", TimeUtils.toSimpleFormat(clanHall.getSiegeDate()));
		player.sendPacket(msg);
	}
}