package services;

import l2.commons.threading.RunnableImpl;
import l2.gameserver.Config;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.cache.Msg;
import l2.gameserver.data.htm.HtmCache;
import l2.gameserver.data.xml.holder.ItemHolder;
import l2.gameserver.handler.items.IItemHandler;
import l2.gameserver.handler.items.ItemHandler;
import l2.gameserver.instancemanager.ServerVariables;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Effect;
import l2.gameserver.model.Playable;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.Summon;
import l2.gameserver.model.Zone;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2.gameserver.network.l2.s2c.ShowBoard;
import l2.gameserver.network.l2.s2c.SkillCoolTime;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.scripts.Functions;
import l2.gameserver.scripts.ScriptFile;
import l2.gameserver.skills.TimeStamp;
import l2.gameserver.tables.SkillTable;
import l2.gameserver.templates.item.ItemTemplate;
import l2.gameserver.utils.ItemFunctions;
import l2.gameserver.utils.Location;
import l2.gameserver.utils.Log;
import npc.model.NpcBufferInstance;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.napile.primitive.maps.CIntObjectMap;
import org.napile.primitive.maps.impl.CHashIntObjectMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

public class Buffer extends Functions implements ScriptFile
{
	private static final Logger LOG = LoggerFactory.getLogger(Buffer.class);
	private static final String DISPLAY_NAME = "Buffer";
	private static final String LIST_DELIMITERS = ";,";
	private static final String BUFF_PROFILE_REC_VAR = "BuffProfRec";
	private static final String BUFF_PROFILE_VAR_PREFIX = "BuffProf-";
	private static final CIntObjectMap<BuffTemplate> BUFF_TEMPLATES = new CHashIntObjectMap();
	private static final int CANCEL_MENU_ID = -2;
	private static final long CANCEL_ADENA_PRICE = 100;
	private static final int RESTORE_CP_MP_HP_MENU_ID = -3;
	private static final long RESTORE_ADENA_PRICE = 50;
	private static final int SHARED_REUSE_GROUP = -99999;
	private static final int PROFILE_MAX_PROFILES = 5;
	private static final int PROFILE_REC_MENU_ID = -100;
	private static final int PROFILE_BASE_MENU_ID = -1000;
	private static final int PROFILE_SAVE_MENU_ID = -2000;
	private static final SAXReader READER = new SAXReader(true);
	private static final File BUFF_TEMPLATES_FILE = new File(Config.DATAPACK_ROOT, "data/buff_templates.xml");
	private static BuffItemHandler BUFF_ITEMS_HANDLER;
	private static int[] BUFF_ITEMS_IDS;
	
	private static boolean checkReuse(Player player)
	{
		if(player.isSharedGroupDisabled(-99999))
		{
			player.sendPacket(new SystemMessage(48).addString("Buffer"));
			return false;
		}
		TimeStamp timeStamp = new TimeStamp(-99999, System.currentTimeMillis() + Config.ALT_NPC_BUFFER_REUSE_DELAY, Config.ALT_NPC_BUFFER_REUSE_DELAY);
		player.addSharedGroupReuse(-99999, timeStamp);
		return true;
	}
	
