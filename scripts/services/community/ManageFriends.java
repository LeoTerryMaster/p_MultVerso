package services.community;

import l2.gameserver.Config;
import l2.gameserver.data.htm.HtmCache;
import l2.gameserver.handler.bbs.CommunityBoardManager;
import l2.gameserver.handler.bbs.ICommunityBoardHandler;
import l2.gameserver.model.Player;
import l2.gameserver.model.actor.instances.player.Friend;
import l2.gameserver.network.l2.s2c.ShowBoard;
import l2.gameserver.scripts.ScriptFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

public class ManageFriends implements ScriptFile, ICommunityBoardHandler
{
	private static final Logger _log = LoggerFactory.getLogger(ManageFriends.class);
	
	private static String getFriendList(Player player)
	{
		StringBuilder friendList = new StringBuilder("");
		Map<Integer, Friend> fl = player.getFriendList().getList();
		Iterator var3 = fl.entrySet().iterator();
		
		while(var3.hasNext())
		{
			Entry<Integer, Friend> entry = (Entry) var3.next();
			friendList.append("<a action=\"bypass _friendlist_1_").append(entry.getKey()).append("\">").append(entry.getValue().getName()).append("</a> (").append(entry.getValue().isOnline() ? "on" : "off").append(") &nbsp;");
		}
		
		return friendList.toString();
	}
	
	private static String getSelectedList(Player player)
	{
		String selected = player.getSessionVar("selFriends");
		if(selected == null)
		{
			return "";
		}
		else
		{
			String[] sels = selected.split(";");
			StringBuilder selectedList = new StringBuilder("");
			String[] var4 = sels;
			int var5 = sels.length;
			
			for(int var6 = 0;var6 < var5;++var6)
			{
				String objectId = var4[var6];
				if(!objectId.isEmpty())
				{
					selectedList.append("<a action=\"bypass _friendlist_2_").append(objectId).append("\">").append(player.getFriendList().getList().get(Integer.parseInt(objectId)).getName()).append("</a>;");
				}
			}
			
			return selectedList.toString();
		}
	}
	
	private static String getBlockList(Player player)
	{
		StringBuilder blockList = new StringBuilder("");
		Map<Integer, String> bl = player.getBlockListMap();
		Iterator var3 = bl.keySet().iterator();
		
		while(var3.hasNext())
		{
			Integer objectId = (Integer) var3.next();
			blockList.append(bl.get(objectId)).append("&nbsp; <a action=\"bypass _friendblockdelete_").append(objectId).append("\">Delete</a>&nbsp;&nbsp;");
		}
		
		return blockList.toString();
	}
	
	@Override
	public void onLoad()
	{
		if(Config.COMMUNITYBOARD_ENABLED)
		{
			_log.info("CommunityBoard: Manage Friends service loaded.");
			CommunityBoardManager.getInstance().registerHandler(this);
		}
	}
	
	@Override
	public void onReload()
	{
		if(Config.COMMUNITYBOARD_ENABLED)
		{
			CommunityBoardManager.getInstance().removeHandler(this);
		}
	}
	
	@Override
	public void onShutdown()
	{
	}
	
	@Override
	public String[] getBypassCommands()
	{
		return new String[] {"_friendlist_", "_friendblocklist_", "_frienddelete_", "_frienddeleteallconfirm_", "_frienddeleteall_", "_friendblockdelete_", "_friendblockadd_", "_friendblockdeleteallconfirm_", "_friendblockdeleteall_"};
	}
	
