package l2.gameserver.utils;

import l2.gameserver.model.Player;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.network.l2.components.NpcString;
import l2.gameserver.network.l2.components.SysString;

public class HtmlUtils
{
	public static final String PREV_BUTTON = "<button value=\"&$1037;\" action=\"bypass %prev_bypass%\" width=65 height=20 back=\"l2ui_ch3.smallbutton2_down\" fore=\"l2ui_ch3.smallbutton2\">";
	public static final String NEXT_BUTTON = "<button value=\"&$1038;\" action=\"bypass %next_bypass%\" width=65 height=20 back=\"l2ui_ch3.smallbutton2_down\" fore=\"l2ui_ch3.smallbutton2\">";
	
	public static String htmlResidenceName(int id)
	{
		return "&%" + id + ";";
	}
	
	public static String htmlNpcName(int npcId)
	{
		return "&@" + npcId + ";";
	}
	
	public static String htmlSysString(SysString sysString)
	{
		return htmlSysString(sysString.getId());
	}
	
	public static String htmlSysString(int id)
	{
		return "&$" + id + ";";
	}
	
	public static String htmlItemName(int itemId)
	{
		return "&#" + itemId + ";";
	}
	
	public static String htmlClassName(int classId, Player player)
	{
		return new CustomMessage(String.format("ClassName.%d", classId), player, new Object[0]).toString();
	}
	
	public static String htmlNpcString(NpcString id, Object... params)
	{
		return htmlNpcString(id.getId(), params);
	}
	
	public static String htmlNpcString(int id, Object... params)
	{
		String replace = "<fstring";
		if(params.length > 0)
		{
			for(int i = 0;i < params.length;++i)
			{
				replace = replace + " p" + (i + 1) + "=\"" + params[i] + "\"";
			}
		}
		replace = replace + ">" + id + "</fstring>";
		return replace;
	}
	
	public static String htmlButton(String value, String action, int width)
	{
		return htmlButton(value, action, width, 22);
	}
	
	public static String htmlButton(String value, String action, int width, int height)
	{
		return String.format("<button value=\"%s\" action=\"%s\" back=\"L2UI_CH3.bigbutton2_down\" width=%d height=%d fore=\"L2UI_CH3.bigbutton2\">", value, action, width, height);
	}
}