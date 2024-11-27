package achievements;

import l2.commons.time.cron.SchedulingPattern;
import l2.gameserver.model.AggroList;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.entity.oly.Competition;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.instances.RaidBossInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

public abstract class AchievementCondition
{
	public static AchievementCondition makeCond(String condName, String condValue)
	{
		try
		{
			for(Class clazz : AchievementCondition.class.getClasses())
			{
				if(AchievementCondition.class.isAssignableFrom(clazz))
				{
					AchievementCondition.AchievementConditionName conditionName = (AchievementCondition.AchievementConditionName) clazz.getAnnotation(AchievementCondition.AchievementConditionName.class);
					if(conditionName != null && condName.equalsIgnoreCase(conditionName.value()))
					{
						Constructor<? extends AchievementCondition> ctor = clazz.getConstructor(String.class);
						if(ctor != null)
						{
							return ctor.newInstance(condValue);
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			throw new RuntimeException("Can't make condition " + condName + "(" + condValue + ")", e);
		}
		return null;
	}
	
	public abstract boolean test(Player selfPlayer, Object... args);
	
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface AchievementConditionName
	{
		String value();
	}
	
	@AchievementConditionName("self_min_online_time")
	public static class AchSelfMinOnlineTime extends AchievementCondition
	{
		private final long _minOnlineTime;
		
		public AchSelfMinOnlineTime(String value)
		{
			_minOnlineTime = Long.parseLong(value);
		}
		
		@Override
		public boolean test(Player selfPlayer, Object... args)
		{
			long onlineTimeBegin = selfPlayer.getOnlineBeginTime();
			long now = System.currentTimeMillis();
			if(onlineTimeBegin > 0 && onlineTimeBegin <= now)
			{
				return (now - onlineTimeBegin) / 1000 >= _minOnlineTime;
			}
			return false;
		}
	}
	
	@AchievementConditionName("self_quest_state_is")
	public static class AchSelfQuestStateIs extends AchievementCondition
	{
		private final int _questStateId;
		
		public AchSelfQuestStateIs(String value)
		{
			_questStateId = Quest.getStateId(value);
		}
		
		@Override
		public boolean test(Player selfPlayer, Object... args)
		{
			for(Object obj : args)
			{
				if(obj == null)
					continue;
				QuestState questState = null;
				if(obj instanceof QuestState)
				{
					questState = (QuestState) obj;
				}
				if(obj instanceof Quest)
				{
					questState = selfPlayer.getQuestState((Quest) obj);
				}
				if(questState == null)
					continue;
				return questState.getState() == _questStateId;
			}
			return false;
		}
	}
	
	@AchievementConditionName("self_quest_id_in")
	public static class AchSelfQuestId extends AchievementCondition
	{
		private final Set<Integer> _questIds = new HashSet<>();
		
		public AchSelfQuestId(String value)
		{
			StringTokenizer tok = new StringTokenizer(value, ";,");
			while(tok.hasMoreTokens())
			{
				_questIds.add(Integer.parseInt(tok.nextToken()));
			}
		}
		
		@Override
		public boolean test(Player selfPlayer, Object... args)
		{
			for(Object obj : args)
			{
				if(obj == null)
					continue;
				Quest quest = null;
				if(obj instanceof Quest)
				{
					quest = (Quest) obj;
				}
				if(obj instanceof QuestState)
				{
					quest = ((QuestState) obj).getQuest();
				}
				if(quest == null)
					continue;
				return _questIds.contains(quest.getQuestIntId());
			}
			return false;
		}
	}
	
	@AchievementConditionName("target_npc_min_damage_to_me")
	public static class AchTargetAggroMinDamageToMe extends AchievementCondition
	{
		private final int _minDamage;
		
		public AchTargetAggroMinDamageToMe(String value)
		{
			_minDamage = Integer.parseInt(value);
		}
		
		@Override
		public boolean test(Player selfPlayer, Object... args)
		{
			for(Object obj : args)
			{
				if(obj == null || !(obj instanceof NpcInstance))
					continue;
				NpcInstance targetNpc = (NpcInstance) obj;
				AggroList.AggroInfo aggroInfo = targetNpc.getAggroList().get(selfPlayer);
				if(aggroInfo == null)
				{
					return false;
				}
				return aggroInfo.damage >= _minDamage;
			}
			return false;
		}
	}
	
	@AchievementConditionName("target_npc_min_hate_to_me")
	public static class AchTargetAggroMinHateToMe extends AchievementCondition
	{
		private final int _minHate;
		
		public AchTargetAggroMinHateToMe(String value)
		{
			_minHate = Integer.parseInt(value);
		}
		
		@Override
		public boolean test(Player selfPlayer, Object... args)
		{
			for(Object obj : args)
			{
				if(obj == null || !(obj instanceof NpcInstance))
					continue;
				NpcInstance targetNpc = (NpcInstance) obj;
				AggroList.AggroInfo aggroInfo = targetNpc.getAggroList().get(selfPlayer);
				if(aggroInfo == null)
				{
					return false;
				}
				return aggroInfo.hate >= _minHate;
			}
			return false;
		}
	}
	
	@AchievementConditionName("self_target_max_lvl_diff")
	public static class AchSelfTargetMaxLvlDiff extends AchievementCondition
	{
		private final int _maxLvlDiff;
		
		public AchSelfTargetMaxLvlDiff(String value)
		{
			_maxLvlDiff = Integer.parseInt(value);
		}
		
		@Override
		public boolean test(Player selfPlayer, Object... args)
		{
			for(Object obj : args)
			{
				if(obj == null || !(obj instanceof Creature))
					continue;
				Creature targetCreature = (Creature) obj;
				return Math.abs(targetCreature.getLevel() - selfPlayer.getLevel()) <= _maxLvlDiff;
			}
			return false;
		}
	}
	
	@AchievementConditionName("self_is_hero")
	public static class AchSelfIsHero extends AchievementCondition
	{
		private final boolean _isHero;
		
		public AchSelfIsHero(String value)
		{
			_isHero = Boolean.parseBoolean(value);
		}
		
		@Override
		public boolean test(Player selfPlayer, Object... args)
		{
			return selfPlayer.isHero() == _isHero;
		}
	}
	
	@AchievementConditionName("self_is_noble")
	public static class AchSelfIsNoble extends AchievementCondition
	{
		private final boolean _isNoble;
		
		public AchSelfIsNoble(String value)
		{
			_isNoble = Boolean.parseBoolean(value);
		}
		
		@Override
		public boolean test(Player selfPlayer, Object... args)
		{
			return selfPlayer.isNoble() == _isNoble;
		}
	}
	
	@AchievementConditionName("self_level_in_range")
	public static class AchSelfLevelInRange extends AchievementCondition
	{
		private final int _minLevel;
		private final int _maxLevel;
		
		public AchSelfLevelInRange(String value)
		{
			int delimIdx = value.indexOf(45);
			_minLevel = Integer.parseInt(value.substring(0, delimIdx).trim());
			_maxLevel = Integer.parseInt(value.substring(delimIdx + 1).trim());
		}
		
		@Override
		public boolean test(Player selfPlayer, Object... args)
		{
			return selfPlayer.getLevel() >= _minLevel && selfPlayer.getLevel() < _maxLevel;
		}
	}
	
	@AchievementConditionName("self_is_clan_leader")
	public static class AchSelfIsClanLeader extends AchievementCondition
	{
		private final boolean _isClanleader;
		
		public AchSelfIsClanLeader(String value)
		{
			_isClanleader = Boolean.parseBoolean(value);
		}
		
		@Override
		public boolean test(Player selfPlayer, Object... args)
		{
			return selfPlayer.isClanLeader() == _isClanleader;
		}
	}
	
	@AchievementConditionName("self_is_subclass_active")
	public static class AchSelfIsSubclassActive extends AchievementCondition
	{
		private final boolean _isSubclassActive;
		
		public AchSelfIsSubclassActive(String value)
		{
			_isSubclassActive = Boolean.parseBoolean(value);
		}
		
		@Override
		public boolean test(Player selfPlayer, Object... args)
		{
			return selfPlayer.isSubClassActive() == _isSubclassActive;
		}
	}
	
	@AchievementConditionName("is_target_player_class_id_in")
	public static class AchTargetPlayerIsActiveClass extends AchievementCondition
	{
		private final Set<Integer> _classIds = new HashSet<>();
		
		public AchTargetPlayerIsActiveClass(String value)
		{
			StringTokenizer st = new StringTokenizer(value, ";,");
			while(st.hasMoreTokens())
			{
				_classIds.add(Integer.parseInt(st.nextToken()));
			}
		}
		
		@Override
		public boolean test(Player selfPlayer, Object... args)
		{
			for(Object obj : args)
			{
				if(obj == null || !(obj instanceof Player))
					continue;
				Player targetPlayer = (Player) obj;
				return _classIds.contains(targetPlayer.getClassId());
			}
			return false;
		}
	}
	
	@AchievementConditionName("self_is_class_id_in")
	public static class AchSelfIsActiveClass extends AchievementCondition
	{
		private final Set<Integer> _classIds = new HashSet<>();
		
		public AchSelfIsActiveClass(String value)
		{
			StringTokenizer st = new StringTokenizer(value, ";,");
			while(st.hasMoreTokens())
			{
				_classIds.add(Integer.parseInt(st.nextToken()));
			}
		}
		
		@Override
		public boolean test(Player selfPlayer, Object... args)
		{
			return _classIds.contains(selfPlayer.getClassId());
		}
	}
	
	@AchievementConditionName("is_oly_winner")
	public static class AchIsOlyWinner extends AchievementCondition
	{
		private final boolean _isWinner;
		
		public AchIsOlyWinner(String value)
		{
			_isWinner = Boolean.parseBoolean(value);
		}
		
		@Override
		public boolean test(Player selfPlayer, Object... args)
		{
			boolean haveComp = false;
			for(Object obj : args)
			{
				if(!(obj instanceof Competition))
					continue;
				haveComp = true;
			}
			if(haveComp)
			{
				for(Object obj : args)
				{
					if(!(obj instanceof Boolean))
						continue;
					return _isWinner == (Boolean) obj;
				}
			}
			return false;
		}
	}
	
	@AchievementConditionName("now_match_cron")
	public static class AchievementConditionIsNowMatchCron extends AchievementCondition
	{
		private final SchedulingPattern _pattern;
		
		public AchievementConditionIsNowMatchCron(String pattern)
		{
			_pattern = new SchedulingPattern(pattern);
		}
		
		@Override
		public boolean test(Player selfPlayer, Object... args)
		{
			return _pattern.match(System.currentTimeMillis());
		}
	}
	
	@AchievementConditionName("npc_id_in_list")
	public static class AchievementConditionNpcIdInList extends AchievementCondition
	{
		private final Set<Integer> _npcIds = new HashSet<>();
		
		public AchievementConditionNpcIdInList(String value)
		{
			StringTokenizer tok = new StringTokenizer(value, ";,");
			while(tok.hasMoreTokens())
			{
				_npcIds.add(Integer.parseInt(tok.nextToken()));
			}
		}
		
		@Override
		public boolean test(Player selfPlayer, Object... args)
		{
			for(Object obj : args)
			{
				if(obj == null || !(obj instanceof NpcInstance))
					continue;
				NpcInstance npc = (NpcInstance) obj;
				if(!_npcIds.contains(npc.getNpcId()))
					continue;
				return true;
			}
			return false;
		}
	}
	
	@AchievementConditionName("is_karma_player")
	public static class AchievementConditionIsKarmaPlayer extends AchievementCondition
	{
		private final boolean _value;
		
		public AchievementConditionIsKarmaPlayer(String value)
		{
			_value = Boolean.parseBoolean(value);
		}
		
		@Override
		public boolean test(Player selfPlayer, Object... args)
		{
			for(Object obj : args)
			{
				if(obj == null || !(_value ? obj instanceof Player && ((Player) obj).getKarma() > 0 : obj instanceof Player && ((Player) obj).getKarma() == 0))
					continue;
				return true;
			}
			return false;
		}
	}
	
	@AchievementConditionName("is_raid_boss")
	public static class AchievementConditionHaveRaid extends AchievementCondition
	{
		private final boolean _value;
		
		public AchievementConditionHaveRaid(String value)
		{
			_value = Boolean.parseBoolean(value);
		}
		
		@Override
		public boolean test(Player selfPlayer, Object... args)
		{
			for(Object obj : args)
			{
				if(obj == null || !(_value == obj instanceof RaidBossInstance))
					continue;
				return true;
			}
			return false;
		}
	}
}