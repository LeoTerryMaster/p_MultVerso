package services;

import l2.gameserver.Config;
import l2.gameserver.cache.Msg;
import l2.gameserver.data.StringHolder;
import l2.gameserver.data.xml.holder.ItemHolder;
import l2.gameserver.instancemanager.QuestManager;
import l2.gameserver.model.Player;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2.gameserver.scripts.Functions;
import l2.gameserver.scripts.ScriptFile;
import l2.gameserver.templates.item.ItemTemplate;
import l2.gameserver.utils.ItemFunctions;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

public class QuestSell extends Functions implements ScriptFile
{
	private static final Logger LOG = LoggerFactory.getLogger(QuestSell.class);
	private static final String HTML_BASE = "mods/questsell";
	private static final String BYPASS_PREFIX = "-h scripts_services.QuestSell:";
	private static final int ITEMS_PER_PAGE = 5;
	
	private static Map<Set<Quest>, List<Pair<Integer, Long>>> parseQuestSellList(String qsText)
	{
		return parseQuestSellList(qsText, false);
	}
	
	private static boolean isAllowedNpc(Player player, NpcInstance npc)
	{
		if(player == null || npc == null)
		{
			return false;
		}
		return !player.isActionsDisabled() && Config.ALLOW_TALK_WHILE_SITTING || !player.isSitting() && npc.isInActingRange(player);
	}
	
	private static Map<Set<Quest>, List<Pair<Integer, Long>>> parseQuestSellList(String qsText, boolean inform)
	{
		LinkedHashMap<Set<Quest>, List<Pair<Integer, Long>>> result = new LinkedHashMap<>();
		StringTokenizer questListEntryTextTok = new StringTokenizer(qsText, ";");
		block0:
		while(questListEntryTextTok.hasMoreTokens())
		{
			String questListEntryText = questListEntryTextTok.nextToken().trim();
			if(questListEntryText.isEmpty())
				continue;
			int priceDelimIdx = questListEntryText.indexOf(58);
			if(priceDelimIdx <= 0)
			{
				if(!inform)
					continue;
				LOG.warn("QuestSellService: Can't process quest sell list entry \"" + questListEntryText + "\"");
				continue;
			}
			String questListText = questListEntryText.substring(0, priceDelimIdx).trim();
			StringTokenizer questNameOrIdTextTok = new StringTokenizer(questListText, ",");
			Set<Quest> quests = new LinkedHashSet<>();
			while(questNameOrIdTextTok.hasMoreTokens())
			{
				String questNameOrId = questNameOrIdTextTok.nextToken();
				Quest quest = QuestManager.getQuest2(questNameOrId);
				if(quest == null)
				{
					if(!inform)
						continue block0;
					LOG.warn("QuestSellService: Can't get quest \"" + questNameOrId + "\" in \"" + questListEntryText + "\"");
					continue block0;
				}
				quests.add(quest);
			}
			quests = Collections.unmodifiableSet(quests);
			String questListPricesText = questListEntryText.substring(priceDelimIdx + 1).trim();
			StringTokenizer priceTextTok = new StringTokenizer(questListPricesText, ",");
			ArrayList<Pair<Integer, Long>> price = new ArrayList<>();
			while(priceTextTok.hasMoreTokens())
			{
				String priceText = priceTextTok.nextToken().trim();
				int idAmountDelimIdx = priceText.indexOf(45);
				if(idAmountDelimIdx <= 0)
				{
					if(!inform)
						continue block0;
					LOG.warn("QuestSellService: Can't get price of \"" + questListEntryText + "\"");
					continue block0;
				}
				Integer itemId = Integer.parseInt(priceText.substring(0, idAmountDelimIdx).trim());
				if(ItemHolder.getInstance().getTemplate(itemId.intValue()) == null)
				{
					if(!inform)
						continue block0;
					LOG.warn("QuestSellService: Can't get item \"" + itemId + "\" of \"" + questListEntryText + "\"");
					continue block0;
				}
				Long itemAmount = Long.parseLong(priceText.substring(idAmountDelimIdx + 1).trim());
				price.add(Pair.of(itemId, itemAmount));
			}
			if(result.containsKey(quests))
			{
				if(!inform)
					continue;
				LOG.warn("QuestSellService: Quests already defined \"" + questListText + "\"");
				continue;
			}
			result.put(quests, Collections.unmodifiableList(price));
		}
		return result;
	}
	
