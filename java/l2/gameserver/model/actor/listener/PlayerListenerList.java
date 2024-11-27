package l2.gameserver.model.actor.listener;

import l2.commons.listener.Listener;
import l2.gameserver.listener.actor.player.OnGainExpSpListener;
import l2.gameserver.listener.actor.player.OnOlyCompetitionListener;
import l2.gameserver.listener.actor.player.OnPlayerEnterListener;
import l2.gameserver.listener.actor.player.OnPlayerExitListener;
import l2.gameserver.listener.actor.player.OnPlayerPartyInviteListener;
import l2.gameserver.listener.actor.player.OnPlayerPartyLeaveListener;
import l2.gameserver.listener.actor.player.OnPvpPkKillListener;
import l2.gameserver.listener.actor.player.OnQuestStateChangeListener;
import l2.gameserver.listener.actor.player.OnTeleportListener;
import l2.gameserver.model.Player;
import l2.gameserver.model.entity.Reflection;
import l2.gameserver.model.entity.oly.Competition;
import l2.gameserver.model.quest.QuestState;

public class PlayerListenerList extends CharListenerList
{
	public PlayerListenerList(Player actor)
	{
		super(actor);
	}
	
	@Override
	public Player getActor()
	{
		return (Player) actor;
	}
	
	public void onEnter()
	{
		if(!global.getListeners().isEmpty())
		{
			for(Listener listener : global.getListeners())
			{
				if(!OnPlayerEnterListener.class.isInstance(listener))
					continue;
				((OnPlayerEnterListener) listener).onPlayerEnter(getActor());
			}
		}
		if(!getListeners().isEmpty())
		{
			for(Listener listener : getListeners())
			{
				if(!OnPlayerEnterListener.class.isInstance(listener))
					continue;
				((OnPlayerEnterListener) listener).onPlayerEnter(getActor());
			}
		}
	}
	
	public void onExit()
	{
		if(!global.getListeners().isEmpty())
		{
			for(Listener listener : global.getListeners())
			{
				if(!OnPlayerExitListener.class.isInstance(listener))
					continue;
				((OnPlayerExitListener) listener).onPlayerExit(getActor());
			}
		}
		if(!getListeners().isEmpty())
		{
			for(Listener listener : getListeners())
			{
				if(!OnPlayerExitListener.class.isInstance(listener))
					continue;
				((OnPlayerExitListener) listener).onPlayerExit(getActor());
			}
		}
	}
	
	public void onTeleport(int x, int y, int z, Reflection reflection)
	{
		if(!global.getListeners().isEmpty())
		{
			for(Listener listener : global.getListeners())
			{
				if(!OnTeleportListener.class.isInstance(listener))
					continue;
				((OnTeleportListener) listener).onTeleport(getActor(), x, y, z, reflection);
			}
		}
		if(!getListeners().isEmpty())
		{
			for(Listener listener : getListeners())
			{
				if(!OnTeleportListener.class.isInstance(listener))
					continue;
				((OnTeleportListener) listener).onTeleport(getActor(), x, y, z, reflection);
			}
		}
	}
	
	public void onQuestStateChange(QuestState questState)
	{
		if(!global.getListeners().isEmpty())
		{
			for(Listener listener : global.getListeners())
			{
				if(!OnQuestStateChangeListener.class.isInstance(listener))
					continue;
				((OnQuestStateChangeListener) listener).onQuestStateChange(getActor(), questState);
			}
		}
		if(!getListeners().isEmpty())
		{
			for(Listener listener : getListeners())
			{
				if(!OnQuestStateChangeListener.class.isInstance(listener))
					continue;
				((OnQuestStateChangeListener) listener).onQuestStateChange(getActor(), questState);
			}
		}
	}
	
	public void onOlyCompetitionCompleted(Competition competition, boolean isWin)
	{
		if(!global.getListeners().isEmpty())
		{
			for(Listener listener : global.getListeners())
			{
				if(!OnOlyCompetitionListener.class.isInstance(listener))
					continue;
				((OnOlyCompetitionListener) listener).onOlyCompetitionCompleted(getActor(), competition, isWin);
			}
		}
		if(!getListeners().isEmpty())
		{
			for(Listener listener : getListeners())
			{
				if(!OnOlyCompetitionListener.class.isInstance(listener))
					continue;
				((OnOlyCompetitionListener) listener).onOlyCompetitionCompleted(getActor(), competition, isWin);
			}
		}
	}
	
	public void onGainExpSp(long exp, long sp)
	{
		if(!global.getListeners().isEmpty())
		{
			for(Listener listener : global.getListeners())
			{
				if(!OnGainExpSpListener.class.isInstance(listener))
					continue;
				((OnGainExpSpListener) listener).onGainExpSp(getActor(), exp, sp);
			}
		}
		if(!getListeners().isEmpty())
		{
			for(Listener listener : getListeners())
			{
				if(!OnGainExpSpListener.class.isInstance(listener))
					continue;
				((OnGainExpSpListener) listener).onGainExpSp(getActor(), exp, sp);
			}
		}
	}
	
	public void onPvpPkKill(Player victim, boolean isPk)
	{
		if(!global.getListeners().isEmpty())
		{
			for(Listener listener : global.getListeners())
			{
				if(!OnPvpPkKillListener.class.isInstance(listener))
					continue;
				((OnPvpPkKillListener) listener).onPvpPkKill(getActor(), victim, isPk);
			}
		}
		if(!getListeners().isEmpty())
		{
			for(Listener listener : getListeners())
			{
				if(!OnPvpPkKillListener.class.isInstance(listener))
					continue;
				((OnPvpPkKillListener) listener).onPvpPkKill(getActor(), victim, isPk);
			}
		}
	}
	
	public void onPartyInvite()
	{
		if(!global.getListeners().isEmpty())
		{
			for(Listener listener : global.getListeners())
			{
				if(!OnPlayerPartyInviteListener.class.isInstance(listener))
					continue;
				((OnPlayerPartyInviteListener) listener).onPartyInvite(getActor());
			}
		}
		if(!getListeners().isEmpty())
		{
			for(Listener listener : getListeners())
			{
				if(!OnPlayerPartyInviteListener.class.isInstance(listener))
					continue;
				((OnPlayerPartyInviteListener) listener).onPartyInvite(getActor());
			}
		}
	}
	
	public void onPartyLeave()
	{
		if(!global.getListeners().isEmpty())
		{
			for(Listener listener : global.getListeners())
			{
				if(!OnPlayerPartyLeaveListener.class.isInstance(listener))
					continue;
				((OnPlayerPartyLeaveListener) listener).onPartyLeave(getActor());
			}
		}
		if(!getListeners().isEmpty())
		{
			for(Listener listener : getListeners())
			{
				if(!OnPlayerPartyLeaveListener.class.isInstance(listener))
					continue;
				((OnPlayerPartyLeaveListener) listener).onPartyLeave(getActor());
			}
		}
	}
}