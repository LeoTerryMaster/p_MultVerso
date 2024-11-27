package npc.model;

import l2.gameserver.Config;
import l2.gameserver.cache.Msg;
import l2.gameserver.data.xml.holder.ItemHolder;
import l2.gameserver.model.Player;
import l2.gameserver.model.base.ClassId;
import l2.gameserver.model.instances.MerchantInstance;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.MagicSkillUse;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2.gameserver.templates.item.ItemTemplate;
import l2.gameserver.templates.npc.NpcTemplate;
import l2.gameserver.utils.HtmlUtils;
import l2.gameserver.utils.ItemFunctions;
import l2.gameserver.utils.Util;

import java.util.StringTokenizer;

public final class ClassMasterInstance extends MerchantInstance
{
	public ClassMasterInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	private String makeMessage(Player player)
	{
		ClassId classId = player.getClassId();
		int jobLevel = classId.getLevel();
		int level = player.getLevel();
		StringBuilder html = new StringBuilder();
		if(Config.ALLOW_CLASS_MASTERS_LIST.isEmpty() || !Config.ALLOW_CLASS_MASTERS_LIST.contains(jobLevel))
		{
			jobLevel = 4;
		}
		if((level >= 20 && jobLevel == 1 || level >= 40 && jobLevel == 2 || level >= 76 && jobLevel == 3) && Config.ALLOW_CLASS_MASTERS_LIST.contains(jobLevel))
		{
			int jobLevelPriceIdx = jobLevel - 1;
			ItemTemplate item = ItemHolder.getInstance().getTemplate(Config.CLASS_MASTERS_PRICE_ITEM[jobLevelPriceIdx]);
			if(Config.CLASS_MASTERS_PRICE_LIST[jobLevelPriceIdx] > 0)
			{
				html.append("Price: ").append(Util.formatAdena(Config.CLASS_MASTERS_PRICE_LIST[jobLevelPriceIdx])).append(" ").append(item.getName()).append("<br1>");
			}
			for(ClassId cid : ClassId.VALUES)
			{
				if(!cid.childOf(classId) || cid.getLevel() != classId.getLevel() + 1)
					continue;
				html.append("<a action=\"bypass -h npc_").append(getObjectId()).append("_change_class ").append(cid.getId()).append(" ").append(jobLevelPriceIdx).append("\">").append(HtmlUtils.htmlClassName(cid.getId(), player)).append("</a><br>");
			}
			player.sendPacket(new NpcHtmlMessage(player, this).setHtml(html.toString()));
		}
		else
		{
			switch(jobLevel)
			{
				case 1:
				{
					html.append(new CustomMessage("ClassMaster.Need20Level", player));
					break;
				}
				case 2:
				{
					html.append(new CustomMessage("ClassMaster.Need40Level", player));
					break;
				}
				case 3:
				{
					html.append(new CustomMessage("ClassMaster.Need76Level", player));
					break;
				}
				case 4:
				{
					html.append(new CustomMessage("ClassMaster.NothingToUp", player));
				}
			}
		}
		return html.toString();
	}
	
	@Override
	public void showChatWindow(Player player, int val, Object... arg)
	{
		NpcHtmlMessage msg = new NpcHtmlMessage(player, this);
		msg.setFile("custom/31860.htm");
		msg.replace("%classmaster%", makeMessage(player));
		player.sendPacket(msg);
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
		{
			return;
		}
		StringTokenizer st = new StringTokenizer(command);
		if(st.nextToken().equals("change_class"))
		{
			int val = Integer.parseInt(st.nextToken());
			int idx = Integer.parseInt(st.nextToken());
			if(idx < Config.CLASS_MASTERS_PRICE_ITEM.length && idx < Config.CLASS_MASTERS_PRICE_LIST.length)
			{
				int itemId = Config.CLASS_MASTERS_PRICE_ITEM[idx];
				long itemCount = Config.CLASS_MASTERS_PRICE_LIST[idx];
				if(player.getInventory().destroyItemByItemId(itemId, itemCount))
				{
					changeClass(player, val);
					if(Config.CLASS_MASTERS_REWARD_ITEM.length > idx && Config.CLASS_MASTERS_REWARD_ITEM[idx] > 0 && Config.CLASS_MASTERS_REWARD_AMOUNT.length > idx && Config.CLASS_MASTERS_REWARD_AMOUNT[idx] > 0)
					{
						ItemFunctions.addItem(player, Config.CLASS_MASTERS_REWARD_ITEM[idx], Config.CLASS_MASTERS_REWARD_AMOUNT[idx], true);
					}
				}
				else if(itemId == 57)
				{
					player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
				}
				else
				{
					player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
				}
			}
			else
			{
				System.out.println("Incorect job index " + idx);
			}
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
	
	private void changeClass(Player player, int val)
	{
		if(player.getClassId().getLevel() == 3)
		{
			player.sendPacket(Msg.YOU_HAVE_COMPLETED_THE_QUEST_FOR_3RD_OCCUPATION_CHANGE_AND_MOVED_TO_ANOTHER_CLASS_CONGRATULATIONS);
		}
		else
		{
			player.sendPacket(Msg.CONGRATULATIONS_YOU_HAVE_TRANSFERRED_TO_A_NEW_CLASS);
		}
		player.setClassId(val, false, false);
		player.broadcastCharInfo();
		player.broadcastPacket(new MagicSkillUse(player, player, 4339, 1, 0, 0));
	}
}