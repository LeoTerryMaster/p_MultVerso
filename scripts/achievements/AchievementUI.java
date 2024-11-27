package achievements;

import l2.gameserver.cache.Msg;
import l2.gameserver.data.StringHolder;
import l2.gameserver.data.htm.HtmCache;
import l2.gameserver.model.Player;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.reward.RewardData;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2.gameserver.scripts.Functions;
import l2.gameserver.scripts.ScriptFile;
import l2.gameserver.templates.item.ItemTemplate;
import l2.gameserver.utils.ItemFunctions;
import l2.gameserver.utils.Strings;
import l2.gameserver.utils.Util;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class AchievementUI extends Functions implements ScriptFile
{
	static final String SCRIPT_BYPASS_CLASS = "scripts_" + AchievementUI.class.getName();
	private static final int ACHIEVEMENT_LEVELS_PER_PAGE = 5;
	private static final NumberFormat pf = NumberFormat.getPercentInstance(Locale.ENGLISH);
	
	public void achievements()
	{
		achievements(ArrayUtils.EMPTY_STRING_ARRAY);
	}
	
	public void achievements(String... args)
	{
		Player player = getSelf();
		if(player == null)
		{
			return;
		}
		if(!Achievements.getInstance().isEnabled())
		{
			player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/service_disabled.htm"));
			return;
		}
		String htmlContent = HtmCache.getInstance().getNotNull("scripts/achievements/achievements.htm", player);
		HtmlTabUI categoriesTabsUI = new HtmlTabUI(HtmlTabUI.TabStyle.inventory);
		List<AchievementInfo.AchievementInfoCategory> categories = Achievements.getInstance().getCategories();
		int activeCategoryIdx = Math.min(Math.max(0, args.length > 0 ? Integer.parseInt(args[0]) : 0), categories.size() - 1);
		AchievementInfo.AchievementInfoCategory activeCategory = null;
		for(int categoryIdx = 0;categoryIdx < categories.size();++categoryIdx)
		{
			AchievementInfo.AchievementInfoCategory category = categories.get(categoryIdx);
			if(activeCategoryIdx == categoryIdx)
			{
				activeCategory = category;
				categoriesTabsUI.addTab(category.getTitle(player), String.format("%s:achievements %d", SCRIPT_BYPASS_CLASS, categoryIdx), true);
				continue;
			}
			categoriesTabsUI.addTab(category.getTitle(player), String.format("%s:achievements %d", SCRIPT_BYPASS_CLASS, categoryIdx), false);
		}
		htmlContent = htmlContent.replace("%categories_tabs%", categoriesTabsUI.toHtml());
		if(activeCategory != null)
		{
			htmlContent = htmlContent.replace("%active_category_title%", activeCategory.getTitle(player));
			int activePageIdx = args.length > 1 ? Integer.parseInt(args[1]) : 0;
			int activeAchId = args.length > 2 ? Integer.parseInt(args[2]) : -1;
			int activeAchLvl = args.length > 3 ? Integer.parseInt(args[3]) : -1;
			Paginator<AchievementInfo> paginator = new Paginator<AchievementInfo>(5, activePageIdx)
			{
				
				@Override
				protected String getBypassForPageOrdinal(int pageIdx, int pageOrd)
				{
					return String.format("%s:achievements %d %d", SCRIPT_BYPASS_CLASS, activeCategoryIdx, pageIdx);
				}
			};
			List<AchievementInfo> achievementInfos = Achievements.getInstance().getAchievementInfosByCategory(activeCategory);
			StringBuilder achievementsHtml = new StringBuilder();
			if(achievementInfos != null && !achievementInfos.isEmpty())
			{
				for(AchievementInfo achievementInfo : achievementInfos)
				{
					paginator.addItem(achievementInfo, achievementInfo.getLevels().size());
				}
				List<Pair<AchievementInfo, Pair<Integer, Integer>>> achievementInfoLevelsIdxLimList = paginator.getItems();
				boolean invColor = true;
				for(Pair achievementInfoLevelsIdxLim : achievementInfoLevelsIdxLimList)
				{
					AchievementInfo achievementInfo = (AchievementInfo) achievementInfoLevelsIdxLim.getLeft();
					invColor = !invColor;
					achievementsHtml.append(buildAchievementHtml(activeCategoryIdx, activePageIdx, activeAchId, activeAchLvl, player, achievementInfo, (Pair) achievementInfoLevelsIdxLim.getRight(), invColor));
				}
			}
			htmlContent = htmlContent.replace("%achievements_list%", achievementsHtml.toString());
			htmlContent = htmlContent.replace("%pagination%", paginator.toHtml());
		}
		NpcHtmlMessage htmlPacket = new NpcHtmlMessage(player, null);
		htmlPacket.setHtml(truncateHtmlTagsSpaces(htmlContent));
		player.sendPacket(htmlPacket);
	}
	
	private String buildAchievementHtml(int catIdx, int pageIdx, int activeAchId, int activeAchLvl, Player player, AchievementInfo achievementInfo, Pair<Integer, Integer> levelOffAndLimit, boolean invColor)
	{
		Achievement achievement = new Achievement(achievementInfo, player);
		AchievementInfo.AchievementInfoLevel nextLevel = achievement.getNextLevel();
		String achievementHtml = achievement.isCompleted() ? HtmCache.getInstance().getNotNull("scripts/achievements/entry.completed.htm", player) : HtmCache.getInstance().getNotNull("scripts/achievements/entry.htm", player);
		achievementHtml = achievementHtml.replace("%template_bg_color%", invColor ? " bgcolor=000000" : "");
		achievementHtml = achievementHtml.replace("%achievement_icon%", achievementInfo.getIcon());
		achievementHtml = achievementHtml.replace("%achievement_name%", Strings.bbParse(achievementInfo.getName(player)));
		achievementHtml = achievementHtml.replace("%achievement_current_level%", String.valueOf(achievement.isCompleted() ? achievementInfo.getMaxLevel() : nextLevel.getLevel()));
		achievementHtml = achievementHtml.replace("%achievement_max_level%", String.valueOf(achievementInfo.getMaxLevel()));
		List<AchievementInfo.AchievementInfoLevel> levels = achievementInfo.getLevels();
		StringBuilder levelsListHtml = new StringBuilder();
		int levelIdxLim = levelOffAndLimit.getRight();
		for(int levelIdx = levelOffAndLimit.getLeft().intValue();levelIdx < levelIdxLim;++levelIdx)
		{
			AchievementInfo.AchievementInfoLevel level = levels.get(levelIdx);
			AchFaceLevelDisplayType levelDisplayType = AchFaceLevelDisplayType.DISPLAY_DEFAULT;
			if(level == nextLevel)
			{
				levelDisplayType = AchFaceLevelDisplayType.DISPLAY_PROGRESSING;
			}
			else if(achievement.isCompleted() || nextLevel != null && level.getLevel() < nextLevel.getLevel())
			{
				levelDisplayType = achievement.isLevelRewarded(level) ? AchFaceLevelDisplayType.DISPLAY_REWARDED : AchFaceLevelDisplayType.DISPLAY_COMPLETED;
			}
			levelsListHtml.append(buildAchivementLevelHtml(catIdx, pageIdx, activeAchId, activeAchLvl, player, levelDisplayType, achievement, level));
		}
		achievementHtml = achievementHtml.replace("%achievement_levels_list%", levelsListHtml.toString());
		return achievementHtml.trim();
	}
	
	private String buildAchivementLevelHtml(int catIdx, int pageIdx, int activeAchId, int activeAchLvl, Player player, AchFaceLevelDisplayType displayType, Achievement achievement, AchievementInfo.AchievementInfoLevel level)
	{
		String levelHtml = HtmCache.getInstance().getNotNull("scripts/achievements/" + displayType.templateFileName, player);
		levelHtml = levelHtml.replaceAll("%achievement_id%", String.valueOf(achievement.getAchInfo().getId()));
		levelHtml = levelHtml.replaceAll("%achievement_level_ordinal%", String.valueOf(level.getLevel()));
		levelHtml = levelHtml.replace("%achievement_level_description%", level.getDesc(player).replace("\\n", "<br1>"));
		if(displayType.haveRewardList)
		{
			if(activeAchId == achievement.getAchInfo().getId() && activeAchLvl == level.getLevel())
			{
				String collapseButton = StringHolder.getInstance().getNotNull(player, "achievements.rewardList.collapseButton");
				collapseButton = collapseButton.replace("%collapse_bypass%", String.format("%s:achievements %d %d", SCRIPT_BYPASS_CLASS, catIdx, pageIdx));
				List<RewardData> rewardDatas = level.getRewardDataList();
				levelHtml = levelHtml.replace("%reward_list%", buildAchivementLevelRewardHtml(player, rewardDatas));
				levelHtml = levelHtml.replace("%reward_switch%", collapseButton);
			}
			else
			{
				String expandButton = StringHolder.getInstance().getNotNull(player, "achievements.rewardList.expandButton");
				expandButton = expandButton.replace("%expand_bypass%", String.format("%s:achievements %d %d %d %d", SCRIPT_BYPASS_CLASS, catIdx, pageIdx, achievement.getAchInfo().getId(), level.getLevel()));
				levelHtml = levelHtml.replace("%reward_list%", StringHolder.getInstance().getNotNull(player, "achievements.rewardList.collapsedText"));
				levelHtml = levelHtml.replace("%reward_switch%", expandButton);
			}
		}
		if(displayType.haveProgressBar)
		{
			levelHtml = levelHtml.replace("%achievement_level_progress_bar%", new HtmlProgressBarUI(HtmlProgressBarUI.ProgressBarStyle.flax_light).setBarWidth(64).setFull(level.getValue()).setValue(achievement.getCounter().getVal()).toHtml());
		}
		if(displayType.haveRewardBypass)
		{
			levelHtml = activeAchId >= 0 && activeAchLvl >= 0 ? levelHtml.replace("%achievement_level_reward_bypass%", String.format("%s:achieveReward %d %d %d %d %d %d", SCRIPT_BYPASS_CLASS, achievement.getAchInfo().getId(), level.getLevel(), catIdx, pageIdx, activeAchId, activeAchLvl)) : levelHtml.replace("%achievement_level_reward_bypass%", String.format("%s:achieveReward %d %d %d %d", SCRIPT_BYPASS_CLASS, achievement.getAchInfo().getId(), level.getLevel(), catIdx, pageIdx));
		}
		return levelHtml.trim();
	}
	
	private String buildAchivementLevelRewardHtml(Player player, List<RewardData> rewardDatas)
	{
		StringBuilder rewardListHtml = new StringBuilder();
		for(RewardData rewardData : rewardDatas)
		{
			String rewardHtml = HtmCache.getInstance().getNotNull("scripts/achievements/reward.htm", player);
			ItemTemplate itemTemplate = rewardData.getItem();
			rewardHtml = rewardHtml.replace("%item_icon%", itemTemplate.getIcon());
			rewardHtml = rewardHtml.replace("%item_id%", String.valueOf(itemTemplate.getItemId()));
			rewardHtml = rewardHtml.replace("%item_name%", itemTemplate.getName());
			rewardHtml = rewardHtml.replace("%min_amount%", String.valueOf(rewardData.getMinDrop()));
			rewardHtml = rewardHtml.replace("%max_amount%", String.valueOf(rewardData.getMaxDrop()));
			rewardHtml = rewardHtml.replace("%chance%", pf.format(rewardData.getChance() / 1000000.0));
			rewardListHtml.append(rewardHtml);
		}
		return rewardListHtml.toString().trim();
	}
	
	public void achieveReward(String... args)
	{
		Player player = getSelf();
		if(player == null)
		{
			return;
		}
		NpcInstance npc = getNpc();
		if(!Achievements.getInstance().isEnabled())
		{
			player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/service_disabled.htm"));
			return;
		}
		if(args.length < 4)
		{
			return;
		}
		int achId = Integer.parseInt(args[0]);
		int achLvl = Integer.parseInt(args[1]);
		AchievementInfo achievementInfo = Achievements.getInstance().getAchievementInfoById(achId);
		if(achievementInfo == null)
		{
			return;
		}
		AchievementInfo.AchievementInfoLevel level = achievementInfo.getLevel(achLvl);
		if(level == null)
		{
			return;
		}
		Achievement achievement = new Achievement(achievementInfo, player);
		if(achievement.isRewardableLevel(level))
		{
			List<RewardData> rewardDataList = level.getRewardDataList();
			long weight = 0;
			long slots = 0;
			for(RewardData rewardData : rewardDataList)
			{
				weight += (long) rewardData.getItem().getWeight() * rewardData.getMaxDrop();
				slots += rewardData.getItem().isStackable() ? 1 : rewardData.getMaxDrop();
			}
			if(!player.getInventory().validateWeight(weight))
			{
				player.sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
				return;
			}
			if(!player.getInventory().validateCapacity(slots))
			{
				player.sendPacket(Msg.YOUR_INVENTORY_IS_FULL);
				return;
			}
			achievement.setLevelRewarded(level, true);
			for(RewardData rewardData : rewardDataList)
			{
				long roll = Util.rollDrop(rewardData.getMinDrop(), rewardData.getMaxDrop(), rewardData.getChance(), false);
				if(roll <= 0)
					continue;
				ItemFunctions.addItem(player, rewardData.getItemId(), roll, true);
			}
			player.sendMessage(new CustomMessage("achievements.rewardedS1LevelS2", player, achievementInfo.getName(player), level.getLevel()));
		}
		achievements(args[2], args[3]);
	}
	
	public void adminReload()
	{
		Player self = getSelf();
		if(self == null)
		{
			return;
		}
		if(!self.isGM())
		{
			return;
		}
		Achievements.getInstance().parse();
	}
	
	@Override
	public void onLoad()
	{
	}
	
	@Override
	public void onReload()
	{
	}
	
	@Override
	public void onShutdown()
	{
	}
	
	private enum AchFaceLevelDisplayType
	{
		DISPLAY_DEFAULT("level.htm", false, false, true),
		DISPLAY_PROGRESSING("level.progressing.htm", true, false, true),
		DISPLAY_COMPLETED("level.completed.htm", false, true, true),
		DISPLAY_REWARDED("level.rewarded.htm", false, false, false);
		
		final String templateFileName;
		final boolean haveProgressBar;
		final boolean haveRewardBypass;
		final boolean haveRewardList;
		
		AchFaceLevelDisplayType(String templateFileName, boolean haveProgressBar, boolean haveRewardBypass, boolean haveRewardList)
		{
			this.templateFileName = templateFileName;
			this.haveProgressBar = haveProgressBar;
			this.haveRewardBypass = haveRewardBypass;
			this.haveRewardList = haveRewardList;
		}
	}
	
	public abstract static class Paginator<ItemType>
	{
		private final int pageSize;
		private final List<Pair<ItemType, Integer>> items = new LinkedList<>();
		private int pageIdx;
		
		public Paginator(int pageSize, int pageIdx)
		{
			this.pageSize = pageSize;
			this.pageIdx = pageIdx;
		}
		
		public Paginator(int pageLength)
		{
			this(pageLength, 0);
		}
		
		public int getPageIdx()
		{
			return pageIdx;
		}
		
		public Paginator<ItemType> setPageIdx(int pageIdx)
		{
			this.pageIdx = pageIdx;
			return this;
		}
		
		public Paginator<ItemType> addItem(ItemType item, int length)
		{
			items.add((Pair) Pair.of(item, (Object) length));
			return this;
		}
		
		private int itemsLength()
		{
			int lengthSum = 0;
			for(Pair<ItemType, Integer> itemAndLength : items)
			{
				lengthSum += itemAndLength.getRight().intValue();
			}
			return lengthSum;
		}
		
		public List<Pair<ItemType, Pair<Integer, Integer>>> getItems(int pageNum)
		{
			int begin = pageNum * pageSize;
			int end = begin + pageSize;
			int offset = 0;
			LinkedList<Pair<ItemType, Pair<Integer, Integer>>> result = new LinkedList<>();
			for(Pair<ItemType, Integer> itemAndLength : items)
			{
				int length = itemAndLength.getRight();
				if(offset < end && offset + length > begin)
				{
					result.add((Pair) Pair.of((Object) itemAndLength.getLeft(), (Object) Pair.of((Object) Math.max(begin - offset, 0), (Object) Math.min(length, end - offset))));
				}
				offset += length;
			}
			return result;
		}
		
		public List<Pair<ItemType, Pair<Integer, Integer>>> getItems()
		{
			return getItems(pageIdx);
		}
		
		protected abstract String getBypassForPageOrdinal(int pageIdx, int pageOrd);
		
		public String toHtml()
		{
			int itemsLength = itemsLength();
			StringBuilder paginationHtml = new StringBuilder();
			paginationHtml.append("<table border=0 cellspacing=0 cellpadding=0><tr>");
			int pages = (itemsLength + pageSize - 1) / pageSize;
			for(int pageIdx = 0;pageIdx < pages;++pageIdx)
			{
				int pageOrd = pageIdx + 1;
				paginationHtml.append("<td>");
				if(pageIdx == this.pageIdx)
				{
					paginationHtml.append(pageOrd);
				}
				else
				{
					paginationHtml.append("<a action=\"bypass -h ").append(getBypassForPageOrdinal(pageIdx, pageOrd)).append("\">").append(pageOrd).append("</a>");
				}
				paginationHtml.append("</td>");
			}
			paginationHtml.append("</tr></table>");
			return paginationHtml.toString();
		}
	}
	
	public static class HtmlProgressBarUI
	{
		private final ProgressBarStyle style;
		private int barWidth;
		private int percent = -1;
		private int full = -1;
		private int value = -1;
		
		public HtmlProgressBarUI(ProgressBarStyle style)
		{
			this.style = style;
			barWidth = style.maxWidth >= 0 ? style.maxWidth : 100;
		}
		
		public int getBarWidth()
		{
			return barWidth;
		}
		
		public HtmlProgressBarUI setBarWidth(int barWidth)
		{
			this.barWidth = barWidth;
			return this;
		}
		
		public int getValue()
		{
			return value;
		}
		
		public HtmlProgressBarUI setValue(int value)
		{
			this.value = value;
			if(value > full)
			{
				full = value;
			}
			return this;
		}
		
		public int getFull()
		{
			return full;
		}
		
		public HtmlProgressBarUI setFull(int full)
		{
			this.full = full;
			return this;
		}
		
		public int getPercent()
		{
			return percent;
		}
		
		public void setPercent(int percent)
		{
			this.percent = percent;
		}
		
		public String toHtml()
		{
			StringBuilder html = new StringBuilder();
			int width = style.maxWidth >= 0 ? Math.min(barWidth, style.maxWidth) : barWidth;
			html.append("<table width=").append(width).append(" border=0 cellspacing=0 cellpadding=0><tr>");
			if(value >= 0 && full >= 0)
			{
				if(value < full)
				{
					int valWidth = (int) ((float) width / (float) full * (float) value);
					html.append("<td><img src=\"").append(style.valueTexture).append("\" width=").append(valWidth).append(" height=").append(style.height).append("></td>");
					html.append("<td><img src=\"").append(style.backTexture).append("\" width=").append(width - valWidth).append(" height=").append(style.height).append("></td>");
				}
				else
				{
					html.append("<td><img src=\"").append(full == 0 ? style.backTexture : style.valueTexture).append("\" width=").append(width).append(" height=").append(style.height).append("></td>");
				}
			}
			else if(percent >= 0)
			{
				int valWidth = (int) ((float) percent / 100.0f * (float) value);
				if(percent < 100)
				{
					html.append("<td><img src=\"").append(style.valueTexture).append("\" width=").append(valWidth).append(" height=").append(style.height).append("></td>");
					html.append("<td><img src=\"").append(style.backTexture).append("\" width=").append(width - valWidth).append(" height=").append(style.height).append("></td>");
				}
				else
				{
					html.append("<td><img src=\"").append(percent == 0 ? style.backTexture : style.valueTexture).append("\" width=").append(width).append(" height=").append(style.height).append("></td>");
				}
			}
			html.append("</tr></table>");
			return html.toString();
		}
		
		public enum ProgressBarStyle
		{
			classic_red(3, 96, "sek.cbui62", "sek.cbui64"),
			classic_blue(3, 96, "sek.cbui63", "sek.cbui64"),
			yellow(12, -1, "L2UI_CH3.br_bar1_cp", "L2UI_CH3.br_bar1back_cp"),
			flax(12, -1, "L2UI_CH3.br_bar1_cp1", "L2UI_CH3.br_bar1back_cp"),
			flax_light(8, -1, "L2UI_CH3.br_bar1_cp1", "L2UI_CH3.br_bar1back_cp");
			
			final int height;
			final int maxWidth;
			final String valueTexture;
			final String backTexture;
			
			ProgressBarStyle(int height, int maxWidth, String valueTexture, String backTexture)
			{
				this.height = height;
				this.maxWidth = maxWidth;
				this.valueTexture = valueTexture;
				this.backTexture = backTexture;
			}
		}
	}
	
	public static class HtmlTabUI
	{
		private final TabStyle style;
		private final List<TabRecord> records = new ArrayList<>();
		private TabRecord active;
		private int tabsPerRow = -1;
		
		public HtmlTabUI(TabStyle style)
		{
			this(style, 296 / style.width);
		}
		
		public HtmlTabUI(TabStyle style, int tabsPerRow)
		{
			this.style = style;
			this.tabsPerRow = tabsPerRow;
		}
		
		public TabRecord addTab(String title, String bypass, boolean active)
		{
			TabRecord tabRecord = new TabRecord(title, bypass);
			if(active)
			{
				setActive(tabRecord);
			}
			records.add(tabRecord);
			return tabRecord;
		}
		
		public TabRecord addTab(String title, String bypass)
		{
			return addTab(title, bypass, false);
		}
		
		public void setActive(TabRecord active)
		{
			this.active = active;
		}
		
		public void setTabsPerRow(int tabsPerRow)
		{
			this.tabsPerRow = tabsPerRow;
		}
		
		public String toHtml()
		{
			StringBuilder html = new StringBuilder();
			html.append("<table width=").append(tabsPerRow * style.width).append(" border=0 cellspacing=0 cellpadding=0>");
			int row = 0;
			while(row * tabsPerRow < records.size())
			{
				html.append("<tr>");
				for(int col = 0;col < tabsPerRow;++col)
				{
					html.append("<td width=").append(style.width).append(">");
					int idx = col + row * tabsPerRow;
					if(idx < records.size())
					{
						TabRecord tabRecord = records.get(idx);
						html.append("<button").append(" width=").append(style.width).append(" height=").append(style.height);
						if(tabRecord.title != null)
						{
							html.append(" value=\"").append(tabRecord.title).append("\"");
						}
						if(tabRecord.bypass != null)
						{
							html.append(" action=\"bypass -h ").append(tabRecord.bypass).append("\"");
						}
						if(tabRecord == active)
						{
							html.append(" fore=").append(style.active).append(" back=").append(style.active);
						}
						else
						{
							html.append(" fore=").append(style.fore).append(" back=").append(style.back);
						}
						html.append(">");
					}
					else
					{
						html.append("&nbsp;");
					}
					html.append("</td>");
				}
				html.append("</tr>");
				++row;
			}
			html.append("</table>");
			return html.toString();
		}
		
		public enum TabStyle
		{
			board(74, 22, "L2UI_CH3.board_tab1", "L2UI_CH3.board_tab2", "L2UI_CH3.board_tab2"),
			chatting(true, 64, 22, "L2UI_CH3.chatting_tab1", "L2UI_CH3.chatting_tab2", "L2UI_CH3.chatting_tab2"),
			inventory(94, 22, "L2UI_CH3.inventory_tab1", "L2UI_CH3.inventory_tab2", "L2UI_CH3.inventory_tab2"),
			msn(114, 22, "L2UI_CH3.msn_tab1", "L2UI_CH3.msn_tab2", "L2UI_CH3.msn_tab2"),
			normal(74, 22, "L2UI_CH3.normal_tab", "L2UI_CH3.normal_tab_on", "L2UI_CH3.normal_tab_on");
			
			final boolean isButtom;
			final int width;
			final int height;
			final String active;
			final String fore;
			final String back;
			
			TabStyle(boolean isButtom, int width, int height, String active, String fore, String back)
			{
				this.isButtom = isButtom;
				this.width = width;
				this.height = height;
				this.active = active;
				this.fore = fore;
				this.back = back;
			}
			
			TabStyle(int width, int height, String active, String fore, String back)
			{
				this(false, width, height, active, fore, back);
			}
		}
		
		public class TabRecord
		{
			final String title;
			final String bypass;
			
			private TabRecord(String title, String bypass)
			{
				this.title = title;
				this.bypass = bypass;
			}
		}
	}
}