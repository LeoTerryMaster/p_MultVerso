package l2.gameserver.model.instances;

import l2.commons.threading.RunnableImpl;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.cache.Msg;
import l2.gameserver.instancemanager.ServerVariables;
import l2.gameserver.model.Player;
import l2.gameserver.model.entity.MonsterRace;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.network.l2.s2c.DeleteObject;
import l2.gameserver.network.l2.s2c.L2GameServerPacket;
import l2.gameserver.network.l2.s2c.MonRaceInfo;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2.gameserver.network.l2.s2c.PlaySound;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.templates.npc.NpcTemplate;
import l2.gameserver.utils.ItemFunctions;
import l2.gameserver.utils.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class RaceManagerInstance extends NpcInstance
{
	public static final int LANES = 8;
	public static final int WINDOW_START = 0;
	protected static final int[][] codes;
	private static final long SECOND = 1000;
	private static final long MINUTE = 60000;
	private static final int ACCEPTING_BETS = 0;
	private static final int WAITING = 1;
	private static final int STARTING_RACE = 2;
	private static final int RACE_END = 3;
	protected static MonRaceInfo packet;
	protected static int[] cost;
	private static List<Race> history;
	private static Set<RaceManagerInstance> managers;
	private static int _raceNumber;
	private static int minutes;
	private static int state;
	private static boolean notInitialized;
	
	static
	{
		_raceNumber = 1;
		minutes = 5;
		state = 3;
		codes = new int[][] {{-1, 0}, {0, 15322}, {13765, -1}};
		notInitialized = true;
		cost = new int[] {100, 500, 1000, 5000, 10000, 20000, 50000, 100000};
	}
	
	public RaceManagerInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		if(notInitialized)
		{
			notInitialized = false;
			_raceNumber = ServerVariables.getInt("monster_race", 1);
			history = new ArrayList<>();
			managers = new CopyOnWriteArraySet<>();
			ThreadPoolManager s = ThreadPoolManager.getInstance();
			s.scheduleAtFixedRate(new Announcement(816), 0, 600000);
			s.scheduleAtFixedRate(new Announcement(817), 30000, 600000);
			s.scheduleAtFixedRate(new Announcement(816), 60000, 600000);
			s.scheduleAtFixedRate(new Announcement(817), 90000, 600000);
			s.scheduleAtFixedRate(new Announcement(818), 120000, 600000);
			s.scheduleAtFixedRate(new Announcement(818), 180000, 600000);
			s.scheduleAtFixedRate(new Announcement(818), 240000, 600000);
			s.scheduleAtFixedRate(new Announcement(818), 300000, 600000);
			s.scheduleAtFixedRate(new Announcement(819), 360000, 600000);
			s.scheduleAtFixedRate(new Announcement(819), 420000, 600000);
			s.scheduleAtFixedRate(new Announcement(820), 420000, 600000);
			s.scheduleAtFixedRate(new Announcement(820), 480000, 600000);
			s.scheduleAtFixedRate(new Announcement(821), 510000, 600000);
			s.scheduleAtFixedRate(new Announcement(822), 530000, 600000);
			s.scheduleAtFixedRate(new Announcement(823), 535000, 600000);
			s.scheduleAtFixedRate(new Announcement(823), 536000, 600000);
			s.scheduleAtFixedRate(new Announcement(823), 537000, 600000);
			s.scheduleAtFixedRate(new Announcement(823), 538000, 600000);
			s.scheduleAtFixedRate(new Announcement(823), 539000, 600000);
			s.scheduleAtFixedRate(new Announcement(824), 540000, 600000);
		}
		managers.add(this);
	}
	
	public void removeKnownPlayer(Player player)
	{
		for(int i = 0;i < 8;++i)
		{
			player.sendPacket(new DeleteObject(MonsterRace.getInstance().getMonsters()[i]));
		}
	}
	
	public void makeAnnouncement(int type)
	{
		SystemMessage sm = new SystemMessage(type);
		switch(type)
		{
			case 816:
			case 817:
			{
				if(state != 0)
				{
					state = 0;
					startRace();
				}
				sm.addNumber(_raceNumber);
				break;
			}
			case 818:
			case 820:
			case 823:
			{
				sm.addNumber(minutes);
				sm.addNumber(_raceNumber);
				--minutes;
				break;
			}
			case 819:
			{
				sm.addNumber(_raceNumber);
				state = 1;
				minutes = 2;
				break;
			}
			case 822:
			case 825:
			{
				sm.addNumber(_raceNumber);
				minutes = 5;
				break;
			}
			case 826:
			{
				state = 3;
				sm.addNumber(MonsterRace.getInstance().getFirstPlace() + 1);
				sm.addNumber(MonsterRace.getInstance().getSecondPlace() + 1);
			}
		}
		broadcast(sm);
		if(type == 824)
		{
			state = 2;
			startRace();
			minutes = 5;
		}
	}
	
	protected void broadcast(L2GameServerPacket pkt)
	{
		for(RaceManagerInstance manager : managers)
		{
			if(manager.isDead())
				continue;
			manager.broadcastPacketToOthers(pkt);
		}
	}
	
	public void sendMonsterInfo()
	{
		broadcast(packet);
	}
	
	private void startRace()
	{
		MonsterRace race = MonsterRace.getInstance();
		if(state == 2)
		{
			PlaySound SRace = new PlaySound("S_Race");
			broadcast(SRace);
			PlaySound SRace2 = new PlaySound(PlaySound.Type.SOUND, "ItemSound2.race_start", 1, 121209259, new Location(12125, 182487, -3559));
			broadcast(SRace2);
			packet = new MonRaceInfo(codes[1][0], codes[1][1], race.getMonsters(), race.getSpeeds());
			sendMonsterInfo();
			ThreadPoolManager.getInstance().schedule(new RunRace(), 5000);
		}
		else
		{
			race.newRace();
			race.newSpeeds();
			packet = new MonRaceInfo(codes[0][0], codes[0][1], race.getMonsters(), race.getSpeeds());
			sendMonsterInfo();
		}
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
		{
			return;
		}
		if(command.startsWith("BuyTicket") && state != 0)
		{
			player.sendPacket(Msg.MONSTER_RACE_TICKETS_ARE_NO_LONGER_AVAILABLE);
			command = "Chat 0";
		}
		if(command.startsWith("ShowOdds") && state == 0)
		{
			player.sendPacket(Msg.MONSTER_RACE_PAYOUT_INFORMATION_IS_NOT_AVAILABLE_WHILE_TICKETS_ARE_BEING_SOLD);
			command = "Chat 0";
		}
		if(command.startsWith("BuyTicket"))
		{
			int val = Integer.parseInt(command.substring(10));
			if(val == 0)
			{
				player.setRace(0, 0);
				player.setRace(1, 0);
			}
			if(val == 10 && player.getRace(0) == 0 || val == 20 && player.getRace(0) == 0 && player.getRace(1) == 0)
			{
				val = 0;
			}
			showBuyTicket(player, val);
		}
		else if(command.equals("ShowOdds"))
		{
			showOdds(player);
		}
		else if(command.equals("ShowInfo"))
		{
			showMonsterInfo(player);
		}
		else if(!command.equals("calculateWin") && !command.equals("viewHistory"))
		{
			super.onBypassFeedback(player, command);
		}
	}
	
	public void showOdds(Player player)
	{
		if(state == 0)
		{
			return;
		}
		int npcId = getTemplate().npcId;
		NpcHtmlMessage html = new NpcHtmlMessage(player, this);
		String filename = getHtmlPath(npcId, 5, player);
		html.setFile(filename);
		for(int i = 0;i < 8;++i)
		{
			int n = i + 1;
			String search = "Mob" + n;
			html.replace(search, MonsterRace.getInstance().getMonsters()[i].getTemplate().name);
		}
		html.replace("1race", String.valueOf(_raceNumber));
		player.sendPacket(html);
		player.sendActionFailed();
	}
	
	public void showMonsterInfo(Player player)
	{
		int npcId = getTemplate().npcId;
		NpcHtmlMessage html = new NpcHtmlMessage(player, this);
		String filename = getHtmlPath(npcId, 6, player);
		html.setFile(filename);
		for(int i = 0;i < 8;++i)
		{
			int n = i + 1;
			String search = "Mob" + n;
			html.replace(search, MonsterRace.getInstance().getMonsters()[i].getTemplate().name);
		}
		player.sendPacket(html);
		player.sendActionFailed();
	}
	
	public void showBuyTicket(Player player, int val)
	{
		if(state != 0)
		{
			return;
		}
		int npcId = getTemplate().npcId;
		NpcHtmlMessage html = new NpcHtmlMessage(player, this);
		if(val < 10)
		{
			String filename = getHtmlPath(npcId, 2, player);
			html.setFile(filename);
			String search;
			for(int i = 0;i < 8;++i)
			{
				int n = i + 1;
				search = "Mob" + n;
				html.replace(search, MonsterRace.getInstance().getMonsters()[i].getTemplate().name);
			}
			search = "No1";
			if(val == 0)
			{
				html.replace(search, "");
			}
			else
			{
				html.replace(search, "" + val);
				player.setRace(0, val);
			}
		}
		else if(val < 20)
		{
			if(player.getRace(0) == 0)
			{
				return;
			}
			String filename = getHtmlPath(npcId, 3, player);
			html.setFile(filename);
			html.replace("0place", "" + player.getRace(0));
			String search = "Mob1";
			String replace = MonsterRace.getInstance().getMonsters()[player.getRace(0) - 1].getTemplate().name;
			html.replace(search, replace);
			search = "0adena";
			if(val == 10)
			{
				html.replace(search, "");
			}
			else
			{
				html.replace(search, "" + cost[val - 11]);
				player.setRace(1, val - 10);
			}
		}
		else if(val == 20)
		{
			if(player.getRace(0) == 0 || player.getRace(1) == 0)
			{
				return;
			}
			String filename = getHtmlPath(npcId, 4, player);
			html.setFile(filename);
			html.replace("0place", "" + player.getRace(0));
			String search = "Mob1";
			String replace = MonsterRace.getInstance().getMonsters()[player.getRace(0) - 1].getTemplate().name;
			html.replace(search, replace);
			search = "0adena";
			int price = cost[player.getRace(1) - 1];
			html.replace(search, "" + price);
			search = "0tax";
			int tax = 0;
			html.replace(search, "" + tax);
			search = "0total";
			int total = price + tax;
			html.replace(search, "" + total);
		}
		else
		{
			if(player.getRace(0) == 0 || player.getRace(1) == 0)
			{
				return;
			}
			if(player.getAdena() < (long) cost[player.getRace(1) - 1])
			{
				player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
				return;
			}
			int ticket = player.getRace(0);
			int priceId = player.getRace(1);
			player.setRace(0, 0);
			player.setRace(1, 0);
			player.reduceAdena(cost[priceId - 1], true);
			SystemMessage sm = new SystemMessage(371);
			sm.addNumber(_raceNumber);
			sm.addItemName(4443);
			player.sendPacket(sm);
			ItemInstance item = ItemFunctions.createItem(4443);
			item.setEnchantLevel(_raceNumber);
			item.setBlessed(ticket);
			item.setDamaged(cost[priceId - 1] / 100);
			player.getInventory().addItem(item);
			return;
		}
		html.replace("1race", String.valueOf(_raceNumber));
		player.sendPacket(html);
		player.sendActionFailed();
	}
	
	public MonRaceInfo getPacket()
	{
		return packet;
	}
	
	class RunEnd extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			makeAnnouncement(826);
			makeAnnouncement(825);
			_raceNumber++;
			ServerVariables.set("monster_race", _raceNumber);
			for(int i = 0;i < 8;++i)
			{
				broadcast(new DeleteObject(MonsterRace.getInstance().getMonsters()[i]));
			}
		}
	}
	
	class RunRace extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			packet = new MonRaceInfo(codes[2][0], codes[2][1], MonsterRace.getInstance().getMonsters(), MonsterRace.getInstance().getSpeeds());
			sendMonsterInfo();
			ThreadPoolManager.getInstance().schedule(new RunEnd(), 30000);
		}
	}
	
	public class Race
	{
		private final Info[] info;
		
		public Race(Info[] info)
		{
			this.info = info;
		}
		
		public Info getLaneInfo(int lane)
		{
			return info[lane];
		}
		
		public class Info
		{
			private final int id;
			private final int place;
			private final int odds;
			private final int payout;
			
			public Info(int id, int place, int odds, int payout)
			{
				this.id = id;
				this.place = place;
				this.odds = odds;
				this.payout = payout;
			}
			
			public int getId()
			{
				return id;
			}
			
			public int getOdds()
			{
				return odds;
			}
			
			public int getPayout()
			{
				return payout;
			}
			
			public int getPlace()
			{
				return place;
			}
		}
	}
	
	class Announcement extends RunnableImpl
	{
		private final int type;
		
		public Announcement(int type)
		{
			this.type = type;
		}
		
		@Override
		public void runImpl() throws Exception
		{
			makeAnnouncement(type);
		}
	}
}