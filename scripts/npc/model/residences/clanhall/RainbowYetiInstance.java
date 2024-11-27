package npc.model.residences.clanhall;

import l2.commons.threading.RunnableImpl;
import l2.commons.util.Rnd;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.model.GameObject;
import l2.gameserver.model.Player;
import l2.gameserver.model.World;
import l2.gameserver.model.entity.events.impl.ClanHallMiniGameEvent;
import l2.gameserver.model.entity.events.objects.CMGSiegeClanObject;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.network.l2.components.NpcString;
import l2.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2.gameserver.network.l2.s2c.SystemMessage2;
import l2.gameserver.templates.npc.NpcTemplate;
import l2.gameserver.utils.ItemFunctions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

public class RainbowYetiInstance extends NpcInstance
{
	private static final int ItemA = 8035;
	private static final int ItemB = 8036;
	private static final int ItemC = 8037;
	private static final int ItemD = 8038;
	private static final int ItemE = 8039;
	private static final int ItemF = 8040;
	private static final int ItemG = 8041;
	private static final int ItemH = 8042;
	private static final int ItemI = 8043;
	private static final int ItemK = 8045;
	private static final int ItemL = 8046;
	private static final int ItemN = 8047;
	private static final int ItemO = 8048;
	private static final int ItemP = 8049;
	private static final int ItemR = 8050;
	private static final int ItemS = 8051;
	private static final int ItemT = 8052;
	private static final int ItemU = 8053;
	private static final int ItemW = 8054;
	private static final int ItemY = 8055;
	private static final Word[] WORLD_LIST = new Word[8];
	
	static
	{
		WORLD_LIST[0] = new RainbowYetiInstance.Word("BABYDUCK", new int[][] {{8036, 2}, {8035, 1}, {8055, 1}, {8038, 1}, {8053, 1}, {8037, 1}, {8045, 1}});
		WORLD_LIST[1] = new RainbowYetiInstance.Word("ALBATROS", new int[][] {{8035, 2}, {8046, 1}, {8036, 1}, {8052, 1}, {8050, 1}, {8048, 1}, {8051, 1}});
		WORLD_LIST[2] = new RainbowYetiInstance.Word("PELICAN", new int[][] {{8049, 1}, {8039, 1}, {8046, 1}, {8043, 1}, {8037, 1}, {8035, 1}, {8047, 1}});
		WORLD_LIST[3] = new RainbowYetiInstance.Word("KINGFISHER", new int[][] {{8045, 1}, {8043, 1}, {8047, 1}, {8041, 1}, {8040, 1}, {8043, 1}, {8051, 1}, {8042, 1}, {8039, 1}, {8050, 1}});
		WORLD_LIST[4] = new RainbowYetiInstance.Word("CYGNUS", new int[][] {{8037, 1}, {8055, 1}, {8041, 1}, {8047, 1}, {8053, 1}, {8051, 1}});
		WORLD_LIST[5] = new RainbowYetiInstance.Word("TRITON", new int[][] {{8052, 2}, {8050, 1}, {8043, 1}, {8047, 1}});
		WORLD_LIST[6] = new RainbowYetiInstance.Word("RAINBOW", new int[][] {{8050, 1}, {8035, 1}, {8043, 1}, {8047, 1}, {8036, 1}, {8048, 1}, {8054, 1}});
		WORLD_LIST[7] = new RainbowYetiInstance.Word("SPRING", new int[][] {{8051, 1}, {8049, 1}, {8050, 1}, {8043, 1}, {8047, 1}, {8041, 1}});
	}
	
	private final List<GameObject> _mobs = new ArrayList<>();
	private int _generated = -1;
	private Future<?> _task;
	
