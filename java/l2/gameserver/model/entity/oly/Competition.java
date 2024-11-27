package l2.gameserver.model.entity.oly;

import gnu.trove.TIntIntHashMap;
import l2.gameserver.Config;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.cache.Msg;
import l2.gameserver.model.Effect;
import l2.gameserver.model.Party;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.Summon;
import l2.gameserver.model.base.TeamType;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.network.l2.s2c.*;
import l2.gameserver.skills.TimeStamp;
import l2.gameserver.skills.effects.EffectCubic;
import l2.gameserver.tables.SkillTable;
import l2.gameserver.utils.Location;

import java.util.Collection;
import java.util.concurrent.ScheduledFuture;

public class Competition
{
	private static final double RESTORE_MOD = 0.8;
	private final Stadium _stadium;
	private final CompetitionType _type;
	public Participant[] _participants;
	private long _start_time;
	private CompetitionState _state;
	private ScheduledFuture<?> _currentTask;
	
	public Competition(CompetitionType type, Stadium stadium)
	{
		_type = type;
		_stadium = stadium;
		_state = null;
		_start_time = 0;
	}
	
	private static int CalcPoints(int points)
	{
		return Math.max(1, (Math.min(50, points) - 1) / 5 + 1);
	}
	
	public static SystemMessage checkPlayer(Player player)
	{
		if(!player.isNoble())
		{
			return new SystemMessage(1501).addName(player);
		}
		if(player.isInDuel())
		{
			return new SystemMessage(1599);
		}
		if(player.getBaseClassId() != player.getClassId().getId() || player.getClassId().getLevel() < 4)
		{
			return new SystemMessage(1500).addName(player);
		}
		if((double) player.getInventoryLimit() * 0.8 <= (double) player.getInventory().getSize())
		{
			return new SystemMessage(1691).addName(player);
		}
		if(player.isCursedWeaponEquipped())
		{
			return new SystemMessage(1857).addName(player).addItemName(player.getCursedWeaponEquippedId());
		}
		if(NoblesController.getInstance().getPointsOf(player.getObjectId()) < 1)
		{
			return new SystemMessage(2941);
		}
		return null;
	}
	
	public CompetitionState getState()
	{
		return _state;
	}
	
	public void setState(CompetitionState state)
	{
		if(_state == CompetitionState.STAND_BY && state == CompetitionState.PLAYING)
		{
			_start_time = System.currentTimeMillis();
		}
		_state = state;
	}
	
	public CompetitionType getType()
	{
		return _type;
	}
	
	public Stadium getStadium()
	{
		return _stadium;
	}
	
	public void setPlayers(Participant[] participants)
	{
		for(Participant participant : _participants = participants)
		{
			for(Player player : participant.getPlayers())
			{
				player.setOlyParticipant(participant);
			}
		}
	}
	
	public void start()
	{
		for(Participant participant : _participants)
		{
			participant.OnStart();
		}
	}
	