	private static void applyMenuItem(int menuId, Player player)
	{
		switch(menuId)
		{
			case -100:
			{
				player.setVar("BuffProfRec", "", -1);
				break;
			}
			case -2:
			{
				if(player.isInCombat())
				{
					player.sendPacket(new SystemMessage(113).addString("Buffer"));
					return;
				}
				if(!player.reduceAdena(100, true))
				{
					player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
					return;
				}
				if(player.getPet() != null)
				{
					player.getPet().getEffectList().stopAllEffects();
				}
				player.getEffectList().stopAllEffects();
				player.sendPacket(new SkillCoolTime(player));
				break;
			}
			case -3:
			{
				if(player.isInCombat())
				{
					player.sendPacket(new SystemMessage(113).addString("Buffer"));
					return;
				}
				if(!player.reduceAdena(50, true))
				{
					player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
					return;
				}
				if(!checkReuse(player))
					break;
				player.setCurrentHpMp((double) player.getMaxHp(), (double) player.getMaxMp());
				player.setCurrentCp((double) player.getMaxCp());
				player.sendStatusUpdate(true, false, 9, 11, 33, 10, 12, 34);
				if(player.getPet() == null)
					break;
				Summon summon = player.getPet();
				summon.setCurrentHpMp((double) summon.getMaxHp(), (double) summon.getMaxMp(), false);
				break;
			}
			default:
			{
				if(menuId > 0)
				{
					if(!checkReuse(player))
						break;
					BuffTemplate buffTemplate = BUFF_TEMPLATES.get(menuId);
					if(buffTemplate != null)
					{
						buffTemplate.apply(player);
						AddProfileRec(player, menuId);
						break;
					}
					LOG.warn("Buffer: Unknown menuId \"" + menuId + "\" used.");
					break;
				}
				if(menuId <= -2000 && menuId > -2005)
				{
					int profileNum = Math.abs(-2000 - menuId);
					SaveProfile(player, profileNum);
					break;
				}
				if(menuId > -1000 || menuId <= -1005)
					break;
				if(!checkReuse(player))
					break;
				int profileNum = Math.abs(-1000 - menuId);
				ApplyProfile(player, profileNum);
			}
		}
	}
	
	private static void ApplyProfile(Player player, int profileNum)
	{
		if(profileNum > 5)
		{
			return;
		}
		String profileStr = player.getVar(String.format("%s%d", "BuffProf-", profileNum));
		if(profileStr == null || profileStr.trim().isEmpty())
		{
			player.sendMessage(new CustomMessage("scripts.npc.model.L2NpcBufferInstance.EmptyProfile", player));
			return;
		}
		StringTokenizer st = new StringTokenizer(profileStr.trim(), ";,", false);
		LinkedList<BuffTemplate> profileBuffTemplates = new LinkedList<>();
		while(st.hasMoreTokens())
		{
			String menuIdStr = st.nextToken();
			if(menuIdStr.isEmpty())
				continue;
			try
			{
				int menuId = Integer.parseInt(menuIdStr);
				BuffTemplate buffTemplate = BUFF_TEMPLATES.get(menuId);
				if(buffTemplate == null)
					continue;
				profileBuffTemplates.add(buffTemplate);
			}
			catch(NumberFormatException e)
			{
				LOG.error("Buffer: Can't apply profile \"" + profileNum + "\"", e);
			}
		}
		ThreadPoolManager.getInstance().execute(new RunnableImpl()
		{
			
			@Override
			public void runImpl() throws Exception
			{
				for(BuffTemplate buffTemplate : profileBuffTemplates)
				{
					if(buffTemplate.applySync(player))
						continue;
					return;
				}
			}
		});
	}
	
	private static void SaveProfile(Player player, int profileNum)
	{
		String currRec = player.getVar("BuffProfRec");
		if(profileNum > 5 || currRec == null)
		{
			player.sendMessage(new CustomMessage("scripts.npc.model.L2NpcBufferInstance.CantSaveProfile", player));
			return;
		}
		player.setVar(String.format("%s%d", "BuffProf-", profileNum), currRec, -1);
		player.unsetVar("BuffProfRec");
		player.sendMessage(new CustomMessage("scripts.npc.model.L2NpcBufferInstance.ProfileSaved", player));
	}
	
	private static void AddProfileRec(Player player, int menuId)
	{
		String currRec = player.getVar("BuffProfRec");
		if(currRec == null)
		{
			return;
		}
		if(currRec.trim().isEmpty())
		{
			player.setVar("BuffProfRec", String.format("%d", menuId), -1);
		}
		else if((currRec = currRec + String.format(";%d", menuId)).length() > 250)
		{
			player.sendMessage(new CustomMessage("scripts.npc.model.L2NpcBufferInstance.LimitProfile", player));
			player.unsetVar("BuffProf-");
		}
		else
		{
			player.sendMessage(new CustomMessage("scripts.npc.model.L2NpcBufferInstance.BuffAdded", player));
			player.setVar("BuffProfRec", currRec.trim(), -1);
		}
	}
	