	private static List<String> formatQuestList(Player player, Collection<Quest> quests)
	{
		ArrayList<String> result = new ArrayList<>();
		for(Quest quest : quests)
		{
			String questInfo;
			QuestState questState = player.getQuestState(quest);
			if(questState != null)
			{
				switch(questState.getState())
				{
					case 1:
					case 2:
					{
						questInfo = StringHolder.getInstance().getNotNull(player, "scripts.services.QuestSell.questInfoHave");
						break;
					}
					case 3:
					{
						questInfo = StringHolder.getInstance().getNotNull(player, "scripts.services.QuestSell.questInfoDone");
						break;
					}
					default:
					{
						questInfo = StringHolder.getInstance().getNotNull(player, "scripts.services.QuestSell.questInfo");
						break;
					}
				}
			}
			else
			{
				questInfo = StringHolder.getInstance().getNotNull(player, "scripts.services.QuestSell.questInfo");
			}
			questInfo = questInfo.replace("%quest_name%", quest.getDescr(player));
			questInfo = questInfo.replace("%quest_id%", String.valueOf(quest.getQuestIntId()));
			result.add(questInfo);
		}
		return result;
	}
	
	private static String formatRequiredItemPrice(Player player, Pair<Integer, Long> priceItemPair)
	{
		int itemId = priceItemPair.getKey();
		long itemAmount = priceItemPair.getValue();
		ItemTemplate item = ItemHolder.getInstance().getTemplate(itemId);
		String priceInfo = StringHolder.getInstance().getNotNull(player, "scripts.services.QuestSell.requiredPriceItemInfo");
		priceInfo = priceInfo.replace("%item_id%", String.valueOf(item.getItemId()));
		priceInfo = priceInfo.replace("%item_name%", item.getName());
		priceInfo = priceInfo.replace("%item_amount%", String.valueOf(itemAmount));
		return priceInfo;
	}
	
	private static List<String> formatPriceList(Player player, Collection<Pair<Integer, Long>> requiredItemPairs)
	{
		ArrayList<String> result = new ArrayList<>();
		for(Pair<Integer, Long> ip : requiredItemPairs)
		{
			result.add(formatRequiredItemPrice(player, ip));
		}
		return result;
	}
	
	private static String formatQuestSellInfo(Player player, int idx, Pair<Set<Quest>, List<Pair<Integer, Long>>> questSellInfo)
	{
		String questSellInfoText = StringHolder.getInstance().getNotNull(player, "scripts.services.QuestSell.questSellInfo");
		Set quests = questSellInfo.getKey();
		List price = questSellInfo.getValue();
		questSellInfoText = questSellInfoText.replace("%quests_list%", String.join("<br1>", formatQuestList(player, quests)));
		questSellInfoText = questSellInfoText.replace("%price_list%", String.join("<br1>", formatPriceList(player, price)));
		questSellInfoText = questSellInfoText.replace("%bypass%", "-h scripts_services.QuestSell:buyQuestsListByIdx " + idx);
		return questSellInfoText;
	}
	
	private static String pagingHtml(Player player, String prevBypass, int currPage, String nextBypass)
	{
		String html = StringHolder.getInstance().getNotNull(player, "scripts.services.QuestSell.paging");
		html = html.replace("%prev_button%", prevBypass != null ? "<button value=\"&$1037;\" action=\"bypass %prev_bypass%\" width=65 height=20 back=\"l2ui_ch3.smallbutton2_down\" fore=\"l2ui_ch3.smallbutton2\">".replace("%prev_bypass%", prevBypass) : "");
		html = html.replace("%curr_page%", Integer.toString(currPage + 1));
		html = html.replace("%next_button%", nextBypass != null ? "<button value=\"&$1038;\" action=\"bypass %next_bypass%\" width=65 height=20 back=\"l2ui_ch3.smallbutton2_down\" fore=\"l2ui_ch3.smallbutton2\">".replace("%next_bypass%", nextBypass) : "");
		return html;
	}
	
	private static boolean hasNextPage(int length, int page)
	{
		return (page + 1) * 5 < length;
	}
	
	private static String pagingHtml(Player player, int page, Object[] items, String method)
	{
		String bypassFmt = "-h scripts_services.QuestSell:" + method + " %d";
		return pagingHtml(player, page > 0 ? String.format(bypassFmt, page - 1).trim() : null, page, hasNextPage(items.length, page) ? String.format(bypassFmt, page + 1).trim() : null);
	}
	