	@Override
	public void onBypassCommand(Player player, String bypass)
	{
		StringTokenizer st = new StringTokenizer(bypass, "_");
		String cmd = st.nextToken();
		String html = HtmCache.getInstance().getNotNull(cmd.startsWith("friendbloc") ? "scripts/services/community/bbs_block_list.htm" : "scripts/services/community/bbs_friend_list.htm", player);
		player.setSessionVar("add_fav", null);
		String objId;
		String name;
		if(cmd.equals("friendlist"))
		{
			objId = st.nextToken();
			html = html.replace("%friend_list%", getFriendList(player));
			if(objId.equals("0"))
			{
				if(player.getSessionVar("selFriends") != null)
				{
					player.setSessionVar("selFriends", null);
				}
				
				html = html.replace("%selected_friend_list%", "");
				html = html.replace("%delete_all_msg%", "");
			}
			else
			{
				if(objId.equals("1"))
				{
					objId = st.nextToken();
					if((name = player.getSessionVar("selFriends")) == null)
					{
						name = objId + ";";
					}
					else if(!name.contains(objId))
					{
						name = name + objId + ";";
					}
					
					player.setSessionVar("selFriends", name);
					html = html.replace("%selected_friend_list%", getSelectedList(player));
					html = html.replace("%delete_all_msg%", "");
				}
				else if(objId.equals("2"))
				{
					objId = st.nextToken();
					name = player.getSessionVar("selFriends");
					if(name != null)
					{
						name = name.replace(objId + ";", "");
						player.setSessionVar("selFriends", name);
					}
					
					html = html.replace("%selected_friend_list%", getSelectedList(player));
					html = html.replace("%delete_all_msg%", "");
				}
			}
		}
		else if(cmd.equals("frienddeleteallconfirm"))
		{
			html = html.replace("%friend_list%", getFriendList(player));
			html = html.replace("%selected_friend_list%", getSelectedList(player));
			html = html.replace("%delete_all_msg%", "<br>\nAre you sure you want to delete all friends from the friends list? <button value = \"OK\" action=\"bypass _frienddeleteall_\" back=\"l2ui_ct1.button.button_df_small_down\" width=70 height=25 fore=\"l2ui_ct1.button.button_df_small\">");
		}
		else if(cmd.equals("frienddelete"))
		{
			objId = player.getSessionVar("selFriends");
			if(objId != null)
			{
				String[] var12 = objId.split(";");
				int var15 = var12.length;
				
				for(int var9 = 0;var9 < var15;++var9)
				{
					objId = var12[var9];
					if(!objId.isEmpty())
					{
						player.getFriendList().removeFriend(player.getFriendList().getList().get(Integer.parseInt(objId)).getName());
					}
				}
			}
			
			player.setSessionVar("selFriends", null);
			html = html.replace("%friend_list%", getFriendList(player));
			html = html.replace("%selected_friend_list%", "");
			html = html.replace("%delete_all_msg%", "");
		}
		else
		{
			ArrayList bl;
			Iterator var13;
			if(cmd.equals("frienddeleteall"))
			{
				bl = new ArrayList(1);
				bl.addAll(player.getFriendList().getList().values());
				var13 = bl.iterator();
				
				while(var13.hasNext())
				{
					Friend friend = (Friend) var13.next();
					player.getFriendList().removeFriend(friend.getName());
				}
				
				player.setSessionVar("selFriends", null);
				html = html.replace("%friend_list%", "");
				html = html.replace("%selected_friend_list%", "");
				html = html.replace("%delete_all_msg%", "");
			}
			else if(cmd.equals("friendblocklist"))
			{
				html = html.replace("%block_list%", getBlockList(player));
				html = html.replace("%delete_all_msg%", "");
			}
			else if(cmd.equals("friendblockdeleteallconfirm"))
			{
				html = html.replace("%block_list%", getBlockList(player));
				html = html.replace("%delete_all_msg%", "<br>\nDo you want to delete all characters from the block list? <button value = \"OK\" action=\"bypass _friendblockdeleteall_\" back=\"l2ui_ct1.button.button_df_small_down\" width=70 height=25 fore=\"l2ui_ct1.button.button_df_small\" >");
			}
			else if(cmd.equals("friendblockdelete"))
			{
				objId = st.nextToken();
				if(objId != null && !objId.isEmpty())
				{
					int objectId = Integer.parseInt(objId);
					name = player.getBlockListMap().get(objectId);
					if(name != null)
					{
						player.removeFromBlockList(name);
					}
				}
				
				html = html.replace("%block_list%", getBlockList(player));
				html = html.replace("%delete_all_msg%", "");
			}
			else if(cmd.equals("friendblockdeleteall"))
			{
				bl = new ArrayList(1);
				bl.addAll(player.getBlockList());
				var13 = bl.iterator();
				
				while(var13.hasNext())
				{
					name = (String) var13.next();
					player.removeFromBlockList(name);
				}
				
				html = html.replace("%block_list%", "");
				html = html.replace("%delete_all_msg%", "");
			}
		}
		
		ShowBoard.separateAndSend(html, player);
	}
	
	@Override
	public void onWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4, String arg5)
	{
		String html = HtmCache.getInstance().getNotNull("scripts/services/community/bbs_block_list.htm", player);
		if("_friendblockadd_".equals(bypass) && arg3 != null && !arg3.isEmpty())
		{
			player.addToBlockList(arg3);
		}
		
		html = html.replace("%block_list%", getBlockList(player));
		html = html.replace("%delete_all_msg%", "");
		ShowBoard.separateAndSend(html, player);
	}
}