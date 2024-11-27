package l2.gameserver.model.instances;

import l2.gameserver.Config;
import l2.gameserver.cache.Msg;
import l2.gameserver.data.htm.HtmCache;
import l2.gameserver.data.xml.holder.BuyListHolder;
import l2.gameserver.data.xml.holder.MultiSellHolder;
import l2.gameserver.data.xml.holder.ResidenceHolder;
import l2.gameserver.instancemanager.MapRegionManager;
import l2.gameserver.instancemanager.ReflectionManager;
import l2.gameserver.model.Player;
import l2.gameserver.model.entity.residence.Castle;
import l2.gameserver.network.l2.s2c.BuyList;
import l2.gameserver.network.l2.s2c.ExGetPremiumItemList;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2.gameserver.network.l2.s2c.SellRefundList;
import l2.gameserver.network.l2.s2c.ShopPreviewList;
import l2.gameserver.templates.mapregion.DomainArea;
import l2.gameserver.templates.npc.NpcTemplate;
import l2.gameserver.utils.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.StringTokenizer;

public class MerchantInstance extends NpcInstance
{
	private static final Logger _log = LoggerFactory.getLogger(MerchantInstance.class);
	private static final int NEWBIE_EXCHANGE_MULTISELL = 6001;
	
	public MerchantInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public String getHtmlPath(int npcId, int val, Player player)
	{
		String pom = val == 0 ? "" + npcId : "" + npcId + "-" + val;
		if(getTemplate().getHtmRoot() != null)
		{
			return getTemplate().getHtmRoot() + pom + ".htm";
		}
		String temp = "merchant/" + pom + ".htm";
		if(HtmCache.getInstance().getNullable(temp, player) != null)
		{
			return temp;
		}
		temp = "teleporter/" + pom + ".htm";
		if(HtmCache.getInstance().getNullable(temp, player) != null)
		{
			return temp;
		}
		temp = "petmanager/" + pom + ".htm";
		if(HtmCache.getInstance().getNullable(temp, player) != null)
		{
			return temp;
		}
		return "default/" + pom + ".htm";
	}
	
	private void showWearWindow(Player player, int val)
	{
		if(!player.getPlayerAccess().UseShop)
		{
			return;
		}
		BuyListHolder.NpcTradeList list = BuyListHolder.getInstance().getBuyList(val);
		if(list != null)
		{
			ShopPreviewList bl = new ShopPreviewList(list, player);
			player.sendPacket(bl);
		}
		else
		{
			_log.warn("no buylist with id:" + val);
			player.sendActionFailed();
		}
	}
	
	protected void showShopWindow(Player player, int listId, boolean tax)
	{
		if(!player.getPlayerAccess().UseShop)
		{
			return;
		}
		double taxRate = 0.0;
		Castle castle;
		if(tax && (castle = getCastle(player)) != null)
		{
			taxRate = castle.getTaxRate();
		}
		BuyListHolder.NpcTradeList list;
		if((list = BuyListHolder.getInstance().getBuyList(listId)) == null || list.getNpcId() == getNpcId())
		{
			player.sendPacket(new BuyList(list, player, taxRate));
		}
		else
		{
			_log.warn("[L2MerchantInstance] possible client hacker: " + player.getName() + " attempting to buy from GM shop! < Ban him!");
			_log.warn("buylist id:" + listId + " / list_npc = " + list.getNpcId() + " / npc = " + getNpcId());
		}
	}
	
	protected void showShopWindow(Player player)
	{
		showShopWindow(player, 0, false);
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
		if(actualCommand.equalsIgnoreCase("Buy"))
		{
			int val = 0;
			if(st.countTokens() > 0)
			{
				val = Integer.parseInt(st.nextToken());
			}
			showShopWindow(player, val, true);
		}
		else if(actualCommand.equalsIgnoreCase("Sell"))
		{
			player.sendPacket(new SellRefundList(player, false));
		}
		else if(actualCommand.equalsIgnoreCase("Wear"))
		{
			if(st.countTokens() < 1)
			{
				return;
			}
			int val = Integer.parseInt(st.nextToken());
			showWearWindow(player, val);
		}
		else if(actualCommand.equalsIgnoreCase("Multisell"))
		{
			if(st.countTokens() < 1)
			{
				return;
			}
			int val = Integer.parseInt(st.nextToken());
			Castle castle;
			MultiSellHolder.getInstance().SeparateAndSend(val, player, (castle = getCastle(player)) != null ? castle.getTaxRate() : 0.0);
		}
		else if(actualCommand.equalsIgnoreCase("Exchange"))
		{
			if(player.getLevel() < 25)
			{
				MultiSellHolder.getInstance().SeparateAndSend(6001, player, 0.0);
			}
			else
			{
				player.sendPacket(new NpcHtmlMessage(player, this, "merchant/merchant_for_newbie001.htm", 0));
			}
		}
		else if(actualCommand.equalsIgnoreCase("ReceivePremium"))
		{
			if(player.getPremiumItemList().isEmpty())
			{
				player.sendPacket(Msg.THERE_ARE_NO_MORE_VITAMIN_ITEMS_TO_BE_FOUND);
				return;
			}
			player.sendPacket(new ExGetPremiumItemList(player));
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
	
	@Override
	public Castle getCastle(Player player)
	{
		if(Config.SERVICES_OFFSHORE_NO_CASTLE_TAX || getReflection() == ReflectionManager.GIRAN_HARBOR && Config.SERVICES_GIRAN_HARBOR_NOTAX)
		{
			return null;
		}
		if(getReflection() == ReflectionManager.GIRAN_HARBOR)
		{
			String var = player.getVar("backCoords");
			if(var != null && !var.isEmpty())
			{
				Location loc = Location.parseLoc(var);
				DomainArea domain = MapRegionManager.getInstance().getRegionData(DomainArea.class, loc);
				if(domain != null)
				{
					return ResidenceHolder.getInstance().getResidence(Castle.class, domain.getId());
				}
			}
			return getCastle();
		}
		return super.getCastle(player);
	}
	
	@Override
	public boolean isMerchantNpc()
	{
		return true;
	}
}