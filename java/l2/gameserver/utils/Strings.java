package l2.gameserver.utils;

import l2.gameserver.Config;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

public class Strings
{
	private static final Logger _log = LoggerFactory.getLogger(Strings.class);
	private static String[] tr;
	private static String[] trb;
	private static String[] trcode;
	
	public static String stripSlashes(String s)
	{
		if(s == null)
		{
			return "";
		}
		s = s.replace("\\'", "'");
		s = s.replace("\\\\", "\\");
		return s;
	}
	
	public static Boolean parseBoolean(Object x)
	{
		if(x == null)
		{
			return false;
		}
		if(x instanceof Number)
		{
			return ((Number) x).intValue() > 0;
		}
		if(x instanceof Boolean)
		{
			return (Boolean) x;
		}
		if(x instanceof Double)
		{
			return Math.abs((Double) x) < 1.0E-5;
		}
		return !String.valueOf(x).isEmpty();
	}
	
	public static void reload()
	{
		try
		{
			String[] pairs = FileUtils.readFileToString(new File(Config.DATAPACK_ROOT, "data/translit.txt")).split("\n");
			tr = new String[pairs.length * 2];
			String[] ss;
			int i;
			for(i = 0;i < pairs.length;++i)
			{
				ss = pairs[i].split(" +");
				tr[i * 2] = ss[0];
				tr[i * 2 + 1] = ss[1];
			}
			pairs = FileUtils.readFileToString(new File(Config.DATAPACK_ROOT, "data/translit_back.txt")).split("\n");
			trb = new String[pairs.length * 2];
			for(i = 0;i < pairs.length;++i)
			{
				ss = pairs[i].split(" +");
				trb[i * 2] = ss[0];
				trb[i * 2 + 1] = ss[1];
			}
			pairs = FileUtils.readFileToString(new File(Config.DATAPACK_ROOT, "data/transcode.txt")).split("\n");
			trcode = new String[pairs.length * 2];
			for(i = 0;i < pairs.length;++i)
			{
				ss = pairs[i].split(" +");
				trcode[i * 2] = ss[0];
				trcode[i * 2 + 1] = ss[1];
			}
		}
		catch(IOException e)
		{
			_log.error("", e);
		}
		_log.info("Loaded " + (tr.length + tr.length + trcode.length) + " translit entries.");
	}
	
	public static String translit(String s)
	{
		for(int i = 0;i < tr.length;i += 2)
		{
			s = s.replace(tr[i], tr[i + 1]);
		}
		return s;
	}
	
	public static String fromTranslit(String s, int type)
	{
		if(type == 1)
		{
			int i = 0;
			while(i < trb.length)
			{
				s = s.replace(trb[i], trb[i + 1]);
				i += 2;
			}
			return s;
		}
		if(type != 2)
			return s;
		int i = 0;
		while(i < trcode.length)
		{
			s = s.replace(trcode[i], trcode[i + 1]);
			i += 2;
		}
		return s;
	}
	
	public static String replace(String str, String regex, int flags, String replace)
	{
		return Pattern.compile(regex, flags).matcher(str).replaceAll(replace);
	}
	
	public static boolean matches(String str, String regex, int flags)
	{
		return Pattern.compile(regex, flags).matcher(str).matches();
	}
	
	public static String bbParse(String s)
	{
		if(s == null)
		{
			return null;
		}
		s = s.replace("\r", "");
		s = s.replaceAll("(\\s|\"|'|\\(|^|\n)\\*(.*?)\\*(\\s|\"|'|\\)|\\?|\\.|!|:|;|,|$|\n)", "$1<font color=\"LEVEL\">$2</font>$3");
		s = s.replaceAll("(\\s|\"|'|\\(|^|\n)\\$(.*?)\\$(\\s|\"|'|\\)|\\?|\\.|!|:|;|,|$|\n)", "$1<font color=\"00FFFF\">$2</font>$3");
		s = replace(s, "^!(.*?)$", 8, "<font color=\"FFFFFF\">$1</font>\n\n");
		s = s.replaceAll("%%\\s*\n", "<br1>");
		s = s.replaceAll("\n\n+", "<br>");
		s = replace(s, "\\[([^\\]\\|]*?)\\|([^\\]]*?)\\]", 32, "<a action=\"bypass -h $1\">$2</a>");
		s = s.replaceAll(" @", "\" msg=\"");
		return s;
	}
	
	public static String joinStrings(String glueStr, String[] strings, int startIdx, int maxCount)
	{
		String result = "";
		if(startIdx < 0 && (startIdx += strings.length) < 0)
		{
			return result;
		}
		while(startIdx < strings.length && maxCount != 0)
		{
			if(!result.isEmpty() && glueStr != null && !glueStr.isEmpty())
			{
				result = result + glueStr;
			}
			result = result + strings[startIdx++];
			--maxCount;
		}
		return result;
	}
	
	public static String joinStrings(String glueStr, String[] strings, int startIdx)
	{
		return joinStrings(glueStr, strings, startIdx, -1);
	}
	
	public static String joinStrings(String glueStr, String[] strings)
	{
		return joinStrings(glueStr, strings, 0);
	}
	
	public static String stripToSingleLine(String s)
	{
		if(s.isEmpty())
		{
			return s;
		}
		int i = (s = s.replaceAll("\\\\n", "\n")).indexOf("\n");
		if(i > -1)
		{
			s = s.substring(0, i);
		}
		return s;
	}
}