package instances;

import events.GvG.GvG;
import gnu.trove.TIntObjectHashMap;
import l2.commons.lang.reference.HardReference;
import l2.commons.lang.reference.HardReferences;
import l2.commons.listener.Listener;
import l2.commons.threading.RunnableImpl;
import l2.gameserver.Config;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.instancemanager.ReflectionManager;
import l2.gameserver.listener.actor.OnDeathListener;
import l2.gameserver.listener.actor.player.OnPlayerPartyLeaveListener;
import l2.gameserver.listener.actor.player.OnTeleportListener;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Party;
import l2.gameserver.model.Player;
import l2.gameserver.model.Zone;
import l2.gameserver.model.entity.Reflection;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2.gameserver.network.l2.s2c.L2GameServerPacket;
import l2.gameserver.network.l2.s2c.Revive;
import l2.gameserver.scripts.Functions;
import l2.gameserver.utils.Location;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;

public class GvGInstance extends Reflection
{
	private static final int BOX_ID = 25656;
	private static final int BOSS_ID = 25655;
	private static final int SCORE_BOX = 20;
	private static final int SCORE_BOSS = 100;
	private static final int SCORE_KILL = 5;
	private static final int SCORE_DEATH = 3;
	private final int eventTime = 1200;
	private final long bossSpawnTime = 600000;
	private final List<HardReference<Player>> bothTeams = new CopyOnWriteArrayList<>();
	private final TIntObjectHashMap<MutableInt> score = new TIntObjectHashMap();
	private final DeathListener _deathListener;
	private final TeleportListener _teleportListener;
	private final PlayerPartyLeaveListener _playerPartyLeaveListener;
	private boolean active;
	private Party team1;
	private Party team2;
	private int team1Score;
	private int team2Score;
	private long startTime;
	private ScheduledFuture<?> _bossSpawnTask;
	private ScheduledFuture<?> _countDownTask;
	private ScheduledFuture<?> _battleEndTask;
	private Zone zonebattle;
	private Zone zonepvp;
	private Zone zonepeace1;
	private Zone peace1;
	private Zone zonepeace2;
	private Zone peace2;
	
	public GvGInstance()
	{
		_deathListener = new DeathListener();
		_teleportListener = new TeleportListener();
		_playerPartyLeaveListener = new PlayerPartyLeaveListener();
	}
	
	public void setTeam1(Party party1)
	{
		team1 = party1;
	}
	
	public void setTeam2(Party party2)
	{
		team2 = party2;
	}
	
	public void start()
	{
		zonepvp = getZone("[gvg_battle_zone]");
		peace1 = getZone("[gvg_1_peace]");
		peace2 = getZone("[gvg_2_peace]");
		Location[] boxes = {new Location(78568, 92408, -2440, 0), new Location(78664, 92440, -2440, 0), new Location(78760, 92488, -2440, 0), new Location(78872, 92264, -2440, 0), new Location(78696, 92168, -2440, 0), new Location(79624, 89736, -2440, 0), new Location(79576, 89830, -2440, 0), new Location(79534, 89917, -2440, 0), new Location(79743, 90028, -2440, 0)};
		for(int i = 0;i < boxes.length;++i)
		{
			addSpawnWithoutRespawn(25656, boxes[i], 0);
		}
		addSpawnWithoutRespawn(35423, new Location(79928, 88328, -2880), 0);
		addSpawnWithoutRespawn(35426, new Location(77016, 93080, -2880), 0);
		_bossSpawnTask = ThreadPoolManager.getInstance().schedule(new BossSpawn(), bossSpawnTime);
		_countDownTask = ThreadPoolManager.getInstance().schedule(new CountingDown(), (long) (eventTime - 1) * 1000);
		_battleEndTask = ThreadPoolManager.getInstance().schedule(new BattleEnd(), (long) (eventTime - 6) * 1000);
		for(Player member : team1.getPartyMembers())
		{
			bothTeams.add(member.getRef());
			member.addListener(_deathListener);
			member.addListener(_teleportListener);
			member.addListener(_playerPartyLeaveListener);
		}
		for(Player member : team2.getPartyMembers())
		{
			bothTeams.add(member.getRef());
			member.addListener(_deathListener);
			member.addListener(_teleportListener);
			member.addListener(_playerPartyLeaveListener);
		}
		startTime = System.currentTimeMillis() + (long) eventTime * 1000;
		for(Player tm : HardReferences.unwrap(bothTeams))
		{
			score.put(tm.getObjectId(), new MutableInt());
			tm.setCurrentCp((double) tm.getMaxCp());
			tm.setCurrentHp((double) tm.getMaxHp(), false);
			tm.setCurrentMp((double) tm.getMaxMp());
			tm.sendActionFailed();
		}
		active = true;
	}
	
	private void broadCastPacketToBothTeams(L2GameServerPacket packet)
	{
		for(Player tm : HardReferences.unwrap(bothTeams))
		{
			tm.sendPacket(packet);
		}
	}
	
	private boolean isActive()
	{
		return active;
	}
	
	private boolean isRedTeam(Player player)
	{
		return team2.containsMember(player);
	}
	
