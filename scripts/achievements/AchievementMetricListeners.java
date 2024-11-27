package achievements;

import l2.commons.listener.Listener;
import l2.gameserver.listener.CharListener;
import l2.gameserver.listener.PlayerListener;
import l2.gameserver.listener.actor.OnDeathListener;
import l2.gameserver.listener.actor.OnKillListener;
import l2.gameserver.listener.actor.player.OnGainExpSpListener;
import l2.gameserver.listener.actor.player.OnOlyCompetitionListener;
import l2.gameserver.listener.actor.player.OnPlayerEnterListener;
import l2.gameserver.listener.actor.player.OnPvpPkKillListener;
import l2.gameserver.listener.actor.player.OnQuestStateChangeListener;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.actor.listener.CharListenerList;
import l2.gameserver.model.entity.oly.Competition;
import l2.gameserver.model.instances.RaidBossInstance;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.utils.Location;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

public class AchievementMetricListeners
{
	private static final AchievementMetricListeners INSTANCE = new AchievementMetricListeners();
	private final List<? extends Listener<?>> _listenersInstances = Arrays.asList(new AchievementOnPlayerEnter(), new AchievementOnKill(), new AchievementOnDeath(), new AchievementOnPvPPkKill(), new AchievementOnGainExpSp(), new AchievementOnOlyCompetitionCompleted(), new AchievementOnQuestStateChange());
	
	private AchievementMetricListeners()
	{
	}
	
	public static AchievementMetricListeners getInstance()
	{
		return INSTANCE;
	}
	
	public void metricEvent(Player player, AchievementMetricType eventType, Object... args)
	{
		if(!Achievements.getInstance().isEnabled())
		{
			return;
		}
		List<AchievementInfo> achievementInfos = Achievements.getInstance().getAchievementInfosByMetric(eventType);
		if(achievementInfos == null || achievementInfos.isEmpty())
		{
			return;
		}
		for(AchievementInfo ie : achievementInfos)
		{
			Achievement achievement = new Achievement(ie, player);
			if(achievement.isCompleted())
				continue;
			achievement.onMetricEvent(args);
		}
	}
	
	public void init()
	{
		for(Listener listener : _listenersInstances)
		{
			if(listener instanceof PlayerListener)
			{
				CharListenerList.addGlobal(listener);
				continue;
			}
			if(listener instanceof CharListener)
			{
				CharListenerList.addGlobal(listener);
				continue;
			}
			throw new IllegalStateException("Unknown listener " + listener.getClass());
		}
	}
	
	public void done()
	{
		for(Listener listener : _listenersInstances)
		{
			if(listener instanceof PlayerListener)
			{
				CharListenerList.removeGlobal(listener);
				continue;
			}
			if(!(listener instanceof CharListener))
				continue;
			CharListenerList.removeGlobal(listener);
		}
	}
	
	public static class AchievementOnQuestStateChange implements OnQuestStateChangeListener
	{
		@Override
		public void onQuestStateChange(Player player, QuestState questState)
		{
			getInstance().metricEvent(player, AchievementMetricType.QUEST_STATE, questState);
		}
	}
	
	public static class AchievementOnOlyCompetitionCompleted implements OnOlyCompetitionListener
	{
		@Override
		public void onOlyCompetitionCompleted(Player player, Competition competition, boolean isWin)
		{
			getInstance().metricEvent(player, AchievementMetricType.OLYMPIAD, competition, isWin);
		}
	}
	
	public static class AchievementOnGainExpSp implements OnGainExpSpListener
	{
		@Override
		public void onGainExpSp(Player player, long exp, long sp)
		{
			getInstance().metricEvent(player, AchievementMetricType.LEVEL, player.getLevel());
		}
	}
	
	public static class AchievementOnPvPPkKill implements OnPvpPkKillListener
	{
		@Override
		public void onPvpPkKill(Player killer, Player victim, boolean isPk)
		{
			if(!isPk)
			{
				getInstance().metricEvent(killer, AchievementMetricType.PVP_KILL, killer.getPvpKills(), victim);
			}
		}
	}
	
	public static class AchievementOnDeath implements OnDeathListener
	{
		@Override
		public void onDeath(Creature actor, Creature killer)
		{
			if(actor.isPlayer())
			{
				getInstance().metricEvent(actor.getPlayer(), AchievementMetricType.DEATH, killer);
			}
		}
	}
	
	public static class AchievementOnKill implements OnKillListener
	{
		@Override
		public void onKill(Creature actor, Creature victim)
		{
			if(actor == null || !actor.isPlayer() || victim == null)
			{
				return;
			}
			if(victim.isNpc())
			{
				getInstance().metricEvent(actor.getPlayer(), AchievementMetricType.NPC_KILL, victim);
				if(victim instanceof RaidBossInstance)
				{
					RaidBossInstance raidBoss = (RaidBossInstance) victim;
					Location raidBossLoc = raidBoss.getLoc();
					ArrayList<Creature> raidParticipants = new ArrayList(raidBoss.getAggroList().getCharMap().keySet());
					LinkedHashSet<Player> raidPlayerParticipants = new LinkedHashSet<>();
					for(Creature creature : raidParticipants)
					{
						if(creature == null || raidBossLoc.distance3D(creature.getLoc()) > 1500.0 || !(creature instanceof Player))
							continue;
						raidPlayerParticipants.add((Player) creature);
					}
					for(Player raidParticipant : raidPlayerParticipants)
					{
						getInstance().metricEvent(raidParticipant, AchievementMetricType.RAID_PARTICIPATION, victim);
					}
				}
			}
		}
		
		@Override
		public boolean ignorePetOrSummon()
		{
			return true;
		}
	}
	
	public static class AchievementOnPlayerEnter implements OnPlayerEnterListener
	{
		@Override
		public void onPlayerEnter(Player player)
		{
			getInstance().metricEvent(player, AchievementMetricType.LOGIN, player);
		}
	}
}