	private static boolean haveBuffItem(Player player)
	{
		for(int buffItemIdx = 0;buffItemIdx < BUFF_ITEMS_IDS.length;++buffItemIdx)
		{
			if(Functions.getItemCount(player, BUFF_ITEMS_IDS[buffItemIdx]) <= 0)
				continue;
			return true;
		}
		return false;
	}
	
	private static boolean isAllowedNpc(Player player, NpcInstance npc)
	{
		if(npc == null)
		{
			return false;
		}
		if(!(npc instanceof NpcBufferInstance))
		{
			return false;
		}
		return !player.isActionsDisabled() && Config.ALLOW_TALK_WHILE_SITTING || !player.isSitting() && npc.isInActingRange(player);
	}
	
	public static void showPage(Player player, String page, NpcInstance npc)
	{
		if(page == null || page.contains(".."))
		{
			return;
		}
		NpcHtmlMessage html = new NpcHtmlMessage(player, npc);
		html.setFile("mods/buffer/" + page + ".htm");
		player.sendPacket(html);
	}
	
	public static void showBBSPage(Player player, String page)
	{
		if(page == null || page.contains(".."))
		{
			return;
		}
		String html = HtmCache.getInstance().getNotNull("mods/buffer/community_buffer/" + page + ".htm", player);
		ShowBoard.separateAndSend(html, player);
	}
	
	private static void LoadBuffItems()
	{
		String buffItemIdsText = ServerVariables.getString("BuffItemIds", "3433");
		StringTokenizer st = new StringTokenizer(buffItemIdsText, ";,", false);
		ArrayList<Integer> buffItemIds = new ArrayList<>();
		while(st.hasMoreTokens())
		{
			int handleBuffItemId = Integer.parseInt(st.nextToken());
			buffItemIds.add(handleBuffItemId);
		}
		if(BUFF_ITEMS_HANDLER != null)
		{
			ItemHandler.getInstance().unregisterItemHandler(BUFF_ITEMS_HANDLER);
		}
		BUFF_ITEMS_IDS = new int[buffItemIds.size()];
		for(int buffItemIdx = 0;buffItemIdx < buffItemIds.size();++buffItemIdx)
		{
			BUFF_ITEMS_IDS[buffItemIdx] = buffItemIds.get(buffItemIdx);
		}
		BUFF_ITEMS_HANDLER = new BuffItemHandler();
		ItemHandler.getInstance().registerItemHandler(BUFF_ITEMS_HANDLER);
		LOG.info("Buffer: Loaded " + BUFF_ITEMS_IDS.length + " buff item(s).");
	}
	
