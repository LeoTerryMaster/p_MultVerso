package l2.gameserver.scripts;

import l2.commons.collections.MultiValueSet;
import l2.commons.lang.reference.HardReference;
import l2.commons.lang.reference.HardReferences;
import l2.commons.threading.RunnableImpl;
import l2.gameserver.Config;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.cache.Msg;
import l2.gameserver.data.xml.holder.ItemHolder;
import l2.gameserver.data.xml.holder.NpcHolder;
import l2.gameserver.instancemanager.ReflectionManager;
import l2.gameserver.instancemanager.ServerVariables;
import l2.gameserver.model.Creature;
import l2.gameserver.model.GameObjectsStorage;
import l2.gameserver.model.Party;
import l2.gameserver.model.Playable;
import l2.gameserver.model.Player;
import l2.gameserver.model.SimpleSpawner;
import l2.gameserver.model.Summon;
import l2.gameserver.model.World;
import l2.gameserver.model.entity.Reflection;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.model.mail.Mail;
import l2.gameserver.network.l2.components.ChatType;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.network.l2.components.NpcString;
import l2.gameserver.network.l2.s2c.ExNoticePostArrived;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2.gameserver.network.l2.s2c.NpcSay;
import l2.gameserver.templates.item.ItemTemplate;
import l2.gameserver.templates.npc.NpcTemplate;
import l2.gameserver.utils.ItemFunctions;
import l2.gameserver.utils.Location;
import l2.gameserver.utils.MapUtils;
import l2.gameserver.utils.NpcUtils;
import l2.gameserver.utils.Strings;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ScheduledFuture;

public class Functions
{
	private static final String ITEM_ID_AMOUNT_LIST_DELIMITERS = ",;/";
	private static final String ITEM_ID_AMOUNT_ITEM_DELIMITERS = "-:_";
	public HardReference<Player> self = HardReferences.emptyRef();
	public HardReference<NpcInstance> npc = HardReferences.emptyRef();
	
	public static ScheduledFuture<?> executeTask(Player caller, String className, String methodName, Object[] args, Map<String, Object> variables, long delay)
	{
		return ThreadPoolManager.getInstance().schedule(new RunnableImpl()
		{
			
			@Override
			public void runImpl() throws Exception
			{
				callScripts(caller, className, methodName, args, variables);
			}
		}, delay);
	}
	
	public static ScheduledFuture<?> executeTask(String className, String methodName, Object[] args, Map<String, Object> variables, long delay)
	{
		return executeTask(null, className, methodName, args, variables, delay);
	}
	
	public static ScheduledFuture<?> executeTask(Player player, String className, String methodName, Object[] args, long delay)
	{
		return executeTask(player, className, methodName, args, null, delay);
	}
	
	public static ScheduledFuture<?> executeTask(String className, String methodName, Object[] args, long delay)
	{
		return executeTask(className, methodName, args, null, delay);
	}
	
	public static Object callScripts(String className, String methodName, Object[] args)
	{
		return callScripts(className, methodName, args, null);
	}
	
	public static Object callScripts(String className, String methodName, Object[] args, Map<String, Object> variables)
	{
		return callScripts(null, className, methodName, args, variables);
	}
	
	public static Object callScripts(Player player, String className, String methodName, Object[] args, Map<String, Object> variables)
	{
		return Scripts.getInstance().callScripts(player, className, methodName, args, variables);
	}
	
	public static List<Pair<ItemTemplate, Long>> parseItemIdAmountList(String itemIdAmountListText)
	{
		ArrayList<Pair<ItemTemplate, Long>> result = new ArrayList<>();
		StringTokenizer itemsListTokenizer = new StringTokenizer(itemIdAmountListText, ",;/");
		while(itemsListTokenizer.hasMoreTokens())
		{
			String consumeItemTextTok = itemsListTokenizer.nextToken();
			StringTokenizer itemIdAmountTokenizer = new StringTokenizer(consumeItemTextTok, "-:_");
			int itemId = Integer.parseInt(itemIdAmountTokenizer.nextToken());
			ItemTemplate itemTemplate = ItemHolder.getInstance().getTemplate(itemId);
			long itemCount = Long.parseLong(itemIdAmountTokenizer.nextToken());
			result.add(Pair.of(itemTemplate, itemCount));
		}
		return Collections.unmodifiableList(result);
	}
	
