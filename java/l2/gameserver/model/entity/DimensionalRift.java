package l2.gameserver.model.entity;

import l2.commons.threading.RunnableImpl;
import l2.commons.util.Rnd;
import l2.gameserver.Config;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.data.xml.holder.InstantZoneHolder;
import l2.gameserver.instancemanager.DimensionalRiftManager;
import l2.gameserver.model.GameObject;
import l2.gameserver.model.Party;
import l2.gameserver.model.Player;
import l2.gameserver.model.SimpleSpawner;
import l2.gameserver.model.Spawner;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.templates.InstantZone;
import l2.gameserver.utils.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

public class DimensionalRift extends Reflection
{
	protected static final long seconds_5 = 5000;
	protected static final int MILLISECONDS_IN_MINUTE = 60000;
	protected int _roomType;
	protected List<Integer> _completedRooms = new ArrayList<>();
	protected int jumps_current;
	protected int _choosenRoom = -1;
	protected boolean _hasJumped;
	protected boolean isBossRoom;
	private Future<?> teleporterTask;
	private Future<?> spawnTask;
	private Future<?> killRiftTask;
	
	public DimensionalRift(Party party, int type, int room)
	{
		onCreate();
		startCollapseTimer(7200000);
		setName("Dimensional Rift");
		if(this instanceof DelusionChamber)
		{
			InstantZone iz = InstantZoneHolder.getInstance().getInstantZone(type + 120);
			setInstancedZone(iz);
			setName(iz.getName());
		}
		_roomType = type;
		setParty(party);
		if(!(this instanceof DelusionChamber))
		{
			party.setDimensionalRift(this);
		}
		party.setReflection(this);
		_choosenRoom = room;
		checkBossRoom(_choosenRoom);
		Location coords = getRoomCoord(_choosenRoom);
		setReturnLoc(party.getPartyLeader().getLoc());
		setTeleportLoc(coords);
		for(Player p : party.getPartyMembers())
		{
			p.setVar("backCoords", getReturnLoc().toXYZString(), -1);
			DimensionalRiftManager.teleToLocation(p, Location.findPointToStay(coords, 50, 100, getGeoIndex()), this);
			p.setReflection(this);
		}
		createSpawnTimer(_choosenRoom);
		createTeleporterTimer();
	}
	
	public int getType()
	{
		return _roomType;
	}
	
	public int getCurrentRoom()
	{
		return _choosenRoom;
	}
	
	protected void createTeleporterTimer()
	{
		if(teleporterTask != null)
		{
			teleporterTask.cancel(false);
			teleporterTask = null;
		}
		teleporterTask = ThreadPoolManager.getInstance().schedule(new RunnableImpl()
		{
			
			@Override
			public void runImpl() throws Exception
			{
				if(jumps_current < getMaxJumps() && getPlayersInside(true) > 0)
				{
					++jumps_current;
					teleportToNextRoom();
					createTeleporterTimer();
				}
				else
				{
					createNewKillRiftTimer();
				}
			}
		}, calcTimeToNextJump());
	}
	
	public void createSpawnTimer(int room)
	{
		if(spawnTask != null)
		{
			spawnTask.cancel(false);
			spawnTask = null;
		}
		DimensionalRiftManager.DimensionalRiftRoom riftRoom = DimensionalRiftManager.getInstance().getRoom(_roomType, room);
		spawnTask = ThreadPoolManager.getInstance().schedule(new RunnableImpl()
		{
			
			@Override
			public void runImpl() throws Exception
			{
				for(SimpleSpawner s : riftRoom.getSpawns())
				{
					SimpleSpawner sp = s.clone();
					sp.setReflection(DimensionalRift.this);
					addSpawn(sp);
					if(!isBossRoom)
					{
						sp.startRespawn();
					}
					for(int i = 0;i < sp.getAmount();++i)
					{
						sp.doSpawn(true);
					}
				}
				addSpawnWithoutRespawn(getManagerId(), riftRoom.getTeleportCoords(), 0);
			}
		}, Config.RIFT_SPAWN_DELAY);
	}
	
	public synchronized void createNewKillRiftTimer()
	{
		if(killRiftTask != null)
		{
			killRiftTask.cancel(false);
			killRiftTask = null;
		}
		killRiftTask = ThreadPoolManager.getInstance().schedule(new RunnableImpl()
		{
			
			@Override
			public void runImpl() throws Exception
			{
				if(isCollapseStarted())
				{
					return;
				}
				for(Player p : getParty().getPartyMembers())
				{
					if(p == null || p.getReflection() != DimensionalRift.this)
						continue;
					DimensionalRiftManager.getInstance().teleportToWaitingRoom(p);
				}
				collapse();
			}
		}, 100);
	}
	
	public void partyMemberInvited()
	{
		createNewKillRiftTimer();
	}
	
