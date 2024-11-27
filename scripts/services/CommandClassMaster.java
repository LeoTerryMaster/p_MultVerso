package services;

import l2.gameserver.Config;
import l2.gameserver.cache.Msg;
import l2.gameserver.data.StringHolder;
import l2.gameserver.data.xml.holder.ItemHolder;
import l2.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2.gameserver.handler.voicecommands.VoicedCommandHandler;
import l2.gameserver.listener.actor.player.OnGainExpSpListener;
import l2.gameserver.model.Player;
import l2.gameserver.model.actor.listener.CharListenerList;
import l2.gameserver.model.base.ClassId;
import l2.gameserver.model.base.TeamType;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.MagicSkillUse;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2.gameserver.scripts.Functions;
import l2.gameserver.scripts.ScriptFile;
import l2.gameserver.scripts.Scripts;
import l2.gameserver.templates.item.ItemTemplate;
import l2.gameserver.utils.ItemFunctions;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

public class CommandClassMaster extends Functions implements ScriptFile, OnGainExpSpListener, IVoicedCommandHandler
{
	private static final CommandClassMaster INSTANCE = new CommandClassMaster();
	private static final String SCRIPT_BYPASS_CLASS = "scripts_" + CommandClassMaster.class.getName();
	private static final String SHOW_COUNT_VAR_NAME = "cmd_class_master_show_cnt";
	private static Map<ClassId, ClassMasterPath> CLASS_INFOS = Collections.emptyMap();
	private String[] _voiceCommands = ArrayUtils.EMPTY_STRING_ARRAY;
	
	protected static CommandClassMaster getInstance()
	{
		return INSTANCE;
	}
	
	private static ClassMasterPath getClassInfoForClassId(ClassId classId)
	{
		return CLASS_INFOS.get(classId);
	}
	
	private static int getMinPlayerLevelForClassId(int classLevel)
	{
		switch(classLevel)
		{
			case 1:
			{
				return 1;
			}
			case 2:
			{
				return 20;
			}
			case 3:
			{
				return 40;
			}
			case 4:
			{
				return 76;
			}
		}
		return -1;
	}
	
	private static String getMinPlayerLevelForClassIdMessageAddress(int classLevel)
	{
		switch(classLevel)
		{
			case 2:
			{
				return "ClassMaster.Need20Level";
			}
			case 3:
			{
				return "ClassMaster.Need40Level";
			}
			case 4:
			{
				return "ClassMaster.Need76Level";
			}
		}
		return "ClassMaster.NothingToUp";
	}
	
	private static List<Pair<Integer, Long>> parseItems(String itemsStr)
	{
		StringTokenizer itemListCfgTok = new StringTokenizer(itemsStr, ",");
		LinkedList<Pair<Integer, Long>> result = new LinkedList<>();
		while(itemListCfgTok.hasMoreTokens())
		{
			String itemEntryCfg = itemListCfgTok.nextToken().trim();
			if(itemEntryCfg.isEmpty())
				continue;
			int itemCountDelim = itemEntryCfg.indexOf(":");
			if(itemCountDelim < 0)
			{
				throw new RuntimeException("Can't parse items \"" + itemsStr + "\"");
			}
			int itemId = Integer.parseInt(itemEntryCfg.substring(0, itemCountDelim).trim());
			long itemCount = Long.parseLong(itemEntryCfg.substring(itemCountDelim + 1).trim());
			result.add(Pair.of(itemId, itemCount));
		}
		return Collections.unmodifiableList(result);
	}
	