	public static void show(String text, Player self, NpcInstance npc, Object... arg)
	{
		if(text == null || self == null)
		{
			return;
		}
		NpcHtmlMessage msg = new NpcHtmlMessage(self, npc);
		if(text.endsWith(".html") || text.endsWith(".htm"))
		{
			msg.setFile(text);
		}
		else
		{
			msg.setHtml(Strings.bbParse(text));
		}
		if(arg != null && arg.length % 2 == 0)
		{
			int i = 0;
			while(i < arg.length)
			{
				msg.replace(String.valueOf(arg[i]), String.valueOf(arg[i + 1]));
				i = 2;
			}
		}
		self.sendPacket(msg);
	}
	
	public static void show(CustomMessage message, Player self)
	{
		show(message.toString(), self, null);
	}
	
	public static void sendMessage(String text, Player self)
	{
		self.sendMessage(text);
	}
	
	public static void sendMessage(CustomMessage message, Player self)
	{
		self.sendMessage(message);
	}
	
	public static void npcSayInRange(NpcInstance npc, String text, int range)
	{
		npcSayInRange(npc, range, NpcString.NONE, text);
	}
	
	public static void npcSayInRange(NpcInstance npc, int range, NpcString fStringId, String... params)
	{
		if(npc == null)
		{
			return;
		}
		NpcSay cs = new NpcSay(npc, ChatType.NPC_NORMAL, fStringId, params);
		for(Player player : World.getAroundPlayers(npc, range, Math.max(range / 2, 200)))
		{
			if(npc.getReflection() != player.getReflection())
				continue;
			player.sendPacket(cs);
		}
	}
	
	public static void npcSay(NpcInstance npc, String text)
	{
		npcSayInRange(npc, text, 1500);
	}
	
	public static void npcSay(NpcInstance npc, NpcString npcString, String... params)
	{
		npcSayInRange(npc, 1500, npcString, params);
	}
	
	public static void npcSayInRangeCustomMessage(NpcInstance npc, int range, String address, Object... replacements)
	{
		if(npc == null)
		{
			return;
		}
		for(Player player : World.getAroundPlayers(npc, range, Math.max(range / 2, 200)))
		{
			if(npc.getReflection() != player.getReflection())
				continue;
			player.sendPacket(new NpcSay(npc, ChatType.NPC_NORMAL, new CustomMessage(address, player, replacements).toString()));
		}
	}
	
	public static void npcSayInRangeCustomMessage(NpcInstance npc, ChatType chatType, int range, String address, Object... replacements)
	{
		if(npc == null)
		{
			return;
		}
		for(Player player : World.getAroundPlayers(npc, range, Math.max(range / 2, 200)))
		{
			if(npc.getReflection() != player.getReflection())
				continue;
			player.sendPacket(new NpcSay(npc, chatType, new CustomMessage(address, player, replacements).toString()));
		}
	}
	
	public static void npcSayCustomMessage(NpcInstance npc, ChatType chatType, String address, Object... replacements)
	{
		npcSayInRangeCustomMessage(npc, chatType, 1500, address, replacements);
	}
	
	public static void npcSayCustomMessage(NpcInstance npc, String address, Object... replacements)
	{
		npcSayCustomMessage(npc, ChatType.NPC_NORMAL, address, replacements);
	}
	
	public static void npcSayToPlayer(NpcInstance npc, Player player, String text)
	{
		npcSayToPlayer(npc, player, NpcString.NONE, text);
	}
	
	public static void npcSayToPlayer(NpcInstance npc, Player player, NpcString npcString, String... params)
	{
		if(npc == null)
		{
			return;
		}
		player.sendPacket(new NpcSay(npc, ChatType.TELL, npcString, params));
	}
	
	public static void npcShout(NpcInstance npc, String text)
	{
		npcShout(npc, NpcString.NONE, text);
	}
	
	public static void npcShout(NpcInstance npc, NpcString npcString, String... params)
	{
		if(npc == null)
		{
			return;
		}
		NpcSay cs = new NpcSay(npc, ChatType.SHOUT, npcString, params);
		int rx = MapUtils.regionX(npc);
		int ry = MapUtils.regionY(npc);
		int offset = Config.SHOUT_OFFSET;
		for(Player player : GameObjectsStorage.getAllPlayersForIterate())
		{
			if(player.getReflection() != npc.getReflection())
				continue;
			int tx = MapUtils.regionX(player);
			int ty = MapUtils.regionY(player);
			if(tx < rx - offset || tx > rx + offset || ty < ry - offset || ty > ry + offset)
				continue;
			player.sendPacket(cs);
		}
	}
	
