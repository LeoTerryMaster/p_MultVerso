package actions;

import l2.gameserver.Config;
import l2.gameserver.cache.Msg;
import l2.gameserver.model.Player;
import l2.gameserver.model.base.Experience;
import l2.gameserver.model.instances.ChestInstance;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.instances.RaidBossInstance;
import l2.gameserver.model.reward.RewardData;
import l2.gameserver.model.reward.RewardGroup;
import l2.gameserver.model.reward.RewardList;
import l2.gameserver.model.reward.RewardType;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.ActionFail;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2.gameserver.scripts.Functions;
import l2.gameserver.stats.Stats;
import l2.gameserver.utils.HtmlUtils;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RewardListInfo extends Functions
{
	private static final int ITEMS_PER_PAGE = 15;
	private static final RewardType[] ITEMS_REWARD_ORDER = {RewardType.RATED_GROUPED, RewardType.SWEEP, RewardType.NOT_RATED_GROUPED, RewardType.NOT_RATED_NOT_GROUPED};
	private static final NumberFormat pf = NumberFormat.getPercentInstance(Locale.ENGLISH);
	private static final NumberFormat df = NumberFormat.getInstance(Locale.ENGLISH);
	
	static
	{
		pf.setMaximumFractionDigits(4);
		df.setMinimumFractionDigits(2);
	}
	
	private static boolean canBypassCheck(Player player, NpcInstance npc)
	{
		if(npc == null)
		{
			player.sendPacket(Msg.THAT_IS_THE_INCORRECT_TARGET);
			player.sendActionFailed();
			return false;
		}
		if(!Config.ALLOW_TALK_WHILE_SITTING && player.isSitting())
		{
			player.sendPacket(Msg.YOU_CANNOT_MOVE_WHILE_SITTING);
			player.sendActionFailed();
			return false;
		}
		if(!npc.isInRange(player, 2500))
		{
			player.sendPacket(SystemMsg.POSITIONING_CANNOT_BE_DONE_HERE_BECAUSE_THE_DISTANCE_BETWEEN_MERCENARIES_IS_TOO_SHORT, ActionFail.STATIC);
			player.sendActionFailed();
			return false;
		}
		return true;
	}
	
	public static void showRewardHtml(Player player, NpcInstance npc)
	{
		showRewardHtml(player, npc, 0);
	}
	
	public static void showRewardHtml(Player player, NpcInstance npc, int pageNum)
	{
		if(!canBypassCheck(player, npc))
		{
			return;
		}
		int diff = npc.calculateLevelDiffForDrop(player.isInParty() ? player.getParty().getLevel() : player.getLevel());
		double mod = npc.calcStat(Stats.ITEM_REWARD_MULTIPLIER, 1.0, player, null);
		NpcHtmlMessage htmlMessage = new NpcHtmlMessage(player, npc);
		htmlMessage.replace("%npc_name%", HtmlUtils.htmlNpcName(npc.getNpcId()));
		if((mod *= Experience.penaltyModifier((long) diff, 9.0)) <= 0.0)
		{
			htmlMessage.setFile("actions/rewardlist_to_weak.htm");
			player.sendPacket(htmlMessage);
			return;
		}
		if(npc instanceof ChestInstance)
		{
			player.sendMessage("You can't view drop in the chest");
			player.sendActionFailed();
			return;
		}
		if(npc.getTemplate().getRewards().isEmpty())
		{
			htmlMessage.setFile("actions/rewardlist_empty.htm");
			player.sendPacket(htmlMessage);
			return;
		}
		htmlMessage.setFile("actions/rewardlist_info.htm");
		Map rewards = npc.getTemplate().getRewards();
		ArrayList<String> tmp = new ArrayList<>();
		block6:
		for(RewardType rewardType : ITEMS_REWARD_ORDER)
		{
			RewardList rewardList = (RewardList) rewards.get(rewardType);
			if(rewardList == null || rewardList.isEmpty())
				continue;
			switch(rewardType)
			{
				case RATED_GROUPED:
				{
					tmp.add("<font color=\"aaccff\">RATED GROUP:</font>");
					ratedGroupedRewardList(tmp, npc, rewardList, player, mod);
					continue block6;
				}
				case NOT_RATED_GROUPED:
				{
					tmp.add("<font color=\"aaccff\">NOT RATED GROUP:</font>");
					notRatedGroupedRewardList(tmp, rewardList, mod);
					continue block6;
				}
				case NOT_RATED_NOT_GROUPED:
				{
					tmp.add("<font color=\"aaccff\">NOT RATED GROUP NOT GROUPED:</font>");
					notGroupedRewardList(tmp, rewardList, 1.0, mod);
					continue block6;
				}
				case SWEEP:
				{
					tmp.add("<font color=\"aaccff\">SWEEP:</font>");
					notGroupedRewardList(tmp, rewardList, Config.RATE_DROP_SPOIL * player.getRateSpoil(), mod);
				}
			}
		}
		StringBuilder builder = new StringBuilder();
		int pages = tmp.size() / 15;
		pageNum = Math.min(pageNum, pages);
		int firstIdx = pageNum * 15;
		int lastIdx = Math.max(0, Math.min((pageNum + 1) * 15 - 1, tmp.size() - 1));
		for(int idx = firstIdx;idx <= lastIdx;++idx)
		{
			builder.append(tmp.get(idx));
		}
		htmlMessage.replace("%info%", builder.toString());
		builder.setLength(0);
		builder.append("<table><tr>");
		for(int p = 0;p <= pages;++p)
		{
			builder.append("<td>");
			if(p == pageNum)
			{
				builder.append(p + 1);
			}
			else
			{
				builder.append("<a action=\"bypass -h scripts_actions.RewardListInfo:showReward ").append(p).append("\">").append(p + 1).append("</a>");
			}
			builder.append("</td>");
		}
		builder.append("</tr></table>");
		htmlMessage.replace("%paging%", builder.toString());
		player.sendPacket(htmlMessage);
	}
	
	public static void ratedGroupedRewardList(List<String> tmp, NpcInstance npc, RewardList list, Player player, double mod)
	{
		for(RewardGroup g : list)
		{
			double grate;
			List<RewardData> items = g.getItems();
			double gchance = g.getChance();
			double gmod = mod;
			double rateDrop = npc instanceof RaidBossInstance ? Config.RATE_DROP_RAIDBOSS * (double) player.getBonus().getDropRaidItems() : npc.isSiegeGuard() ? Config.RATE_DROP_SIEGE_GUARD : Config.RATE_DROP_ITEMS * player.getRateItems();
			double rateAdena = Config.RATE_DROP_ADENA * player.getRateAdena();
			double rateSealStone = Config.RATE_DROP_SEAL_STONES * player.getRateItems();
			if(g.isAdena())
			{
				if(rateAdena == 0.0)
					continue;
				grate = rateAdena;
				if(gmod > 10.0)
				{
					gmod *= g.getChance() / 1000000.0;
				}
				grate *= gmod;
			}
			else if(g.isSealStone())
			{
				if(rateSealStone == 0.0)
					continue;
				grate = rateSealStone;
				grate = g.notRate() ? Math.min(gmod, 1.0) : grate * gmod;
			}
			else
			{
				if(rateDrop == 0.0)
					continue;
				grate = rateDrop;
				grate = g.notRate() ? Math.min(gmod, 1.0) : grate * gmod;
			}
			double gmult = Math.ceil(grate);
			tmp.add(formatRewardGroupHtml(g));
			for(RewardData d : items)
			{
				tmp.add(formatRewardDataHtml(d, gmult));
			}
		}
	}
	
	public static void notRatedGroupedRewardList(List<String> tmp, RewardList list, double mod)
	{
		for(RewardGroup g : list)
		{
			List<RewardData> items = g.getItems();
			tmp.add(formatRewardGroupHtml(g));
			for(RewardData d : items)
			{
				tmp.add(formatRewardDataHtml(d, 1.0));
			}
		}
	}
	
	public static void notGroupedRewardList(List<String> tmp, RewardList list, double rate, double mod)
	{
		for(RewardGroup g : list)
		{
			List<RewardData> items = g.getItems();
			if(rate == 0.0)
				continue;
			double grate = rate;
			double gmod = mod;
			grate = g.notRate() ? Math.min(gmod, 1.0) : grate * gmod;
			double gmult = Math.ceil(grate);
			for(RewardData d : items)
			{
				tmp.add(formatRewardDataHtml(d, gmult));
			}
		}
	}
	
	private static String formatRewardGroupHtml(RewardGroup g)
	{
		return String.format("<table width=270 border=0 bgcolor=333333><tr><td width=170><font color=\"a2a0a2\">Group Chance:</font><font color=\"b09979\">%s</font></td><td width=100 align=right></td></tr></table>", pf.format(g.getChance() / 1000000.0));
	}
	
	private static String formatRewardDataHtml(RewardData d, double gmult)
	{
		String icon = d.getItem().getIcon();
		if(icon == null || icon.equals(""))
		{
			icon = "icon.etc_question_mark_i00";
		}
		Object[] arrobject = new Object[5];
		arrobject[0] = icon;
		arrobject[1] = HtmlUtils.htmlItemName(d.getItemId());
		arrobject[2] = d.getMinDrop();
		arrobject[3] = Math.round((double) d.getMaxDrop() * (d.notRate() ? 1.0 : gmult));
		arrobject[4] = pf.format(d.getChance() / 1000000.0);
		return String.format("<table width=270 border=0><tr><td width=32><img src=%s width=32 height=32></td><td width=238>%s<br1><font color=\"b09979\">[%d..%d]&nbsp;%s</font></td></tr></table>", arrobject);
	}
	
	public void showReward(String[] param)
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(player == null || npc == null)
		{
			return;
		}
		int pageNum = 0;
		if(param.length > 0)
		{
			try
			{
				pageNum = Integer.parseInt(param[0]);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		showRewardHtml(player, npc, pageNum);
	}
}