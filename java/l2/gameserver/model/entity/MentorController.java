package l2.gameserver.model.entity;

import l2.commons.dbutils.DbUtils;
import l2.gameserver.Config;
import l2.gameserver.cache.Msg;
import l2.gameserver.dao.CharacterDAO;
import l2.gameserver.dao.CharacterVariablesDAO;
import l2.gameserver.database.DatabaseFactory;
import l2.gameserver.listener.actor.player.OnAnswerListener;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.SubClass;
import l2.gameserver.model.World;
import l2.gameserver.model.base.ClassId;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.model.mail.Mail;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.ConfirmDlg;
import l2.gameserver.network.l2.s2c.ExNoticePostArrived;
import l2.gameserver.tables.SkillTable;
import l2.gameserver.utils.ItemFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MentorController
{
	private static final Logger _log = LoggerFactory.getLogger(MentorController.class);
	private static final long MENTOR_PENALTY_TIME = 604800000;
	private static final String MENTOR_PENALTY_VAR = "MentorPenaltyTime";
	private static final String MENTOR_FLAG_VAR = "MentoredFlag";
	private static final String NEW_SUB_CNT_VAR = "MenteeSubCnt";
	private static MentorController _instance;
	private final Set<Mentoring> _cache;
	private final ReentrantReadWriteLock _rwlock;
	private final Skill _mentor_buff;
	private final Skill _mentee_buff;
	
	public MentorController()
	{
		_log.info("MentorController: Init.");
		_cache = new ConcurrentSkipListSet<>();
		_rwlock = new ReentrantReadWriteLock();
		_mentor_buff = SkillTable.getInstance().getInfo(Config.MENTORING_MENTOR_SKILL_ID, Config.MENTORING_MENTOR_SKILL_LEVEL);
		_mentee_buff = SkillTable.getInstance().getInfo(Config.MENTORING_MENTEE_SKILL_ID, Config.MENTORING_MENTEE_SKILL_LEVEL);
	}
	
	public static final MentorController getInstance()
	{
		if(_instance == null)
		{
			_instance = new MentorController();
		}
		return _instance;
	}
	
	public static boolean isCanBeMentor(Player player)
	{
		if(player == null)
		{
			return false;
		}
		if(player.getLevel() < 85)
		{
			return false;
		}
		for(SubClass sc : player.getSubClasses().values())
		{
			for(ClassId ci : ClassId.VALUES)
			{
				if(ci.getId() != sc.getClassId() || ci.getLevel() == 4)
					continue;
				return false;
			}
		}
		return player.isNoble();
	}
	
	public static boolean isCanBeMentee(Player player)
	{
		if(player == null)
		{
			return false;
		}
		if(player.getLevel() >= 85)
		{
			return false;
		}
		for(SubClass sc : player.getSubClasses().values())
		{
			for(ClassId ci : ClassId.VALUES)
			{
				if(ci.getId() != sc.getClassId() || ci.getLevel() < 4)
					continue;
				return false;
			}
		}
		if(player.getVarB("MentoredFlag", false))
		{
			return false;
		}
		if(player.getCreateTime() < Config.MENTORING_CHAR_START_TS)
		{
			return false;
		}
		return !player.isNoble();
	}
	
	private void lockRead()
	{
		_rwlock.readLock().lock();
	}
	
	private void unlockRead()
	{
		_rwlock.readLock().unlock();
	}
	
	private void lockWrite()
	{
		_rwlock.writeLock().lock();
	}
	
	private void unlockWrite()
	{
		_rwlock.writeLock().unlock();
	}
	
	private Mentoring getMentoringByMentorObjId(int oid)
	{
		Mentoring result = null;
		lockRead();
		try
		{
			for(Mentoring m : _cache)
			{
				if(m == null || !m.isMentor(oid))
					continue;
				Mentoring mentoring = m;
				return mentoring;
			}
			Connection con = null;
			PreparedStatement pstmt = null;
			ResultSet rset = null;
			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				pstmt = con.prepareStatement("SELECT `menteeObjId` FROM `mentoring` WHERE `mentorObjId` = ?");
				pstmt.setInt(1, oid);
				rset = pstmt.executeQuery();
				ArrayList<Integer> list = new ArrayList<>(3);
				while(rset.next())
				{
					list.add(rset.getInt("menteeObjId"));
				}
				if(!list.isEmpty())
				{
					result = new Mentoring(oid, list);
					_cache.add(result);
				}
			}
			catch(SQLException e)
			{
				_log.warn("Can't get mentoring info for mentorObjId=" + oid, e);
			}
			finally
			{
				DbUtils.closeQuietly(con, pstmt, rset);
			}
		}
		finally
		{
			unlockRead();
		}
		return result;
	}
	
	private Mentoring getMentoringByMenteeObjId(int oid)
	{
		Mentoring result = null;
		lockRead();
		try
		{
			for(Mentoring m : _cache)
			{
				if(m == null || !m.isMentee(oid))
					continue;
				Mentoring mentoring = m;
				return mentoring;
			}
			Connection con = null;
			PreparedStatement pstmt = null;
			ResultSet rset = null;
			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				pstmt = con.prepareStatement("SELECT `mentorObjId` FROM `mentoring` WHERE `menteeObjId` = ?");
				pstmt.setInt(1, oid);
				rset = pstmt.executeQuery();
				if(rset.next())
				{
					int mentorObjId = rset.getInt("mentorObjId");
					DbUtils.closeQuietly(pstmt, rset);
					pstmt = con.prepareStatement("SELECT `menteeObjId` FROM `mentoring` WHERE `mentorObjId` = ?");
					pstmt.setInt(1, mentorObjId);
					rset = pstmt.executeQuery();
					ArrayList<Integer> list = new ArrayList<>(3);
					while(rset.next())
					{
						list.add(rset.getInt("menteeObjId"));
					}
					if(!list.isEmpty())
					{
						result = new Mentoring(mentorObjId, list);
						_cache.add(result);
					}
				}
			}
			catch(SQLException e)
			{
				_log.warn("Can't get mentoring info for menteeObjId=" + oid, e);
			}
			finally
			{
				DbUtils.closeQuietly(con, pstmt, rset);
			}
		}
		finally
		{
			unlockRead();
		}
		return result;
	}
	
	public boolean addMentee(Player mentor, Player mentee)
	{
		Mentoring m;
		int roid = mentor.getObjectId();
		int eoid = mentee.getObjectId();
		Mentoring mentoring = m = mentor.getMentoring() == null ? getMentoringByMentorObjId(roid) : mentor.getMentoring();
		if(m != null)
		{
			if(m.getMenteeCount() >= 3)
			{
				mentor.sendMessage(new CustomMessage("l2.gameserver.model.entity.MentorController.ToManyMentee", mentor));
				return false;
			}
			if(mentee.getMentoring() != null)
			{
				mentor.sendMessage(new CustomMessage("l2.gameserver.model.entity.MentorController.AlreadyMentee", mentor));
				return false;
			}
		}
		mentee.unsetVar("MenteeSubCnt");
		lockWrite();
		try
		{
			Connection con = null;
			PreparedStatement pstmt = null;
			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				pstmt = con.prepareStatement("INSERT INTO `mentoring` (`mentorObjId`, `menteeObjId`) VALUES (?, ?)");
				pstmt.setInt(1, roid);
				pstmt.setInt(2, eoid);
				pstmt.executeUpdate();
				if(m == null)
				{
					m = new Mentoring(roid, Collections.singleton(new Integer(eoid)));
					_cache.add(m);
				}
				else
				{
					m.addMenteeObjId(eoid);
				}
				if(mentor.getMentoring() == null)
				{
					mentor.setMentroring(m);
				}
				mentee.setMentroring(m);
			}
			catch(SQLException e)
			{
				_log.warn("Can't add mentee for mentor " + roid + " and mentee " + eoid, e);
			}
			finally
			{
				DbUtils.closeQuietly(con, pstmt);
			}
		}
		finally
		{
			unlockWrite();
		}
		return m != null;
	}
	
	public boolean removeMentee(Mentoring m, int menteeObjId)
	{
		if(m == null || !m.isMentee(menteeObjId))
		{
			return false;
		}
		lockWrite();
		try
		{
			Connection con = null;
			PreparedStatement pstmt = null;
			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				pstmt = con.prepareStatement("DELETE FROM `mentoring` WHERE `menteeObjId` = ? AND `mentorObjId` = ?");
				pstmt.setInt(1, menteeObjId);
				pstmt.setInt(2, m.getMentorObjId());
				pstmt.executeUpdate();
				m.removeMenteeObjId(menteeObjId);
				if(m.getMenteeCount() == 0)
				{
					if(m.getMentor() != null)
					{
						m.getMentor().setMentroring(null);
					}
					_cache.remove(m);
				}
			}
			catch(SQLException e)
			{
				_log.warn("Can't remove mentee " + menteeObjId, e);
			}
			finally
			{
				DbUtils.closeQuietly(con, pstmt);
			}
		}
		finally
		{
			unlockWrite();
		}
		return true;
	}
	
	public Mentoring getMentoringByPlayer(Player player)
	{
		if(player == null)
		{
			return null;
		}
		return getMentoringByObjId(player.getObjectId());
	}
	
	public Mentoring getMentoringByObjId(int objId)
	{
		Mentoring m = getMentoringByMentorObjId(objId);
		if(m == null)
		{
			m = getMentoringByMenteeObjId(objId);
		}
		return m;
	}
	
	public void onPlayerEnter(Mentoring m, Player player)
	{
		if(m == null)
		{
			return;
		}
		player.setMentroring(m);
		if(m.isMentor(player))
		{
			if(m.isMentorBuffApplicable(player))
			{
				if(isCanBeMentor(player))
				{
					_mentor_buff.getEffects(player, player, false, false);
					for(Player mentee : m.getMentees())
					{
						mentee.sendMessage(new CustomMessage("l2.gameserver.model.entity.MentorController.MentorLoggedIn", mentee));
						_mentee_buff.getEffects(mentee, mentee, false, false);
					}
				}
				else
				{
					player.sendMessage(new CustomMessage("l2.gameserver.model.entity.MentorController.MentoringCondition", player));
				}
			}
			else
			{
				player.getEffectList().stopEffect(_mentor_buff);
			}
		}
		if(m.isMentee(player))
		{
			if(m.isMenteeBuffApplicable(player))
			{
				_mentee_buff.getEffects(player, player, false, false);
				Player mentor = m.getMentor();
				if(mentor != null)
				{
					mentor.sendMessage(new CustomMessage("l2.gameserver.model.entity.MentorController.MenteeS1LoggedIn", mentor, player.getName()));
					_mentor_buff.getEffects(mentor, mentor, false, false);
				}
			}
			else
			{
				player.getEffectList().stopEffect(_mentee_buff);
			}
		}
	}
	
	public void onPlayerExit(Mentoring m, Player player)
	{
		if(m == null)
		{
			return;
		}
		player.setMentroring(null);
		if(m.isMentor(player))
		{
			for(Player mentee : m.getMentees())
			{
				mentee.getEffectList().stopEffect(_mentee_buff);
				mentee.sendMessage(new CustomMessage("l2.gameserver.model.entity.MentorController.MentorLoggedOut", mentee));
			}
		}
		Player mentor;
		if(m.isMentee(player) && (mentor = m.getMentor()) != null)
		{
			if(!m.isMentorBuffApplicable(mentor))
			{
				mentor.getEffectList().stopEffect(_mentor_buff);
			}
			mentor.sendMessage(new CustomMessage("l2.gameserver.model.entity.MentorController.MenteeS1LoggedOut", mentor, player.getName()));
		}
		if(m.getMentees().size() == 0)
		{
			_cache.remove(m);
		}
	}
	
	public void askForMentoring(Player mentor, String menteeName)
	{
		if(!isCanBeMentor(mentor))
		{
			mentor.sendMessage(new CustomMessage("l2.gameserver.model.entity.MentorController.MentoringCondition", mentor));
			return;
		}
		if(mentor.getVarLong("MentorPenaltyTime", 0) > System.currentTimeMillis())
		{
			mentor.sendMessage(new CustomMessage("l2.gameserver.model.entity.MentorController.MentorPenalty", mentor));
			return;
		}
		Player mentee = World.getPlayer(menteeName);
		if(mentee == null)
		{
			mentor.sendMessage(new CustomMessage("l2.gameserver.model.entity.MentorController.S1NotInWorld", mentor, menteeName));
			return;
		}
		if(!isCanBeMentee(mentee))
		{
			mentor.sendMessage(new CustomMessage("l2.gameserver.model.entity.MentorController.MentoringCondition", mentor));
			return;
		}
		if(mentor.getMentoring() != null && mentor.getMentoring().getMenteeCount() >= 3)
		{
			mentor.sendMessage(new CustomMessage("l2.gameserver.model.entity.MentorController.ToManyMentee", mentor));
			return;
		}
		if(mentee.getMentoring() != null)
		{
			mentor.sendMessage(new CustomMessage("l2.gameserver.model.entity.MentorController.AlreadyMentee", mentor));
			return;
		}
		mentee.ask(new ConfirmDlg(SystemMsg.S1, 30000).addString(new CustomMessage("l2.gameserver.model.entity.MentorController.S1ProposeMentoring", mentee, mentor.getName()).toString()), new MentoringRequest(mentor.getObjectId(), mentee.getObjectId()));
	}
	
	public void removeMentoring(Mentoring m, int menteeObjId)
	{
		Player mentee = World.getPlayer(menteeObjId);
		Player mentor = m.getMentor();
		if(mentor != null)
		{
			mentor.setVar("MentorPenaltyTime", 604800000 + System.currentTimeMillis(), -1);
			mentor.sendMessage(new CustomMessage("l2.gameserver.model.entity.MentorController.MentorPenalty", mentor));
		}
		else
		{
			CharacterVariablesDAO.getInstance().setVar(m.getMentorObjId(), "MentorPenaltyTime", String.valueOf(604800000 + System.currentTimeMillis()), -1);
		}
		removeMentee(m, menteeObjId);
		if(mentee != null)
		{
			onPlayerExit(m, mentee);
		}
	}
	
	public void onMenteeNewSubclass(Player mentee, Mentoring m)
	{
		int sub_cnt = mentee.getVarInt("MenteeSubCnt", 0);
		if(++sub_cnt <= 4)
		{
			mentee.setVar("MenteeSubCnt", sub_cnt, -1);
			Player mentor = m.getMentor();
			if(mentor != null)
			{
				mentor.sendMessage(new CustomMessage("l2.gameserver.model.entity.MentorController.MenteeS1NewSub", mentor, mentee.getName()));
			}
			sendMail(m.getMentorObjId(), new CustomMessage("l2.gameserver.model.entity.MentorController.MenteeS1GotSubMail", mentee, mentee.getName()).toString(), Collections.singletonMap(Config.MENTORING_NEW_SUB_ITEM_ID, Config.MENTORING_NEW_SUB_ITEM_COUNT));
		}
	}
	
	public void onMentoringComplete(Player mentee, Mentoring m)
	{
		Player mentor = m.getMentor();
		removeMentoring(m, mentee.getObjectId());
		if(mentor != null)
		{
			mentor.sendMessage(new CustomMessage("l2.gameserver.model.entity.MentorController.MenteeS1Competed", mentor, mentee.getName()));
		}
		mentee.unsetVar("MenteeSubCnt");
		mentee.setVar("MentoredFlag", "true", -1);
		sendMail(m.getMentorObjId(), new CustomMessage("l2.gameserver.model.entity.MentorController.MenteeS1CompetedMail", mentee, mentee.getName()).toString(), Collections.singletonMap(Config.MENTORING_END_MENTOR_ITEM_ID, Config.MENTORING_END_MENTOR_ITEM_COUNT));
		sendMail(mentee.getObjectId(), new CustomMessage("l2.gameserver.model.entity.MentorController.YouEndMentoringMail", mentee, mentee.getName()).toString(), Collections.singletonMap(Config.MENTORING_END_MENTEE_ITEM_ID, Config.MENTORING_END_MENTEE_ITEM_COUNT));
	}
	
	private void sendMail(int receiverOid, String body, Map<Integer, Long> items)
	{
		Player p = World.getPlayer(receiverOid);
		Mail mail = new Mail();
		mail.setSenderId(1);
		mail.setSenderName("Mentoring");
		mail.setReceiverId(receiverOid);
		mail.setReceiverName(p != null ? p.getName() : CharacterDAO.getInstance().getNameByObjectId(receiverOid));
		mail.setTopic("Mentoring");
		mail.setBody(body);
		if(items != null)
		{
			for(Map.Entry<Integer, Long> itm : items.entrySet())
			{
				ItemInstance item = ItemFunctions.createItem(itm.getKey());
				item.setOwnerId(receiverOid);
				item.setLocation(ItemInstance.ItemLocation.MAIL);
				item.setCount(itm.getValue());
				item.save();
				mail.addAttachment(item);
			}
		}
		mail.setType(Mail.SenderType.NEWS_INFORMER);
		mail.setUnread(true);
		mail.setExpireTime(2592000 + (int) (System.currentTimeMillis() / 1000));
		mail.save();
		if(p != null)
		{
			p.sendPacket(ExNoticePostArrived.STATIC_TRUE);
			p.sendPacket(Msg.THE_MAIL_HAS_ARRIVED);
		}
	}
	
	private class MentoringRequest implements OnAnswerListener
	{
		private final int _mentorObjId;
		private final int _menteeObjId;
		
		public MentoringRequest(int mentorObjId, int menteeObjId)
		{
			_mentorObjId = mentorObjId;
			_menteeObjId = menteeObjId;
		}
		
		@Override
		public void sayYes()
		{
			Player mentor = World.getPlayer(_mentorObjId);
			Player mentee = World.getPlayer(_menteeObjId);
			if(mentor != null && mentor.isOnline() && mentee != null && mentee.isOnline() && getInstance().addMentee(mentor, mentee))
			{
				mentor.sendMessage(new CustomMessage("l2.gameserver.model.entity.MentorController.S1AcceptMentoring", mentor, mentee.getName()));
				getInstance().onPlayerEnter(mentee.getMentoring(), mentee);
			}
		}
		
		@Override
		public void sayNo()
		{
			Player mentor = World.getPlayer(_mentorObjId);
			Player mentee = World.getPlayer(_menteeObjId);
			if(mentor != null && mentor.isOnline() && mentee != null && mentee.isOnline())
			{
				mentor.sendMessage(new CustomMessage("l2.gameserver.model.entity.MentorController.S1DenyMentoring", mentor, mentee.getName()));
			}
		}
	}
}