package services;

import l2.commons.util.Rnd;
import l2.gameserver.Config;
import l2.gameserver.cache.Msg;
import l2.gameserver.data.htm.HtmCache;
import l2.gameserver.model.Player;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.scripts.Functions;
import l2.gameserver.utils.GameStats;
import l2.gameserver.utils.Util;

public class Roulette extends Functions
{
	private static final String R = "red";
	private static final String B = "black";
	private static final String fst = "first";
	private static final String snd = "second";
	private static final String trd = "third";
	private static final String E = "even";
	private static final String O = "odd";
	private static final String L = "low";
	private static final String H = "high";
	private static final String Z = "zero";
	private static final String[][] Numbers = {{"0", "zero", "zero", "zero", "zero", "zero"}, {"1", "red", "first", "first", "odd", "low"}, {"2", "black", "first", "second", "even", "low"}, {"3", "red", "first", "third", "odd", "low"}, {"4", "black", "first", "first", "even", "low"}, {"5", "red", "first", "second", "odd", "low"}, {"6", "black", "first", "third", "even", "low"}, {"7", "red", "first", "first", "odd", "low"}, {"8", "black", "first", "second", "even", "low"}, {"9", "red", "first", "third", "odd", "low"}, {"10", "black", "first", "first", "even", "low"}, {"11", "black", "first", "second", "odd", "low"}, {"12", "red", "first", "third", "even", "low"}, {"13", "black", "second", "first", "odd", "low"}, {"14", "red", "second", "second", "even", "low"}, {"15", "black", "second", "third", "odd", "low"}, {"16", "red", "second", "first", "even", "low"}, {"17", "black", "second", "second", "odd", "low"}, {"18", "red", "second", "third", "even", "low"}, {"19", "red", "second", "first", "odd", "high"}, {"20", "black", "second", "second", "even", "high"}, {"21", "red", "second", "third", "odd", "high"}, {"22", "black", "second", "first", "even", "high"}, {"23", "red", "second", "second", "odd", "high"}, {"24", "black", "second", "third", "even", "high"}, {"25", "red", "third", "first", "odd", "high"}, {"26", "black", "third", "second", "even", "high"}, {"27", "red", "third", "third", "odd", "high"}, {"28", "black", "third", "first", "even", "high"}, {"29", "black", "third", "second", "odd", "high"}, {"30", "red", "third", "third", "even", "high"}, {"31", "black", "third", "first", "odd", "high"}, {"32", "red", "third", "second", "even", "high"}, {"33", "black", "third", "third", "odd", "high"}, {"34", "red", "third", "first", "even", "high"}, {"35", "black", "third", "second", "odd", "high"}, {"36", "red", "third", "third", "even", "high"}};
	
	private static final int check(String betID, String[] roll, GameType type)
	{
		switch(type)
		{
			case StraightUp:
			{
				if(betID.equals(roll[0]))
				{
					return 35;
				}
				return 0;
			}
			case ColumnBet:
			{
				if(betID.equals(roll[3]))
				{
					return 2;
				}
				return 0;
			}
			case DozenBet:
			{
				if(betID.equals(roll[2]))
				{
					return 2;
				}
				return 0;
			}
			case RedOrBlack:
			{
				if(betID.equals(roll[1]))
				{
					return 1;
				}
				return 0;
			}
			case EvenOrOdd:
			{
				if(betID.equals(roll[4]))
				{
					return 1;
				}
				return 0;
			}
			case LowOrHigh:
			{
				if(betID.equals(roll[5]))
				{
					return 1;
				}
				return 0;
			}
		}
		return 0;
	}
	
	public void dialog()
	{
		Player player = getSelf();
		show(HtmCache.getInstance().getNotNull("scripts/services/roulette.htm", player).replaceFirst("%min%", Util.formatAdena(Config.SERVICES_ROULETTE_MIN_BET)).replaceFirst("%max%", Util.formatAdena(Config.SERVICES_ROULETTE_MAX_BET)), player);
	}
	
