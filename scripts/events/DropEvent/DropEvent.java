package events.DropEvent;

import l2.commons.lang.ArrayUtils;
import l2.commons.util.Rnd;
import l2.gameserver.Config;
import l2.gameserver.listener.actor.OnDeathListener;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.actor.listener.CharListenerList;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.scripts.Functions;
import l2.gameserver.scripts.ScriptFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DropEvent extends Functions implements ScriptFile, OnDeathListener
{
	private static final Logger LOG = LoggerFactory.getLogger(DropEvent.class);
	private static final String EVENT_NAME = "DropEvent";
	private static final DropEvent INSTANCE = new DropEvent();
	private static final int MAX_LEVEL = 99;
	private static boolean ACTIVE;
	private static DropEventItem[][] ITEMS_BY_NPC_LVL;
	private static Map<Integer, List<DropEventItem>> ITEMS_BY_NPC_ID;
	
	private static boolean isActive()
	{
		return IsActive("DropEvent");
	}
	
	private static DropEventItem parseItemInfo(String itemInfo)
	{
		Pattern p = Pattern.compile("(\\d+)[:-](\\d+)\\((\\d+)\\)(<(\\d+)-(\\d+)>)?(\\[([\\d,]+)\\])?");
		Matcher m = p.matcher(itemInfo);
		if(m.matches())
		{
			int itemId = Integer.parseInt(m.group(1));
			long itemCount = Long.parseLong(m.group(2));
			double chance = Double.parseDouble(m.group(3));
			int minLvl = m.group(5) != null ? Integer.parseInt(m.group(5)) : 1;
			int maxLvl = m.group(5) != null ? Integer.parseInt(m.group(6)) : 99;
			ArrayList<Integer> npcIdsList = new ArrayList<>();
			if(m.group(8) != null)
			{
				String[] npcIdTexts = m.group(8).split(",");
				minLvl = 0;
				maxLvl = -1;
				for(String npcIdText : npcIdTexts)
				{
					npcIdsList.add(Integer.parseInt(npcIdText));
				}
			}
			int[] npcIds = new int[npcIdsList.size()];
			for(int npcIdIdx = 0;npcIdIdx < npcIds.length;++npcIdIdx)
			{
				npcIds[npcIdIdx] = npcIdsList.get(npcIdIdx);
			}
			return new DropEventItem(itemId, itemCount, chance, npcIds, minLvl, maxLvl);
		}
		throw new RuntimeException("Can't parse drop event item \"" + itemInfo + "\"");
	}
	
	private static List<DropEventItem> parseDropEventItemsInfos(String itemsInfos)
	{
		ArrayList<DropEventItem> dropEventItems = new ArrayList<>();
		StringTokenizer itemsListTokenizer = new StringTokenizer(itemsInfos, ";");
		while(itemsListTokenizer.hasMoreTokens())
		{
			String itemInfoTextTok = itemsListTokenizer.nextToken();
			dropEventItems.add(parseItemInfo(itemInfoTextTok));
		}
		return dropEventItems;
	}
	
	private static void loadConfig()
	{
		ITEMS_BY_NPC_ID = new HashMap<>();
		ITEMS_BY_NPC_LVL = new DropEventItem[99][];
		if(isActive())
		{
			List<DropEventItem> dropEventItems = parseDropEventItemsInfos(Config.EVENT_DropEvent_Items);
			for(DropEventItem dropEventItem : dropEventItems)
			{
				for(int lvl = dropEventItem.getMinLvl();lvl <= dropEventItem.getMaxLvl();++lvl)
				{
					if(lvl <= 0)
						continue;
					DropEvent.DropEventItem[] byLvl = ITEMS_BY_NPC_LVL[lvl];
					ITEMS_BY_NPC_LVL[lvl] = (DropEventItem[]) ArrayUtils.add(byLvl, (Object) dropEventItem);
				}
				for(Integer npcId : dropEventItem.getNpcIds())
				{
					List<DropEventItem> items = ITEMS_BY_NPC_ID.get(npcId);
					if(items == null)
					{
						items = new ArrayList<>();
						ITEMS_BY_NPC_ID.put(npcId, items);
					}
					items.add(dropEventItem);
				}
			}
		}
	}
	
	@Override
	public void onDeath(Creature actor, Creature killer)
	{
		try
		{
			if(!Functions.SimpleCheckDrop(actor, killer))
			{
				return;
			}
			NpcInstance npc = (NpcInstance) actor;
			DropEventItem[] byLvl = ITEMS_BY_NPC_LVL[npc.getLevel()];
			List<DropEventItem> byId = ITEMS_BY_NPC_ID.get(npc.getNpcId());
			HashSet<DropEventItem> availableDropEventItems = new HashSet<>();
			if(byLvl != null)
			{
				for(DropEventItem byLvlItem : byLvl)
				{
					availableDropEventItems.add(byLvlItem);
				}
			}
			if(byId != null)
			{
				availableDropEventItems.addAll(byId);
			}
			for(DropEventItem dropEventItem : availableDropEventItems)
			{
				if(!Rnd.chance(dropEventItem.getChance()))
					continue;
				npc.dropItem(killer.getPlayer(), dropEventItem.getItemId(), dropEventItem.getItemCount());
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void startEvent()
	{
		Player player = getSelf();
		if(!player.getPlayerAccess().IsEventGm)
		{
			return;
		}
		if(SetActive("DropEvent", true))
		{
			if(!ACTIVE)
			{
				CharListenerList.addGlobal(INSTANCE);
			}
			player.sendMessage("Event 'DropEvent' started.");
		}
		else
		{
			player.sendMessage("Event 'DropEvent' already started.");
		}
		ACTIVE = true;
		show("admin/events/events.htm", player);
	}
	
	public void stopEvent()
	{
		Player player = getSelf();
		if(!player.getPlayerAccess().IsEventGm)
		{
			return;
		}
		if(SetActive("DropEvent", false))
		{
			if(ACTIVE)
			{
				CharListenerList.removeGlobal(INSTANCE);
			}
			System.out.println("Event: 'DropEvent' stopped.");
		}
		else
		{
			player.sendMessage("Event: 'DropEvent' not started.");
		}
		ACTIVE = false;
		show("admin/events/events.htm", player);
	}
	
	@Override
	public void onLoad()
	{
		loadConfig();
		if(isActive())
		{
			CharListenerList.addGlobal(INSTANCE);
			ACTIVE = true;
			LOG.info("Loaded Event: Drop Event [state: activated]");
		}
		else
		{
			LOG.info("Loaded Event: Drop Event [state: deactivated]");
		}
	}
	
	@Override
	public void onReload()
	{
		onShutdown();
		onLoad();
	}
	
	@Override
	public void onShutdown()
	{
		if(ACTIVE)
		{
			CharListenerList.removeGlobal(INSTANCE);
			ACTIVE = false;
		}
	}
	
	private static class DropEventItem
	{
		private final int _itemId;
		private final long _itemCount;
		private final double _chance;
		private final int[] _npcIds;
		private final int _minLvl;
		private final int _maxLvl;
		
		private DropEventItem(int itemId, long itemCount, double chance, int[] npcIds, int minLvl, int maxLvl)
		{
			Arrays.sort(npcIds);
			_itemId = itemId;
			_itemCount = itemCount;
			_chance = chance;
			_npcIds = npcIds;
			_minLvl = minLvl;
			_maxLvl = maxLvl;
		}
		
		public double getChance()
		{
			return _chance;
		}
		
		public long getItemCount()
		{
			return _itemCount;
		}
		
		public int getItemId()
		{
			return _itemId;
		}
		
		public int getMaxLvl()
		{
			return _maxLvl;
		}
		
		public int getMinLvl()
		{
			return _minLvl;
		}
		
		public int[] getNpcIds()
		{
			return _npcIds;
		}
	}
}