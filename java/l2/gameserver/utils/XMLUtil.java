package l2.gameserver.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

public class XMLUtil
{
	private static final Logger _log = LoggerFactory.getLogger(XMLUtil.class);
	
	public static String getAttributeValue(Node n, String item)
	{
		Node d = n.getAttributes().getNamedItem(item);
		if(d == null)
		{
			return "";
		}
		String val = d.getNodeValue();
		if(val == null)
		{
			return "";
		}
		return val;
	}
	
	public static boolean getAttributeBooleanValue(Node n, String item, boolean dflt)
	{
		Node d = n.getAttributes().getNamedItem(item);
		if(d == null)
		{
			return dflt;
		}
		String val = d.getNodeValue();
		if(val == null)
		{
			return dflt;
		}
		return Boolean.parseBoolean(val);
	}
	
	public static int getAttributeIntValue(Node n, String item, int dflt)
	{
		Node d = n.getAttributes().getNamedItem(item);
		if(d == null)
		{
			return dflt;
		}
		String val = d.getNodeValue();
		if(val == null)
		{
			return dflt;
		}
		return Integer.parseInt(val);
	}
	
	public static long getAttributeLongValue(Node n, String item, long dflt)
	{
		Node d = n.getAttributes().getNamedItem(item);
		if(d == null)
		{
			return dflt;
		}
		String val = d.getNodeValue();
		if(val == null)
		{
			return dflt;
		}
		return Long.parseLong(val);
	}
}