	public void play(String[] param)
	{
		GameType type;
		Player player = getSelf();
		long bet;
		String betID;
		try
		{
			if(param.length != 3)
			{
				throw new NumberFormatException();
			}
			type = GameType.valueOf(param[0]);
			betID = param[1].trim();
			bet = Long.parseLong(param[2]);
			if(type == GameType.StraightUp && (betID.length() > 2 || Integer.parseInt(betID) < 0 || Integer.parseInt(betID) > 36))
			{
				throw new NumberFormatException();
			}
		}
		catch(NumberFormatException e)
		{
			show("Invalid value input!<br><a action=\"bypass -h scripts_services.Roulette:dialog\">Back</a>", player);
			return;
		}
		if(bet < Config.SERVICES_ROULETTE_MIN_BET)
		{
			show("Too small bet!<br><a action=\"bypass -h scripts_services.Roulette:dialog\">Back</a>", player);
			return;
		}
		if(bet > Config.SERVICES_ROULETTE_MAX_BET)
		{
			show("Too large bet!<br><a action=\"bypass -h scripts_services.Roulette:dialog\">Back</a>", player);
			return;
		}
		if(player.getAdena() < bet)
		{
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			show("You do not have enough adena!<br><a action=\"bypass -h scripts_services.Roulette:dialog\">Back</a>", player);
			return;
		}
		String[] roll = Numbers[Rnd.get(Numbers.length)];
		int result = check(betID, roll, type);
		String ret = HtmCache.getInstance().getNotNull("scripts/services/rouletteresult.htm", player);
		if(result == 0)
		{
			removeItem(player, 57, bet);
			GameStats.addRoulette(bet);
			ret = ret.replace("%result%", "<font color=\"FF0000\">Fail!</font>");
		}
		else
		{
			addItem(player, 57, bet * (long) result);
			GameStats.addRoulette(-1 * bet * (long) result);
			ret = ret.replace("%result%", "<font color=\"00FF00\">Succes!</font>");
		}
		if(player.isGM())
		{
			player.sendMessage("Roulette balance: " + Util.formatAdena(GameStats.getRouletteSum()));
		}
		ret = ret.replace("%bettype%", new CustomMessage("Roulette." + type, player, new Object[0]).toString());
		ret = ret.replace("%betnumber%", type == GameType.StraightUp ? betID : new CustomMessage("Roulette." + betID, player, new Object[0]).toString());
		ret = ret.replace("%number%", roll[0]);
		ret = ret.replace("%color%", new CustomMessage("Roulette." + roll[1], player, new Object[0]).toString());
		ret = ret.replace("%evenness%", new CustomMessage("Roulette." + roll[4], player, new Object[0]).toString());
		ret = ret.replace("%column%", new CustomMessage("Roulette." + roll[3], player, new Object[0]).toString());
		ret = ret.replace("%dozen%", new CustomMessage("Roulette." + roll[2], player, new Object[0]).toString());
		ret = ret.replace("%highness%", new CustomMessage("Roulette." + roll[5], player, new Object[0]).toString());
		ret = ret.replace("%param%", param[0] + " " + param[1] + " " + param[2]);
		show(ret, player);
	}
	
	public String DialogAppend_30990(Integer val)
	{
		return getHtmlAppends(val);
	}
	
	public String DialogAppend_30991(Integer val)
	{
		return getHtmlAppends(val);
	}
	
	public String DialogAppend_30992(Integer val)
	{
		return getHtmlAppends(val);
	}
	
	public String DialogAppend_30993(Integer val)
	{
		return getHtmlAppends(val);
	}
	
	public String DialogAppend_30994(Integer val)
	{
		return getHtmlAppends(val);
	}
	
	public String getHtmlAppends(Integer val)
	{
		Player player = getSelf();
		if(Config.SERVICES_ALLOW_ROULETTE)
		{
			return "<br><a action=\"bypass -h scripts_services.Roulette:dialog\">" + new CustomMessage("Roulette.dialog", player) + "</a>";
		}
		return "";
	}
	
	private enum GameType
	{
		StraightUp,
		ColumnBet,
		DozenBet,
		RedOrBlack,
		EvenOrOdd,
		LowOrHigh;
	}
}