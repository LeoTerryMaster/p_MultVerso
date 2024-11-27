package services;

import l2.gameserver.Announcements;
import l2.gameserver.Config;
import l2.gameserver.listener.actor.player.OnPlayerEnterListener;
import l2.gameserver.model.Player;
import l2.gameserver.model.actor.listener.CharListenerList;
import l2.gameserver.model.entity.oly.OlyController;
import l2.gameserver.scripts.ScriptFile;

public class OlyInform implements ScriptFile
{
	private static final long SECONDS_IN_MINUTE = 60;
	private static final long SECONDS_IN_HOUR = 3600;
	private static final long SECONDS_IN_A_DAY = 86400;
	
	public static void informOlyEnd(Player player)
	{
		long olyEndTime;
		long now = System.currentTimeMillis();
		if(now / 1000 > (olyEndTime = OlyController.getInstance().getSeasonEndTime()))
		{
			return;
		}
		long reamainingSec = olyEndTime - now / 1000;
		int remainingDays = (int) (reamainingSec / 86400);
		reamainingSec %= 86400;
		int remainingHours = (int) (reamainingSec / 3600);
		reamainingSec %= 3600;
		int remainingMinutes = (int) (reamainingSec / 60);
		Announcements.getInstance().announceToPlayerByCustomMessage(player, "l2p.gameserver.model.entity.OlympiadGame.EndSeasonTime", new String[] {String.valueOf(remainingDays), String.valueOf(remainingHours), String.valueOf(remainingMinutes)});
	}
	
	@Override
	public void onLoad()
	{
		if(Config.ANNOUNCE_OLYMPIAD_GAME_END)
		{
			CharListenerList.addGlobal(new OnPlayerEnterListener()
			{
				
				@Override
				public void onPlayerEnter(Player player)
				{
					informOlyEnd(player);
				}
			});
		}
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