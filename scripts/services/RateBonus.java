package services;

import l2.gameserver.Config;
import l2.gameserver.cache.Msg;
import l2.gameserver.dao.AccountBonusDAO;
import l2.gameserver.data.StringHolder;
import l2.gameserver.data.htm.HtmCache;
import l2.gameserver.data.xml.holder.ItemHolder;
import l2.gameserver.model.Player;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.scripts.Functions;
import l2.gameserver.utils.ItemFunctions;
import l2.gameserver.utils.Log;
import l2.gameserver.utils.TimeUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Locale;

public class RateBonus extends Functions
{
	public void list()
	{
		Player player = getSelf();
		if(!Config.SERVICES_RATE_ENABLED)
		{
			show(HtmCache.getInstance().getNotNull("npcdefault.htm", player), player);
			return;
		}
		String html;
		long expireTime = player.getBonus().getBonusExpire();
		if(expireTime > System.currentTimeMillis() / 1000)
		{
			html = HtmCache.getInstance().getNotNull("scripts/services/RateBonusAlready.htm", player).replaceFirst("endtime", TimeUtils.toSimpleFormat(expireTime * 1000));
		}
		else
		{
			html = HtmCache.getInstance().getNotNull("scripts/services/RateBonus.htm", player);
			StringBuilder sb = new StringBuilder();
			for(Config.RateBonusInfo rateBonusInfo : Config.SERVICES_RATE_BONUS_INFO)
			{
				String rbHtml = StringHolder.getInstance().getNotNull(player, "scripts.services.RateBonus.BonusHtml");
				rbHtml = rbHtml.replace("%bonus_idx%", String.valueOf(rateBonusInfo.id));
				rbHtml = rbHtml.replace("%exp_rate%", String.format(Locale.US, "%.1f", Float.valueOf(rateBonusInfo.rateXp)));
				rbHtml = rbHtml.replace("%sp_rate%", String.format(Locale.US, "%.1f", Float.valueOf(rateBonusInfo.rateSp)));
				rbHtml = rbHtml.replace("%quest_rate%", String.format(Locale.US, "%.1f", Float.valueOf(rateBonusInfo.questRewardRate)));
				rbHtml = rbHtml.replace("%quest_drop_rate%", String.format(Locale.US, "%.1f", Float.valueOf(rateBonusInfo.questDropRate)));
				rbHtml = rbHtml.replace("%adena_drop_rate%", String.format(Locale.US, "%.1f", Float.valueOf(rateBonusInfo.dropAdena)));
				rbHtml = rbHtml.replace("%items_drop_rate%", String.format(Locale.US, "%.1f", Float.valueOf(rateBonusInfo.dropItems)));
				rbHtml = rbHtml.replace("%spoil_drop_rate%", String.format(Locale.US, "%.1f", Float.valueOf(rateBonusInfo.dropSpoil)));
				rbHtml = rbHtml.replace("%enchant_item_bonus%", String.valueOf((int) (100.0f * (rateBonusInfo.enchantItemMul - 1.0f))));
				rbHtml = rbHtml.replace("%period_days%", String.valueOf(rateBonusInfo.bonusTimeSeconds / 86400));
				rbHtml = rbHtml.replace("%price%", String.valueOf(rateBonusInfo.consumeItemAmount));
				rbHtml = rbHtml.replace("%price_item_id%", String.valueOf(rateBonusInfo.consumeItemId));
				rbHtml = rbHtml.replace("%price_item_name%", ItemHolder.getInstance().getTemplate(rateBonusInfo.consumeItemId).getName());
				sb.append(rbHtml);
			}
			html = html.replaceFirst("%toreplace%", sb.toString());
		}
		show(html, player);
	}
	
	public void get(String[] param)
	{
		Player player = getSelf();
		if(!Config.SERVICES_RATE_ENABLED)
		{
			show(HtmCache.getInstance().getNotNull("npcdefault.htm", player), player);
			return;
		}
		int id = Integer.parseInt(param[0]);
		Config.RateBonusInfo rateBonusInfo = null;
		for(Config.RateBonusInfo rbi : Config.SERVICES_RATE_BONUS_INFO)
		{
			if(rbi.id != id)
				continue;
			rateBonusInfo = rbi;
		}
		if(rateBonusInfo == null)
		{
			return;
		}
		if(!player.getInventory().destroyItemByItemId(rateBonusInfo.consumeItemId, rateBonusInfo.consumeItemAmount))
		{
			if(rateBonusInfo.consumeItemId == 57)
			{
				player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			}
			else
			{
				player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
			}
			return;
		}
		Log.add(player.getName() + "|" + player.getObjectId() + "|rate bonus|" + rateBonusInfo.id + "|" + rateBonusInfo.bonusTimeSeconds + "|", "services");
		AccountBonusDAO.getInstance().store(player.getAccountName(), rateBonusInfo.makeBonus());
		player.stopBonusTask();
		player.startBonusTask();
		for(Pair rewardPair : rateBonusInfo.rewardItem)
		{
			ItemFunctions.addItem(player, (Integer) rewardPair.getLeft(), (Long) rewardPair.getRight(), true);
		}
		if(rateBonusInfo.nameColor != null)
		{
			player.setNameColor(rateBonusInfo.nameColor.intValue());
		}
		if(player.getParty() != null)
		{
			player.getParty().recalculatePartyData();
		}
		player.broadcastUserInfo(true);
		show(HtmCache.getInstance().getNotNull("scripts/services/RateBonusGet.htm", player), player);
	}
}