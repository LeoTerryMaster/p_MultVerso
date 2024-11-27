package services.community.custom;

import l2.commons.lang.ArrayUtils;
import l2.gameserver.Config;
import l2.gameserver.cache.Msg;
import l2.gameserver.data.htm.HtmCache;
import l2.gameserver.data.xml.holder.ItemHolder;
import l2.gameserver.handler.bbs.CommunityBoardManager;
import l2.gameserver.handler.bbs.ICommunityBoardHandler;
import l2.gameserver.model.Player;
import l2.gameserver.model.base.ClassId;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.ExPCCafePointInfo;
import l2.gameserver.network.l2.s2c.ShowBoard;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.scripts.Functions;
import l2.gameserver.scripts.ScriptFile;
import l2.gameserver.utils.HtmlUtils;
import l2.gameserver.utils.Log;
import l2.gameserver.utils.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class CommunityQuests implements ScriptFile, ICommunityBoardHandler
{
	private static final int[] class_levels = {20, 40, 76};
	private static final Map<Integer, String> CLASSES_EN = new HashMap<>();
	private static final Map<Integer, String> CLASSES_RU = new HashMap<>();
	
	static
	{
		CLASSES_RU.put(0, "Воин");
		CLASSES_RU.put(1, "Воитель");
		CLASSES_RU.put(2, "Гладиатор");
		CLASSES_RU.put(3, "Копейщик");
		CLASSES_RU.put(4, "Рыцарь");
		CLASSES_RU.put(5, "Паладин");
		CLASSES_RU.put(6, "Мститель");
		CLASSES_RU.put(7, "Разбойник");
		CLASSES_RU.put(8, "Искатель Сокровищ");
		CLASSES_RU.put(9, "Стрелок");
		CLASSES_RU.put(10, "Мистик");
		CLASSES_RU.put(11, "Маг");
		CLASSES_RU.put(12, "Властитель Огня");
		CLASSES_RU.put(13, "Некромант");
		CLASSES_RU.put(14, "Колдун");
		CLASSES_RU.put(15, "Клерик");
		CLASSES_RU.put(16, "Епископ");
		CLASSES_RU.put(17, "Проповедник");
		CLASSES_RU.put(18, "Светлый Воин");
		CLASSES_RU.put(19, "Светлый Рыцарь");
		CLASSES_RU.put(20, "Рыцарь Евы");
		CLASSES_RU.put(21, "Менестрель");
		CLASSES_RU.put(22, "Разведчик");
		CLASSES_RU.put(23, "Следопыт");
		CLASSES_RU.put(24, "Серебряный Рейнджер");
		CLASSES_RU.put(25, "Светлый Мистик");
		CLASSES_RU.put(26, "Светлый Маг");
		CLASSES_RU.put(27, "Певец Заклинаний");
		CLASSES_RU.put(28, "Последователь Стихий");
		CLASSES_RU.put(29, "Оракул Евы");
		CLASSES_RU.put(30, "Мудрец Евы");
		CLASSES_RU.put(31, "Темный Воин");
		CLASSES_RU.put(32, "Темный Рыцарь");
		CLASSES_RU.put(33, "Рыцарь Шилен");
		CLASSES_RU.put(34, "Танцор Смерти");
		CLASSES_RU.put(35, "Ассасин");
		CLASSES_RU.put(36, "Странник Бездны");
		CLASSES_RU.put(37, "Призрачный Рейнджер");
		CLASSES_RU.put(38, "Темный Мистик");
		CLASSES_RU.put(39, "Темный Маг");
		CLASSES_RU.put(40, "Заклинатель Ветра");
		CLASSES_RU.put(41, "Последователь Тьмы");
		CLASSES_RU.put(42, "Оракул Шилен");
		CLASSES_RU.put(43, "Мудрец Шилен");
		CLASSES_RU.put(44, "Боец");
		CLASSES_RU.put(45, "Налетчик");
		CLASSES_RU.put(46, "Разрушитель");
		CLASSES_RU.put(47, "Монах");
		CLASSES_RU.put(48, "Отшельник");
		CLASSES_RU.put(49, "Адепт");
		CLASSES_RU.put(50, "Шаман");
		CLASSES_RU.put(51, "Верховный Шаман");
		CLASSES_RU.put(52, "Вестник Войны");
		CLASSES_RU.put(53, "Подмастерье");
		CLASSES_RU.put(54, "Собиратель");
		CLASSES_RU.put(55, "Охотник за Наградой");
		CLASSES_RU.put(56, "Ремесленник");
		CLASSES_RU.put(57, "Кузнец");
		CLASSES_RU.put(88, "Дуэлист");
		CLASSES_RU.put(89, "Полководец");
		CLASSES_RU.put(90, "Рыцарь Феникса");
		CLASSES_RU.put(91, "Рыцарь Ада");
		CLASSES_RU.put(92, "Снайпер");
		CLASSES_RU.put(93, "Авантюрист");
		CLASSES_RU.put(94, "Архимаг");
		CLASSES_RU.put(95, "Пожиратель Душ");
		CLASSES_RU.put(96, "Чернокнижник");
		CLASSES_RU.put(97, "Кардинал");
		CLASSES_RU.put(98, "Апостол");
		CLASSES_RU.put(99, "Храмовник Евы");
		CLASSES_RU.put(100, "Виртуоз");
		CLASSES_RU.put(101, "Странник Ветра");
		CLASSES_RU.put(102, "Страж Лунного Света");
		CLASSES_RU.put(103, "Магистр Магии");
		CLASSES_RU.put(104, "Мастер Стихий");
		CLASSES_RU.put(105, "Жрец Евы");
		CLASSES_RU.put(106, "Храмовник Шилен");
		CLASSES_RU.put(107, "Призрачный Танцор");
		CLASSES_RU.put(108, "Призрачный Охотник");
		CLASSES_RU.put(109, "Страж Теней");
		CLASSES_RU.put(110, "Повелитель Бури");
		CLASSES_RU.put(111, "Владыка Теней");
		CLASSES_RU.put(112, "Жрец Шилен");
		CLASSES_RU.put(113, "Титан");
		CLASSES_RU.put(114, "Аватар");
		CLASSES_RU.put(115, "Деспот");
		CLASSES_RU.put(116, "Глас Судьбы");
		CLASSES_RU.put(117, "Кладоискатель");
		CLASSES_RU.put(118, "Мастер");
		CLASSES_EN.put(0, "Human Fighter");
		CLASSES_EN.put(1, "Warrior");
		CLASSES_EN.put(2, "Gladiator");
		CLASSES_EN.put(3, "Warlord");
		CLASSES_EN.put(4, "Human Knight");
		CLASSES_EN.put(5, "Paladin");
		CLASSES_EN.put(6, "Dark Avenger");
		CLASSES_EN.put(7, "Rogue");
		CLASSES_EN.put(8, "Treasure Hunter");
		CLASSES_EN.put(9, "Hawkeye");
		CLASSES_EN.put(10, "Human Mystic");
		CLASSES_EN.put(11, "Human Wizard");
		CLASSES_EN.put(12, "Sorcerer");
		CLASSES_EN.put(13, "Necromancer");
		CLASSES_EN.put(14, "Warlock");
		CLASSES_EN.put(15, "Cleric");
		CLASSES_EN.put(16, "Bishop");
		CLASSES_EN.put(17, "Prophet");
		CLASSES_EN.put(18, "Elven Fighter");
		CLASSES_EN.put(19, "Elven Knight");
		CLASSES_EN.put(20, "Temple Knight");
		CLASSES_EN.put(21, "Sword Singer");
		CLASSES_EN.put(22, "Elven Scout");
		CLASSES_EN.put(23, "Plains Walker");
		CLASSES_EN.put(24, "Silver Ranger");
		CLASSES_EN.put(25, "Elven Mystic");
		CLASSES_EN.put(26, "Elven Wizard");
		CLASSES_EN.put(27, "Spellsinger");
		CLASSES_EN.put(28, "Elemental Summoner");
		CLASSES_EN.put(29, "Elven Oracle");
		CLASSES_EN.put(30, "Elven Elder");
		CLASSES_EN.put(31, "Dark Fighter");
		CLASSES_EN.put(32, "Palus Knight");
		CLASSES_EN.put(33, "Shillien Knight");
		CLASSES_EN.put(34, "Bladedancer");
		CLASSES_EN.put(35, "Assassin");
		CLASSES_EN.put(36, "Abyss Walker");
		CLASSES_EN.put(37, "Phantom Ranger");
		CLASSES_EN.put(38, "Dark Mystic");
		CLASSES_EN.put(39, "Dark Wizard");
		CLASSES_EN.put(40, "Spellhowler");
		CLASSES_EN.put(41, "Phantom Summoner");
		CLASSES_EN.put(42, "Shillien Oracle");
		CLASSES_EN.put(43, "Shillien Elder");
		CLASSES_EN.put(44, "Orc Fighter");
		CLASSES_EN.put(45, "Orc Raider");
		CLASSES_EN.put(46, "Destroyer");
		CLASSES_EN.put(47, "Monk");
		CLASSES_EN.put(48, "Tyrant");
		CLASSES_EN.put(49, "Orc Mystic");
		CLASSES_EN.put(50, "Orc Shaman");
		CLASSES_EN.put(51, "Overlord");
		CLASSES_EN.put(52, "Warcryer");
		CLASSES_EN.put(53, "Dwarven Fighter");
		CLASSES_EN.put(54, "Scavenger");
		CLASSES_EN.put(55, "Bounty Hunter");
		CLASSES_EN.put(56, "Artisan");
		CLASSES_EN.put(57, "Warsmith");
		CLASSES_EN.put(88, "Duelist");
		CLASSES_EN.put(89, "Dreadnought");
		CLASSES_EN.put(90, "Phoenix Knight");
		CLASSES_EN.put(91, "Hell Knight");
		CLASSES_EN.put(92, "Sagittarius");
		CLASSES_EN.put(93, "Adventurer");
		CLASSES_EN.put(94, "Archmage");
		CLASSES_EN.put(95, "Soultaker");
		CLASSES_EN.put(96, "Arcana Lord");
		CLASSES_EN.put(97, "Cardinal");
		CLASSES_EN.put(98, "Hierophant");
		CLASSES_EN.put(99, "Eva's Templar");
		CLASSES_EN.put(100, "Sword Muse");
		CLASSES_EN.put(101, "Wind Rider");
		CLASSES_EN.put(102, "Moonlight Sentinel");
		CLASSES_EN.put(103, "Mystic Muse");
		CLASSES_EN.put(104, "Elemental Master");
		CLASSES_EN.put(105, "Eva's Saint");
		CLASSES_EN.put(106, "Shillien Templar");
		CLASSES_EN.put(107, "Spectral Dancer");
		CLASSES_EN.put(108, "Ghost Hunter");
		CLASSES_EN.put(109, "Ghost Sentinel");
		CLASSES_EN.put(110, "Storm Screamer");
		CLASSES_EN.put(111, "Spectral Master");
		CLASSES_EN.put(112, "Shillien Saint");
		CLASSES_EN.put(113, "Titan");
		CLASSES_EN.put(114, "Grand Khavatari");
		CLASSES_EN.put(115, "Dominator");
		CLASSES_EN.put(116, "Doom Cryer");
		CLASSES_EN.put(117, "Fortune Seeker");
		CLASSES_EN.put(118, "Maestro");
	}
	
	public static String htmlButton(String value, int width, int height, String function, Object... args)
	{
		String action = "bypass " + function;
		for(Object arg : args)
		{
			action = action + " " + arg;
		}
		return HtmlUtils.htmlButton(value, action, width, height);
	}
	
	public static String htmlButton(String value, int width, String function, Object... args)
	{
		return htmlButton(value, width, 22, function, args);
	}
	
	private static boolean checkHaveItem(Player player, int itemId, long count)
	{
		if(Functions.getItemCount(player, itemId) < count)
		{
			if(itemId == 57)
			{
				player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			}
			else
			{
				player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
			}
			return false;
		}
		return true;
	}
	
	private static ArrayList<ClassId> getAvailClasses(ClassId playerClass)
	{
		ArrayList<ClassId> result = new ArrayList<>();
		for(ClassId _class : ClassId.values())
		{
			if(_class.getLevel() != playerClass.getLevel() + 1 || !_class.childOf(playerClass))
				continue;
			result.add(_class);
		}
		return result;
	}
	
	private static String tableOcupation(Player player)
	{
		ClassId playerClass = player.getClassId();
		String playerClassName = getClassIdSysstring(player, playerClass.getId());
		StringBuilder result = new StringBuilder();
		result.append("<center>");
		result.append(localize(player, 1)).append(": <font color=LEVEL>").append(playerClassName).append("</font>");
		result.append("</center>");
		if(playerClass.getLevel() == 4)
		{
			return result + "<br>";
		}
		int need_level = class_levels[playerClass.getLevel() - 1];
		if(player.getLevel() < need_level)
		{
			return result + "<br1>" + localize(player, 1) + ": " + need_level + "<br>";
		}
		ArrayList<ClassId> avail_classes = getAvailClasses(playerClass);
		if(avail_classes.size() == 0)
		{
			return result + "<br>";
		}
		result.append("<center><table>");
		result.append("<tr><td>");
		switch(playerClass.getLevel())
		{
			case 1:
			{
				result.append(localize(player, 3, ACbConfigManager.FIRST_CLASS_ID, ACbConfigManager.FIRST_CLASS_PRICE));
				break;
			}
			case 2:
			{
				result.append(localize(player, 3, ACbConfigManager.SECOND_CLASS_ID, ACbConfigManager.SECOND_CLASS_PRICE));
				break;
			}
			case 3:
			{
				result.append(localize(player, 3, ACbConfigManager.THRID_CLASS_ID, ACbConfigManager.THRID_CLASS_PRICE));
			}
		}
		result.append("</td></tr>");
		for(ClassId _class : avail_classes)
		{
			String _className = getClassIdSysstring(player, _class.getId());
			result.append("<tr><td>");
			result.append("<button value=\"");
			result.append(_className);
			result.append("\" action=\"bypass _cbbsquestsocupation ");
			result.append(_class.getId());
			if(playerClass.getLevel() == 3)
			{
				result.append(" 0");
			}
			result.append("\" back=\"L2UI_CH3.bigbutton3_down\" fore=\"L2UI_CH3.bigbutton3\" width=134 height=22>");
			result.append("</td></tr>");
		}
		result.append("</table></center><br><br>");
		return result.toString();
	}
	
	public static String localize(Player player, int ID, Object... args)
	{
		boolean ru = player.isLangRus();
		switch(ID)
		{
			case 1:
			{
				return ru ? "Ваша текущая профессия" : "Your current occupation";
			}
			case 2:
			{
				return ru ? "Для получения следующей профессии вы должны достичь уровня" : "To get your's next occupation you should reach level";
			}
			case 3:
			{
				int itemId = ((Number) args[0]).intValue();
				String itemName = ItemHolder.getInstance().getTemplate(itemId).getName();
				long itemCount = ((Number) args[1]).longValue();
				return ru ? "Цена за профессию: " + Util.formatAdena(itemCount) + " " + itemName : "Class occupation price: " + Util.formatAdena(itemCount) + " " + itemName;
			}
		}
		return "Unknown localize String - " + ID;
	}
	
	private static String getClassIdSysstring(Player player, int classId)
	{
		String className = (player.isLangRus() ? CLASSES_RU : CLASSES_EN).get(classId);
		return className != null ? className : "Unknown";
	}
	
	private static boolean reducePoints(Player player, int count)
	{
		if(player == null || player.getPcBangPoints() < count)
		{
			return false;
		}
		player.setPcBangPoints(player.getPcBangPoints() - count);
		player.sendPacket(new ExPCCafePointInfo(player, 0, 1, 2, 12));
		return true;
	}
	
	@Override
	public void onLoad()
	{
		if(Config.COMMUNITYBOARD_ENABLED)
		{
			CommunityBoardManager.getInstance().registerHandler(this);
		}
	}
	
	@Override
	public void onReload()
	{
		if(Config.COMMUNITYBOARD_ENABLED)
		{
			CommunityBoardManager.getInstance().removeHandler(this);
		}
	}
	
	@Override
	public void onShutdown()
	{
	}
	
	@Override
	public String[] getBypassCommands()
	{
		return new String[] {"_cbbsquestsmain", "_cbbsquestsocupation"};
	}
	
	@Override
	public void onBypassCommand(Player player, String bypass)
	{
		if(!CommunityTools.checkConditions(player))
		{
			String html = HtmCache.getInstance().getNotNull("scripts/services/community/pages/locked.htm", player);
			html = html.replace("%name%", player.getName());
			ShowBoard.separateAndSend(html, player);
			return;
		}
		String html = HtmCache.getInstance().getNotNull("scripts/services/community/pages/quests.htm", player);
		String content = "";
		if(bypass.startsWith("_cbbsquestsmain"))
		{
			content = html(player);
		}
		else
		{
			StringTokenizer bf = new StringTokenizer(bypass, " ");
			bf.nextToken();
			Object[] arg = new String[] {};
			while(bf.hasMoreTokens())
			{
				arg = ArrayUtils.add(arg, bf.nextToken());
			}
			if(bypass.startsWith("_cbbsquestsocupation"))
			{
				content = getOcupation((String[]) arg, player);
			}
		}
		html = html.replace("%content%", content);
		ShowBoard.separateAndSend(html, player);
	}
	
	@Override
	public void onWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4, String arg5)
	{
	}
	
	private String html(Player player)
	{
		String result = "";
		result = result + tableOcupation(player);
		return result;
	}
	
	private String getOcupation(String[] var, Player player)
	{
		ClassId playerClass = player.getClassId();
		if(playerClass.getLevel() == 4)
		{
			return html(player);
		}
		int need_level = class_levels[playerClass.getLevel() - 1];
		if(player.getLevel() < need_level)
		{
			return html(player);
		}
		int RequestClass = Integer.parseInt(var[0]);
		ClassId RequestClassId = null;
		ArrayList<ClassId> avail_classes = getAvailClasses(playerClass);
		for(ClassId _class : avail_classes)
		{
			if(_class.getId() != RequestClass)
				continue;
			RequestClassId = _class;
			break;
		}
		if(RequestClassId == null)
		{
			return html(player);
		}
		int need_item_id = 0;
		int need_item_count = 0;
		switch(playerClass.getLevel())
		{
			case 1:
			{
				need_item_id = ACbConfigManager.FIRST_CLASS_ID;
				need_item_count = ACbConfigManager.FIRST_CLASS_PRICE;
				break;
			}
			case 2:
			{
				need_item_id = ACbConfigManager.SECOND_CLASS_ID;
				need_item_count = ACbConfigManager.SECOND_CLASS_PRICE;
				break;
			}
			case 3:
			{
				need_item_id = ACbConfigManager.THRID_CLASS_ID;
				need_item_count = ACbConfigManager.THRID_CLASS_PRICE;
			}
		}
		if(need_item_id == 0 || need_item_count == 0)
		{
			return html(player);
		}
		if(need_item_id == -300)
		{
			if(!reducePoints(player, need_item_count))
			{
				return html(player);
			}
		}
		else
		{
			if(!checkHaveItem(player, need_item_id, need_item_count))
			{
				return html(player);
			}
			Functions.removeItem(player, need_item_id, (long) need_item_count);
		}
		Log.add("QUEST\tСмена професии " + playerClass.getId() + " -> " + RequestClassId.getId() + " за " + need_item_id + ":" + need_item_count, "service_quests", player);
		player.sendPacket(new SystemMessage(1308));
		player.setClassId(RequestClass, false, false);
		player.broadcastUserInfo(true);
		return html(player);
	}
}