	private static Map<ClassId, ClassMasterPath> parseConfig(String cfgStr)
	{
		TreeMap<ClassId, ClassMasterPath> result = new TreeMap<>();
		StringTokenizer classLvlCfgTok = new StringTokenizer(cfgStr.trim(), ";");
		while(classLvlCfgTok.hasMoreTokens())
		{
			int classLevelOrd;
			String classLvlCfg = classLvlCfgTok.nextToken().trim();
			int priceDelimIdx = classLvlCfg.indexOf("-");
			List price = Collections.emptyList();
			List reward = Collections.emptyList();
			if(priceDelimIdx < 0)
			{
				classLevelOrd = Integer.parseInt(classLvlCfg.trim());
			}
			else
			{
				classLevelOrd = Integer.parseInt(classLvlCfg.substring(0, priceDelimIdx).trim());
				String priceAndRewardStr = classLvlCfg.substring(priceDelimIdx + 1).trim();
				int rewardDelimIdx = priceAndRewardStr.indexOf("/");
				if(rewardDelimIdx < 0)
				{
					price = parseItems(priceAndRewardStr);
				}
				else
				{
					price = parseItems(priceAndRewardStr.substring(0, rewardDelimIdx).trim());
					reward = parseItems(priceAndRewardStr.substring(rewardDelimIdx + 1).trim());
				}
			}
			for(ClassId fromClassId : ClassId.VALUES)
			{
				if(fromClassId.getLevel() != classLevelOrd)
					continue;
				LinkedList<ClassId> availableClassIds = new LinkedList<>();
				for(ClassId availableClassId : ClassId.VALUES)
				{
					if(!availableClassId.childOf(fromClassId) || fromClassId.getLevel() + 1 != availableClassId.getLevel())
						continue;
					availableClassIds.add(availableClassId);
				}
				result.put(fromClassId, new ClassMasterPath(availableClassIds, fromClassId, price, reward));
			}
		}
		return Collections.unmodifiableMap(result);
	}
	
	private boolean canProcess(Player player, boolean notify)
	{
		if(player == null || player.isLogoutStarted() || player.isOutOfControl() || player.isDead() || player.isInDuel() || player.isAlikeDead() || player.isOlyParticipant() || player.isFlying() || player.isSitting() || player.getTeam() != TeamType.NONE || player.isInStoreMode() || player.entering)
		{
			if(notify && player != null && !player.entering)
			{
				player.sendMessage(new CustomMessage("common.TryLater", player));
			}
			return false;
		}
		return true;
	}
	
	private void showClassMasterPath(Player player, ClassMasterPath classMasterPath)
	{
		if(!canProcess(player, false) || classMasterPath == null || classMasterPath.getAvailableClassIds().isEmpty())
		{
			return;
		}
		NpcHtmlMessage html = new NpcHtmlMessage(player, null);
		html.setFile("scripts/services/command_class_master.htm");
		StringBuilder classMasterListHtml = new StringBuilder();
		for(ClassId classId : classMasterPath.getAvailableClassIds())
		{
			classMasterListHtml.append("<button width=140 height=21 back=l2ui_ch3.msnbutton fore=l2ui_ch3.msnbutton ").append("action=\"bypass -h ").append(SCRIPT_BYPASS_CLASS).append(":classMaster ").append(classId.getId()).append("\" ").append("value=\"").append(new CustomMessage(String.format("ClassName.%d", classId.getId()), player)).append("\">").append("<br1>");
		}
		html.replace("%class_master_list%", classMasterListHtml.toString());
		StringBuilder requiredListHtml = new StringBuilder();
		for(Pair<Integer, Long> requiredItem : classMasterPath.getPrice())
		{
			ItemTemplate itemTemplate = ItemHolder.getInstance().getTemplate(requiredItem.getKey().intValue());
			String reqItemHtml = StringHolder.getInstance().getNotNull(player, "scripts.services.CommandClassMaster.requiredItem");
			reqItemHtml = reqItemHtml.replace("%item_id%", String.valueOf(requiredItem.getKey()));
			reqItemHtml = reqItemHtml.replace("%item_name%", itemTemplate.getName());
			reqItemHtml = reqItemHtml.replace("%item_count%", String.valueOf(requiredItem.getValue()));
			requiredListHtml.append(reqItemHtml);
		}
		html.replace("%required_items_list%", requiredListHtml.toString());
		StringBuilder rewardListHtml = new StringBuilder();
		for(Pair<Integer, Long> rewardItem : classMasterPath.getReward())
		{
			ItemTemplate itemTemplate = ItemHolder.getInstance().getTemplate(rewardItem.getKey().intValue());
			String rewItemHtml = StringHolder.getInstance().getNotNull(player, "scripts.services.CommandClassMaster.rewardItem");
			rewItemHtml = rewItemHtml.replace("%item_id%", String.valueOf(rewardItem.getKey()));
			rewItemHtml = rewItemHtml.replace("%item_name%", itemTemplate.getName());
			rewItemHtml = rewItemHtml.replace("%item_count%", String.valueOf(rewardItem.getValue()));
			rewardListHtml.append(rewItemHtml);
		}
		html.replace("%reward_items_list%", rewardListHtml.toString());
		player.sendPacket(html);
	}
	