	public RainbowYetiInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		_hasRandomWalk = false;
	}
	
	@Override
	public void onSpawn()
	{
		super.onSpawn();
		ClanHallMiniGameEvent event = getEvent(ClanHallMiniGameEvent.class);
		if(event == null)
		{
			return;
		}
		List<Player> around = World.getAroundPlayers(this, 750, 100);
		for(Player player : around)
		{
			CMGSiegeClanObject siegeClanObject = event.getSiegeClan("attackers", player.getClan());
			if(siegeClanObject != null && siegeClanObject.getPlayers().contains(player.getObjectId()))
				continue;
			player.teleToLocation(event.getResidence().getOtherRestartPoint());
		}
		_task = ThreadPoolManager.getInstance().scheduleAtFixedRate(new GenerateTask(), 10000, 300000);
	}
	
	@Override
	public void onDelete()
	{
		super.onDelete();
		if(_task != null)
		{
			_task.cancel(false);
			_task = null;
		}
		for(GameObject object : _mobs)
		{
			object.deleteMe();
		}
		_mobs.clear();
	}
	
	public void teleportFromArena()
	{
		ClanHallMiniGameEvent event = getEvent(ClanHallMiniGameEvent.class);
		if(event == null)
		{
			return;
		}
		List<Player> around = World.getAroundPlayers(this, 750, 100);
		for(Player player : around)
		{
			player.teleToLocation(event.getResidence().getOtherRestartPoint());
		}
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
		{
			return;
		}
		if(command.equalsIgnoreCase("get"))
		{
			NpcHtmlMessage msg = new NpcHtmlMessage(player, this);
			boolean has = true;
			if(_generated == -1)
			{
				has = false;
			}
			else
			{
				Word word = WORLD_LIST[_generated];
				for(int[] itemInfo : word.getItems())
				{
					if(player.getInventory().getCountOf(itemInfo[0]) >= (long) itemInfo[1])
						continue;
					has = false;
				}
				if(has)
				{
					for(int[] itemInfo : word.getItems())
					{
						if(player.consumeItem(itemInfo[0], (long) itemInfo[1]))
							continue;
						return;
					}
					int rnd = Rnd.get(100);
					if(_generated >= 0 && _generated <= 5)
					{
						if(rnd < 70)
						{
							addItem(player, 8030);
						}
						else if(rnd < 80)
						{
							addItem(player, 8031);
						}
						else if(rnd < 90)
						{
							addItem(player, 8032);
						}
						else
						{
							addItem(player, 8033);
						}
					}
					else if(rnd < 10)
					{
						addItem(player, 8030);
					}
					else if(rnd < 40)
					{
						addItem(player, 8031);
					}
					else if(rnd < 70)
					{
						addItem(player, 8032);
					}
					else
					{
						addItem(player, 8033);
					}
				}
			}
			if(!has)
			{
				msg.setFile("residence2/clanhall/watering_manager002.htm");
			}
			else
			{
				msg.setFile("residence2/clanhall/watering_manager004.htm");
			}
			player.sendPacket(msg);
		}
		else if(command.equalsIgnoreCase("see"))
		{
			NpcHtmlMessage msg = new NpcHtmlMessage(player, this);
			msg.setFile("residence2/clanhall/watering_manager005.htm");
			if(_generated == -1)
			{
				msg.replaceNpcString("%word%", NpcString.UNDECIDED);
			}
			else
			{
				msg.replace("%word%", WORLD_LIST[_generated].getName());
			}
			player.sendPacket(msg);
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
	
	private void addItem(Player player, int itemId)
	{
		ClanHallMiniGameEvent event = getEvent(ClanHallMiniGameEvent.class);
		if(event == null)
		{
			return;
		}
		ItemInstance item = ItemFunctions.createItem(itemId);
		item.addEvent(event);
		player.getInventory().addItem(item);
		player.sendPacket(SystemMessage2.obtainItems(item));
	}
	
	@Override
	public void showChatWindow(Player player, int val, Object... arg)
	{
		showChatWindow(player, "residence2/clanhall/watering_manager001.htm");
	}
	
	public void addMob(GameObject object)
	{
		_mobs.add(object);
	}
	
	private static class Word
	{
		private final String _name;
		private final int[][] _items;
		
		public Word(String name, int[]... items)
		{
			_name = name;
			_items = items;
		}
		
		public String getName()
		{
			return _name;
		}
		
		public int[][] getItems()
		{
			return _items;
		}
	}
	
	private class GenerateTask extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			_generated = Rnd.get(WORLD_LIST.length);
			Word word = WORLD_LIST[_generated];
			List<Player> around = World.getAroundPlayers(RainbowYetiInstance.this, 750, 100);
			ExShowScreenMessage msg = new ExShowScreenMessage(word.getName(), 5000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, true);
			for(Player player : around)
			{
				player.sendPacket(msg);
			}
		}
	}
}