	public static void npcShoutCustomMessage(NpcInstance npc, String address, Object... replacements)
	{
		if(npc == null)
		{
			return;
		}
		int rx = MapUtils.regionX(npc);
		int ry = MapUtils.regionY(npc);
		int offset = Config.SHOUT_OFFSET;
		for(Player player : GameObjectsStorage.getAllPlayersForIterate())
		{
			if(player.getReflection() != npc.getReflection())
				continue;
			int tx = MapUtils.regionX(player);
			int ty = MapUtils.regionY(player);
			if((tx < rx - offset || tx > rx + offset || ty < ry - offset || ty > ry + offset) && !npc.isInRange(player, (long) Config.CHAT_RANGE))
				continue;
			player.sendPacket(new NpcSay(npc, ChatType.SHOUT, new CustomMessage(address, player, replacements).toString()));
		}
	}
	
	public static void npcSay(NpcInstance npc, NpcString address, ChatType type, int range, String... replacements)
	{
		if(npc == null)
		{
			return;
		}
		for(Player player : World.getAroundPlayers(npc, range, Math.max(range / 2, 200)))
		{
			if(player.getReflection() != npc.getReflection())
				continue;
			player.sendPacket(new NpcSay(npc, type, address, replacements));
		}
	}
	
	public static void addItem(Playable playable, int itemId, long count)
	{
		ItemFunctions.addItem(playable, itemId, count, true);
	}
	
	public static long getItemCount(Playable playable, int itemId)
	{
		return ItemFunctions.getItemCount(playable, itemId);
	}
	
	public static long removeItem(Playable playable, int itemId, long count)
	{
		return ItemFunctions.removeItem(playable, itemId, count, true);
	}
	
	public static boolean ride(Player player, int pet)
	{
		if(player.isMounted())
		{
			player.setMount(0, 0, 0);
		}
		if(player.getPet() != null)
		{
			player.sendPacket(Msg.YOU_ALREADY_HAVE_A_PET);
			return false;
		}
		player.setMount(pet, 0, 0);
		return true;
	}
	
	public static void unRide(Player player)
	{
		if(player.isMounted())
		{
			player.setMount(0, 0, 0);
		}
	}
	
	public static void unSummonPet(Player player, boolean onlyPets)
	{
		Summon pet = player.getPet();
		if(pet == null)
		{
			return;
		}
		if(pet.isPet() || !onlyPets)
		{
			pet.unSummon();
		}
	}
	
	@Deprecated
	public static NpcInstance spawn(Location loc, int npcId)
	{
		return spawn(loc, npcId, ReflectionManager.DEFAULT);
	}
	
	@Deprecated
	public static NpcInstance spawn(Location loc, int npcId, Reflection reflection)
	{
		return NpcUtils.spawnSingle(npcId, loc, reflection, 0);
	}
	
	@Deprecated
	public static void SpawnNPCs(int npcId, int[][] locations, List<SimpleSpawner> list)
	{
		NpcTemplate template = NpcHolder.getInstance().getTemplate(npcId);
		if(template == null)
		{
			System.out.println("WARNING! Functions.SpawnNPCs template is null for npc: " + npcId);
			Thread.dumpStack();
			return;
		}
		for(int[] location : locations)
		{
			SimpleSpawner sp = new SimpleSpawner(template);
			Location loc = new Location(location[0], location[1], location[2]);
			if(location.length > 3)
			{
				loc.setH(location[3]);
			}
			sp.setLoc(loc);
			sp.setAmount(1);
			sp.setRespawnDelay(0);
			sp.init();
			if(list == null)
				continue;
			list.add(sp);
		}
	}
	
	public static void deSpawnNPCs(List<SimpleSpawner> list)
	{
		for(SimpleSpawner sp : list)
		{
			sp.deleteAll();
		}
		list.clear();
	}
	
	public static void teleportParty(Party party, Location loc, int radius)
	{
		for(Player partyMember : party.getPartyMembers())
		{
			partyMember.teleToLocation(Location.findPointToStay(loc, radius, partyMember.getGeoIndex()));
		}
	}
	
	public static boolean IsActive(String name)
	{
		return ServerVariables.getString(name, "off").equalsIgnoreCase("on");
	}
	
	public static boolean SetActive(String name, boolean active)
	{
		if(active == IsActive(name))
		{
			return false;
		}
		if(active)
		{
			ServerVariables.set(name, "on");
		}
		else
		{
			ServerVariables.unset(name);
		}
		return true;
	}
	
