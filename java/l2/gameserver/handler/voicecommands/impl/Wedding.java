package l2.gameserver.handler.voicecommands.impl;

import l2.commons.dbutils.DbUtils;
import l2.commons.lang.reference.HardReference;
import l2.commons.threading.RunnableImpl;
import l2.gameserver.Config;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.ai.CtrlIntention;
import l2.gameserver.cache.Msg;
import l2.gameserver.database.DatabaseFactory;
import l2.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2.gameserver.handler.voicecommands.VoicedCommandHandler;
import l2.gameserver.instancemanager.CoupleManager;
import l2.gameserver.instancemanager.ReflectionManager;
import l2.gameserver.listener.actor.player.OnAnswerListener;
import l2.gameserver.model.GameObjectsStorage;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.Zone;
import l2.gameserver.model.entity.Couple;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.ConfirmDlg;
import l2.gameserver.network.l2.s2c.MagicSkillUse;
import l2.gameserver.network.l2.s2c.SetupGauge;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.skills.AbnormalEffect;
import l2.gameserver.tables.SkillTable;
import l2.gameserver.utils.Location;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Wedding implements IVoicedCommandHandler
{
	private static final Logger _log = LoggerFactory.getLogger(Wedding.class);
	private static final String[] _voicedCommands = {"divorce", "engage", "gotolove"};
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		if(!Config.ALLOW_WEDDING)
		{
			return false;
		}
		if(command.startsWith("engage"))
		{
			return engage(activeChar);
		}
		if(command.startsWith("divorce"))
		{
			return divorce(activeChar);
		}
		if(command.startsWith("gotolove"))
		{
			return goToLove(activeChar);
		}
		return false;
	}
	
	public boolean divorce(Player activeChar)
	{
		if(activeChar.getPartnerId() == 0)
		{
			return false;
		}
		int _partnerId = activeChar.getPartnerId();
		long AdenaAmount = 0;
		if(activeChar.isMaried())
		{
			activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.Divorced", activeChar));
			AdenaAmount = Math.abs(activeChar.getAdena() / 100 * (long) Config.WEDDING_DIVORCE_COSTS - 10);
			activeChar.reduceAdena(AdenaAmount, true);
		}
		else
		{
			activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.Disengaged", activeChar));
		}
		activeChar.setMaried(false);
		activeChar.setPartnerId(0);
		Couple couple = CoupleManager.getInstance().getCouple(activeChar.getCoupleId());
		couple.divorce();
		
		Player partner = GameObjectsStorage.getPlayer(_partnerId);
		if(partner != null)
		{
			partner.setPartnerId(0);
			if(partner.isMaried())
			{
				partner.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.PartnerDivorce", partner));
			}
			else
			{
				partner.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.PartnerDisengage", partner));
			}
			partner.setMaried(false);
			if(AdenaAmount > 0)
			{
				partner.addAdena(AdenaAmount);
			}
		}
		return true;
	}
	
	public boolean engage(Player activeChar)
	{
		if(activeChar.getTarget() == null)
		{
			activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.NoneTargeted", activeChar));
			return false;
		}
		if(!activeChar.getTarget().isPlayer())
		{
			activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.OnlyAnotherPlayer", activeChar));
			return false;
		}
		if(activeChar.getPartnerId() != 0)
		{
			activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.AlreadyEngaged", activeChar));
			if(Config.WEDDING_PUNISH_INFIDELITY)
			{
				activeChar.startAbnormalEffect(AbnormalEffect.BIG_HEAD);
				int skillLevel = 1;
				if(activeChar.getLevel() > 40)
				{
					skillLevel = 2;
				}
				int skillId = activeChar.isMageClass() ? 4361 : 4362;
				Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);
				if(activeChar.getEffectList().getEffectsBySkill(skill) == null)
				{
					skill.getEffects(activeChar, activeChar, false, false);
					activeChar.sendPacket(new SystemMessage(110).addSkillName(skillId, skillLevel));
				}
			}
			return false;
		}
		Player ptarget = (Player) activeChar.getTarget();
		if(ptarget.getObjectId() == activeChar.getObjectId())
		{
			activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.EngagingYourself", activeChar));
			return false;
		}
		if(ptarget.isMaried())
		{
			activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.PlayerAlreadyMarried", activeChar));
			return false;
		}
		if(ptarget.getPartnerId() != 0)
		{
			activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.PlayerAlreadyEngaged", activeChar));
			return false;
		}
		Pair<Integer, OnAnswerListener> entry = ptarget.getAskListener(false);
		if(entry != null && entry.getValue() instanceof CoupleAnswerListener)
		{
			activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.PlayerAlreadyAsked", activeChar));
			return false;
		}
		if(ptarget.getPartnerId() != 0)
		{
			activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.PlayerAlreadyEngaged", activeChar));
			return false;
		}
		if(ptarget.getSex() == activeChar.getSex() && !Config.WEDDING_SAMESEX)
		{
			activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.SameSex", activeChar));
			return false;
		}
		boolean FoundOnFriendList = false;
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT friend_id FROM character_friends WHERE char_id=?");
			statement.setInt(1, ptarget.getObjectId());
			rset = statement.executeQuery();
			while(rset.next())
			{
				int objectId = rset.getInt("friend_id");
				if(objectId != activeChar.getObjectId())
					continue;
				FoundOnFriendList = true;
				break;
			}
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		if(!FoundOnFriendList)
		{
			activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.NotInFriendlist", activeChar));
			return false;
		}
		ConfirmDlg packet = new ConfirmDlg(SystemMsg.S1, 60000).addString("Player " + activeChar.getName() + " asking you to engage. Do you want to start new relationship?");
		ptarget.ask(packet, new CoupleAnswerListener(activeChar, ptarget));
		return true;
	}
	
	public boolean goToLove(Player activeChar)
	{
		if(!activeChar.isMaried())
		{
			activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.YoureNotMarried", activeChar));
			return false;
		}
		if(activeChar.getPartnerId() == 0)
		{
			activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.PartnerNotInDB", activeChar));
			return false;
		}
		Player partner = GameObjectsStorage.getPlayer(activeChar.getPartnerId());
		if(partner == null)
		{
			activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.PartnerOffline", activeChar));
			return false;
		}
		if(partner.isOlyParticipant() || partner.isFestivalParticipant() || activeChar.isMovementDisabled() || activeChar.isMuted(null) || activeChar.isOlyParticipant() || activeChar.isInDuel() || activeChar.isFestivalParticipant() || partner.isInZone(Zone.ZoneType.no_summon))
		{
			activeChar.sendMessage(new CustomMessage("common.TryLater", activeChar));
			return false;
		}
		if(activeChar.isInParty() && activeChar.getParty().isInDimensionalRift() || partner.isInParty() && partner.getParty().isInDimensionalRift())
		{
			activeChar.sendMessage(new CustomMessage("common.TryLater", activeChar));
			return false;
		}
		if(activeChar.getTeleMode() != 0 || activeChar.getReflection() != ReflectionManager.DEFAULT)
		{
			activeChar.sendMessage(new CustomMessage("common.TryLater", activeChar));
			return false;
		}
		if(partner.isInZoneBattle() || partner.isInZone(Zone.ZoneType.SIEGE) || partner.isInZone(Zone.ZoneType.no_restart) || partner.isOlyParticipant() || activeChar.isInZoneBattle() || activeChar.isInZone(Zone.ZoneType.SIEGE) || activeChar.isInZone(Zone.ZoneType.no_restart) || activeChar.isOlyParticipant() || partner.getReflection() != ReflectionManager.DEFAULT || partner.isInZone(Zone.ZoneType.no_summon) || activeChar.isInObserverMode() || partner.isInObserverMode() || partner.isInZone(Zone.ZoneType.fun) || activeChar.isInZone(Zone.ZoneType.fun))
		{
			activeChar.sendPacket(Msg.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING);
			return false;
		}
		if(!activeChar.reduceAdena(Config.WEDDING_TELEPORT_PRICE, true))
		{
			activeChar.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			return false;
		}
		int teleportTimer = Config.WEDDING_TELEPORT_INTERVAL;
		activeChar.abortAttack(true, true);
		activeChar.abortCast(true, true);
		activeChar.sendActionFailed();
		activeChar.stopMove();
		activeChar.startParalyzed();
		activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.Teleport", activeChar).addNumber(teleportTimer / 60));
		activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		activeChar.broadcastPacket(new MagicSkillUse(activeChar, activeChar, 1050, 1, teleportTimer, 0));
		activeChar.sendPacket(new SetupGauge(activeChar, 0, teleportTimer));
		ThreadPoolManager.getInstance().schedule(new EscapeFinalizer(activeChar, partner.getLoc()), (long) teleportTimer * 1000);
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}
	
	public void onLoad()
	{
		VoicedCommandHandler.getInstance().registerVoicedCommandHandler(this);
	}
	
	public void onReload()
	{
	}
	
	public void onShutdown()
	{
	}
	
	static class EscapeFinalizer extends RunnableImpl
	{
		private final Player _activeChar;
		private final Location _loc;
		
		EscapeFinalizer(Player activeChar, Location loc)
		{
			_activeChar = activeChar;
			_loc = loc;
		}
		
		@Override
		public void runImpl() throws Exception
		{
			_activeChar.stopParalyzed();
			if(_activeChar.isDead())
			{
				return;
			}
			_activeChar.teleToLocation(_loc);
		}
	}
	
	private static class CoupleAnswerListener implements OnAnswerListener
	{
		private final HardReference<Player> _playerRef1;
		private final HardReference<Player> _playerRef2;
		
		public CoupleAnswerListener(Player player1, Player player2)
		{
			_playerRef1 = player1.getRef();
			_playerRef2 = player2.getRef();
		}
		
		@Override
		public void sayYes()
		{
			Player player2;
			Player player1 = _playerRef1.get();
			if(player1 == null || (player2 = _playerRef2.get()) == null)
			{
				return;
			}
			CoupleManager.getInstance().createCouple(player1, player2);
			player1.sendMessage(new CustomMessage("l2p.gameserver.model.L2Player.EngageAnswerYes", player2));
		}
		
		@Override
		public void sayNo()
		{
			Player player2;
			Player player1 = _playerRef1.get();
			if(player1 == null || (player2 = _playerRef2.get()) == null)
			{
				return;
			}
			player1.sendMessage(new CustomMessage("l2p.gameserver.model.L2Player.EngageAnswerNo", player2));
		}
	}
}