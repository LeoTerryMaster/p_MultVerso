package l2.gameserver.model.instances;

import l2.gameserver.Config;
import l2.gameserver.cache.Msg;
import l2.gameserver.instancemanager.games.LotteryManager;
import l2.gameserver.model.Player;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.network.l2.s2c.SystemMessage2;
import l2.gameserver.templates.npc.NpcTemplate;
import l2.gameserver.utils.ItemFunctions;
import l2.gameserver.utils.Log;

import java.text.DateFormat;

public class LotteryManagerInstance extends NpcInstance
{
	public LotteryManagerInstance(int objectID, NpcTemplate template)
	{
		super(objectID, template);
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
		{
			return;
		}
		if(command.startsWith("Loto"))
		{
			try
			{
				int val = Integer.parseInt(command.substring(5));
				showLotoWindow(player, val);
			}
			catch(NumberFormatException e)
			{
				Log.debug("L2LotteryManagerInstance: bypass: " + command + "; player: " + player, e);
			}
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
	
	@Override
	public String getHtmlPath(int npcId, int val, Player player)
	{
		String pom = val == 0 ? "LotteryManager" : "LotteryManager-" + val;
		return "lottery/" + pom + ".htm";
	}
	
	public void showLotoWindow(Player player, int val)
	{
		int npcId = getTemplate().npcId;
		NpcHtmlMessage html = new NpcHtmlMessage(player, this);
		switch(val)
		{
			case 0:
			{
				String filename = getHtmlPath(npcId, 1, player);
				html.setFile(filename);
				break;
			}
			case 1:
			case 2:
			case 3:
			case 4:
			case 5:
			case 6:
			case 7:
			case 8:
			case 9:
			case 10:
			case 11:
			case 12:
			case 13:
			case 14:
			case 15:
			case 16:
			case 17:
			case 18:
			case 19:
			case 20:
			case 21:
			{
				if(!LotteryManager.getInstance().isStarted())
				{
					player.sendPacket(Msg.LOTTERY_TICKETS_ARE_NOT_CURRENTLY_BEING_SOLD);
					return;
				}
				if(!LotteryManager.getInstance().isSellableTickets())
				{
					player.sendPacket(Msg.TICKETS_FOR_THE_CURRENT_LOTTERY_ARE_NO_LONGER_AVAILABLE);
					return;
				}
				String filename = getHtmlPath(npcId, 5, player);
				html.setFile(filename);
				int count = 0;
				boolean found = false;
				int i;
				for(i = 0;i < 5;++i)
				{
					if(player.getLoto(i) == val)
					{
						player.setLoto(i, 0);
						found = true;
						continue;
					}
					if(player.getLoto(i) <= 0)
						continue;
					++count;
				}
				if(count < 5 && !found && val <= 20)
				{
					for(i = 0;i < 5;++i)
					{
						if(player.getLoto(i) != 0)
							continue;
						player.setLoto(i, val);
						break;
					}
				}
				count = 0;
				for(i = 0;i < 5;++i)
				{
					if(player.getLoto(i) <= 0)
						continue;
					++count;
					String button = String.valueOf(player.getLoto(i));
					if(player.getLoto(i) < 10)
					{
						button = "0" + button;
					}
					String search = "fore=\"L2UI.lottoNum" + button + "\" back=\"L2UI.lottoNum" + button + "a_check\"";
					String replace = "fore=\"L2UI.lottoNum" + button + "a_check\" back=\"L2UI.lottoNum" + button + "\"";
					html.replace(search, replace);
				}
				if(count != 5)
					break;
				String search;
				String replace;
				if(!player.isLangRus())
				{
					search = "0\">Return";
					replace = "22\">The winner selected the numbers above.";
				}
				else
				{
					search = "0\">Назад";
					replace = "22\">Выигрышные номера выбранные выше.";
				}
				html.replace(search, replace);
				break;
			}
			case 22:
			{
				if(!LotteryManager.getInstance().isStarted())
				{
					player.sendPacket(Msg.LOTTERY_TICKETS_ARE_NOT_CURRENTLY_BEING_SOLD);
					return;
				}
				if(!LotteryManager.getInstance().isSellableTickets())
				{
					player.sendPacket(Msg.TICKETS_FOR_THE_CURRENT_LOTTERY_ARE_NO_LONGER_AVAILABLE);
					return;
				}
				int price = Config.SERVICES_ALT_LOTTERY_PRICE;
				int lotonumber = LotteryManager.getInstance().getId();
				int enchant = 0;
				int type2 = 0;
				for(int i = 0;i < 5;++i)
				{
					if(player.getLoto(i) == 0)
					{
						return;
					}
					if(player.getLoto(i) < 17)
					{
						enchant = (int) ((double) enchant + Math.pow(2.0, player.getLoto(i) - 1));
						continue;
					}
					type2 = (int) ((double) type2 + Math.pow(2.0, player.getLoto(i) - 17));
				}
				if(player.getAdena() < (long) price)
				{
					player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
					return;
				}
				player.reduceAdena(price, true);
				SystemMessage sm = new SystemMessage(371);
				sm.addNumber(lotonumber);
				sm.addItemName(4442);
				player.sendPacket(sm);
				ItemInstance item = ItemFunctions.createItem(4442);
				item.setBlessed(lotonumber);
				item.setEnchantLevel(enchant);
				item.setDamaged(type2);
				player.getInventory().addItem(item);
				String filename = getHtmlPath(npcId, 3, player);
				html.setFile(filename);
				break;
			}
			case 23:
			{
				String filename = getHtmlPath(npcId, 3, player);
				html.setFile(filename);
				break;
			}
			case 24:
			{
				String filename = getHtmlPath(npcId, 4, player);
				html.setFile(filename);
				int lotonumber = LotteryManager.getInstance().getId();
				String message = "";
				for(ItemInstance item : player.getInventory().getItems())
				{
					if(item == null || item.getItemId() != 4442 || item.getBlessed() >= lotonumber)
						continue;
					message = message + "<a action=\"bypass -h npc_%objectId%_Loto " + item.getObjectId() + "\">" + item.getBlessed();
					message = message + " " + new CustomMessage("LotteryManagerInstance.NpcString.EVENT_NUMBER", player) + " ";
					int[] numbers = LotteryManager.getInstance().decodeNumbers(item.getEnchantLevel(), item.getDamaged());
					for(int i = 0;i < 5;++i)
					{
						message = message + numbers[i] + " ";
					}
					int[] check = LotteryManager.getInstance().checkTicket(item);
					if(check[0] > 0)
					{
						message = message + "- ";
						switch(check[0])
						{
							case 1:
							{
								message = message + new CustomMessage("LotteryManagerInstance.NpcString.FIRST_PRIZE", player);
								break;
							}
							case 2:
							{
								message = message + new CustomMessage("LotteryManagerInstance.NpcString.SECOND_PRIZE", player);
								break;
							}
							case 3:
							{
								message = message + new CustomMessage("LotteryManagerInstance.NpcString.THIRD_PRIZE", player);
								break;
							}
							case 4:
							{
								message = message + new CustomMessage("LotteryManagerInstance.NpcString.FOURTH_PRIZE", player);
							}
						}
						message = message + " " + check[1] + "a.";
					}
					message = message + "</a>";
				}
				if(message.length() == 0)
				{
					message = message + new CustomMessage("LotteryManagerInstance.NpcString.THERE_HAS_BEEN_NO_WINNING_LOTTERY_TICKET", player);
				}
				html.replace("%result%", message);
				break;
			}
			case 25:
			{
				String filename = getHtmlPath(npcId, 2, player);
				html.setFile(filename);
				break;
			}
			default:
			{
				if(val <= 25)
					break;
				int lotonumber = LotteryManager.getInstance().getId();
				ItemInstance item = player.getInventory().getItemByObjectId(val);
				if(item == null || item.getItemId() != 4442 || item.getBlessed() >= lotonumber)
				{
					return;
				}
				int[] check = LotteryManager.getInstance().checkTicket(item);
				if(player.getInventory().destroyItem(item, 1))
				{
					player.sendPacket(SystemMessage2.removeItems(4442, 1));
					int adena = check[1];
					if(adena > 0)
					{
						player.addAdena(adena);
					}
				}
				return;
			}
		}
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%race%", "" + LotteryManager.getInstance().getId());
		html.replace("%adena%", "" + LotteryManager.getInstance().getPrize());
		html.replace("%ticket_price%", "" + Config.SERVICES_LOTTERY_TICKET_PRICE);
		html.replace("%prize5%", "" + Config.SERVICES_LOTTERY_5_NUMBER_RATE * 100.0);
		html.replace("%prize4%", "" + Config.SERVICES_LOTTERY_4_NUMBER_RATE * 100.0);
		html.replace("%prize3%", "" + Config.SERVICES_LOTTERY_3_NUMBER_RATE * 100.0);
		html.replace("%prize2%", "" + Config.SERVICES_LOTTERY_2_AND_1_NUMBER_PRIZE);
		html.replace("%enddate%", "" + DateFormat.getDateInstance().format(LotteryManager.getInstance().getEndDate()));
		player.sendPacket(html);
		player.sendActionFailed();
	}
}