package handler.admincommands;

import l2.commons.collections.MultiValueSet;
import l2.gameserver.model.Player;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2.gameserver.utils.Location;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class AdminTeleportBookmark extends ScriptAdminCommand
{
	private static final int BOOKBARKS_PER_PAGE = 12;
	private static final String BOOKBARKS_VAR_PREFIX = "gmbk_";
	private static final Comparator<Pair<String, Location>> BOOKMARKS_SORT_COMPARATOR = new Comparator<Pair<String, Location>>()
	{
		@Override
		public int compare(Pair<String, Location> o1, Pair<String, Location> o2)
		{
			return o1.getLeft().compareTo(o2.getLeft());
		}
	};
	
	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player player)
	{
		Commands c = (Commands) comm;
		if(!player.getPlayerAccess().CanUseGMCommand)
		{
			return false;
		}
		switch(c)
		{
			case admin_bkpage:
			{
				int page = wordList.length > 1 ? Integer.parseInt(wordList[1]) : 1;
				NpcHtmlMessage reply = new NpcHtmlMessage(5);
				reply.setHtml(formatBookmarkListHtml(getBookmarksList(player), page));
				player.sendPacket(reply);
				return true;
			}
			case admin_bkgo:
			{
				if(wordList.length > 1)
				{
					String name = wordList[1].replace("<", "&lt;").replace(">", "&gt;");
					String xyz = player.getVar(BOOKBARKS_VAR_PREFIX + name);
					if(xyz != null)
					{
						player.teleToLocation(Location.parseLoc(xyz));
					}
				}
				return true;
			}
			case admin_bk:
			{
				if(wordList.length > 1)
				{
					String name = wordList[1].replace("<", "&lt;").replace(">", "&gt;");
					player.setVar(BOOKBARKS_VAR_PREFIX + name, player.getLoc().toXYZString(), -1);
				}
				NpcHtmlMessage reply = new NpcHtmlMessage(5);
				reply.setHtml(formatBookmarkListHtml(getBookmarksList(player), 1));
				player.sendPacket(reply);
				return true;
			}
			case admin_bkdel:
			{
				if(wordList.length > 1)
				{
					String name = wordList[1].replace("<", "&lt;").replace(">", "&gt;");
					player.unsetVar(BOOKBARKS_VAR_PREFIX + name);
				}
				NpcHtmlMessage reply = new NpcHtmlMessage(5);
				reply.setHtml(formatBookmarkListHtml(getBookmarksList(player), 1));
				player.sendPacket(reply);
				return true;
			}
		}
		return false;
	}
	
	private List<Pair<String, Location>> getBookmarksList(Player player)
	{
		ArrayList<Pair<String, Location>> result = new ArrayList<>();
		MultiValueSet<String> userVars = player.getVars();
		for(Map.Entry e : userVars.entrySet())
		{
			if(e == null || e.getKey() == null || !((String) e.getKey()).startsWith(BOOKBARKS_VAR_PREFIX))
				continue;
			result.add((Pair) Pair.of((Object) ((String) e.getKey()).substring(BOOKBARKS_VAR_PREFIX.length()), (Object) Location.parseLoc(e.getValue().toString())));
		}
		Collections.sort(result, BOOKMARKS_SORT_COMPARATOR);
		return result;
	}
	
	private String formatBookmarkListHtml(List<Pair<String, Location>> bookmarksList, int page)
	{
		StringBuilder htmlText = new StringBuilder();
		htmlText.append("<html><body>");
		htmlText.append("<center><br>Bookmark list<br>");
		htmlText.append("<table width=270 border=0>");
		int bkMinIdx = Math.max(0, (page - 1) * BOOKBARKS_PER_PAGE);
		int bkMaxIdx = Math.min(bookmarksList.size(), page * BOOKBARKS_PER_PAGE);
		for(int bkIdx = bkMinIdx;bkIdx < bkMaxIdx;++bkIdx)
		{
			Pair<String, Location> bk = bookmarksList.get(bkIdx);
			htmlText.append("<tr>");
			htmlText.append("<td> ");
			htmlText.append("<a action=\"bypass admin_bkgo ").append(bk.getKey()).append("\"> [");
			htmlText.append(bk.getKey()).append(", ").append(bk.getValue().toXYZString());
			htmlText.append("] </a> </td><td> ");
			htmlText.append("<a action=\"bypass admin_bkdel ").append(bk.getKey()).append("\">del");
			htmlText.append(" </td>");
			htmlText.append(" </tr>");
		}
		htmlText.append("<tr><td align=center>");
		htmlText.append("<table border=0 cellspacing=2 cellpadding=2><tr>");
		page = Math.max(1, page);
		if(page > 1)
		{
			htmlText.append("<td align=right> ").append("<button value=\"&$1037;\" action=\"bypass %prev_bypass%\" width=65 height=20 back=\"l2ui_ch3.smallbutton2_down\" fore=\"l2ui_ch3.smallbutton2\">".replace("%prev_bypass%", "admin_bkpage " + (page - 1))).append(" </td>");
		}
		htmlText.append("<td> ").append(page).append(" </td>");
		if(page <= bookmarksList.size() / BOOKBARKS_PER_PAGE)
		{
			htmlText.append("<td> ").append("<button value=\"&$1038;\" action=\"bypass %next_bypass%\" width=65 height=20 back=\"l2ui_ch3.smallbutton2_down\" fore=\"l2ui_ch3.smallbutton2\">".replace("%next_bypass%", "admin_bkpage " + (page + 1))).append(" </td>");
		}
		htmlText.append("</tr></table>");
		htmlText.append("</td><td></td></tr>");
		htmlText.append("</table>");
		htmlText.append("</center>");
		htmlText.append("</body></html>");
		return htmlText.toString();
	}
	
	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}
	
	enum Commands
	{
		admin_bk,
		admin_bkgo,
		admin_bkdel,
		admin_bkpage;
	}
}