	public static boolean SimpleCheckDrop(Creature mob, Creature killer)
	{
		return mob != null && mob.isMonster() && !mob.isRaid() && killer != null && killer.getPlayer() != null && killer.getLevel() - mob.getLevel() < 9;
	}
	
	public static boolean isPvPEventStarted()
	{
		if(((Boolean) callScripts("events.TvT.TvT", "isRunned", new Object[0])).booleanValue())
		{
			return true;
		}
		return ((Boolean) callScripts("events.lastHero.LastHero", "isRunned", new Object[0])).booleanValue();
	}
	
	public static MultiValueSet<String> parseParams(String mapText)
	{
		MultiValueSet<String> result = new MultiValueSet<>();
		char[] chs = mapText.toCharArray();
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
	
	public static void sendDebugMessage(Player player, String message)
	{
		if(!player.isGM())
		{
			return;
		}
		player.sendMessage(message);
	}
	
	public static void sendSystemMail(Player receiver, String title, String body, Map<Integer, Long> items)
	{
		if(receiver == null || !receiver.isOnline())
		{
			return;
		}
		if(title == null)
		{
			return;
		}
		if(items.keySet().size() > 8)
		{
			return;
		}
		Mail mail = new Mail();
		mail.setSenderId(1);
		mail.setSenderName("Admin");
		mail.setReceiverId(receiver.getObjectId());
		mail.setReceiverName(receiver.getName());
		mail.setTopic(title);
		mail.setBody(body);
		for(Map.Entry<Integer, Long> itm : items.entrySet())
		{
			ItemInstance item = ItemFunctions.createItem(itm.getKey());
			item.setLocation(ItemInstance.ItemLocation.MAIL);
			item.setCount(itm.getValue());
			item.setOwnerId(receiver.getObjectId());
			item.save();
			mail.addAttachment(item);
		}
		mail.setType(Mail.SenderType.NEWS_INFORMER);
		mail.setUnread(true);
		mail.setExpireTime(2592000 + (int) (System.currentTimeMillis() / 1000));
		mail.save();
		receiver.sendPacket(ExNoticePostArrived.STATIC_TRUE);
		receiver.sendPacket(Msg.THE_MAIL_HAS_ARRIVED);
	}
	
	public static final String truncateHtmlTagsSpaces(String srcHtml)
	{
		StringBuilder dstHtml = new StringBuilder(srcHtml.length());
		StringBuilder buff = new StringBuilder();
		boolean doBuff = false;
		int srcLen = srcHtml.length();
		for(int srcIdx = 0;srcIdx < srcLen;++srcIdx)
		{
			char srcCh = srcHtml.charAt(srcIdx);
			if(srcCh == '<')
			{
				doBuff = false;
				if(buff.length() > 0)
				{
					dstHtml.append(StringUtils.trim(buff.toString()));
					buff.setLength(0);
				}
			}
			if(!doBuff)
			{
				dstHtml.append(srcCh);
			}
			else
			{
				buff.append(srcCh);
			}
			if(srcCh != '>')
				continue;
			doBuff = true;
		}
		if(buff.length() > 0)
		{
			dstHtml.append(StringUtils.trim(buff.toString()));
			buff.setLength(0);
		}
		return dstHtml.toString();
	}
	
	public static Map<String, ScheduledFuture<?>> ScheduleTimeStarts(Runnable r, String[] times)
	{
		HashMap result = new HashMap();
		if(r == null || times == null || times.length == 0)
		{
			return result;
		}
		Calendar currentTime = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		for(String str_time : times)
		{
			String[] spl_time = str_time.trim().split(":");
			int hour = Integer.parseInt(spl_time[0].trim());
			int minute = Integer.parseInt(spl_time[1].trim());
			Calendar nextStartTime = Calendar.getInstance();
			nextStartTime.set(11, hour);
			nextStartTime.set(12, minute);
			if(nextStartTime.getTimeInMillis() < currentTime.getTimeInMillis())
			{
				nextStartTime.add(5, 1);
			}
			long millsLeft;
			if((millsLeft = nextStartTime.getTimeInMillis() - currentTime.getTimeInMillis()) <= 0)
				continue;
			result.put(sdf.format(nextStartTime.getTime()), ThreadPoolManager.getInstance().schedule(r, millsLeft));
		}
		return result;
	}
	
	public void show(String text, Player self)
	{
		show(text, self, getNpc());
	}
	
	public Player getSelf()
	{
		return self.get();
	}
	
	public NpcInstance getNpc()
	{
		return npc.get();
	}
}