	private void prepareParticipantsForReturn()
	{
		for(Participant part : _participants)
		{
			for(Player player : part.getPlayers())
			{
				try
				{
					if(player.getClan() != null)
					{
						player.getClan().enableSkills(player);
					}
					for(int restrictedSkillIdx = 0;restrictedSkillIdx < Config.OLY_RESTRICTED_SKILL_IDS.length;++restrictedSkillIdx)
					{
						Skill skill;
						int restrictedSkillId = Config.OLY_RESTRICTED_SKILL_IDS[restrictedSkillIdx];
						if(!player.isUnActiveSkill(restrictedSkillId) || (skill = player.getKnownSkill(restrictedSkillId)) == null)
							continue;
						player.removeUnActiveSkill(skill);
					}
					if(player.isDead())
					{
						player.broadcastPacket(new Revive(player));
						player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp(), true);
						player.setCurrentCp(player.getMaxCp());
					}
					else
					{
						player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp(), false);
						player.setCurrentCp(player.getMaxCp());
					}
					Collection<TimeStamp> reuse = player.getSkillReuses();
					for(TimeStamp ts : reuse)
					{
						Skill skill = SkillTable.getInstance().getInfo(ts.getId(), ts.getLevel());
						player.enableSkill(skill);
					}
					if(player.isHero())
					{
						HeroController.addSkills(player);
					}
					player.sendPacket(new ExOlympiadMode(0), new SkillList(player), new SkillCoolTime(player));
					player.updateStats();
					player.updateEffectIcons();
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
			}
		}
	}
	
	private void prepareParticipantsForCompetition()
	{
		for(Participant part : _participants)
		{
			for(Player player : part.getPlayers())
			{
				try
				{
					boolean update = false;
					for(Effect e : player.getEffectList().getAllFirstEffects())
					{
						if(e instanceof EffectCubic && e.getSkill().getTargetType() == Skill.SkillTargetType.TARGET_SELF)
							continue;
						e.exit();
						update = true;
					}
					Collection<TimeStamp> reuse = player.getSkillReuses();
					for(TimeStamp ts : reuse)
					{
						Skill skill = SkillTable.getInstance().getInfo(ts.getId(), ts.getLevel());
						player.enableSkill(skill);
						update = true;
					}
					if(update)
					{
						player.sendPacket(new SkillCoolTime(player));
						player.updateStats();
						player.updateEffectIcons();
					}
					if(Config.OLY_LIMIT_ENCHANT_STAT_LEVEL_ARMOR >= 0)
					{
						player.sendMessage(new CustomMessage("l2p.gameserver.model.entity.OlympiadGame.Competition.EnchantArmorLevelLimited", player, Config.OLY_LIMIT_ENCHANT_STAT_LEVEL_ARMOR));
					}
					if(Config.OLY_LIMIT_ENCHANT_STAT_LEVEL_WEAPON_PHYS >= 0)
					{
						player.sendMessage(new CustomMessage("l2p.gameserver.model.entity.OlympiadGame.Competition.EnchantWeaponPhysLevelLimited", player, Config.OLY_LIMIT_ENCHANT_STAT_LEVEL_WEAPON_PHYS));
					}
					if(Config.OLY_LIMIT_ENCHANT_STAT_LEVEL_WEAPON_MAGE >= 0)
					{
						player.sendMessage(new CustomMessage("l2p.gameserver.model.entity.OlympiadGame.Competition.EnchantWeaponMageLevelLimited", player, Config.OLY_LIMIT_ENCHANT_STAT_LEVEL_WEAPON_MAGE));
					}
					if(Config.OLY_LIMIT_ENCHANT_STAT_LEVEL_ACCESSORY >= 0)
					{
						player.sendMessage(new CustomMessage("l2p.gameserver.model.entity.OlympiadGame.Competition.EnchantAccessoryLevelLimited", player, Config.OLY_LIMIT_ENCHANT_STAT_LEVEL_ACCESSORY));
					}
					if(player.getClan() != null)
					{
						player.getClan().disableSkills(player);
					}
					for(int restrictedSkillIdx = 0;restrictedSkillIdx < Config.OLY_RESTRICTED_SKILL_IDS.length;++restrictedSkillIdx)
					{
						int restrictedSkillId = Config.OLY_RESTRICTED_SKILL_IDS[restrictedSkillIdx];
						Skill skill = player.getKnownSkill(restrictedSkillId);
						if(skill == null)
							continue;
						player.addUnActiveSkill(skill);
					}
					if(player.isHero())
					{
						HeroController.removeSkills(player);
					}
					if(player.isCastingNow())
					{
						player.abortCast(true, false);
					}
					if(player.isMounted())
					{
						player.setMount(0, 0, 0);
					}
					if(player.getPet() != null)
					{
						Summon summon = player.getPet();
						if(summon.isPet())
						{
							summon.unSummon();
						}
						else
						{
							summon.getEffectList().stopAllEffects();
						}
					}
					if(player.getAgathionId() > 0)
					{
						player.setAgathion(0);
					}
					player.sendPacket(new SkillList(player));
					ItemInstance wpn = player.getInventory().getPaperdollItem(7);
					if(wpn != null && wpn.isHeroWeapon())
					{
						player.getInventory().unEquipItem(wpn);
						player.abortAttack(true, true);
						player.refreshExpertisePenalty();
					}
					for(int itemId : player.getAutoSoulShot())
					{
						player.removeAutoSoulShot(itemId);
						player.sendPacket(new ExAutoSoulShot(itemId, false));
					}
					ItemInstance weapon = player.getActiveWeaponInstance();
					if(weapon != null)
					{
						weapon.setChargedSpiritshot(0);
						weapon.setChargedSoulshot(0);
					}
					restoreHPCPMP();
					player.broadcastUserInfo(true);
					if(getType() != CompetitionType.TEAM_CLASS_FREE)
					{
						if(player.getParty() == null)
							continue;
						player.getParty().removePartyMember(player, false);
						continue;
					}
					boolean upp = false;
					if(player.getParty() != null)
					{
						if(player.getParty().getPartyMembers().size() != part.getPlayers().length)
						{
							upp = true;
						}
						else
						{
							for(Player pm0 : player.getParty().getPartyMembers())
							{
								boolean contains = false;
								for(Player pm1 : part.getPlayers())
								{
									if(pm0 != pm1)
										continue;
									contains = true;
								}
								if(contains)
									continue;
								upp = true;
							}
						}
					}
					else
					{
						upp = true;
					}
					if(!upp)
						continue;
					Player[] party_r = part.getPlayers();
					if(party_r[0].getParty() != null)
					{
						party_r[0].getParty().removePartyMember(party_r[0], false);
					}
					Party party = new Party(party_r[0], 0);
					party_r[0].setParty(party);
					if(party_r[1].isInParty())
					{
						party_r[1].getParty().removePartyMember(party_r[1], false);
					}
					party.addPartyMember(party_r[1]);
					if(party_r[2].isInParty())
					{
						party_r[2].getParty().removePartyMember(party_r[2], false);
					}
					party.addPartyMember(party_r[2]);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	public void applyBuffs()
	{
		for(Participant part : _participants)
		{
			for(Player player : part.getPlayers())
			{
				try
				{
					TIntIntHashMap buffs = Config.OLY_BUFFS.get(player.getActiveClassId());
					for(int skillId : buffs.keys())
					{
						Skill buff = SkillTable.getInstance().getInfo(skillId, buffs.get(skillId));
						buff.getEffects(player, player, false, false);
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	public void restoreHPCPMP()
	{
		for(Participant part : _participants)
		{
			for(Player player : part.getPlayers())
			{
				try
				{
					player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
					player.setCurrentCp(player.getMaxCp());
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	public void finish()
	{
		for(Participant participant : _participants)
		{
			for(Player player : participant.getPlayers())
			{
				if((double) player.getMaxCp() * 0.8 > player.getCurrentCp())
				{
					player.setCurrentCp((double) player.getMaxCp() * 0.8);
				}
				if((double) player.getMaxHp() * 0.8 > player.getCurrentHp())
				{
					player.setCurrentHp((double) player.getMaxHp() * 0.8, false);
				}
				if((double) player.getMaxMp() * 0.8 <= player.getCurrentMp())
					continue;
				player.setCurrentMp((double) player.getMaxMp() * 0.8);
			}
			participant.OnFinish();
		}
	}
	
	private int getParticipantsMinPoint()
	{
		int pmin = Integer.MAX_VALUE;
		for(Participant participant : _participants)
		{
			for(Player player : participant.getPlayers())
			{
				int ppoint;
				if(player == null || (ppoint = NoblesController.getInstance().getPointsOf(player.getObjectId())) >= pmin)
					continue;
				pmin = ppoint;
			}
		}
		return pmin;
	}
	
	private void processPoints(Participant winn, Participant loose, boolean tie)
	{
		processPoints(winn, loose, tie, false);
	}
	
	private void processPoints(Participant winn, Participant loose, boolean tie, boolean looserDisconnected)
	{
		if(!looserDisconnected)
		{
			broadcastPacket(tie ? Msg.THE_GAME_ENDED_IN_A_TIE : new SystemMessage(1497).addString(winn.getName()), true, true);
		}
		long comp_spend_time = 0;
		if(_start_time > 0)
		{
			comp_spend_time = Math.min(300000, System.currentTimeMillis() - _start_time) / 1000;
		}
		Player[] loose_arr = loose.getPlayers();
		Player[] winn_arr = winn.getPlayers();
		int looser_sum = 0;
		int i;
		for(i = 0;i < loose_arr.length;++i)
		{
			try
			{
				Player lp = loose_arr[i];
				if(lp == null)
					continue;
				looser_sum += NoblesController.getInstance().getPointsOf(lp.getObjectId());
				continue;
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		int winner_sum = 0;
		for(i = 0;i < winn_arr.length;++i)
		{
			try
			{
				Player wp = winn_arr[i];
				if(wp == null)
					continue;
				winner_sum += NoblesController.getInstance().getPointsOf(wp.getObjectId());
				continue;
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		int min_points = Math.max(0, Math.min(winner_sum, looser_sum));
		int loosed_points_sum = 0;
		for(int i2 = 0;i2 < loose_arr.length;++i2)
		{
			try
			{
				Player lp = loose_arr[i2];
				if(lp == null || NoblesController.getInstance() == null)
					continue;
				int curr_points = NoblesController.getInstance().getPointsOf(lp.getObjectId());
				int loose_points = Math.max(1, (int) ((double) min_points * Config.OLY_LOOSE_POINTS_MUL));
				loosed_points_sum += loose_points;
				NoblesController.NobleRecord lnr = NoblesController.getInstance().getNobleRecord(lp.getObjectId());
				int looser_points = Math.max(0, curr_points - loose_points);
				lnr.points_current = looser_points;
				++lnr.comp_loose;
				++lnr.comp_done;
				switch(getType())
				{
					case CLASS_FREE:
					{
						++lnr.class_free_cnt;
						break;
					}
					case CLASS_INDIVIDUAL:
					{
						++lnr.class_based_cnt;
						break;
					}
					case TEAM_CLASS_FREE:
					{
						++lnr.team_cnt;
					}
				}
				NoblesController.getInstance().SaveNobleRecord(lnr);
				lp.sendPacket(new SystemMessage(1658).addName(lp).addNumber(loose_points));
				for(QuestState qs : lp.getAllQuestsStates())
				{
					if(!qs.isStarted())
						continue;
					qs.getQuest().notifyOlympiadResult(qs, getType(), false);
				}
				for(Effect e : lp.getEffectList().getAllEffects())
				{
					if(e == null || !e.isCancelable())
						continue;
					e.exit();
				}
				lp.sendChanges();
				lp.updateEffectIcons();
				CompetitionController.getInstance().addCompetitionResult(OlyController.getInstance().getCurrentSeason(), NoblesController.getInstance().getNobleRecord(winn_arr[i2].getObjectId()), loose_points, NoblesController.getInstance().getNobleRecord(lp.getObjectId()), loose_points, getType(), tie, looserDisconnected, comp_spend_time);
				lp.getListeners().onOlyCompetitionCompleted(this, false);
				continue;
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		if(!looserDisconnected)
		{
			int points = loosed_points_sum / loose_arr.length;
			for(int i3 = 0;i3 < winn_arr.length;++i3)
			{
				try
				{
					Player wp = winn_arr[i3];
					int win_points = Math.max(0, NoblesController.getInstance().getPointsOf(wp.getObjectId()) + points);
					NoblesController.getInstance().setPointsOf(wp.getObjectId(), win_points);
					NoblesController.NobleRecord wnr = NoblesController.getInstance().getNobleRecord(wp.getObjectId());
					wnr.points_current = win_points;
					++wnr.comp_win;
					++wnr.comp_done;
					switch(getType())
					{
						case CLASS_FREE:
						{
							++wnr.class_free_cnt;
							break;
						}
						case CLASS_INDIVIDUAL:
						{
							++wnr.class_based_cnt;
							break;
						}
						case TEAM_CLASS_FREE:
						{
							++wnr.team_cnt;
						}
					}
					NoblesController.getInstance().SaveNobleRecord(wnr);
					wp.sendPacket(new SystemMessage(1657).addName(wp).addNumber(points));
					for(QuestState qs : wp.getAllQuestsStates())
					{
						if(!qs.isStarted())
							continue;
						qs.getQuest().notifyOlympiadResult(qs, getType(), !tie);
					}
					for(Effect e : wp.getEffectList().getAllEffects())
					{
						if(e == null || !e.isCancelable())
							continue;
						e.exit();
					}
					wp.sendChanges();
					wp.updateEffectIcons();
					int rvicnt = 0;
					switch(getType())
					{
						case CLASS_FREE:
						{
							rvicnt = Config.OLY_VICTORY_CFREE_RITEMCNT;
							break;
						}
						case CLASS_INDIVIDUAL:
						{
							rvicnt = Config.OLY_VICTORY_CBASE_RITEMCNT;
							break;
						}
						case TEAM_CLASS_FREE:
						{
							rvicnt = Config.OLY_VICTORY_3TEAM_RITEMCNT;
						}
					}
					if(rvicnt > 0)
					{
						wp.getInventory().addItem(Config.OLY_VICTORY_RITEMID, rvicnt);
						wp.sendPacket(SystemMessage2.obtainItems(Config.OLY_VICTORY_RITEMID, rvicnt, 0));
					}
					CompetitionController.getInstance().addCompetitionResult(OlyController.getInstance().getCurrentSeason(), NoblesController.getInstance().getNobleRecord(wp.getObjectId()), points, NoblesController.getInstance().getNobleRecord(loose_arr[i3].getObjectId()), points, getType(), tie, looserDisconnected, comp_spend_time);
					wp.getListeners().onOlyCompetitionCompleted(this, !tie);
					continue;
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	public boolean ValidateParticipants()
	{
		boolean cancel = false;
		for(Participant participant : _participants)
		{
			if(participant.validateThis())
				continue;
			cancel = true;
			break;
		}
		if(cancel)
		{
			cancelTask();
			CompetitionController.getInstance().FinishCompetition(this);
			return true;
		}
		return false;
	}
	
	public synchronized void ValidateWinner()
	{
		if(getState() == CompetitionState.INIT)
		{
			broadcastPacket(Msg.THE_GAME_HAS_BEEN_CANCELLED_BECAUSE_THE_OTHER_PARTY_ENDS_THE_GAME, true, false);
			if(!_participants[0].isAlive())
			{
				processPoints(_participants[1], _participants[0], false, true);
			}
			else if(!_participants[1].isAlive())
			{
				processPoints(_participants[0], _participants[1], false, true);
			}
			cancelTask();
			CompetitionController.getInstance().FinishCompetition(this);
			return;
		}
		if(!(getState() == CompetitionState.FINISH || _participants[0].isAlive() && _participants[1].isAlive()))
		{
			cancelTask();
			setState(CompetitionState.FINISH);
			CompetitionController.getInstance().scheduleFinishCompetition(this, 20, 100);
		}
		if(getState() == CompetitionState.FINISH)
		{
			if(!_participants[0].isAlive())
			{
				processPoints(_participants[1], _participants[0], false);
			}
			else if(!_participants[1].isAlive())
			{
				processPoints(_participants[0], _participants[1], false);
			}
			else
			{
				double dmg1;
				double dmg0 = _participants[0].getTotalDamage();
				if(dmg0 < (dmg1 = _participants[1].getTotalDamage()))
				{
					processPoints(_participants[0], _participants[1], false);
				}
				else if(dmg0 > dmg1)
				{
					processPoints(_participants[1], _participants[0], false);
				}
				else
				{
					processPoints(_participants[0], _participants[1], true);
				}
			}
			for(Participant participant : _participants)
			{
				for(Player player : participant.getPlayers())
				{
					if(player.isDead())
					{
						player.doRevive(100.0);
					}
					player.block();
					player.sendPacket(new ExOlympiadMode(0));
				}
			}
			broadcastPacket(new ExOlympiadMode(3), false, true);
		}
	}
	
	public void teleportParticipantsOnStadium()
	{
		prepareParticipantsForCompetition();
		for(Participant participant : _participants)
		{
			for(Player player : participant.getPlayers())
			{
				try
				{
					if(player == null)
						continue;
					Location loc = Location.findAroundPosition(getStadium().getLocForParticipant(participant), 0, 32);
					player.setVar("backCoords", player.getLoc().toXYZString(), -1);
					player.teleToLocation(loc, _stadium);
					player.sendPacket(new ExOlympiadMode(participant.getSide()));
					if(getType() == CompetitionType.TEAM_CLASS_FREE)
					{
						player.setTeam(participant.getSide() == 1 ? TeamType.BLUE : TeamType.RED);
					}
					Summon summon;
					if((summon = player.getPet()) == null)
						continue;
					if(summon.isPet())
					{
						summon.unSummon();
						continue;
					}
					summon.teleToLocation(loc, _stadium);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	public void teleportParticipantsBack()
	{
		prepareParticipantsForReturn();
		for(Participant participant : _participants)
		{
			for(Player player : participant.getPlayers())
			{
				try
				{
					if(player == null || player.getVar("backCoords") == null)
						continue;
					Location loc = Location.parseLoc(player.getVar("backCoords"));
					player.unsetVar("backCoords");
					player.sendPacket(new ExOlympiadMode(0));
					if(player.isBlocked())
					{
						player.unblock();
					}
					if(getType() == CompetitionType.TEAM_CLASS_FREE)
					{
						player.setTeam(TeamType.NONE);
					}
					player.setReflection(0);
					player.teleToLocation(loc);
					Summon summon = player.getPet();
					if(summon == null)
						continue;
					if(summon.isPet())
					{
						summon.unSummon();
						continue;
					}
					summon.setReflection(0);
					summon.teleToLocation(loc);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	public void broadcastEverybodyOlympiadUserInfo()
	{
		for(Participant participant : _participants)
		{
			for(Player player : participant.getPlayers())
			{
				if(player == null)
					continue;
				ExOlympiadUserInfo oui = new ExOlympiadUserInfo(player);
				broadcastPacket(oui, true, true);
				player.broadcastRelationChanged();
			}
		}
	}
	
	public void broadcastEverybodyEffectIcons()
	{
		for(Participant participant : _participants)
		{
			for(Player player : participant.getPlayers())
			{
				broadcastEffectIcons(player, player.getEffectList().getAllFirstEffects());
			}
		}
	}
	
	public void broadcastEffectIcons(Player player, Effect[] effects)
	{
		ExOlympiadSpelledInfo osi = new ExOlympiadSpelledInfo();
		for(Effect effect : effects)
		{
			if(effect == null || !effect.isInUse())
				continue;
			effect.addOlympiadSpelledIcon(player, osi);
		}
		if(getState() == CompetitionState.PLAYING)
		{
			broadcastPacket(osi, true, true);
		}
		else
		{
			player.getOlyParticipant().sendPacket(osi);
		}
	}
	
	public void broadcastPacket(L2GameServerPacket gsp, boolean toParticipants, boolean toObservers)
	{
		if(getState() == null)
			return;
		if(getState() == CompetitionState.INIT && toParticipants)
		{
			Participant[] arrparticipant = _participants;
			int n = arrparticipant.length;
			int n2 = 0;
			while(n2 < n)
			{
				Participant participant = arrparticipant[n2];
				for(Player player : participant.getPlayers())
				{
					player.sendPacket(gsp);
				}
				++n2;
			}
			return;
		}
		for(Player player : getStadium().getPlayers())
		{
			if((!toParticipants || !player.isOlyParticipant()) && (!toObservers || !player.isOlyObserver()))
				continue;
			player.sendPacket(gsp);
		}
	}
	
	public synchronized void scheduleTask(Runnable task, long delay)
	{
		_currentTask = ThreadPoolManager.getInstance().schedule(task, delay);
	}
	
	public synchronized void cancelTask()
	{
		if(_currentTask != null)
		{
			_currentTask.cancel(false);
			_currentTask = null;
		}
	}
	
	public Participant[] getParticipants()
	{
		return _participants;
	}
}