package l2.gameserver.model.instances;

import l2.gameserver.Config;
import l2.gameserver.ai.CtrlIntention;
import l2.gameserver.model.Player;
import l2.gameserver.network.l2.s2c.MyTargetSelected;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2.gameserver.network.l2.s2c.ValidateLocation;
import l2.gameserver.scripts.Events;
import l2.gameserver.tables.SkillTable;
import l2.gameserver.templates.npc.NpcTemplate;
import l2.gameserver.utils.WarehouseFunctions;

import java.util.StringTokenizer;

public final class NpcFriendInstance extends MerchantInstance
{
	public NpcFriendInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onAction(Player player, boolean shift)
	{
		if(this != player.getTarget())
		{
			player.setTarget(this);
			player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()), new ValidateLocation(this));
			if(isAutoAttackable(player))
			{
				player.sendPacket(makeStatusUpdate(9, 10));
			}
			player.sendActionFailed();
			return;
		}
		player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()));
		if(Events.onAction(player, this, shift))
		{
			return;
		}
		if(isAutoAttackable(player))
		{
			player.getAI().Attack(this, false, shift);
			return;
		}
		if(!isInActingRange(player))
		{
			if(player.getAI().getIntention() != CtrlIntention.AI_INTENTION_INTERACT)
			{
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
			}
			return;
		}
		if(!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && player.getKarma() > 0 && !player.isGM())
		{
			player.sendActionFailed();
			return;
		}
		if(!Config.ALLOW_TALK_WHILE_SITTING && player.isSitting() || player.isAlikeDead())
		{
			return;
		}
		if(hasRandomAnimation())
		{
			onRandomAnimation();
		}
		player.sendActionFailed();
		player.setLastNpcInteractionTime();
		String filename = "";
		if(getNpcId() >= 31370 && getNpcId() <= 31376 && player.getVarka() > 0 || getNpcId() >= 31377 && getNpcId() < 31384 && player.getKetra() > 0)
		{
			filename = "npc_friend/" + getNpcId() + "-nofriend.htm";
			showChatWindow(player, filename);
			return;
		}
		switch(getNpcId())
		{
			case 31370:
			case 31371:
			case 31373:
			case 31377:
			case 31378:
			case 31380:
			case 31553:
			case 31554:
			{
				filename = "npc_friend/" + getNpcId() + ".htm";
				break;
			}
			case 31372:
			{
				if(player.getKetra() > 2)
				{
					filename = "npc_friend/" + getNpcId() + "-bufflist.htm";
					break;
				}
				filename = "npc_friend/" + getNpcId() + ".htm";
				break;
			}
			case 31379:
			{
				if(player.getVarka() > 2)
				{
					filename = "npc_friend/" + getNpcId() + "-bufflist.htm";
					break;
				}
				filename = "npc_friend/" + getNpcId() + ".htm";
				break;
			}
			case 31374:
			{
				if(player.getKetra() > 1)
				{
					filename = "npc_friend/" + getNpcId() + "-warehouse.htm";
					break;
				}
				filename = "npc_friend/" + getNpcId() + ".htm";
				break;
			}
			case 31381:
			{
				if(player.getVarka() > 1)
				{
					filename = "npc_friend/" + getNpcId() + "-warehouse.htm";
					break;
				}
				filename = "npc_friend/" + getNpcId() + ".htm";
				break;
			}
			case 31375:
			{
				if(player.getKetra() == 3 || player.getKetra() == 4)
				{
					filename = "npc_friend/" + getNpcId() + "-special1.htm";
					break;
				}
				if(player.getKetra() == 5)
				{
					filename = "npc_friend/" + getNpcId() + "-special2.htm";
					break;
				}
				filename = "npc_friend/" + getNpcId() + ".htm";
				break;
			}
			case 31382:
			{
				if(player.getVarka() == 3 || player.getVarka() == 4)
				{
					filename = "npc_friend/" + getNpcId() + "-special1.htm";
					break;
				}
				if(player.getVarka() == 5)
				{
					filename = "npc_friend/" + getNpcId() + "-special2.htm";
					break;
				}
				filename = "npc_friend/" + getNpcId() + ".htm";
				break;
			}
			case 31376:
			{
				if(player.getKetra() == 4)
				{
					filename = "npc_friend/" + getNpcId() + "-normal.htm";
					break;
				}
				if(player.getKetra() == 5)
				{
					filename = "npc_friend/" + getNpcId() + "-special.htm";
					break;
				}
				filename = "npc_friend/" + getNpcId() + ".htm";
				break;
			}
			case 31383:
			{
				if(player.getVarka() == 4)
				{
					filename = "npc_friend/" + getNpcId() + "-normal.htm";
					break;
				}
				if(player.getVarka() == 5)
				{
					filename = "npc_friend/" + getNpcId() + "-special.htm";
					break;
				}
				filename = "npc_friend/" + getNpcId() + ".htm";
				break;
			}
			case 31555:
			{
				if(player.getRam() == 1)
				{
					filename = "npc_friend/" + getNpcId() + "-special1.htm";
					break;
				}
				if(player.getRam() == 2)
				{
					filename = "npc_friend/" + getNpcId() + "-special2.htm";
					break;
				}
				filename = "npc_friend/" + getNpcId() + ".htm";
				break;
			}
			case 31556:
			{
				if(player.getRam() == 2)
				{
					filename = "npc_friend/" + getNpcId() + "-bufflist.htm";
					break;
				}
				filename = "npc_friend/" + getNpcId() + ".htm";
			}
		}
		showChatWindow(player, filename);
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
		{
			return;
		}
		StringTokenizer st = new StringTokenizer(command, " ");
		String actualCommand = st.nextToken();
		if(actualCommand.equalsIgnoreCase("Buff"))
		{
			if(st.countTokens() < 1)
			{
				return;
			}
			int val = Integer.parseInt(st.nextToken());
			int item = 0;
			switch(getNpcId())
			{
				case 31372:
				{
					item = 7186;
					break;
				}
				case 31379:
				{
					item = 7187;
					break;
				}
				case 31556:
				{
					item = 7251;
				}
			}
			int skill = 0;
			int level = 0;
			long count = 0;
			switch(val)
			{
				case 1:
				{
					skill = 4359;
					level = 2;
					count = 2;
					break;
				}
				case 2:
				{
					skill = 4360;
					level = 2;
					count = 2;
					break;
				}
				case 3:
				{
					skill = 4345;
					level = 3;
					count = 3;
					break;
				}
				case 4:
				{
					skill = 4355;
					level = 2;
					count = 3;
					break;
				}
				case 5:
				{
					skill = 4352;
					level = 1;
					count = 3;
					break;
				}
				case 6:
				{
					skill = 4354;
					level = 3;
					count = 3;
					break;
				}
				case 7:
				{
					skill = 4356;
					level = 1;
					count = 6;
					break;
				}
				case 8:
				{
					skill = 4357;
					level = 2;
					count = 6;
				}
			}
			if(skill != 0 && player.getInventory().destroyItemByItemId(item, count))
			{
				player.doCast(SkillTable.getInstance().getInfo(skill, level), player, true);
			}
			else
			{
				showChatWindow(player, "npc_friend/" + getNpcId() + "-havenotitems.htm");
			}
		}
		else if(command.startsWith("Chat"))
		{
			int val = Integer.parseInt(command.substring(5));
			String fname = "npc_friend/" + getNpcId() + "-" + val + ".htm";
			if(!fname.equals(""))
			{
				showChatWindow(player, fname);
			}
		}
		else if(command.startsWith("Buy"))
		{
			int val = Integer.parseInt(command.substring(4));
			showShopWindow(player, val, false);
		}
		else if(actualCommand.equalsIgnoreCase("Sell"))
		{
			showShopWindow(player);
		}
		else if(command.startsWith("WithdrawP"))
		{
			int val = Integer.parseInt(command.substring(10));
			if(val == 99)
			{
				NpcHtmlMessage html = new NpcHtmlMessage(player, this);
				html.setFile("npc-friend/personal.htm");
				html.replace("%npcname%", getName());
				player.sendPacket(html);
			}
			else
			{
				WarehouseFunctions.showRetrieveWindow(player, val);
			}
		}
		else if(command.equals("DepositP"))
		{
			WarehouseFunctions.showDepositWindow(player);
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
}