	private void end()
	{
		active = false;
		startCollapseTimer(60000);
		paralyzePlayers();
		ThreadPoolManager.getInstance().schedule(new Finish(), 55000);
		if(_bossSpawnTask != null)
		{
			_bossSpawnTask.cancel(false);
			_bossSpawnTask = null;
		}
		if(_countDownTask != null)
		{
			_countDownTask.cancel(false);
			_countDownTask = null;
		}
		if(_battleEndTask != null)
		{
			_battleEndTask.cancel(false);
			_battleEndTask = null;
		}
		boolean isRedWinner = getRedScore() >= getBlueScore();
		reward(isRedWinner ? team2 : team1);
		GvG.updateWinner(isRedWinner ? team2.getPartyLeader() : team1.getPartyLeader());
		zonepvp.setActive(false);
		peace1.setActive(false);
		peace2.setActive(false);
	}
	
	private void reward(Party party)
	{
		for(Player member : party.getPartyMembers())
		{
			String msg = new CustomMessage("scripts.event.gvg.youpartywin", member, new Object[0]).toString();
			member.sendMessage(msg);
			Functions.addItem(member, Config.GVG_REWARD_ID, Config.GVG_REWARD_AMOUNT);
		}
	}
	
	private synchronized void changeScore(int teamId, int toAdd, int toSub, boolean subbing, boolean affectAnotherTeam, Player player)
	{
		int timeLeft = (int) ((startTime - System.currentTimeMillis()) / 1000);
		if(teamId == 1)
		{
			if(subbing)
			{
				team1Score -= toSub;
				if(team1Score < 0)
				{
					team1Score = 0;
				}
				if(affectAnotherTeam)
				{
					team2Score += toAdd;
				}
			}
			else
			{
				team1Score += toAdd;
				if(affectAnotherTeam)
				{
					team2Score -= toSub;
					if(team2Score < 0)
					{
						team2Score = 0;
					}
				}
			}
		}
		else if(teamId == 2)
		{
			if(subbing)
			{
				team2Score -= toSub;
				if(team2Score < 0)
				{
					team2Score = 0;
				}
				if(affectAnotherTeam)
				{
					team1Score += toAdd;
				}
			}
			else
			{
				team2Score += toAdd;
				if(affectAnotherTeam)
				{
					team1Score -= toSub;
					if(team1Score < 0)
					{
						team1Score = 0;
					}
				}
			}
		}
	}
	
	private void addPlayerScore(Player player)
	{
		MutableInt points = score.get(player.getObjectId());
		points.increment();
	}
	
	public int getPlayerScore(Player player)
	{
		MutableInt points = score.get(player.getObjectId());
		return points.intValue();
	}
	
	public void paralyzePlayers()
	{
		for(Player tm : HardReferences.unwrap(bothTeams))
		{
			if(tm.isDead())
			{
				tm.setCurrentHp((double) tm.getMaxHp(), true);
				tm.broadcastPacket(new Revive(tm));
			}
			else
			{
				tm.setCurrentHp((double) tm.getMaxHp(), false);
			}
			tm.setCurrentMp((double) tm.getMaxMp());
			tm.setCurrentCp((double) tm.getMaxCp());
			tm.getEffectList().stopEffect(1411);
			tm.block();
		}
	}
	
	public void unParalyzePlayers()
	{
		for(Player tm : HardReferences.unwrap(bothTeams))
		{
			tm.unblock();
			removePlayer(tm, true);
		}
	}
	
	private void cleanUp()
	{
		team1 = null;
		team2 = null;
		bothTeams.clear();
		team1Score = 0;
		team2Score = 0;
		score.clear();
	}
	
	public void resurrectAtBase(Player player)
	{
		if(player.isDead())
		{
			player.setCurrentHp(0.7 * (double) player.getMaxHp(), true);
			player.broadcastPacket(new Revive(player));
		}
		Location pos = team1.containsMember(player) ? Location.findPointToStay(GvG.TEAM1_LOC, 0, 150, getGeoIndex()) : Location.findPointToStay(GvG.TEAM2_LOC, 0, 150, getGeoIndex());
		player.teleToLocation(pos, this);
	}
	
	private void removePlayer(Player player, boolean legalQuit)
	{
		bothTeams.remove(player.getRef());
		player.removeListener(_deathListener);
		player.removeListener(_teleportListener);
		player.removeListener(_playerPartyLeaveListener);
		player.leaveParty();
		if(!legalQuit)
		{
			player.teleToLocation(Location.findPointToStay(GvG.RETURN_LOC, 0, 150, ReflectionManager.DEFAULT.getGeoIndex()), 0);
		}
	}
	
	private void teamWithdraw(Party party)
	{
		Object player;
		if(party == team1)
		{
			for(Player player2 : team1.getPartyMembers())
			{
				removePlayer(player2, false);
			}
			player = team2.getPartyLeader();
			changeScore(2, 200, 0, false, false, (Player) player);
		}
		else
		{
			for(Player player2 : team2.getPartyMembers())
			{
				removePlayer(player2, false);
			}
			player = team1.getPartyLeader();
			changeScore(1, 200, 0, false, false, (Player) player);
		}
		for(Player tm : HardReferences.unwrap(bothTeams))
		{
			tm.sendPacket(new ExShowScreenMessage(new CustomMessage("scripts.event.gvg.partydispell", tm, new Object[0]).toString(), 4000, ExShowScreenMessage.ScreenMessageAlign.MIDDLE_CENTER, true));
		}
	}
	