	public void partyMemberExited(Player player)
	{
		if(getParty().getMemberCount() < Config.RIFT_MIN_PARTY_SIZE || getParty().getMemberCount() == 1 || getPlayersInside(true) == 0)
		{
			createNewKillRiftTimer();
		}
	}
	
	public void manualTeleport(Player player, NpcInstance npc)
	{
		if(!(player.isInParty() && player.getParty().isInReflection() && player.getParty().getReflection() instanceof DimensionalRift))
		{
			return;
		}
		if(!player.getParty().isLeader(player))
		{
			DimensionalRiftManager.getInstance().showHtmlFile(player, "rift/NotPartyLeader.htm", npc);
			return;
		}
		if(!isBossRoom)
		{
			if(_hasJumped)
			{
				DimensionalRiftManager.getInstance().showHtmlFile(player, "rift/AlreadyTeleported.htm", npc);
				return;
			}
		}
		else
		{
			manualExitRift(player, npc);
			return;
		}
		_hasJumped = true;
		teleportToNextRoom();
	}
	
	public void manualExitRift(Player player, NpcInstance npc)
	{
		if(!player.isInParty() || !player.getParty().isInDimensionalRift())
		{
			return;
		}
		if(!player.getParty().isLeader(player))
		{
			DimensionalRiftManager.getInstance().showHtmlFile(player, "rift/NotPartyLeader.htm", npc);
			return;
		}
		createNewKillRiftTimer();
	}
	
	protected void teleportToNextRoom()
	{
		_completedRooms.add(_choosenRoom);
		for(Spawner s : getSpawns())
		{
			s.deleteAll();
		}
		int size = DimensionalRiftManager.getInstance().getRooms(_roomType).size();
		if(getType() >= 5 && jumps_current == getMaxJumps())
		{
			_choosenRoom = 9;
		}
		else
		{
			ArrayList<Integer> notCompletedRooms = new ArrayList<>();
			for(int i = 1;i <= size;++i)
			{
				if(_completedRooms.contains(i))
					continue;
				notCompletedRooms.add(i);
			}
			_choosenRoom = notCompletedRooms.get(Rnd.get(notCompletedRooms.size()));
		}
		checkBossRoom(_choosenRoom);
		setTeleportLoc(getRoomCoord(_choosenRoom));
		for(Player p : getParty().getPartyMembers())
		{
			if(p.getReflection() != this)
				continue;
			DimensionalRiftManager.teleToLocation(p, Location.findPointToStay(getRoomCoord(_choosenRoom), 50, 100, getGeoIndex()), this);
		}
		createSpawnTimer(_choosenRoom);
	}
	
	@Override
	public void collapse()
	{
		if(isCollapseStarted())
		{
			return;
		}
		Future task = teleporterTask;
		if(task != null)
		{
			teleporterTask = null;
			task.cancel(false);
		}
		if((task = spawnTask) != null)
		{
			spawnTask = null;
			task.cancel(false);
		}
		if((task = killRiftTask) != null)
		{
			killRiftTask = null;
			task.cancel(false);
		}
		_completedRooms = null;
		Party party = getParty();
		if(party != null)
		{
			party.setDimensionalRift(null);
		}
		super.collapse();
	}
	
	protected long calcTimeToNextJump()
	{
		if(isBossRoom)
		{
			return 3600000;
		}
		return Config.RIFT_AUTO_JUMPS_TIME * 60000 + Rnd.get(Config.RIFT_AUTO_JUMPS_TIME_RAND);
	}
	
	public void memberDead(Player player)
	{
		if(getPlayersInside(true) == 0)
		{
			createNewKillRiftTimer();
		}
	}
	
	public void usedTeleport(Player player)
	{
		if(getPlayersInside(false) < Config.RIFT_MIN_PARTY_SIZE)
		{
			createNewKillRiftTimer();
		}
	}
	
	public void checkBossRoom(int room)
	{
		isBossRoom = DimensionalRiftManager.getInstance().getRoom(_roomType, room).isBossRoom();
	}
	
	public Location getRoomCoord(int room)
	{
		return DimensionalRiftManager.getInstance().getRoom(_roomType, room).getTeleportCoords();
	}
	
	public int getMaxJumps()
	{
		return Math.max(Math.min(Config.RIFT_MAX_JUMPS, 8), 1);
	}
	
	@Override
	public boolean canChampions()
	{
		return true;
	}
	
	@Override
	public String getName()
	{
		return "Dimensional Rift";
	}
	
	protected int getManagerId()
	{
		return 31865;
	}
	
	protected int getPlayersInside(boolean alive)
	{
		if(_playerCount == 0)
		{
			return 0;
		}
		int sum = 0;
		for(Player p : getPlayers())
		{
			if(alive && p.isDead())
				continue;
			++sum;
		}
		return sum;
	}
	
	@Override
	public void removeObject(GameObject o)
	{
		if(o.isPlayer() && _playerCount <= 1)
		{
			createNewKillRiftTimer();
		}
		super.removeObject(o);
	}
}