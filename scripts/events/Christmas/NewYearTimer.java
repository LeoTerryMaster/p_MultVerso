package events.Christmas;

import l2.commons.threading.RunnableImpl;
import l2.gameserver.Announcements;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.instancemanager.ServerVariables;
import l2.gameserver.model.GameObjectsStorage;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.network.l2.s2c.MagicSkillUse;
import l2.gameserver.scripts.ScriptFile;
import l2.gameserver.tables.SkillTable;

import java.util.Calendar;

public class NewYearTimer implements ScriptFile
{
	private static NewYearTimer instance;
	
	public NewYearTimer()
	{
		if(instance != null)
		{
			return;
		}
		instance = this;
		if(!isActive())
		{
			return;
		}
		Calendar c = Calendar.getInstance();
		c.set(1, Calendar.getInstance().get(1));
		c.set(2, 0);
		c.set(5, 1);
		c.set(11, 0);
		c.set(12, 0);
		c.set(13, 0);
		c.set(14, 0);
		while(getDelay(c) < 0)
		{
			c.set(1, c.get(1) + 1);
		}
		ThreadPoolManager.getInstance().schedule(new NewYearAnnouncer("С Новым, " + c.get(1) + ", Годом!!!"), getDelay(c));
		c.add(13, -1);
		ThreadPoolManager.getInstance().schedule(new NewYearAnnouncer("1"), getDelay(c));
		c.add(13, -1);
		ThreadPoolManager.getInstance().schedule(new NewYearAnnouncer("2"), getDelay(c));
		c.add(13, -1);
		ThreadPoolManager.getInstance().schedule(new NewYearAnnouncer("3"), getDelay(c));
		c.add(13, -1);
		ThreadPoolManager.getInstance().schedule(new NewYearAnnouncer("4"), getDelay(c));
		c.add(13, -1);
		ThreadPoolManager.getInstance().schedule(new NewYearAnnouncer("5"), getDelay(c));
	}
	
	public static NewYearTimer getInstance()
	{
		if(instance == null)
		{
			new NewYearTimer();
		}
		return instance;
	}
	
	private static boolean isActive()
	{
		return ServerVariables.getString("Christmas", "off").equalsIgnoreCase("on");
	}
	
	private long getDelay(Calendar c)
	{
		return c.getTime().getTime() - System.currentTimeMillis();
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
	
	private class NewYearAnnouncer extends RunnableImpl
	{
		private final String message;
		
		private NewYearAnnouncer(String message)
		{
			this.message = message;
		}
		
		@Override
		public void runImpl() throws Exception
		{
			Announcements.getInstance().announceToAll(message);
			if(message.length() == 1)
			{
				return;
			}
			for(Player player : GameObjectsStorage.getAllPlayersForIterate())
			{
				Skill skill = SkillTable.getInstance().getInfo(3266, 1);
				MagicSkillUse msu = new MagicSkillUse(player, player, 3266, 1, skill.getHitTime(), 0);
				player.broadcastPacket(msu);
			}
			instance = null;
			new NewYearTimer();
		}
	}
}