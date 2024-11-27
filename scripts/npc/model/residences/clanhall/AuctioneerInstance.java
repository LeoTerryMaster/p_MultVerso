package npc.model.residences.clanhall;

import l2.commons.dao.JdbcEntityState;
import l2.gameserver.Config;
import l2.gameserver.dao.SiegeClanDAO;
import l2.gameserver.data.StringHolder;
import l2.gameserver.data.xml.holder.ResidenceHolder;
import l2.gameserver.model.Player;
import l2.gameserver.model.entity.events.impl.ClanHallAuctionEvent;
import l2.gameserver.model.entity.events.impl.SiegeEvent;
import l2.gameserver.model.entity.events.objects.AuctionSiegeClanObject;
import l2.gameserver.model.entity.events.objects.SiegeClanObject;
import l2.gameserver.model.entity.residence.ClanHall;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.pledge.Clan;
import l2.gameserver.model.pledge.Privilege;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2.gameserver.templates.npc.NpcTemplate;
import l2.gameserver.utils.HtmlUtils;
import l2.gameserver.utils.MapUtils;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

public class AuctioneerInstance extends NpcInstance
{
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yy");
	private static final NumberFormat NUMBER_FORMAT = NumberFormat.getIntegerInstance(Locale.KOREA);
	private static final long WEEK = 604800000;
	private static final int CH_PAGE_SIZE = 7;
	private static final String CH_IN_LIST = "\t<tr>\n\t\t<td width=50>\n\t\t\t<font color=\"aaaaff\">&^%id%;</font>\n\t\t</td>\n\t\t<td width=100>\n\t\t\t<a action=\"bypass -h npc_%objectId%_info %id%\"><font color=\"ffffaa\">&%%id%;[%size%]</font></a>\n\t\t</td>\n\t\t<td width=50>%date%</td>\n\t\t<td width=70 align=right>\n\t\t\t<font color=\"aaffff\">%min_bid%</font>\n\t\t</td>\n\t</tr>";
	private static final int BIDDER_PAGE_SIZE = 10;
	private static final String BIDDER_IN_LIST = "\t<tr>\n\t\t<td width=100><font color=\"aaaaff\">&%%id%;</font></td>\n\t\t<td width=100><font color=\"ffffaa\">%clan_name%</font></td>\n\t\t<td width=70>%date%</td>\n\t</tr>";
	
