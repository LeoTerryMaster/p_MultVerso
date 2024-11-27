package l2.gameserver.ai;

import l2.gameserver.model.Player;
import l2.gameserver.model.World;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.instances.RaceManagerInstance;
import l2.gameserver.network.l2.s2c.MonRaceInfo;

import java.util.ArrayList;
import java.util.List;

public class RaceManager extends DefaultAI
{
	private boolean thinking;
	private List<Player> _knownPlayers = new ArrayList<>();
	
	public RaceManager(NpcInstance actor)
	{
		super(actor);
		AI_TASK_ATTACK_DELAY = 5000;
	}
	
	@Override
	public void runImpl() throws Exception
	{
		onEvtThink();
	}
	
	@Override
	protected void onEvtThink()
	{
		RaceManagerInstance actor = getActor();
		if(actor == null)
		{
			return;
		}
		MonRaceInfo packet = actor.getPacket();
		if(packet == null)
		{
			return;
		}
		RaceManager raceManager = this;
		synchronized(raceManager)
		{
			if(thinking)
			{
				return;
			}
			thinking = true;
		}
		try
		{
			ArrayList<Player> newPlayers = new ArrayList<>();
			for(Player player : World.getAroundPlayers(actor, 1200, 200))
			{
				if(player == null)
					continue;
				newPlayers.add(player);
				if(!_knownPlayers.contains(player))
				{
					player.sendPacket(packet);
				}
				_knownPlayers.remove(player);
			}
			for(Player player : _knownPlayers)
			{
				actor.removeKnownPlayer(player);
			}
			_knownPlayers = newPlayers;
		}
		finally
		{
			thinking = false;
		}
	}
	
	@Override
	public RaceManagerInstance getActor()
	{
		return (RaceManagerInstance) super.getActor();
	}
}