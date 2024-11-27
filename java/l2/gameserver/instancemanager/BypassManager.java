package l2.gameserver.instancemanager;

import l2.gameserver.handler.bbs.CommunityBoardManager;
import l2.gameserver.network.l2.GameClient;
import l2.gameserver.utils.Log;

import java.util.List;

public class BypassManager
{
	private static final String[] SIMBLE_BEGININGS = {"_mrsl", "_diary", "_match", "manor_menu_select", "_match", "_olympiad"};
	private static final String[] SIMBLE_BBS_BEGININGS = {"_bbshome", "_bbsgetfav", "_bbslink", "_bbsloc", "_bbsclan", "_bbsmemo", "_maillist_0_1_0_", "_friendlist_0_", "_bbsaddfav"};
	
	private static boolean isSimpleBypass(String bypass, boolean bbs)
	{
		String[] beginings = bbs ? SIMBLE_BBS_BEGININGS : SIMBLE_BEGININGS;
		for(String begining : beginings)
		{
			if(!bypass.startsWith(begining))
				continue;
			return true;
		}
		return false;
	}
	
	public static BypassType getBypassType(String bypass)
	{
		switch(bypass.charAt(0))
		{
			case '0':
			{
				return BypassType.ENCODED;
			}
			case '1':
			{
				return BypassType.ENCODED_BBS;
			}
		}
		if(isSimpleBypass(bypass, false))
		{
			return BypassType.SIMPLE;
		}
		if(isSimpleBypass(bypass, true) && CommunityBoardManager.getInstance().getCommunityHandler(bypass) != null)
		{
			return BypassType.SIMPLE_BBS;
		}
		return BypassType.SIMPLE_DIRECT;
	}
	
	public static String encode(String html_, List<String> bypassStorage, boolean bbs)
	{
		StringBuffer sb = new StringBuffer();
		char[] html = html_.toCharArray();
		int nextAppendIdx = 0;
		int i = 0;
		while(i + 7 < html.length)
		{
			int bypassPos = 0;
			int bypassLen = 0;
			if(html[i] == '\"' && Character.toLowerCase(html[i + 1]) == 'b' && Character.toLowerCase(html[i + 2]) == 'y' && Character.toLowerCase(html[i + 3]) == 'p' && Character.toLowerCase(html[i + 4]) == 'a' && Character.toLowerCase(html[i + 5]) == 's' && Character.toLowerCase(html[i + 6]) == 's' && Character.isWhitespace(html[i + 7]))
			{
				int len = 8;
				while(len + i < html.length && html[len + i] != '\"')
				{
					++len;
				}
				if(len + i == html.length)
				{
					bypassPos = 0;
					bypassLen = 0;
				}
				else
				{
					bypassPos = i + 1;
					bypassLen = len - 1;
				}
			}
			if(bypassLen > 0)
			{
				int j;
				for(j = 7;j < bypassLen && Character.isWhitespace(html[bypassPos + j]);++j)
				{
				}
				boolean haveMinusH;
				boolean bl = haveMinusH = html[bypassPos + j] == '-' && (html[bypassPos + j + 1] == 'h' || html[bypassPos + j + 1] == 'H');
				if(haveMinusH)
				{
					j += 2;
					while(j < bypassLen && (html[bypassPos + j] == ' ' || html[bypassPos + j] == '\t'))
					{
						++j;
					}
				}
				String bypass;
				String code = bypass = new String(html, bypassPos + j, bypassLen - j);
				String params = "";
				int k = bypass.indexOf(" $");
				boolean use_params;
				boolean bl2 = use_params = k >= 0;
				if(use_params)
				{
					code = bypass.substring(0, k);
					params = bypass.substring(k);
				}
				sb.append(html, nextAppendIdx, bypassPos - nextAppendIdx);
				nextAppendIdx = bypassPos + bypassLen;
				sb.append("bypass ");
				if(haveMinusH)
				{
					sb.append("-h ");
				}
				sb.append(bbs ? '1' : '0');
				List<String> list = bypassStorage;
				synchronized(list)
				{
					sb.append(Integer.toHexString(bypassStorage.size()));
					sb.append(params);
					bypassStorage.add(code);
				}
			}
			++i;
		}
		sb.append(html, nextAppendIdx, html.length - nextAppendIdx);
		return sb.toString();
	}
	
	public static DecodedBypass decode(String bypass, List<String> bypassStorage, boolean bbs, GameClient client)
	{
		List<String> list = bypassStorage;
		synchronized(list)
		{
			String bp;
			String[] bypass_parsed = bypass.split(" ");
			int idx = Integer.parseInt(bypass_parsed[0].substring(1), 16);
			try
			{
				bp = bypassStorage.get(idx);
			}
			catch(Exception e)
			{
				bp = null;
			}
			if(bp == null)
			{
				Log.add("Can't decode bypass (bypass not exists): " + (bbs ? "[bbs] " : "") + bypass + " / Client: " + client + " / Npc: " + (client.getActiveChar() == null || client.getActiveChar().getLastNpc() == null ? "null" : client.getActiveChar().getLastNpc().getName()), "debug_bypass");
				return null;
			}
			DecodedBypass result = new DecodedBypass(bp, bbs);
			for(int i = 1;i < bypass_parsed.length;++i)
			{
				result.bypass = result.bypass + " " + bypass_parsed[i];
			}
			result.trim();
			return result;
		}
	}
	
	public enum BypassType
	{
		ENCODED,
		ENCODED_BBS,
		SIMPLE,
		SIMPLE_BBS,
		SIMPLE_DIRECT;
	}
	
	public static class DecodedBypass
	{
		public String bypass;
		public boolean bbs;
		
		public DecodedBypass(String _bypass, boolean _bbs)
		{
			bypass = _bypass;
			bbs = _bbs;
		}
		
		public DecodedBypass trim()
		{
			bypass = bypass.trim();
			return this;
		}
	}
}