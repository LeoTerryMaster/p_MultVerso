package l2.gameserver.model.instances;

import l2.gameserver.ai.CtrlIntention;
import l2.gameserver.cache.Msg;
import l2.gameserver.data.xml.holder.BuyListHolder;
import l2.gameserver.data.xml.holder.ResidenceHolder;
import l2.gameserver.instancemanager.CastleManorManager;
import l2.gameserver.model.Player;
import l2.gameserver.model.entity.residence.Castle;
import l2.gameserver.model.items.TradeItem;
import l2.gameserver.network.l2.s2c.*;
import l2.gameserver.templates.manor.SeedProduction;
import l2.gameserver.templates.npc.NpcTemplate;

import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

public class ManorManagerInstance extends MerchantInstance
{
	public ManorManagerInstance(int objectId, NpcTemplate template)
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
		}
		else
		{
			MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel() - getLevel());
			player.sendPacket(my);
			if(!isInActingRange(player))
			{
				if(!player.getAI().isIntendingInteract(this))
				{
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
				}
				player.sendActionFailed();
			}
			else
			{
				if(CastleManorManager.getInstance().isDisabled())
				{
					NpcHtmlMessage html = new NpcHtmlMessage(player, this);
					html.setFile("npcdefault.htm");
					html.replace("%objectId%", String.valueOf(getObjectId()));
					html.replace("%npcname%", getName());
					player.sendPacket(html);
				}
				else if(!player.isGM() && player.isClanLeader() && getCastle() != null && getCastle().getOwnerId() == player.getClanId())
				{
					showMessageWindow(player, "manager-lord.htm");
				}
				else
				{
					showMessageWindow(player, "manager.htm");
				}
				player.sendActionFailed();
			}
		}
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
		{
			return;
		}
		if(command.startsWith("manor_menu_select"))
		{
			if(CastleManorManager.getInstance().isUnderMaintenance())
			{
				player.sendPacket(ActionFail.STATIC, Msg.THE_MANOR_SYSTEM_IS_CURRENTLY_UNDER_MAINTENANCE);
				return;
			}
			String params = command.substring(command.indexOf("?") + 1);
			StringTokenizer st = new StringTokenizer(params, "&");
			int ask = Integer.parseInt(st.nextToken().split("=")[1]);
			int state = Integer.parseInt(st.nextToken().split("=")[1]);
			int time = Integer.parseInt(st.nextToken().split("=")[1]);
			Castle castle = getCastle();
			int castleId = state == -1 ? castle.getId() : state;
			switch(ask)
			{
				case 1:
				{
					if(castleId != castle.getId())
					{
						player.sendPacket(Msg._HERE_YOU_CAN_BUY_ONLY_SEEDS_OF_S1_MANOR);
						break;
					}
					BuyListHolder.NpcTradeList tradeList = new BuyListHolder.NpcTradeList(0);
					List<SeedProduction> seeds = castle.getSeedProduction(0);
					for(SeedProduction s : seeds)
					{
						TradeItem item = new TradeItem();
						item.setItemId(s.getId());
						item.setOwnersPrice(s.getPrice());
						item.setCount(s.getCanProduce());
						if(item.getCount() <= 0 || item.getOwnersPrice() <= 0)
							continue;
						tradeList.addItem(item);
					}
					BuyListSeed bl = new BuyListSeed(tradeList, castleId, player.getAdena());
					player.sendPacket(bl);
					break;
				}
				case 2:
				{
					player.sendPacket(new ExShowSellCropList(player, castleId, castle.getCropProcure(0)));
					break;
				}
				case 3:
				{
					if(time == 1 && !ResidenceHolder.getInstance().getResidence(Castle.class, castleId).isNextPeriodApproved())
					{
						player.sendPacket(new ExShowSeedInfo(castleId, Collections.emptyList()));
						break;
					}
					player.sendPacket(new ExShowSeedInfo(castleId, ResidenceHolder.getInstance().getResidence(Castle.class, castleId).getSeedProduction(time)));
					break;
				}
				case 4:
				{
					if(time == 1 && !ResidenceHolder.getInstance().getResidence(Castle.class, castleId).isNextPeriodApproved())
					{
						player.sendPacket(new ExShowCropInfo(castleId, Collections.emptyList()));
						break;
					}
					player.sendPacket(new ExShowCropInfo(castleId, ResidenceHolder.getInstance().getResidence(Castle.class, castleId).getCropProcure(time)));
					break;
				}
				case 5:
				{
					player.sendPacket(new ExShowManorDefaultInfo());
					break;
				}
				case 6:
				{
					showShopWindow(player, Integer.parseInt("3" + getNpcId()), false);
					break;
				}
				case 9:
				{
					player.sendPacket(new ExShowProcureCropDetail(state));
				}
			}
		}
		else if(command.startsWith("help"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken();
			String filename = "manor_client_help00" + st.nextToken() + ".htm";
			showMessageWindow(player, filename);
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
	
	public String getHtmlPath()
	{
		return "manormanager/";
	}
	
	@Override
	public String getHtmlPath(int npcId, int val, Player player)
	{
		return "manormanager/manager.htm";
	}
	
	private void showMessageWindow(Player player, String filename)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(player, this);
		html.setFile(getHtmlPath() + filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcId%", String.valueOf(getNpcId()));
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}
}