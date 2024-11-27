package services;

import l2.gameserver.Config;
import l2.gameserver.data.htm.HtmCache;
import l2.gameserver.database.mysql;
import l2.gameserver.model.Player;
import l2.gameserver.model.SubClass;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2.gameserver.scripts.Functions;
import l2.gameserver.utils.HtmlUtils;
import l2.gameserver.utils.ItemFunctions;
import l2.gameserver.utils.Log;

import java.util.Map;

public class SubClassSeparate extends Functions
{
	public void separate_page()
	{
		Player player = getSelf();
		if(player == null)
		{
			return;
		}
		if(!Config.SERVICES_SEPARATE_SUB_ENABLED)
		{
			player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/service_disabled.htm"));
			return;
		}
		if(player.getSubClasses().size() == 1)
		{
			player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/subclass_separate_err01.htm"));
			return;
		}
		NpcHtmlMessage msg = new NpcHtmlMessage(5).setFile("scripts/services/subclass_separate.htm");
		msg.replace("%item_id%", String.valueOf(Config.SERVICES_SEPARATE_SUB_ITEM));
		msg.replace("%item_count%", String.valueOf(Config.SERVICES_SEPARATE_SUB_PRICE));
		msg.replace("%min_level%", String.valueOf(Config.SERVICES_SEPARATE_SUB_MIN_LEVEL));
		String item = HtmCache.getInstance().getNotNull("scripts/services/subclass_separate_list.htm", player);
		StringBuilder sb = new StringBuilder();
		for(SubClass s : player.getSubClasses().values())
		{
			if(s.isBase())
				continue;
			sb.append(item.replace("%class_id%", String.valueOf(s.getClassId())).replace("%class_name%", HtmlUtils.htmlClassName(s.getClassId(), player)));
		}
		msg.replace("%list%", sb.toString());
		player.sendPacket(msg);
	}
	
	public void separate(String[] arg)
	{
		Player player = getSelf();
		if(player == null)
		{
			return;
		}
		if(!Config.SERVICES_SEPARATE_SUB_ENABLED)
		{
			player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/service_disabled.htm"));
			return;
		}
		if(arg == null || arg.length < 2)
		{
			return;
		}
		if(player.getSubClasses().size() == 1)
		{
			player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/subclass_separate_err01.htm"));
			return;
		}
		if(!player.getActiveClass().isBase())
		{
			player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/subclass_separate_err03.htm"));
			return;
		}
		if(player.getActiveClass().getLevel() < 75)
		{
			player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/subclass_separate_err04.htm"));
			return;
		}
		if(player.isHero())
		{
			player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/subclass_separate_err05.htm"));
			return;
		}
		int classtomove = Integer.parseInt(arg[0]);
		int newcharid = 0;
		for(Map.Entry e : player.getAccountChars().entrySet())
		{
			if(!((String) e.getValue()).equalsIgnoreCase(arg[1]))
				continue;
			newcharid = (Integer) e.getKey();
		}
		if(newcharid == 0)
		{
			player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/subclass_separate_err06.htm"));
			return;
		}
		if(mysql.simple_get_int("level", "character_subclasses", "char_obj_id=" + newcharid + " AND level > " + Config.SERVICES_SEPARATE_SUB_MIN_LEVEL) > 1)
		{
			player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/subclass_separate_err07.htm"));
			return;
		}
		if(!player.isInPeaceZone() || !player.getReflection().isDefault())
		{
			player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/subclass_separate_err08.htm"));
			return;
		}
		if(ItemFunctions.getItemCount(player, Config.SERVICES_SEPARATE_SUB_ITEM) < (long) Config.SERVICES_SEPARATE_SUB_PRICE)
		{
			if(Config.SERVICES_SEPARATE_SUB_ITEM == 57)
			{
				player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			}
			else
			{
				player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
			}
			return;
		}
		ItemFunctions.removeItem(player, Config.SERVICES_SEPARATE_SUB_ITEM, (long) Config.SERVICES_SEPARATE_SUB_PRICE, true);
		mysql.set("DELETE FROM character_subclasses WHERE char_obj_id=" + newcharid);
		mysql.set("DELETE FROM character_skills WHERE char_obj_id=" + newcharid);
		mysql.set("DELETE FROM character_skills_save WHERE char_obj_id=" + newcharid);
		mysql.set("DELETE FROM character_effects_save WHERE object_id=" + newcharid);
		mysql.set("DELETE FROM character_hennas WHERE char_obj_id=" + newcharid);
		mysql.set("DELETE FROM character_shortcuts WHERE object_id=" + newcharid);
		mysql.set("DELETE FROM character_variables WHERE obj_id=" + newcharid);
		mysql.set("UPDATE character_subclasses SET char_obj_id=" + newcharid + ", isBase=1 WHERE char_obj_id=" + player.getObjectId() + " AND class_id=" + classtomove);
		mysql.set("UPDATE character_skills SET char_obj_id=" + newcharid + " WHERE char_obj_id=" + player.getObjectId() + " AND class_index=" + classtomove);
		mysql.set("UPDATE character_skills_save SET char_obj_id=" + newcharid + " WHERE char_obj_id=" + player.getObjectId() + " AND class_index=" + classtomove);
		mysql.set("UPDATE character_effects_save SET object_id=" + newcharid + " WHERE object_id=" + player.getObjectId() + " AND id=" + classtomove);
		mysql.set("UPDATE character_hennas SET char_obj_id=" + newcharid + " WHERE char_obj_id=" + player.getObjectId() + " AND class_index=" + classtomove);
		mysql.set("UPDATE character_shortcuts SET object_id=" + newcharid + " WHERE object_id=" + player.getObjectId() + " AND class_index=" + classtomove);
		player.modifySubClass(classtomove, 0);
		player.logout();
		Log.add("Character " + player + " subclass separated to " + arg[1], "services");
	}
}