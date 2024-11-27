package l2.gameserver.network.l2.c2s;

import l2.gameserver.Announcements;
import l2.gameserver.Config;
import l2.gameserver.data.xml.holder.ResidenceHolder;
import l2.gameserver.instancemanager.CoupleManager;
import l2.gameserver.instancemanager.CursedWeaponsManager;
import l2.gameserver.instancemanager.PetitionManager;
import l2.gameserver.instancemanager.PlayerMessageStack;
import l2.gameserver.instancemanager.QuestManager;
import l2.gameserver.listener.actor.player.OnAnswerListener;
import l2.gameserver.listener.actor.player.impl.ReviveAnswerListener;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Effect;
import l2.gameserver.model.GameObjectsStorage;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.Summon;
import l2.gameserver.model.World;
import l2.gameserver.model.base.InvisibleType;
import l2.gameserver.model.entity.SevenSigns;
import l2.gameserver.model.entity.events.impl.ClanHallAuctionEvent;
import l2.gameserver.model.entity.oly.HeroController;
import l2.gameserver.model.entity.residence.ClanHall;
import l2.gameserver.model.pledge.Clan;
import l2.gameserver.model.pledge.SubUnit;
import l2.gameserver.model.pledge.UnitMember;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.network.l2.GameClient;
import l2.gameserver.network.l2.components.ChatType;
import l2.gameserver.network.l2.components.IStaticPacket;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.*;
import l2.gameserver.network.l2.s2c.ConfirmDlg;
import l2.gameserver.skills.AbnormalEffect;
import l2.gameserver.tables.SkillTable;
import l2.gameserver.utils.GameStats;
import l2.gameserver.utils.TradeHelper;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnterWorld extends L2GameClientPacket
{
	private static final Object _lock = new Object();
	private static final Logger _log = LoggerFactory.getLogger(EnterWorld.class);
	
	private static void notifyClanMembers(Player activeChar)
	{
		Clan clan = activeChar.getClan();
		SubUnit subUnit = activeChar.getSubUnit();
		if(clan == null || subUnit == null)
		{
			return;
		}
		UnitMember member = subUnit.getUnitMember(activeChar.getObjectId());
		if(member == null)
		{
			return;
		}
		member.setPlayerInstance(activeChar, false);
		int sponsor = activeChar.getSponsor();
		int apprentice = activeChar.getApprentice();
		Object msg = new SystemMessage2(SystemMsg.CLAN_MEMBER_S1_HAS_LOGGED_INTO_GAME).addName(activeChar);
		PledgeShowMemberListUpdate memberUpdate = new PledgeShowMemberListUpdate(activeChar);
		for(Player clanMember : clan.getOnlineMembers(activeChar.getObjectId()))
		{
			clanMember.sendPacket(memberUpdate);
			if(clanMember.getObjectId() == sponsor)
			{
				clanMember.sendPacket(new SystemMessage2(SystemMsg.YOUR_APPRENTICE_C1_HAS_LOGGED_OUT).addName(activeChar));
				continue;
			}
			if(clanMember.getObjectId() == apprentice)
			{
				clanMember.sendPacket(new SystemMessage2(SystemMsg.YOUR_SPONSOR_C1_HAS_LOGGED_IN).addName(activeChar));
				continue;
			}
			clanMember.sendPacket((IStaticPacket) msg);
		}
		if(!activeChar.isClanLeader())
		{
			return;
		}
		ClanHall clanHall;
		ClanHall clanHall2 = clanHall = clan.getHasHideout() > 0 ? ResidenceHolder.getInstance().getResidence(ClanHall.class, clan.getHasHideout()) : null;
		if(clanHall == null || clanHall.getAuctionLength() != 0)
		{
			return;
		}
		if(clanHall.getSiegeEvent().getClass() != ClanHallAuctionEvent.class)
		{
			return;
		}
		if(clan.getWarehouse().getCountOf(57) < clanHall.getRentalFee())
		{
			activeChar.sendPacket(new SystemMessage2(SystemMsg.PAYMENT_FOR_YOUR_CLAN_HALL_HAS_NOT_BEEN_MADE_PLEASE_ME_PAYMENT_TO_YOUR_CLAN_WAREHOUSE_BY_S1_TOMORROW).addLong(clanHall.getRentalFee()));
		}
	}
	
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		GameClient client = getClient();
		Player activeChar = client.getActiveChar();
		if(activeChar == null)
		{
			client.closeNow(false);
			return;
		}
		int MyObjectId = activeChar.getObjectId();
		Long MyStoreId = activeChar.getStoredId();
		Object object = _lock;
		synchronized(object)
		{
			for(Player cha : GameObjectsStorage.getAllPlayersForIterate())
			{
				if(MyStoreId == cha.getStoredId())
					continue;
				try
				{
					if(cha.getObjectId() != MyObjectId)
						continue;
					_log.warn("Double EnterWorld for char: " + activeChar.getName());
					cha.kick();
				}
				catch(Exception e)
				{
					_log.error("", e);
				}
			}
		}
		GameStats.incrementPlayerEnterGame();
		boolean first = activeChar.entering;
		if(first)
		{
			activeChar.setOnlineStatus(true);
			if(activeChar.getPlayerAccess().GodMode && (!Config.SAVE_GM_EFFECTS || Config.SAVE_GM_EFFECTS && !activeChar.getVarB("gm_vis")))
			{
				activeChar.setInvisibleType(InvisibleType.NORMAL);
			}
			activeChar.setNonAggroTime(Long.MAX_VALUE);
			activeChar.spawnMe();
			if(activeChar.isInStoreMode() && !TradeHelper.checksIfCanOpenStore(activeChar, activeChar.getPrivateStoreType()))
			{
				activeChar.setPrivateStoreType(0);
				activeChar.standUp();
				activeChar.broadcastCharInfo();
			}
			activeChar.setRunning();
			activeChar.standUp();
			activeChar.startTimers();
		}
		activeChar.getMacroses().sendUpdate();
		activeChar.sendPacket(new SSQInfo(), new HennaInfo(activeChar));
		activeChar.sendPacket(new SkillList(activeChar), new SkillCoolTime(activeChar));
		if(Config.SEND_LINEAGE2_WELCOME_MESSAGE)
		{
			activeChar.sendPacket(SystemMsg.WELCOME_TO_THE_WORLD_OF_LINEAGE_II);
		}
		Announcements.getInstance().showAnnouncements(activeChar);
		if(Config.SEND_SSQ_WELCOME_MESSAGE)
		{
			SevenSigns.getInstance().sendCurrentPeriodMsg(activeChar);
		}
		if(first)
		{
			activeChar.getListeners().onEnter();
		}
		if(activeChar.getClan() != null)
		{
			notifyClanMembers(activeChar);
			activeChar.sendPacket(activeChar.getClan().listAll());
			activeChar.sendPacket(new PledgeShowInfoUpdate(activeChar.getClan()), new PledgeSkillList(activeChar.getClan()));
		}
		if(Config.SHOW_HTML_WELCOME && (activeChar.getClan() == null || activeChar.getClan().getNotice() == null || activeChar.getClan().getNotice().isEmpty()))
		{
			NpcHtmlMessage html = new NpcHtmlMessage(1);
			html.setFile("welcome.htm");
			sendPacket(html);
		}
		if(first && Config.ALLOW_WEDDING)
		{
			CoupleManager.getInstance().engage(activeChar);
			CoupleManager.getInstance().notifyPartner(activeChar);
		}
		if(first)
		{
			activeChar.getFriendList().notifyFriends(true);
			loadTutorial(activeChar);
			activeChar.restoreDisableSkills();
		}
		sendPacket(new L2FriendList(activeChar), new QuestList(activeChar), new EtcStatusUpdate(activeChar));
		activeChar.checkHpMessages(activeChar.getMaxHp(), activeChar.getCurrentHp());
		activeChar.checkDayNightMessages();
		if(Config.PETITIONING_ALLOWED)
		{
			PetitionManager.getInstance().checkPetitionMessages(activeChar);
			if(activeChar.isGM() && PetitionManager.getInstance().isPetitionPending())
			{
				activeChar.sendPacket(new Say2(0, ChatType.CRITICAL_ANNOUNCE, "SYS", "There are pended petition(s)"));
				activeChar.sendPacket(new Say2(0, ChatType.CRITICAL_ANNOUNCE, "SYS", "Show all petition: //view_petitions"));
			}
		}
		if(!first)
		{
			if(activeChar.isCastingNow())
			{
				Creature castingTarget = activeChar.getCastingTarget();
				Skill castingSkill = activeChar.getCastingSkill();
				long animationEndTime = activeChar.getAnimationEndTime();
				if(castingSkill != null && castingTarget != null && castingTarget.isCreature() && activeChar.getAnimationEndTime() > 0)
				{
					sendPacket(new MagicSkillUse(activeChar, castingTarget, castingSkill.getId(), castingSkill.getLevel(), (int) (animationEndTime - System.currentTimeMillis()), 0));
				}
			}
			if(activeChar.isInBoat())
			{
				activeChar.sendPacket(activeChar.getBoat().getOnPacket(activeChar, activeChar.getInBoatPosition()));
			}
			if(activeChar.isMoving() || activeChar.isFollowing())
			{
				sendPacket(activeChar.movePacket());
			}
			if(activeChar.getMountNpcId() != 0)
			{
				sendPacket(new Ride(activeChar));
			}
			if(activeChar.isFishing())
			{
				activeChar.stopFishing();
			}
		}
		activeChar.entering = false;
		activeChar.sendUserInfo(true);
		activeChar.sendItemList(false);
		activeChar.sendPacket(new ShortCutInit(activeChar));
		if(activeChar.isSitting())
		{
			activeChar.sendPacket(new ChangeWaitType(activeChar, 0));
		}
		if(activeChar.getPrivateStoreType() != 0)
		{
			if(activeChar.getPrivateStoreType() == 3)
			{
				sendPacket(new PrivateStoreMsgBuy(activeChar));
			}
			else if(activeChar.getPrivateStoreType() == 1 || activeChar.getPrivateStoreType() == 8)
			{
				sendPacket(new PrivateStoreMsgSell(activeChar));
			}
			else if(activeChar.getPrivateStoreType() == 5)
			{
				sendPacket(new RecipeShopMsg(activeChar));
			}
		}
		if(activeChar.isDead())
		{
			sendPacket(new Die(activeChar));
		}
		activeChar.unsetVar("offline");
		activeChar.sendActionFailed();
		if(first && activeChar.isGM() && Config.SAVE_GM_EFFECTS && activeChar.getPlayerAccess().CanUseGMCommand)
		{
			if(activeChar.getVarB("gm_silence"))
			{
				activeChar.setMessageRefusal(true);
				activeChar.sendPacket(SystemMsg.MESSAGE_REFUSAL_MODE);
			}
			if(activeChar.getVarB("gm_invul"))
			{
				activeChar.setIsInvul(true);
				activeChar.startAbnormalEffect(AbnormalEffect.S_INVULNERABLE);
				activeChar.sendMessage(activeChar.getName() + " is now immortal.");
			}
			try
			{
				int var_gmspeed = Integer.parseInt(activeChar.getVar("gm_gmspeed"));
				if(var_gmspeed >= 1 && var_gmspeed <= 4)
				{
					activeChar.doCast(SkillTable.getInstance().getInfo(7029, var_gmspeed), activeChar, true);
				}
			}
			catch(Exception e)
			{
				
			}
		}
		if(first && activeChar.isGM() && activeChar.getPlayerAccess().GodMode && Config.SHOW_GM_LOGIN && activeChar.getInvisibleType() == InvisibleType.NONE)
		{
			Announcements.getInstance().announceByCustomMessage("enterworld.show.gm.login", new String[] {activeChar.getName()});
		}
		PlayerMessageStack.getInstance().CheckMessages(activeChar);
		sendPacket(ClientSetTime.STATIC, new ExSetCompassZoneCode(activeChar));
		Pair<Integer, OnAnswerListener> entry = activeChar.getAskListener(false);
		if(entry != null && entry.getValue() instanceof ReviveAnswerListener)
		{
			sendPacket(new ConfirmDlg(SystemMsg.C1_IS_MAKING_AN_ATTEMPT_TO_RESURRECT_YOU_IF_YOU_CHOOSE_THIS_PATH_S2_EXPERIENCE_WILL_BE_RETURNED_FOR_YOU, 0).addString("Other player").addString("some"));
		}
		if(activeChar.isCursedWeaponEquipped())
		{
			CursedWeaponsManager.getInstance().showUsageTime(activeChar, activeChar.getCursedWeaponEquippedId());
		}
		if(HeroController.isHaveHeroWeapon(activeChar))
		{
			HeroController.checkHeroWeaponary(activeChar);
		}
		if(!first)
		{
			if(activeChar.isInObserverMode())
			{
				if(activeChar.getObserverMode() == 2)
				{
					activeChar.returnFromObserverMode();
				}
				else if(activeChar.isOlyObserver())
				{
					activeChar.leaveOlympiadObserverMode();
				}
				else
				{
					activeChar.leaveObserverMode();
				}
			}
			else if(activeChar.isVisible())
			{
				World.showObjectsToPlayer(activeChar);
			}
			if(activeChar.getPet() != null)
			{
				sendPacket(new PetInfo(activeChar.getPet()));
			}
			if(activeChar.isInParty())
			{
				sendPacket(new PartySmallWindowAll(activeChar.getParty(), activeChar));
				for(Player member : activeChar.getParty().getPartyMembers())
				{
					if(member == activeChar)
						continue;
					sendPacket(new PartySpelled(member, true));
					Summon member_pet = member.getPet();
					if(member_pet != null)
					{
						sendPacket(new PartySpelled(member_pet, true));
					}
					sendPacket(RelationChanged.create(activeChar, member, activeChar));
				}
				if(activeChar.getParty().isInCommandChannel())
				{
					sendPacket(ExMPCCOpen.STATIC);
				}
			}
			for(int shotId : activeChar.getAutoSoulShot())
			{
				sendPacket(new ExAutoSoulShot(shotId, true));
			}
			for(Effect e : activeChar.getEffectList().getAllFirstEffects())
			{
				if(!e.getSkill().isToggle())
					continue;
				sendPacket(new MagicSkillLaunched(activeChar, e.getSkill().getId(), e.getSkill().getLevel(), activeChar));
			}
			activeChar.broadcastCharInfo();
		}
		else
		{
			activeChar.sendUserInfo();
		}
		activeChar.updateEffectIcons();
		activeChar.updateStats();
		if(Config.ALT_PCBANG_POINTS_ENABLED)
		{
			activeChar.sendPacket(new ExPCCafePointInfo(activeChar, 0, 1, 2, 12));
		}
		if(!activeChar.getPremiumItemList().isEmpty())
		{
			activeChar.sendPacket(Config.GOODS_INVENTORY_ENABLED ? ExGoodsInventoryChangedNotify.STATIC : ExNotifyPremiumItem.STATIC);
		}
	}
	
	private void loadTutorial(Player player)
	{
		Quest q = QuestManager.getQuest(255);
		if(q != null)
		{
			player.processQuestEvent(q.getName(), "UC", null);
		}
	}
}