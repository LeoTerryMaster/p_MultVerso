package l2.gameserver.utils;

import l2.commons.util.Rnd;
import l2.gameserver.Config;
import l2.gameserver.model.GameObjectsStorage;
import l2.gameserver.model.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class Util
{
	static final String PATTERN = "0.0000000000E00";
	static final DecimalFormat df;
	private static final NumberFormat adenaFormatter;
	private static final Pattern _pattern;
	
	static
	{
		adenaFormatter = NumberFormat.getIntegerInstance(Locale.FRANCE);
		df = (DecimalFormat) NumberFormat.getNumberInstance(Locale.ENGLISH);
		df.applyPattern("0.0000000000E00");
		df.setPositivePrefix("+");
		_pattern = Pattern.compile("<!--TEMPLET(\\d+)(.*?)TEMPLET-->", 32);
	}
	
	public static boolean isMatchingRegexp(String text, String template)
	{
		Pattern pattern = null;
		try
		{
			pattern = Pattern.compile(template);
		}
		catch(PatternSyntaxException e)
		{
			e.printStackTrace();
		}
		if(pattern == null)
		{
			return false;
		}
		Matcher regexp = pattern.matcher(text);
		return regexp.matches();
	}
	
	public static String formatDouble(double x, String nanString, boolean forceExponents)
	{
		if(Double.isNaN(x))
		{
			return nanString;
		}
		if(forceExponents)
		{
			return df.format(x);
		}
		if((double) (long) x == x)
		{
			return String.valueOf((long) x);
		}
		return String.valueOf(x);
	}
	
	public static String formatAdena(long amount)
	{
		return adenaFormatter.format(amount);
	}
	
	public static String formatTime(int time)
	{
		if(time == 0)
		{
			return "now";
		}
		time = Math.abs(time);
		long numDays = time / 86400;
		time = (int) ((long) time - numDays * 86400);
		long numHours = time / 3600;
		time = (int) ((long) time - numHours * 3600);
		long numMins = time / 60;
		time = (int) ((long) time - numMins * 60);
		long numSeconds = time;
		String ret = "";
		if(numDays > 0)
		{
			ret = ret + numDays + "d ";
		}
		if(numHours > 0)
		{
			ret = ret + numHours + "h ";
		}
		if(numMins > 0)
		{
			ret = ret + numMins + "m ";
		}
		if(numSeconds > 0)
		{
			ret = ret + numSeconds + "s";
		}
		return ret.trim();
	}
	
	public static String getCfgDirect()
	{
		StringBuilder result = new StringBuilder();
		result.append("Auth: ").append(Config.GAME_SERVER_LOGIN_HOST).append('\n');
		result.append("Game:\n");
		try
		{
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while(interfaces.hasMoreElements())
			{
				NetworkInterface iface = interfaces.nextElement();
				if(iface.isLoopback() || !iface.isUp())
					continue;
				Enumeration<InetAddress> addresses = iface.getInetAddresses();
				while(addresses.hasMoreElements())
				{
					InetAddress addr = addresses.nextElement();
					String tmp = addr.getHostAddress();
					result.append(" ").append(tmp).append('\n');
				}
			}
		}
		catch(SocketException e)
		{
			return "none";
		}
		return result.toString();
	}
	
	public static long rollDrop(long min, long max, double calcChance, boolean rate)
	{
		if(calcChance <= 0.0 || min <= 0 || max <= 0)
		{
			return 0;
		}
		if(rate)
		{
			calcChance *= Config.RATE_DROP_ITEMS;
		}
		int dropmult = 1;
		if(calcChance > 1000000.0)
		{
			if(calcChance % 1000000.0 == 0.0)
			{
				dropmult = (int) (calcChance / 1000000.0);
			}
			else
			{
				dropmult = (int) Math.ceil(calcChance / 1000000.0);
				calcChance /= (double) dropmult;
			}
		}
		return Rnd.chance(calcChance / 10000.0) ? Rnd.get(min * (long) dropmult, max * (long) dropmult) : 0;
	}
	
	public static int packInt(int[] a, int bits) throws Exception
	{
		int m = 32 / bits;
		if(a.length > m)
		{
			throw new Exception("Overflow");
		}
		int result = 0;
		int mval = (int) Math.pow(2.0, bits);
		for(int i = 0;i < m;++i)
		{
			result <<= bits;
			int next;
			if(a.length > i)
			{
				next = a[i];
				if(next >= mval || next < 0)
				{
					throw new Exception("Overload, value is out of range");
				}
			}
			else
			{
				next = 0;
			}
			result += next;
		}
		return result;
	}
	
	public static long packLong(int[] a, int bits) throws Exception
	{
		int m = 64 / bits;
		if(a.length > m)
		{
			throw new Exception("Overflow");
		}
		long result = 0;
		int mval = (int) Math.pow(2.0, bits);
		for(int i = 0;i < m;++i)
		{
			result <<= bits;
			int next;
			if(a.length > i)
			{
				next = a[i];
				if(next >= mval || next < 0)
				{
					throw new Exception("Overload, value is out of range");
				}
			}
			else
			{
				next = 0;
			}
			result += (long) next;
		}
		return result;
	}
	
	public static int[] unpackInt(int a, int bits)
	{
		int m = 32 / bits;
		int mval = (int) Math.pow(2.0, bits);
		int[] result = new int[m];
		for(int i = m;i > 0;--i)
		{
			int next = a;
			result[i - 1] = next - (a >>= bits) * mval;
		}
		return result;
	}
	
	public static int[] unpackLong(long a, int bits)
	{
		int m = 64 / bits;
		int mval = (int) Math.pow(2.0, bits);
		int[] result = new int[m];
		for(int i = m;i > 0;--i)
		{
			long next = a;
			result[i - 1] = (int) (next - (a >>= bits) * (long) mval);
		}
		return result;
	}
	
	public static String joinStrings(String glueStr, String[] strings, int startIdx, int maxCount)
	{
		return Strings.joinStrings(glueStr, strings, startIdx, maxCount);
	}
	
	public static String joinStrings(String glueStr, String[] strings, int startIdx)
	{
		return Strings.joinStrings(glueStr, strings, startIdx, -1);
	}
	
	public static boolean isNumber(String s)
	{
		try
		{
			Double.parseDouble(s);
		}
		catch(NumberFormatException e)
		{
			return false;
		}
		return true;
	}
	
	public static String dumpObject(Object o, boolean simpleTypes, boolean parentFields, boolean ignoreStatics)
	{
		Class<?> cls = o.getClass();
		String result = "[" + (simpleTypes ? cls.getSimpleName() : cls.getName()) + "\n";
		ArrayList<Field> fields = new ArrayList<>();
		for(cls = o.getClass();cls != null;cls = cls.getSuperclass())
		{
			for(Field fld : cls.getDeclaredFields())
			{
				if(fields.contains(fld) || ignoreStatics && Modifier.isStatic(fld.getModifiers()))
					continue;
				fields.add(fld);
			}
			if(parentFields)
				continue;
		}
		for(Field fld : fields)
		{
			fld.setAccessible(true);
			String val;
			try
			{
				Object fldObj = fld.get(o);
				val = fldObj == null ? "NULL" : fldObj.toString();
			}
			catch(Throwable e)
			{
				e.printStackTrace();
				val = "<ERROR>";
			}
			String type = simpleTypes ? fld.getType().getSimpleName() : fld.getType().toString();
			result = result + String.format("\t%s [%s] = %s;\n", fld.getName(), type, val);
		}
		result = result + "]\n";
		return result;
	}
	
	public static HashMap<Integer, String> parseTemplate(String html)
	{
		Matcher m = _pattern.matcher(html);
		HashMap<Integer, String> tpls = new HashMap<>();
		while(m.find())
		{
			tpls.put(Integer.parseInt(m.group(1)), m.group(2));
			html = html.replace(m.group(0), "");
		}
		tpls.put(0, html);
		return tpls;
	}
	
	public static int fibonacci(int n)
	{
		int x = 0;
		int y = 1;
		for(int i = 0;i < n;++i)
		{
			int z = x;
			x = y;
			y = z + y;
		}
		return x;
	}
	
	public static double padovan(int n)
	{
		if(n == 0 || n == 1 || n == 2)
		{
			return 1.0;
		}
		return padovan(n - 2) + padovan(n - 3);
	}
	
	public static Player[] GetPlayersFromStoredIds(long[] sids)
	{
		ArrayList<Player> result = new ArrayList<>();
		for(long sid : sids)
		{
			Player player = GameObjectsStorage.getAsPlayer(sid);
			if(player == null)
				continue;
			result.add(player);
		}
		return result.toArray(new Player[result.size()]);
	}
}