	private void showClassMasterPath(Player player)
	{
		ClassMasterPath classMasterPath = CLASS_INFOS.get(player.getClassId());
		if(classMasterPath == null || classMasterPath.getAvailableClassIds().isEmpty() || classMasterPath.getMinPlayerLevel() > player.getLevel())
		{
			return;
		}
		showClassMasterPath(player, classMasterPath);
	}
	
	public void classMaster()
	{
		classMaster(ArrayUtils.EMPTY_STRING_ARRAY);
	}
	
	public void classMaster(String... args)
	{
		Player player = getSelf();
		if(player == null)
		{
			return;
		}
		if(!Config.COMMAND_CLASS_MASTER_ENABLED)
		{
			player.sendMessage(new CustomMessage("common.Disabled", player));
			return;
		}
		if(!canProcess(player, true))
		{
			return;
		}
		ClassId currentClassId = player.getClassId();
		ClassMasterPath classMasterPath = CLASS_INFOS.get(currentClassId);
		if(classMasterPath == null)
		{
			show(new CustomMessage("ClassMaster.NothingToUp", player), player);
			return;
		}
		if(classMasterPath.getMinPlayerLevel() > player.getLevel())
		{
			player.sendMessage(new CustomMessage(getMinPlayerLevelForClassIdMessageAddress(currentClassId.getLevel() + 1), player));
			return;
		}
		if(args.length > 0)
		{
			int requiredClassIdOrd = Integer.parseInt(args[0]);
			ClassId requiredClassId = null;
			for(ClassId classId : classMasterPath.getAvailableClassIds())
			{
				if(classId.getId() != requiredClassIdOrd)
					continue;
				requiredClassId = classId;
				break;
			}
			if(requiredClassId == null)
			{
				return;
			}
			for(Pair requiredItem : classMasterPath.getPrice())
			{
				if(ItemFunctions.getItemCount(player, (Integer) requiredItem.getKey()) >= (Long) requiredItem.getValue())
					continue;
				if((Integer) requiredItem.getKey() == 57)
				{
					player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
				}
				else
				{
					player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
				}
				return;
			}
			long weight = 0;
			long slots = 0;
			for(Pair<Integer, Long> rewardItem : classMasterPath.getReward())
			{
				ItemTemplate rewardItemTemplate = ItemHolder.getInstance().getTemplate(rewardItem.getKey().intValue());
				weight += (long) rewardItemTemplate.getWeight() * rewardItem.getValue();
				slots += rewardItemTemplate.isStackable() ? 1 : (Long) rewardItem.getValue();
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
			for(Pair<Integer, Long> requiredItem : classMasterPath.getPrice())
			{
				if(ItemFunctions.removeItem(player, requiredItem.getKey(), requiredItem.getValue(), true) >= requiredItem.getValue())
					continue;
				return;
			}
			changeClass(player, requiredClassId);
			for(Pair<Integer, Long> rewardItem : classMasterPath.getReward())
			{
				ItemFunctions.addItem(player, rewardItem.getKey(), rewardItem.getValue(), true);
			}
		}
		showClassMasterPath(player);
	}
	
	private void changeClass(Player player, ClassId classId)
	{
		if(player.getClassId().getLevel() == 3)
		{
			player.sendPacket(Msg.YOU_HAVE_COMPLETED_THE_QUEST_FOR_3RD_OCCUPATION_CHANGE_AND_MOVED_TO_ANOTHER_CLASS_CONGRATULATIONS);
		}
		else
		{
			player.sendPacket(Msg.CONGRATULATIONS_YOU_HAVE_TRANSFERRED_TO_A_NEW_CLASS);
		}
		player.setClassId(classId.getId(), false, false);
		player.broadcastCharInfo();
		player.broadcastPacket(new MagicSkillUse(player, player, 4339, 1, 0, 0));
	}
	
	@Override
	public void onGainExpSp(Player player, long exp, long sp)
	{
		if(!Config.COMMAND_CLASS_MASTER_ENABLED || Config.COMMAND_CLASS_POPUP_LIMIT == 0)
		{
			return;
		}
		if(!canProcess(player, false))
		{
			return;
		}
		ClassMasterPath classMasterPath = getClassInfoForClassId(player.getClassId());
		if(classMasterPath == null || classMasterPath.getMinPlayerLevel() > player.getLevel())
		{
			return;
		}
		if(Config.COMMAND_CLASS_POPUP_LIMIT == -1)
		{
			showClassMasterPath(player);
		}
		else if(Config.COMMAND_CLASS_POPUP_LIMIT > 0)
		{
			int cntVarVal = player.getVarInt("cmd_class_master_show_cnt", 0);
			int cntClassId = cntVarVal & 255;
			int cntVal = cntVarVal >> 8;
			if(cntClassId != player.getActiveClassId() || cntVal < Config.COMMAND_CLASS_POPUP_LIMIT)
			{
				player.setVar("cmd_class_master_show_cnt", cntVal + 1 << 8 | player.getActiveClassId() & 255, -1);
				showClassMasterPath(player);
			}
		}
	}
	
	@Override
	public void onLoad()
	{
		if(Config.COMMAND_CLASS_MASTER_ENABLED)
		{
			CLASS_INFOS = parseConfig(Config.COMMAND_CLASS_MASTER_CLASSES);
			if(Config.COMMAND_CLASS_POPUP_LIMIT != 0)
			{
				CharListenerList.addGlobal(getInstance());
			}
			if(Config.COMMAND_CLASS_MASTER_VOICE_COMMANDS.length > 0)
			{
				INSTANCE._voiceCommands = Config.COMMAND_CLASS_MASTER_VOICE_COMMANDS;
				VoicedCommandHandler.getInstance().registerVoicedCommandHandler(INSTANCE);
			}
		}
	}
	
	@Override
	public void onReload()
	{
		if(Config.COMMAND_CLASS_MASTER_ENABLED)
		{
			CLASS_INFOS = parseConfig(Config.COMMAND_CLASS_MASTER_CLASSES);
		}
	}
	
	@Override
	public void onShutdown()
	{
	}
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		for(String cmd : _voiceCommands)
		{
			if(cmd == null || cmd.isEmpty() || !cmd.equalsIgnoreCase(command))
				continue;
			Scripts.getInstance().callScripts(activeChar, CommandClassMaster.class.getName(), "classMaster");
			return true;
		}
		return false;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		if(!Config.COMMAND_CLASS_MASTER_ENABLED)
		{
			return ArrayUtils.EMPTY_STRING_ARRAY;
		}
		return Config.COMMAND_CLASS_MASTER_VOICE_COMMANDS;
	}
	
	private static class ClassMasterPath
	{
		private final List<ClassId> _availableClassIds;
		private final ClassId _fromClassId;
		private final List<Pair<Integer, Long>> _price;
		private final List<Pair<Integer, Long>> _reward;
		
		private ClassMasterPath(List<ClassId> availableClassIds, ClassId fromClassId, List<Pair<Integer, Long>> price, List<Pair<Integer, Long>> reward)
		{
			_availableClassIds = availableClassIds;
			_fromClassId = fromClassId;
			_price = price;
			_reward = reward;
		}
		
		public int getMinPlayerLevel()
		{
			return getMinPlayerLevelForClassId(_fromClassId.getLevel() + 1);
		}
		
		public List<ClassId> getAvailableClassIds()
		{
			return _availableClassIds;
		}
		
		public ClassId getFromClassId()
		{
			return _fromClassId;
		}
		
		public List<Pair<Integer, Long>> getPrice()
		{
			return _price;
		}
		
		public List<Pair<Integer, Long>> getReward()
		{
			return _reward;
		}
	}
}