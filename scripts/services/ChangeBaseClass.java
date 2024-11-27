package services;

import l2.gameserver.Config;
import l2.gameserver.data.htm.HtmCache;
import l2.gameserver.model.Player;
import l2.gameserver.model.SubClass;
import l2.gameserver.model.base.PlayerClass;
import l2.gameserver.model.entity.oly.NoblesController;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2.gameserver.scripts.Functions;
import l2.gameserver.utils.HtmlUtils;
import l2.gameserver.utils.ItemFunctions;
import l2.gameserver.utils.Log;

import java.util.ArrayList;

public class ChangeBaseClass extends Functions
{
	public void changebase_page()
	{
		Player player = getSelf();
		if(player == null)
		{
			return;
		}
		if(!Config.SERVICES_CHANGE_BASE_ENABLED)
		{
			player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/service_disabled.htm"));
			return;
		}
		if(player.getSubClasses().size() == 1)
		{
			player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/changebase_err01.htm"));
			return;
		}
		if(!player.getActiveClass().isBase())
		{
			player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/changebase_err02.htm"));
			return;
		}
		NpcHtmlMessage msg = new NpcHtmlMessage(5).setFile("scripts/services/changebase.htm");
		msg.replace("%item_id%", String.valueOf(Config.SERVICES_CHANGE_BASE_ITEM));
		msg.replace("%item_count%", String.valueOf(Config.SERVICES_CHANGE_BASE_PRICE));
		ArrayList<SubClass> possible = new ArrayList();
		if(player.getActiveClass().isBase())
		{
			possible.addAll(player.getSubClasses().values());
			possible.remove(player.getSubClasses().get(player.getBaseClassId()));
			for(SubClass s : player.getSubClasses().values())
			{
				for(SubClass s2 : player.getSubClasses().values())
				{
					if(s == s2 || Config.SERVICES_CHANGE_BASE_LIST_UNCOMPATABLE || PlayerClass.areClassesComportable(PlayerClass.values()[s.getClassId()], PlayerClass.values()[s2.getClassId()]) || s2.getLevel() < 75)
						continue;
					possible.remove(s2);
				}
			}
		}
		StringBuilder sb = new StringBuilder();
		if(!possible.isEmpty())
		{
			String item = HtmCache.getInstance().getNotNull("scripts/services/changebase_list.htm", player);
			for(SubClass s : possible)
			{
				sb.append(item.replace("%class_id%", String.valueOf(s.getClassId())).replace("%class_name%", HtmlUtils.htmlClassName(s.getClassId(), player)));
			}
		}
		msg.replace("%list%", sb.toString());
		player.sendPacket(msg);
	}
	
	public void changebase(String[] arg)
	{
		Player player = getSelf();
		if(player == null)
		{
			return;
		}
		if(arg == null || arg.length < 1)
		{
			return;
		}
		if(!Config.SERVICES_CHANGE_BASE_ENABLED)
		{
			player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/service_disabled.htm"));
			return;
		}
		if(player.getSubClasses().size() == 1)
		{
			player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/changebase_err01.htm"));
			return;
		}
		if(!player.getActiveClass().isBase())
		{
			player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/changebase_err02.htm"));
			return;
		}
		if(!player.isInPeaceZone() || !player.getReflection().isDefault())
		{
			player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/changebase_err03.htm"));
			return;
		}
		if(player.isHero())
		{
			player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/changebase_err04.htm"));
			return;
		}
		if(ItemFunctions.getItemCount(player, Config.SERVICES_CHANGE_BASE_ITEM) < (long) Config.SERVICES_CHANGE_BASE_PRICE)
		{
			if(Config.SERVICES_CHANGE_BASE_ITEM == 57)
			{
				player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			}
			else
			{
				player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
			}
			return;
		}
		ItemFunctions.removeItem(player, Config.SERVICES_CHANGE_BASE_ITEM, (long) Config.SERVICES_CHANGE_BASE_PRICE, true);
		int target = Integer.parseInt(arg[0]);
		SubClass newBase = player.getSubClasses().get(target);
		player.getActiveClass().setBase(false);
		player.getActiveClass().setExp(player.getExp());
		player.checkSkills();
		newBase.setBase(true);
		player.setBaseClass(target);
		player.setHairColor(0);
		player.setHairStyle(0);
		player.setFace(0);
		if(player.isNoble())
		{
			player.setNoble(false);
			NoblesController.getInstance().removeNoble(player);
			NoblesController.getInstance().addNoble(player);
			player.setNoble(true);
		}
		player.logout();
		Log.add("Character " + player + " base changed to " + target, "services");
	}
}