	private static void loadBuffTemplates()
	{
		CHashIntObjectMap templates = new CHashIntObjectMap();
		try
		{
			Document document = READER.read(BUFF_TEMPLATES_FILE);
			Element rootElement = document.getRootElement();
			Iterator templatesElementIt = document.getRootElement().elementIterator();
			while(templatesElementIt.hasNext())
			{
				Element templatesElement = (Element) templatesElementIt.next();
				if(!"template".equalsIgnoreCase(templatesElement.getName()))
					continue;
				int menuId = Integer.parseInt(templatesElement.attributeValue("menuId"));
				BuffTarget buffTarget = BuffTarget.valueOf(templatesElement.attributeValue("target").toUpperCase());
				ArrayList<BuffTemplateConsume> consumes = new ArrayList<>();
				boolean consumeAnyFirst = false;
				ArrayList<Skill> produces = new ArrayList<>();
				Iterator templateElementIt = templatesElement.elementIterator();
				while(templateElementIt.hasNext())
				{
					Element templateElement = (Element) templateElementIt.next();
					if("consume".equalsIgnoreCase(templateElement.getName()))
					{
						consumeAnyFirst = Boolean.parseBoolean(templateElement.attributeValue("anyFirst", "false"));
						Iterator consumeElementIt = templateElement.elementIterator();
						while(consumeElementIt.hasNext())
						{
							Element consumeElement = (Element) consumeElementIt.next();
							if(!"item".equalsIgnoreCase(consumeElement.getName()))
								continue;
							Element itemElement = consumeElement;
							int itemId = Integer.parseInt(itemElement.attributeValue("id"));
							long amount = Long.parseLong(itemElement.attributeValue("amount"));
							int fromLevel = Integer.parseInt(itemElement.attributeValue("from_level", "0"));
							ItemTemplate itemTemplate = ItemHolder.getInstance().getTemplate(itemId);
							consumes.add(new BuffTemplateConsume(itemTemplate, amount, fromLevel));
						}
						continue;
					}
					if(!"produce".equalsIgnoreCase(templateElement.getName()))
						continue;
					Iterator produceElementIt = templateElement.elementIterator();
					while(produceElementIt.hasNext())
					{
						Element produceElement = (Element) produceElementIt.next();
						if(!"skill".equalsIgnoreCase(produceElement.getName()))
							continue;
						Element skillElement = produceElement;
						int skillId = Integer.parseInt(skillElement.attributeValue("id"));
						int skillLevel = Integer.parseInt(skillElement.attributeValue("level"));
						Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);
						produces.add(skill);
					}
				}
				BuffTemplate buffTemplate = new BuffTemplate(produces, buffTarget, consumes, consumeAnyFirst);
				templates.put(menuId, buffTemplate);
			}
			BUFF_TEMPLATES.clear();
			BUFF_TEMPLATES.putAll(templates);
			LOG.info("Buffer: Loaded " + templates.size() + " buff template(s).");
		}
		catch(Exception e)
		{
			LOG.error(e.getMessage(), e);
		}
	}
	
	private static Map<String, String> ParseArgs(String argsText)
	{
		TreeMap<String, String> result = new TreeMap<>();
		char[] chs = argsText.toCharArray();
		StringBuilder sb = new StringBuilder();
		String key = null;
		String val = null;
		for(int chIdx = 0;chIdx < chs.length;++chIdx)
		{
			char ch = chs[chIdx];
			if(ch == '=' && key == null)
			{
				key = sb.toString();
				sb.setLength(0);
				continue;
			}
			if(ch == '&')
			{
				val = sb.toString();
				result.put(key, val);
				sb.setLength(0);
				key = null;
				val = null;
				continue;
			}
			sb.append(ch);
		}
		if(key != null)
		{
			val = sb.toString();
			result.put(key, val);
		}
		return result;
	}
	
	private static List<Skill> ParseSkillListText(String templateText)
	{
		LinkedList<Skill> result = new LinkedList<>();
		StringTokenizer st = new StringTokenizer(templateText, ";,", false);
		while(st.hasMoreTokens())
		{
			String skillDefStr = st.nextToken();
			int skillDefSplIdx = skillDefStr.indexOf(45);
			if(skillDefSplIdx > 0)
			{
				try
				{
					String skillIdStr = skillDefStr.substring(0, skillDefSplIdx);
					String skillLvlStr = skillDefStr.substring(skillDefSplIdx + 1);
					int skillId = Integer.parseInt(skillIdStr);
					int skillLvl = Integer.parseInt(skillLvlStr);
					Skill skill = SkillTable.getInstance().getInfo(skillId, skillLvl);
					if(skill == null)
					{
						LOG.error("Buffer: Buff template \"" + skillDefStr + "\" skill " + skillId + "-" + skillLvl + " undefined!");
						continue;
					}
					result.add(skill);
				}
				catch(NumberFormatException e)
				{
					LOG.error("Buffer: Can't parse buff template \"" + skillDefStr + "\"", e);
				}
				continue;
			}
			LOG.error("Buffer: Can't parse buff template \"" + skillDefStr + "\"");
		}
		return result;
	}
	
	public void act(String[] args_)
	{
		Player player = getSelf();
		if(player == null || args_ == null || args_.length == 0)
		{
			return;
		}
		NpcInstance npc = null;
		if(!haveBuffItem(player) && !isAllowedNpc(player, npc = getNpc()))
		{
			player.sendActionFailed();
			return;
		}
		if(player.isInCombat())
		{
			player.sendActionFailed();
			return;
		}
		Map<String, String> args = ParseArgs(args_[0]);
		String menuIdStr = args.get("ask");
		String backPage = args.get("reply");
		if(menuIdStr != null && !menuIdStr.isEmpty())
		{
			try
			{
				int menuId = Integer.parseInt(menuIdStr);
				applyMenuItem(menuId, player);
			}
			catch(Exception e)
			{
				LOG.error("Buffer: Can't apply buff of menuId = \"" + menuIdStr + "\"", e);
			}
		}
		if(backPage != null && !backPage.isEmpty())
		{
			showPage(player, backPage, npc);
		}
		else
		{
			player.sendActionFailed();
		}
	}
	
	public void actBBS(String[] args_)
	{
		Player player = getSelf();
		if(player == null || args_ == null || args_.length == 0)
		{
			return;
		}
		if(player.isInCombat())
		{
			player.sendActionFailed();
			return;
		}
		Map<String, String> args = ParseArgs(args_[0]);
		String menuIdStr = args.get("ask");
		String backPage = args.get("reply");
		if(menuIdStr != null && !menuIdStr.isEmpty())
		{
			try
			{
				int menuId = Integer.parseInt(menuIdStr);
				applyMenuItem(menuId, player);
			}
			catch(Exception e)
			{
				LOG.error("Buffer: Can't apply buff of menuId = \"" + menuIdStr + "\"", e);
			}
		}
		if(backPage != null && !backPage.isEmpty())
		{
			showBBSPage(player, backPage);
		}
		else
		{
			player.sendActionFailed();
		}
	}
	
	@Override
	public void onLoad()
	{
		loadBuffTemplates();
		LoadBuffItems();
	}
	
	@Override
	public void onReload()
	{
		loadBuffTemplates();
		LoadBuffItems();
	}
	
	@Override
	public void onShutdown()
	{
		BUFF_TEMPLATES.clear();
	}
	
	public enum BuffTarget
	{
		BUFF_PLAYER,
		BUFF_PET;
	}
	
	private static class BuffTemplate
	{
		private final List<Skill> _buffs;
		private final BuffTarget _target;
		private final List<BuffTemplateConsume> _consumes;
		private final boolean _consumeAnyFirst;
		
		protected BuffTemplate(List<Skill> buffs, BuffTarget target, List<BuffTemplateConsume> consumes, boolean consumeAnyFirst)
		{
			_buffs = buffs;
			_target = target;
			_consumes = consumes;
			_consumeAnyFirst = consumeAnyFirst;
		}
		
		private void applyBuff(Creature target)
		{
			if(target == null)
			{
				return;
			}
			target.block();
			try
			{
				boolean warmOffMsg = false;
				for(Skill sk : _buffs)
				{
					for(Effect e : target.getEffectList().getAllEffects())
					{
						if(e.getSkill().getId() != sk.getId())
							continue;
						e.exit();
						warmOffMsg = true;
					}
					if(warmOffMsg)
					{
						target.sendPacket(new SystemMessage(92).addSkillName(sk.getDisplayId(), sk.getDisplayLevel()));
					}
					if(Config.ALT_NPC_BUFFER_EFFECT_TIME > 0)
					{
						sk.getEffects(target, target, false, false, Config.ALT_NPC_BUFFER_EFFECT_TIME, 1.0, false);
						continue;
					}
					sk.getEffects(target, target, false, false);
				}
			}
			finally
			{
				target.unblock();
			}
		}
		
		private Creature aimingTarget(Creature target)
		{
			switch(_target)
			{
				case BUFF_PLAYER:
				{
					return target.getPlayer();
				}
				case BUFF_PET:
				{
					return target.getPet();
				}
			}
			return null;
		}
		
		private boolean consumeRequirements(Player player)
		{
			if(!_consumeAnyFirst)
			{
				for(BuffTemplateConsume buffTemplateConsume : _consumes)
				{
					if(buffTemplateConsume.getFormLevel() > 0 && player.getLevel() < buffTemplateConsume.getFormLevel() || buffTemplateConsume.mayConsume(player))
						continue;
					return false;
				}
			}
			for(BuffTemplateConsume buffTemplateConsume : _consumes)
			{
				if(buffTemplateConsume.getFormLevel() > 0 && player.getLevel() < buffTemplateConsume.getFormLevel())
					continue;
				if(!_consumeAnyFirst)
				{
					if(buffTemplateConsume.consume(player))
						continue;
					return false;
				}
				if(!buffTemplateConsume.consume(player))
					continue;
				return true;
			}
			return !_consumeAnyFirst;
		}
		
		public boolean canApply(Player player)
		{
			if(player.isInZone(Zone.ZoneType.SIEGE) || player.isInZone(Zone.ZoneType.water) || player.isInDuel())
			{
				return false;
			}
			return !player.isOlyParticipant();
		}
		
		public boolean apply(Player player)
		{
			if(!canApply(player))
			{
				player.sendPacket(new SystemMessage(113).addString("Buffer"));
				return false;
			}
			ThreadPoolManager.getInstance().execute(new RunnableImpl()
			{
				
				@Override
				public void runImpl() throws Exception
				{
					Creature target = aimingTarget(player);
					if(target == null)
					{
						player.sendPacket(Msg.THAT_IS_THE_INCORRECT_TARGET);
						return;
					}
					if(!consumeRequirements(player))
					{
						player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
						return;
					}
					applyBuff(target);
				}
			});
			return true;
		}
		
		public boolean applySync(Player player)
		{
			Player player2 = player;
			synchronized(player2)
			{
				if(!canApply(player))
				{
					player.sendPacket(new SystemMessage(113).addString("Buffer"));
					return false;
				}
				Creature target = aimingTarget(player);
				if(target == null)
				{
					player.sendPacket(Msg.THAT_IS_THE_INCORRECT_TARGET);
					return false;
				}
				if(!consumeRequirements(player))
				{
					return false;
				}
				applyBuff(target);
			}
			return true;
		}
	}
	
	private static class BuffItemHandler implements IItemHandler
	{
		@Override
		public boolean useItem(Playable playable, ItemInstance item, boolean ctrl)
		{
			try
			{
				Player player = playable.getPlayer();
				if(player != null)
				{
					showPage(playable.getPlayer(), "item-" + item.getItemId(), null);
					return true;
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			return false;
		}
		
		@Override
		public void dropItem(Player player, ItemInstance item, long count, Location loc)
		{
			if(!player.getInventory().destroyItem(item, count))
			{
				player.sendActionFailed();
				return;
			}
			Log.LogItem(player, Log.ItemLog.Delete, item);
			player.disableDrop(1000);
			player.sendChanges();
		}
		
		@Override
		public boolean pickupItem(Playable playable, ItemInstance item)
		{
			return false;
		}
		
		@Override
		public int[] getItemIds()
		{
			return BUFF_ITEMS_IDS;
		}
	}
	
	private static class BuffTemplateConsume
	{
		private final ItemTemplate _item;
		private final long _amount;
		private final int _formLevel;
		
		public BuffTemplateConsume(ItemTemplate item, long amount, int formLevel)
		{
			_item = item;
			_amount = amount;
			_formLevel = formLevel;
		}
		
		public ItemTemplate getItem()
		{
			return _item;
		}
		
		public long getAmount()
		{
			return _amount;
		}
		
		public int getFormLevel()
		{
			return _formLevel;
		}
		
		public boolean mayConsume(Player player)
		{
			long count = player.getInventory().getCountOf(getItem().getItemId());
			if(count < getAmount())
			{
				if(getItem().isAdena())
				{
					player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
				}
				else
				{
					player.sendMessage(new CustomMessage("scripts.npc.model.L2NpcBufferInstance.RequiresS1S2", player, getItem().getName(), getAmount()));
				}
				return false;
			}
			return true;
		}
		
		public boolean consume(Player player)
		{
			if(getAmount() == 0)
			{
				return player.getInventory().getCountOf(getItem().getItemId()) > 0;
			}
			return ItemFunctions.removeItem(player, getItem().getItemId(), getAmount(), true) >= getAmount();
		}
	}
}