	public AuctioneerInstance(int objectId, NpcTemplate template)
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
		StringTokenizer tokenizer = new StringTokenizer(command.replace("\r\n", "<br1>"));
		String actualCommand = tokenizer.nextToken();
		if(actualCommand.equalsIgnoreCase("map"))
		{
			showChatWindow(player, getMapDialog());
		}
		else if(actualCommand.equalsIgnoreCase("list_all"))
		{
			int page = Integer.parseInt(tokenizer.nextToken());
			ArrayList<SiegeEvent> events = new ArrayList<>();
			for(ClanHall ch : ResidenceHolder.getInstance().getResidenceList(ClanHall.class))
			{
				if(ch.getSiegeEvent().getClass() != ClanHallAuctionEvent.class || !ch.getSiegeEvent().isInProgress() || !Config.CH_DISPLAY_IDS.contains(ch.getId()))
					continue;
				events.add(ch.getSiegeEvent());
			}
			if(events.isEmpty())
			{
				player.sendPacket(SystemMsg.THERE_ARE_NO_CLAN_HALLS_UP_FOR_AUCTION);
				showChatWindow(player, 0);
				return;
			}
			int min = 7 * page;
			int max = min + 7;
			if(min > events.size())
			{
				min = 0;
				max = min + 7;
			}
			if(max > events.size())
			{
				max = events.size();
			}
			NpcHtmlMessage msg = new NpcHtmlMessage(player, this);
			msg.setFile("residence2/clanhall/auction_list_clanhalls.htm");
			StringBuilder b = new StringBuilder();
			for(int i = min;i < max;++i)
			{
				ClanHallAuctionEvent event = (ClanHallAuctionEvent) events.get(i);
				List attackers = event.getObjects("attackers");
				Calendar endDate = event.getEndSiegeDate();
				String out = "\t<tr>\n\t\t<td width=50>\n\t\t\t<font color=\"aaaaff\">&^%id%;</font>\n\t\t</td>\n\t\t<td width=100>\n\t\t\t<a action=\"bypass -h npc_%objectId%_info %id%\"><font color=\"ffffaa\">&%%id%;[%size%]</font></a>\n\t\t</td>\n\t\t<td width=50>%date%</td>\n\t\t<td width=70 align=right>\n\t\t\t<font color=\"aaffff\">%min_bid%</font>\n\t\t</td>\n\t</tr>".replace("%id%", String.valueOf(event.getId())).replace("%min_bid%", String.valueOf(event.getResidence().getAuctionMinBid())).replace("%size%", String.valueOf(attackers.size())).replace("%date%", DATE_FORMAT.format(endDate.getTimeInMillis()));
				b.append(out);
			}
			msg.replace("%list%", b.toString());
			if(events.size() > max)
			{
				msg.replace("%next_button%", "<td>" + StringHolder.getInstance().getNotNull(player, "html_util_button_next") + "</td>");
				msg.replace("%next_bypass%", "-h npc_%objectId%_list_all " + (page + 1));
			}
			else
			{
				msg.replace("%next_button%", "");
			}
			if(page != 0)
			{
				msg.replace("%prev_button%", "<td>" + StringHolder.getInstance().getNotNull(player, "html_util_button_perv") + "</td>");
				msg.replace("%prev_bypass%", "-h npc_%objectId%_list_all " + (page - 1));
			}
			else
			{
				msg.replace("%prev_button%", "");
			}
			player.sendPacket(msg);
		}
		else if(actualCommand.equalsIgnoreCase("info"))
		{
			String fileName;
			ClanHall clanHall = null;
			SiegeClanObject siegeClan = null;
			if(tokenizer.hasMoreTokens())
			{
				int id = Integer.parseInt(tokenizer.nextToken());
				clanHall = ResidenceHolder.getInstance().getResidence(id);
				fileName = "residence2/clanhall/auction_clanhall_info_main.htm";
			}
			else
			{
				ClanHall clanHall2 = player.getClan() == null ? null : (clanHall = player.getClan().getHasHideout() > 0 ? (ClanHall) ResidenceHolder.getInstance().getResidence(player.getClan().getHasHideout()) : null);
				if(clanHall != null && clanHall.getSiegeEvent().getClass() == ClanHallAuctionEvent.class)
				{
					fileName = clanHall.getSiegeEvent().isInProgress() ? "residence2/clanhall/auction_clanhall_info_owner_sell.htm" : "residence2/clanhall/auction_clanhall_info_owner.htm";
				}
				else
				{
					for(ClanHall ch : ResidenceHolder.getInstance().getResidenceList(ClanHall.class))
					{
						if(ch.getSiegeEvent().getClass() != ClanHallAuctionEvent.class || (siegeClan = ch.getSiegeEvent().getSiegeClan("attackers", player.getClan())) == null)
							continue;
						clanHall = ch;
						break;
					}
					if(siegeClan == null)
					{
						player.sendPacket(SystemMsg.THERE_ARE_NO_OFFERINGS_I_OWN_OR_I_MADE_A_BID_FOR);
						showChatWindow(player, 0);
						return;
					}
					fileName = "residence2/clanhall/auction_clanhall_info_bidded.htm";
				}
			}
			ClanHallAuctionEvent auctionEvent = clanHall.getSiegeEvent();
			List attackers = auctionEvent.getObjects("attackers");
			NpcHtmlMessage msg = new NpcHtmlMessage(player, this);
			msg.setFile(fileName);
			msg.replace("%id%", String.valueOf(clanHall.getId()));
			msg.replace("%bigger_size%", String.valueOf(attackers.size()));
			msg.replace("%grade%", String.valueOf(clanHall.getGrade()));
			msg.replace("%rental_fee%", String.valueOf(clanHall.getRentalFee()));
			msg.replace("%cha_currency%", HtmlUtils.htmlItemName(Config.CH_BID_CURRENCY_ITEM_ID));
			Clan owner = clanHall.getOwner();
			msg.replace("%owner%", owner == null ? "" : owner.getName());
			msg.replace("%owner_leader%", owner == null ? "" : owner.getLeaderName());
			msg.replace("%description%", clanHall.getAuctionDescription());
			msg.replace("%min_bid%", String.valueOf(clanHall.getAuctionMinBid()));
			Calendar c = auctionEvent.getEndSiegeDate();
			msg.replace("%date%", DATE_FORMAT.format(c.getTimeInMillis()));
			msg.replace("%hour%", String.valueOf(c.get(11)));
			int remainingTime = (int) ((c.getTimeInMillis() - System.currentTimeMillis()) / 60000);
			msg.replace("%remaining_hour%", String.valueOf(remainingTime / 60));
			msg.replace("%remaining_minutes%", String.valueOf(remainingTime % 60));
			if(siegeClan != null)
			{
				msg.replace("%my_bid%", String.valueOf(siegeClan.getParam()));
			}
			player.sendPacket(msg);
		}
		else if(actualCommand.equalsIgnoreCase("bidder_list"))
		{
			int id = Integer.parseInt(tokenizer.nextToken());
			int page = Integer.parseInt(tokenizer.nextToken());
			ClanHall clanHall = ResidenceHolder.getInstance().getResidence(id);
			ClanHallAuctionEvent auctionEvent = clanHall.getSiegeEvent();
			List attackers = auctionEvent.getObjects("attackers");
			if(!auctionEvent.isInProgress())
			{
				return;
			}
			int min = 10 * page;
			int max = min + 10;
			if(min > attackers.size())
			{
				min = 0;
				max = min + 10;
			}
			if(max > attackers.size())
			{
				max = attackers.size();
			}
			NpcHtmlMessage msg = new NpcHtmlMessage(player, this);
			msg.setFile("residence2/clanhall/auction_bidder_list.htm");
			msg.replace("%id%", String.valueOf(id));
			StringBuilder b = new StringBuilder();
			for(int i = min;i < max;++i)
			{
				AuctionSiegeClanObject siegeClan = (AuctionSiegeClanObject) attackers.get(i);
				String t = "\t<tr>\n\t\t<td width=100><font color=\"aaaaff\">&%%id%;</font></td>\n\t\t<td width=100><font color=\"ffffaa\">%clan_name%</font></td>\n\t\t<td width=70>%date%</td>\n\t</tr>".replace("%id%", String.valueOf(id)).replace("%clan_name%", siegeClan.getClan().getName()).replace("%date%", DATE_FORMAT.format(siegeClan.getDate()));
				b.append(t);
			}
			msg.replace("%list%", b.toString());
			if(attackers.size() > max)
			{
				msg.replace("%next_button%", "<td>" + StringHolder.getInstance().getNotNull(player, "html_util_button_next") + "</td>");
				msg.replace("%next_bypass%", "-h npc_%objectId%_bidder_list " + id + " " + (page + 1));
			}
			else
			{
				msg.replace("%next_button%", "");
			}
			if(page != 0)
			{
				msg.replace("%prev_button%", "<td>" + StringHolder.getInstance().getNotNull(player, "html_util_button_prev") + "</td>");
				msg.replace("%prev_bypass%", "-h npc_%objectId%_bidder_list " + id + " " + (page - 1));
			}
			else
			{
				msg.replace("%prev_button%", "");
			}
			player.sendPacket(msg);
		}
		else if(actualCommand.equalsIgnoreCase("bid_start"))
		{
			if(!firstChecks(player))
			{
				showChatWindow(player, 0);
				return;
			}
			int id = Integer.parseInt(tokenizer.nextToken());
			ClanHall clanHall = ResidenceHolder.getInstance().getResidence(id);
			ClanHallAuctionEvent auctionEvent = clanHall.getSiegeEvent();
			if(!auctionEvent.isInProgress())
			{
				return;
			}
			long minBid = clanHall.getAuctionMinBid();
			AuctionSiegeClanObject siegeClan = auctionEvent.getSiegeClan("attackers", player.getClan());
			if(siegeClan != null)
			{
				minBid = siegeClan.getParam();
			}
			NpcHtmlMessage msg = new NpcHtmlMessage(player, this);
			msg.setFile("residence2/clanhall/auction_bid_start.htm");
			msg.replace("%id%", String.valueOf(id));
			msg.replace("%min_bid%", String.valueOf(minBid));
			msg.replace("%clan_currency_amount%", String.valueOf(player.getClan().getWarehouse().getCountOf(Config.CH_BID_CURRENCY_ITEM_ID)));
			msg.replace("%cha_currency%", HtmlUtils.htmlItemName(Config.CH_BID_CURRENCY_ITEM_ID));
			player.sendPacket(msg);
		}
		else if(actualCommand.equalsIgnoreCase("bid_next"))
		{
			if(!firstChecks(player))
			{
				showChatWindow(player, 0);
				return;
			}
			int id = Integer.parseInt(tokenizer.nextToken());
			long bid = 0;
			if(tokenizer.hasMoreTokens())
			{
				try
				{
					bid = NUMBER_FORMAT.parse(tokenizer.nextToken()).longValue();
				}
				catch(ParseException e)
				{
					
				}
			}
			ClanHall clanHall;
			ClanHallAuctionEvent auctionEvent;
			if(!(auctionEvent = (clanHall = ResidenceHolder.getInstance().getResidence(id)).getSiegeEvent()).isInProgress())
			{
				return;
			}
			if(!checkBid(player, auctionEvent, bid))
			{
				return;
			}
			long minBid = clanHall.getAuctionMinBid();
			AuctionSiegeClanObject siegeClan = auctionEvent.getSiegeClan("attackers", player.getClan());
			if(siegeClan != null)
			{
				minBid = siegeClan.getParam();
			}
			NpcHtmlMessage msg = new NpcHtmlMessage(player, this);
			msg.setFile("residence2/clanhall/auction_bid_confirm.htm");
			msg.replace("%id%", String.valueOf(id));
			msg.replace("%bid%", String.valueOf(bid));
			msg.replace("%cha_currency%", HtmlUtils.htmlItemName(Config.CH_BID_CURRENCY_ITEM_ID));
			msg.replace("%min_bid%", String.valueOf(minBid));
			Calendar c = auctionEvent.getEndSiegeDate();
			msg.replace("%date%", DATE_FORMAT.format(c.getTimeInMillis()));
			msg.replace("%hour%", String.valueOf(c.get(11)));
			player.sendPacket(msg);
		}
		else if(actualCommand.equalsIgnoreCase("bid_confirm"))
		{
			if(!firstChecks(player))
			{
				showChatWindow(player, 0);
				return;
			}
			int id = Integer.parseInt(tokenizer.nextToken());
			long bid = Long.parseLong(tokenizer.nextToken());
			ClanHall clanHall = ResidenceHolder.getInstance().getResidence(id);
			ClanHallAuctionEvent auctionEvent = clanHall.getSiegeEvent();
			if(!auctionEvent.isInProgress())
			{
				return;
			}
			for(ClanHall ch : ResidenceHolder.getInstance().getResidenceList(ClanHall.class))
			{
				if(clanHall == ch || ch.getSiegeEvent().getClass() != ClanHallAuctionEvent.class || !ch.getSiegeEvent().isInProgress() || ch.getSiegeEvent().getSiegeClan("attackers", player.getClan()) == null)
					continue;
				player.sendPacket(SystemMsg.SINCE_YOU_HAVE_ALREADY_SUBMITTED_A_BID_YOU_ARE_NOT_ALLOWED_TO_PARTICIPATE_IN_ANOTHER_AUCTION_AT_THIS_TIME);
				onBypassFeedback(player, "bid_start " + id);
				return;
			}
			if(!checkBid(player, auctionEvent, bid))
			{
				return;
			}
			long consumeBid = bid;
			AuctionSiegeClanObject siegeClan = auctionEvent.getSiegeClan("attackers", player.getClan());
			if(siegeClan != null)
			{
				consumeBid -= siegeClan.getParam();
				if(bid <= siegeClan.getParam())
				{
					player.sendPacket(SystemMsg.THE_BID_AMOUNT_MUST_BE_HIGHER_THAN_THE_PREVIOUS_BID);
					onBypassFeedback(player, "bid_start " + auctionEvent.getId());
					return;
				}
			}
			player.getClan().getWarehouse().destroyItemByItemId(Config.CH_BID_CURRENCY_ITEM_ID, consumeBid);
			if(siegeClan != null)
			{
				siegeClan.setParam(bid);
				SiegeClanDAO.getInstance().update(clanHall, siegeClan);
			}
			else
			{
				siegeClan = new AuctionSiegeClanObject("attackers", player.getClan(), bid);
				auctionEvent.addObject("attackers", siegeClan);
				SiegeClanDAO.getInstance().insert(clanHall, siegeClan);
			}
			player.sendPacket(SystemMsg.YOUR_BID_HAS_BEEN_SUCCESSFULLY_PLACED);
			onBypassFeedback(player, "info");
		}
		else if(actualCommand.equalsIgnoreCase("cancel_bid"))
		{
			if(!firstChecks(player))
			{
				showChatWindow(player, 0);
				return;
			}
			int id = Integer.parseInt(tokenizer.nextToken());
			ClanHall clanHall = ResidenceHolder.getInstance().getResidence(id);
			ClanHallAuctionEvent auctionEvent = clanHall.getSiegeEvent();
			if(!auctionEvent.isInProgress())
			{
				return;
			}
			AuctionSiegeClanObject siegeClan = auctionEvent.getSiegeClan("attackers", player.getClan());
			if(siegeClan == null)
			{
				return;
			}
			long returnVal = siegeClan.getParam() - (long) ((double) siegeClan.getParam() * 0.1);
			NpcHtmlMessage msg = new NpcHtmlMessage(player, this);
			msg.setFile("residence2/clanhall/auction_bid_cancel.htm");
			msg.replace("%id%", String.valueOf(id));
			msg.replace("%cha_currency%", HtmlUtils.htmlItemName(Config.CH_BID_CURRENCY_ITEM_ID));
			msg.replace("%bid%", String.valueOf(siegeClan.getParam()));
			msg.replace("%return%", String.valueOf(returnVal));
			player.sendPacket(msg);
		}
		else if(actualCommand.equalsIgnoreCase("cancel_bid_confirm"))
		{
			if(!firstChecks(player))
			{
				showChatWindow(player, 0);
				return;
			}
			int id = Integer.parseInt(tokenizer.nextToken());
			ClanHall clanHall = ResidenceHolder.getInstance().getResidence(id);
			ClanHallAuctionEvent auctionEvent = clanHall.getSiegeEvent();
			if(!auctionEvent.isInProgress())
			{
				return;
			}
			AuctionSiegeClanObject siegeClan = auctionEvent.getSiegeClan("attackers", player.getClan());
			if(siegeClan == null)
			{
				return;
			}
			long returnVal = siegeClan.getParam() - (long) ((double) siegeClan.getParam() * 0.1);
			player.getClan().getWarehouse().addItem(Config.CH_BID_CURRENCY_ITEM_ID, returnVal);
			auctionEvent.removeObject("attackers", siegeClan);
			SiegeClanDAO.getInstance().delete(clanHall, siegeClan);
			player.sendPacket(SystemMsg.YOU_HAVE_CANCELED_YOUR_BID);
			showChatWindow(player, 0);
		}
		else if(actualCommand.equalsIgnoreCase("register_start"))
		{
			if(!firstChecks(player))
			{
				showChatWindow(player, 0);
				return;
			}
			ClanHall clanHall = ResidenceHolder.getInstance().getResidence(player.getClan().getHasHideout());
			if(clanHall.getSiegeEvent().getClass() != ClanHallAuctionEvent.class || clanHall.getSiegeEvent().isInProgress())
			{
				return;
			}
			if(clanHall.getLastSiegeDate().getTimeInMillis() + 604800000 > System.currentTimeMillis())
			{
				player.sendPacket(SystemMsg.IT_HAS_NOT_YET_BEEN_SEVEN_DAYS_SINCE_CANCELING_AN_AUCTION);
				onBypassFeedback(player, "info");
				return;
			}
			NpcHtmlMessage msg = new NpcHtmlMessage(player, this);
			msg.setFile("residence2/clanhall/auction_clanhall_register_start.htm");
			msg.replace("%id%", String.valueOf(player.getClan().getHasHideout()));
			msg.replace("%currency_amount%", String.valueOf(player.getClan().getWarehouse().getCountOf(Config.CH_BID_CURRENCY_ITEM_ID)));
			msg.replace("%cha_currency%", HtmlUtils.htmlItemName(Config.CH_BID_CURRENCY_ITEM_ID));
			msg.replace("%deposit%", String.valueOf(clanHall.getDeposit()));
			player.sendPacket(msg);
		}
		else if(actualCommand.equalsIgnoreCase("register_next"))
		{
			if(!firstChecks(player))
			{
				showChatWindow(player, 0);
				return;
			}
			ClanHall clanHall = ResidenceHolder.getInstance().getResidence(player.getClan().getHasHideout());
			if(clanHall.getSiegeEvent().getClass() != ClanHallAuctionEvent.class || clanHall.getSiegeEvent().isInProgress())
			{
				showChatWindow(player, 0);
				return;
			}
			if(player.getClan().getWarehouse().getCountOf(Config.CH_BID_CURRENCY_ITEM_ID) < clanHall.getDeposit())
			{
				player.sendPacket(SystemMsg.THERE_IS_NOT_ENOUGH_ADENA_IN_THE_CLAN_HALL_WAREHOUSE);
				onBypassFeedback(player, "register_start");
				return;
			}
			NpcHtmlMessage msg = new NpcHtmlMessage(player, this);
			msg.setFile("residence2/clanhall/auction_clanhall_register_next.htm");
			msg.replace("%min_bid%", String.valueOf(clanHall.getBaseMinBid()));
			msg.replace("%last_bid%", String.valueOf(clanHall.getBaseMinBid()));
			msg.replace("%cha_currency%", HtmlUtils.htmlItemName(Config.CH_BID_CURRENCY_ITEM_ID));
			player.sendPacket(msg);
		}
		else if(actualCommand.equalsIgnoreCase("register_next2"))
		{
			if(!firstChecks(player))
			{
				showChatWindow(player, 0);
				return;
			}
			ClanHall clanHall = ResidenceHolder.getInstance().getResidence(player.getClan().getHasHideout());
			if(clanHall.getSiegeEvent().getClass() != ClanHallAuctionEvent.class || clanHall.getSiegeEvent().isInProgress())
			{
				showChatWindow(player, 0);
				return;
			}
			int day = Integer.parseInt(tokenizer.nextToken());
			long bid = -1;
			if(tokenizer.hasMoreTokens())
			{
				try
				{
					bid = Long.parseLong(tokenizer.nextToken());
				}
				catch(Exception e)
				{
					
				}
			}
			String comment = "";
			if(tokenizer.hasMoreTokens())
			{
				comment = tokenizer.nextToken();
				while(tokenizer.hasMoreTokens())
				{
					comment = comment + " " + tokenizer.nextToken();
				}
			}
			comment = comment.substring(0, Math.min(comment.length(), 127));
			if(bid <= -1)
			{
				onBypassFeedback(player, "register_next");
				return;
			}
			Calendar cal = Calendar.getInstance();
			cal.add(11, day);
			NpcHtmlMessage msg = new NpcHtmlMessage(player, this);
			msg.setFile("residence2/clanhall/auction_clanhall_register_confirm.htm");
			msg.replace("%description%", comment);
			msg.replace("%day%", String.valueOf(day));
			msg.replace("%bid%", String.valueOf(bid));
			msg.replace("%cha_currency%", HtmlUtils.htmlItemName(Config.CH_BID_CURRENCY_ITEM_ID));
			msg.replace("%base_bid%", String.valueOf(clanHall.getBaseMinBid()));
			msg.replace("%hour%", String.valueOf(cal.get(11)));
			msg.replace("%date%", DATE_FORMAT.format(cal.getTimeInMillis()));
			player.sendPacket(msg);
		}
		else if(actualCommand.equalsIgnoreCase("register_confirm"))
		{
			if(!firstChecks(player))
			{
				showChatWindow(player, 0);
				return;
			}
			ClanHall clanHall = ResidenceHolder.getInstance().getResidence(player.getClan().getHasHideout());
			if(clanHall.getSiegeEvent().getClass() != ClanHallAuctionEvent.class || clanHall.getSiegeEvent().isInProgress())
			{
				showChatWindow(player, 0);
				return;
			}
			if(clanHall.getLastSiegeDate().getTimeInMillis() + 604800000 > System.currentTimeMillis())
			{
				player.sendPacket(SystemMsg.IT_HAS_NOT_YET_BEEN_SEVEN_DAYS_SINCE_CANCELING_AN_AUCTION);
				onBypassFeedback(player, "info");
				return;
			}
			int day = Integer.parseInt(tokenizer.nextToken());
			long bid = Long.parseLong(tokenizer.nextToken());
			String comment = "";
			if(tokenizer.hasMoreTokens())
			{
				comment = tokenizer.nextToken();
				while(tokenizer.hasMoreTokens())
				{
					comment = comment + " " + tokenizer.nextToken();
				}
			}
			if(bid <= -1)
			{
				onBypassFeedback(player, "register_next");
				return;
			}
			clanHall.setAuctionMinBid(bid);
			clanHall.setAuctionDescription(comment);
			clanHall.setAuctionLength(day);
			clanHall.getSiegeDate().setTimeInMillis(System.currentTimeMillis());
			clanHall.setJdbcState(JdbcEntityState.UPDATED);
			clanHall.update();
			clanHall.getSiegeEvent().reCalcNextTime(false);
			onBypassFeedback(player, "info");
			player.sendPacket(SystemMsg.YOU_HAVE_REGISTERED_FOR_A_CLAN_HALL_AUCTION);
		}
		else if(actualCommand.equals("cancel_start"))
		{
			if(!firstChecks(player))
			{
				showChatWindow(player, 0);
				return;
			}
			ClanHall clanHall = ResidenceHolder.getInstance().getResidence(player.getClan().getHasHideout());
			if(clanHall.getSiegeEvent().getClass() != ClanHallAuctionEvent.class || !clanHall.getSiegeEvent().isInProgress())
			{
				showChatWindow(player, 0);
				return;
			}
			NpcHtmlMessage msg = new NpcHtmlMessage(player, this);
			msg.setFile("residence2/clanhall/auction_clanhall_cancel_confirm.htm");
			msg.replace("%deposit%", String.valueOf(clanHall.getDeposit()));
			msg.replace("%cha_currency%", HtmlUtils.htmlItemName(Config.CH_BID_CURRENCY_ITEM_ID));
			player.sendPacket(msg);
		}
		else if(actualCommand.equals("cancel_confirm"))
		{
			if(!firstChecks(player))
			{
				showChatWindow(player, 0);
				return;
			}
			ClanHall clanHall = ResidenceHolder.getInstance().getResidence(player.getClan().getHasHideout());
			if(clanHall.getSiegeEvent().getClass() != ClanHallAuctionEvent.class || !clanHall.getSiegeEvent().isInProgress())
			{
				showChatWindow(player, 0);
				return;
			}
			clanHall.getSiegeEvent().setInProgress(false);
			clanHall.getSiegeDate().setTimeInMillis(0);
			clanHall.getLastSiegeDate().setTimeInMillis(System.currentTimeMillis());
			clanHall.setAuctionDescription("");
			clanHall.setAuctionLength(0);
			clanHall.setAuctionMinBid(0);
			clanHall.setJdbcState(JdbcEntityState.UPDATED);
			clanHall.update();
			ClanHallAuctionEvent auctionEvent = clanHall.getSiegeEvent();
			List<AuctionSiegeClanObject> siegeClans = auctionEvent.removeObjects("attackers");
			SiegeClanDAO.getInstance().delete(clanHall);
			for(AuctionSiegeClanObject siegeClan : siegeClans)
			{
				long returnBid = siegeClan.getParam() - (long) ((double) siegeClan.getParam() * 0.1);
				siegeClan.getClan().getWarehouse().addItem(Config.CH_BID_CURRENCY_ITEM_ID, returnBid);
			}
			clanHall.getSiegeEvent().reCalcNextTime(false);
			onBypassFeedback(player, "info");
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
	
	@Override
	public void showChatWindow(Player player, int val, Object... arg)
	{
		showChatWindow(player, "residence2/clanhall/auction_dealer001.htm");
	}
	
	private boolean firstChecks(Player player)
	{
		if(player.getClan() == null || player.getClan().getLevel() < Config.CLAN_MIN_LEVEL_FOR_BID)
		{
			player.sendPacket(SystemMsg.ONLY_A_CLAN_LEADER_WHOSE_CLAN_IS_OF_LEVEL_2_OR_HIGHER_IS_ALLOWED_TO_PARTICIPATE_IN_A_CLAN_HALL_AUCTION);
			return false;
		}
		if(!player.hasPrivilege(Privilege.CH_AUCTION))
		{
			player.sendPacket(SystemMsg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return false;
		}
		return true;
	}
	
	private boolean checkBid(Player player, ClanHallAuctionEvent auctionEvent, long bid)
	{
		long consumeBid = bid;
		AuctionSiegeClanObject siegeClan = auctionEvent.getSiegeClan("attackers", player.getClan());
		if(siegeClan != null)
		{
			consumeBid -= siegeClan.getParam();
		}
		if(consumeBid > player.getClan().getWarehouse().getCountOf(Config.CH_BID_CURRENCY_ITEM_ID))
		{
			player.sendPacket(SystemMsg.THERE_IS_NOT_ENOUGH_ADENA_IN_THE_CLAN_HALL_WAREHOUSE);
			onBypassFeedback(player, "bid_start " + auctionEvent.getId());
			return false;
		}
		long minBid;
		long l = minBid = siegeClan == null ? auctionEvent.getResidence().getAuctionMinBid() : siegeClan.getParam();
		if(bid < minBid)
		{
			player.sendPacket(SystemMsg.YOUR_BID_PRICE_MUST_BE_HIGHER_THAN_THE_MINIMUM_PRICE_CURRENTLY_BEING_BID);
			onBypassFeedback(player, "bid_start " + auctionEvent.getId());
			return false;
		}
		return true;
	}
	
	private String getMapDialog()
	{
		int rx = MapUtils.regionX(this);
		int ry = MapUtils.regionY(this);
		return String.format("residence2/clanhall/map_agit_%d_%d.htm", rx, ry);
	}
}