	private int getBlueScore()
	{
		return team1Score;
	}
	
	private int getRedScore()
	{
		return team2Score;
	}
	
	@Override
	public NpcInstance addSpawnWithoutRespawn(int npcId, Location loc, int randomOffset)
	{
		NpcInstance npc = super.addSpawnWithoutRespawn(npcId, loc, randomOffset);
		npc.addListener((Listener) _deathListener);
		return npc;
	}
	
	private class PlayerPartyLeaveListener implements OnPlayerPartyLeaveListener
	{
		@Override
		public void onPartyLeave(Player player)
		{
			if(!isActive())
			{
				return;
			}
			Party party = player.getParty();
			if(party.getMemberCount() >= 3)
			{
				removePlayer(player, false);
				return;
			}
			teamWithdraw(party);
		}
	}
	
	private class TeleportListener implements OnTeleportListener
	{
		@Override
		public void onTeleport(Player player, int x, int y, int z, Reflection reflection)
		{
			if(zonepvp.checkIfInZone(x, y, z, reflection) || peace1.checkIfInZone(x, y, z, reflection) || peace2.checkIfInZone(x, y, z, reflection))
			{
				return;
			}
			removePlayer(player, false);
			player.sendMessage(new CustomMessage("scripts.event.gvg.expelled", player));
		}
	}
	
	public class Finish extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			unParalyzePlayers();
			cleanUp();
		}
	}
	
	public class BattleEnd extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			for(Player tm : HardReferences.unwrap(bothTeams))
			{
				tm.sendPacket(new ExShowScreenMessage(new CustomMessage("scripts.event.gvg.teleport1min", tm, new Object[0]).toString(), 4000, ExShowScreenMessage.ScreenMessageAlign.MIDDLE_CENTER, true));
			}
			end();
		}
	}
	
	public class CountingDown extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			for(Player tm : HardReferences.unwrap(bothTeams))
			{
				tm.sendPacket(new ExShowScreenMessage(new CustomMessage("scripts.event.gvg.1mintofinish", tm, new Object[0]).toString(), 4000, ExShowScreenMessage.ScreenMessageAlign.MIDDLE_CENTER, true));
			}
		}
	}
	
	public class BossSpawn extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			for(Player tm : HardReferences.unwrap(bothTeams))
			{
				tm.sendPacket(new ExShowScreenMessage(new CustomMessage("scripts.event.gvg.geraldguard", tm, new Object[0]).toString(), 5000, ExShowScreenMessage.ScreenMessageAlign.MIDDLE_CENTER, true));
			}
			addSpawnWithoutRespawn(25655, new Location(79128, 91000, -2880, 4836), 0);
			openDoor(22200004);
			openDoor(22200005);
			openDoor(22200006);
			openDoor(22200007);
		}
	}
	
	private class DeathListener implements OnDeathListener
	{
		@Override
		public void onDeath(Creature self, Creature killer)
		{
			if(!isActive())
			{
				return;
			}
			if(self.getReflection() != killer.getReflection() || self.getReflection() != GvGInstance.this)
			{
				return;
			}
			if(self.isPlayer() && killer.isPlayable())
			{
				if(team1.containsMember(self.getPlayer()) && team2.containsMember(killer.getPlayer()))
				{
					addPlayerScore(killer.getPlayer());
					changeScore(1, 5, 3, true, true, killer.getPlayer());
				}
				else if(team2.containsMember(self.getPlayer()) && team1.containsMember(killer.getPlayer()))
				{
					addPlayerScore(killer.getPlayer());
					changeScore(2, 5, 3, true, true, killer.getPlayer());
				}
				resurrectAtBase(self.getPlayer());
			}
			else if(self.isPlayer() && !killer.isPlayable())
			{
				resurrectAtBase(self.getPlayer());
			}
			else if(self.isNpc() && killer.isPlayable())
			{
				if(self.getNpcId() == 25656)
				{
					if(team1.containsMember(killer.getPlayer()))
					{
						changeScore(1, 20, 0, false, false, killer.getPlayer());
					}
					else if(team2.containsMember(killer.getPlayer()))
					{
						changeScore(2, 20, 0, false, false, killer.getPlayer());
					}
				}
				else if(self.getNpcId() == 25655)
				{
					if(team1.containsMember(killer.getPlayer()))
					{
						changeScore(1, 100, 0, false, false, killer.getPlayer());
					}
					else if(team2.containsMember(killer.getPlayer()))
					{
						changeScore(2, 100, 0, false, false, killer.getPlayer());
					}
					broadCastPacketToBothTeams(new ExShowScreenMessage("Treasure guard Gerald died at the hand " + killer.getName(), 5000, ExShowScreenMessage.ScreenMessageAlign.MIDDLE_CENTER, true));
					end();
				}
			}
		}
	}
}