	private static boolean isMayTakeQuests(Player player, Collection<Quest> quests)
	{
		for(Quest quest : quests)
		{
			QuestState qs;
			if(!quest.isVisible() || (qs = player.getQuestState(quest)) != null && qs.getState() == 3)
				continue;
			return true;
		}
		return false;
	}
	
	private static Pair<Set<Quest>, List<Pair<Integer, Long>>>[] filterAvailableQuests(Player player, Map<Set<Quest>, List<Pair<Integer, Long>>> questSellMap)
	{
		ArrayList<Pair> result = new ArrayList<>();
		for(Map.Entry<Set<Quest>, List<Pair<Integer, Long>>> e : questSellMap.entrySet())
		{
			if(!isMayTakeQuests(player, e.getKey()))
				continue;
			result.add(Pair.of(e.getKey(), e.getValue()));
		}
		return result.toArray(new Pair[result.size()]);
	}
	
	private static Pair<Set<Quest>, List<Pair<Integer, Long>>>[] getAvailableQuests(Player player)
	{
		return filterAvailableQuests(player, parseQuestSellList(Config.QUEST_SELL_QUEST_PRICES));
	}
	
	private static void doListAvailableQuestsForSell(Player player, NpcInstance npc, int page)
	{
		Pair<Set<Quest>, List<Pair<Integer, Long>>>[] questSell = getAvailableQuests(player);
		StringBuilder questsHtmlBuilder = new StringBuilder();
		NpcHtmlMessage html = new NpcHtmlMessage(player, npc);
		html.setFile("mods/questsell/quests_sell_list.htm");
		int qIdx = 5 * page;
		
		for(int qLastIdx = qIdx + 5;qIdx < qLastIdx && qIdx < questSell.length;++qIdx)
		{
			Pair<Set<Quest>, List<Pair<Integer, Long>>> qsItem = questSell[qIdx];
			questsHtmlBuilder.append(formatQuestSellInfo(player, qIdx, qsItem));
		}
		
		html.replace("%list%", questsHtmlBuilder.toString());
		html.replace("%paging%", pagingHtml(player, page, questSell, "listAvailableQuestsForSell"));
		player.sendPacket(html);
	}
	
	private static void buyQuests(Player player, Collection<Quest> quests, Collection<Pair<Integer, Long>> price)
	{
		for(Pair<Integer, Long> requiredItem : price)
		{
			if(ItemFunctions.getItemCount(player, requiredItem.getKey()) >= requiredItem.getValue())
				continue;
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
			return;
		}
		for(Pair<Integer, Long> requiredItem : price)
		{
			if(ItemFunctions.removeItem(player, requiredItem.getKey(), requiredItem.getValue(), true) >= requiredItem.getValue())
				continue;
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
			return;
		}
		for(Quest quest : quests)
		{
			player.setQuestState(quest.newQuestState(player, 3));
		}
	}
	
	public void listAvailableQuestsForSell()
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(!Config.QUEST_SELL_ENABLE || !isAllowedNpc(player, npc))
		{
			player.sendMessage(new CustomMessage("scripts.services.off", player));
			return;
		}
		doListAvailableQuestsForSell(player, npc, 0);
	}
	
	public void listAvailableQuestsForSell(String[] args)
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(!Config.QUEST_SELL_ENABLE || !isAllowedNpc(player, npc))
		{
			player.sendMessage(new CustomMessage("scripts.services.off", player));
			return;
		}
		doListAvailableQuestsForSell(player, npc, Integer.parseInt(args[0]));
	}
	
	public void buyQuestsListByIdx(String[] args)
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(!Config.QUEST_SELL_ENABLE || !isAllowedNpc(player, npc))
		{
			player.sendMessage(new CustomMessage("scripts.services.off", player));
			return;
		}
		Pair<Set<Quest>, List<Pair<Integer, Long>>>[] questSell = getAvailableQuests(player);
		int questsListIdx = Integer.parseInt(args[0]);
		Pair<Set<Quest>, List<Pair<Integer, Long>>> item = questSell[questsListIdx];
		buyQuests(player, item.getLeft(), item.getRight());
		listAvailableQuestsForSell();
	}
	
	@Override
	public void onLoad()
	{
		LOG.info("QuestSellService: Loading ...");
		parseQuestSellList(Config.QUEST_SELL_QUEST_PRICES, true);
	}
	
	@Override
	public void onReload()
	{
	}
	
	@Override
	public void onShutdown()
	{
	}
}