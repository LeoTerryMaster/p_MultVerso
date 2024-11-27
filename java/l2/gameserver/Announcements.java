package l2.gameserver;

import l2.commons.threading.RunnableImpl;
import l2.gameserver.model.Creature;
import l2.gameserver.model.GameObjectsStorage;
import l2.gameserver.model.Player;
import l2.gameserver.network.l2.components.ChatType;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.network.l2.s2c.Say2;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.utils.MapUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.Future;

public class Announcements
{
	private static final Logger _log = LoggerFactory.getLogger(Announcements.class);
	private static final Announcements _instance = new Announcements();
	private final List<Announce> _announcements = new ArrayList<>();
	
	private Announcements()
	{
		loadAnnouncements();
	}
	
	public static final Announcements getInstance()
	{
		return _instance;
	}
	
	public static void shout(Creature activeChar, String text, ChatType type)
	{
		Say2 cs = new Say2(activeChar.getObjectId(), type, activeChar.getName(), text);
		int rx = MapUtils.regionX(activeChar);
		int ry = MapUtils.regionY(activeChar);
		int offset = Config.SHOUT_OFFSET;
		for(Player player : GameObjectsStorage.getAllPlayersForIterate())
		{
			if(player == activeChar || activeChar.getReflection() != player.getReflection())
				continue;
			int tx = MapUtils.regionX(player);
			int ty = MapUtils.regionY(player);
			if((tx < rx - offset || tx > rx + offset || ty < ry - offset || ty > ry + offset) && !activeChar.isInRangeZ(player, (long) Config.CHAT_RANGE))
				continue;
			player.sendPacket(cs);
		}
		activeChar.sendPacket(cs);
	}
	
	public List<Announce> getAnnouncements()
	{
		return _announcements;
	}
	
	public void loadAnnouncements()
	{
		_announcements.clear();
		try
		{
			List<String> lines = Arrays.asList(FileUtils.readFileToString(new File("config/announcements.txt"), "UTF-8").split("\n"));
			for(String line : lines)
			{
				StringTokenizer token = new StringTokenizer(line, "\t");
				if(token.countTokens() > 1)
				{
					addAnnouncement(Integer.parseInt(token.nextToken()), token.nextToken(), false);
					continue;
				}
				addAnnouncement(0, line, false);
			}
		}
		catch(Exception e)
		{
			_log.error("Error while loading config/announcements.txt!");
		}
	}
	
	public void showAnnouncements(Player activeChar)
	{
		for(Announce announce : _announcements)
		{
			announce.showAnnounce(activeChar);
		}
	}
	
	public void addAnnouncement(int val, String text, boolean save)
	{
		Announce announce = new Announce(val, text);
		announce.start();
		_announcements.add(announce);
		if(save)
		{
			saveToDisk();
		}
	}
	
	public void delAnnouncement(int line)
	{
		Announce announce = _announcements.remove(line);
		if(announce != null)
		{
			announce.stop();
		}
		saveToDisk();
	}
	
	private void saveToDisk()
	{
		try
		{
			File f = new File("config/announcements.txt");
			FileWriter writer = new FileWriter(f, false);
			for(Announce announce : _announcements)
			{
				writer.write("" + announce.getTime() + "\t" + announce.getAnnounce() + "\n");
			}
			writer.close();
		}
		catch(Exception e)
		{
			_log.error("Error while saving config/announcements.txt!", e);
		}
	}
	
	public void announceToAll(String text)
	{
		announceToAll(text, ChatType.ANNOUNCEMENT);
	}
	
	public void announceToAll(String text, ChatType type)
	{
		Say2 cs = new Say2(0, type, "", text);
		for(Player player : GameObjectsStorage.getAllPlayersForIterate())
		{
			player.sendPacket(cs);
		}
	}
	
	public void announceByCustomMessage(String address, String[] replacements)
	{
		for(Player player : GameObjectsStorage.getAllPlayersForIterate())
		{
			announceToPlayerByCustomMessage(player, address, replacements);
		}
	}
	
	public void announceByCustomMessage(String address, String[] replacements, ChatType type)
	{
		for(Player player : GameObjectsStorage.getAllPlayersForIterate())
		{
			announceToPlayerByCustomMessage(player, address, replacements, type);
		}
	}
	
	public void announceToPlayerByCustomMessage(Player player, String address, String[] replacements)
	{
		CustomMessage cm = new CustomMessage(address, player);
		if(replacements != null)
		{
			for(String s : replacements)
			{
				cm.addString(s);
			}
		}
		player.sendPacket(new Say2(0, ChatType.ANNOUNCEMENT, "", cm.toString()));
	}
	
	public void announceToPlayerByCustomMessage(Player player, String address, String[] replacements, ChatType type)
	{
		CustomMessage cm = new CustomMessage(address, player);
		if(replacements != null)
		{
			for(String s : replacements)
			{
				cm.addString(s);
			}
		}
		player.sendPacket(new Say2(0, type, "", cm.toString()));
	}
	
	public void announceToAll(SystemMessage sm)
	{
		for(Player player : GameObjectsStorage.getAllPlayersForIterate())
		{
			player.sendPacket(sm);
		}
	}
	
	public class Announce extends RunnableImpl
	{
		private final int _time;
		private final String _announce;
		private Future<?> _task;
		
		public Announce(int t, String announce)
		{
			_time = t;
			_announce = announce;
		}
		
		@Override
		public void runImpl() throws Exception
		{
			announceToAll(_announce);
		}
		
		public void showAnnounce(Player player)
		{
			Say2 cs = new Say2(0, ChatType.ANNOUNCEMENT, player.getName(), _announce);
			player.sendPacket(cs);
		}
		
		public void start()
		{
			if(_time > 0)
			{
				_task = ThreadPoolManager.getInstance().scheduleAtFixedRate(this, (long) _time * 1000, (long) _time * 1000);
			}
		}
		
		public void stop()
		{
			if(_task != null)
			{
				_task.cancel(false);
				_task = null;
			}
		}
		
		public int getTime()
		{
			return _time;
		}
		
		public String getAnnounce()
		{
			return _announce;
		}
	}
}