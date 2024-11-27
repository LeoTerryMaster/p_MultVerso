package l2.gameserver.dao;

import l2.commons.dao.JdbcDAO;
import l2.commons.dao.JdbcEntityState;
import l2.commons.dao.JdbcEntityStats;
import l2.commons.dbutils.DbUtils;
import l2.gameserver.database.DatabaseFactory;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.model.mail.Mail;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class MailDAO implements JdbcDAO<Integer, Mail>
{
	private static final Logger _log = LoggerFactory.getLogger(MailDAO.class);
	private static final String RESTORE_MAIL = "SELECT sender_id, sender_name, receiver_id, receiver_name, expire_time, topic, body, price, type, unread FROM mail WHERE message_id = ?";
	private static final String STORE_MAIL = "INSERT INTO mail(sender_id, sender_name, receiver_id, receiver_name, expire_time, topic, body, price, type, unread) VALUES (?,?,?,?,?,?,?,?,?,?)";
	private static final String UPDATE_MAIL = "UPDATE mail SET sender_id = ?, sender_name = ?, receiver_id = ?, receiver_name = ?, expire_time = ?, topic = ?, body = ?, price = ?, type = ?, unread = ? WHERE message_id = ?";
	private static final String REMOVE_MAIL = "DELETE FROM mail WHERE message_id = ?";
	private static final String RESTORE_EXPIRED_MAIL = "SELECT message_id FROM mail WHERE expire_time <= ?";
	private static final String RESTORE_OWN_MAIL = "SELECT message_id FROM character_mail WHERE char_id = ? AND is_sender = ?";
	private static final String STORE_OWN_MAIL = "INSERT INTO character_mail(char_id, message_id, is_sender) VALUES (?,?,?)";
	private static final String REMOVE_OWN_MAIL = "DELETE FROM character_mail WHERE char_id = ? AND message_id = ? AND is_sender = ?";
	private static final String RESTORE_MAIL_ATTACHMENTS = "SELECT item_id FROM mail_attachments WHERE message_id = ?";
	private static final String STORE_MAIL_ATTACHMENT = "REPLACE INTO mail_attachments(message_id, item_id) VALUES (?,?)";
	private static final String REMOVE_MAIL_ATTACHMENTS = "DELETE FROM mail_attachments WHERE message_id = ?";
	private static final MailDAO instance = new MailDAO();
	private final AtomicLong load = new AtomicLong();
	private final AtomicLong insert = new AtomicLong();
	private final AtomicLong update = new AtomicLong();
	private final AtomicLong delete = new AtomicLong();
	private final Cache cache;
	private final JdbcEntityStats stats;
	
	private MailDAO()
	{
		stats = new JdbcEntityStats()
		{
			
			@Override
			public long getLoadCount()
			{
				return load.get();
			}
			
			@Override
			public long getInsertCount()
			{
				return insert.get();
			}
			
			@Override
			public long getUpdateCount()
			{
				return update.get();
			}
			
			@Override
			public long getDeleteCount()
			{
				return delete.get();
			}
		};
		cache = CacheManager.getInstance().getCache(Mail.class.getName());
	}
	
	public static MailDAO getInstance()
	{
		return instance;
	}
	
	public Cache getCache()
	{
		return cache;
	}
	
	@Override
	public JdbcEntityStats getStats()
	{
		return stats;
	}
	
	private void save0(Mail mail) throws SQLException
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO mail(sender_id, sender_name, receiver_id, receiver_name, expire_time, topic, body, price, type, unread) VALUES (?,?,?,?,?,?,?,?,?,?)", 1);
			statement.setInt(1, mail.getSenderId());
			statement.setString(2, mail.getSenderName());
			statement.setInt(3, mail.getReceiverId());
			statement.setString(4, mail.getReceiverName());
			statement.setInt(5, mail.getExpireTime());
			statement.setString(6, mail.getTopic());
			statement.setString(7, mail.getBody());
			statement.setLong(8, mail.getPrice());
			statement.setInt(9, mail.getType().ordinal());
			statement.setBoolean(10, mail.isUnread());
			statement.execute();
			rset = statement.getGeneratedKeys();
			rset.next();
			mail.setMessageId(rset.getInt(1));
			if(!mail.getAttachments().isEmpty())
			{
				DbUtils.close(statement);
				statement = con.prepareStatement("REPLACE INTO mail_attachments(message_id, item_id) VALUES (?,?)");
				for(ItemInstance item : mail.getAttachments())
				{
					statement.setInt(1, mail.getMessageId());
					statement.setInt(2, item.getObjectId());
					statement.addBatch();
				}
				statement.executeBatch();
			}
			DbUtils.close(statement);
			if(mail.getType() == Mail.SenderType.NORMAL)
			{
				statement = con.prepareStatement("INSERT INTO character_mail(char_id, message_id, is_sender) VALUES (?,?,?)");
				statement.setInt(1, mail.getSenderId());
				statement.setInt(2, mail.getMessageId());
				statement.setBoolean(3, true);
				statement.execute();
			}
			DbUtils.close(statement);
			statement = con.prepareStatement("INSERT INTO character_mail(char_id, message_id, is_sender) VALUES (?,?,?)");
			statement.setInt(1, mail.getReceiverId());
			statement.setInt(2, mail.getMessageId());
			statement.setBoolean(3, false);
			statement.execute();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		insert.incrementAndGet();
	}
	
	private Mail load0(int messageId) throws SQLException
	{
		Mail mail = null;
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT sender_id, sender_name, receiver_id, receiver_name, expire_time, topic, body, price, type, unread FROM mail WHERE message_id = ?");
			statement.setInt(1, messageId);
			rset = statement.executeQuery();
			if(rset.next())
			{
				mail = new Mail();
				mail.setMessageId(messageId);
				mail.setSenderId(rset.getInt(1));
				mail.setSenderName(rset.getString(2));
				mail.setReceiverId(rset.getInt(3));
				mail.setReceiverName(rset.getString(4));
				mail.setExpireTime(rset.getInt(5));
				mail.setTopic(rset.getString(6));
				mail.setBody(rset.getString(7));
				mail.setPrice(rset.getLong(8));
				mail.setType(Mail.SenderType.VALUES[rset.getInt(9)]);
				mail.setUnread(rset.getBoolean(10));
				DbUtils.close(statement, rset);
				statement = con.prepareStatement("SELECT item_id FROM mail_attachments WHERE message_id = ?");
				statement.setInt(1, messageId);
				rset = statement.executeQuery();
				while(rset.next())
				{
					int objectId = rset.getInt(1);
					ItemInstance item = ItemsDAO.getInstance().load(objectId);
					if(item == null)
						continue;
					mail.addAttachment(item);
				}
			}
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		load.incrementAndGet();
		return mail;
	}
	
	private void update0(Mail mail) throws SQLException
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE mail SET sender_id = ?, sender_name = ?, receiver_id = ?, receiver_name = ?, expire_time = ?, topic = ?, body = ?, price = ?, type = ?, unread = ? WHERE message_id = ?");
			statement.setInt(1, mail.getSenderId());
			statement.setString(2, mail.getSenderName());
			statement.setInt(3, mail.getReceiverId());
			statement.setString(4, mail.getReceiverName());
			statement.setInt(5, mail.getExpireTime());
			statement.setString(6, mail.getTopic());
			statement.setString(7, mail.getBody());
			statement.setLong(8, mail.getPrice());
			statement.setInt(9, mail.getType().ordinal());
			statement.setBoolean(10, mail.isUnread());
			statement.setInt(11, mail.getMessageId());
			statement.execute();
			if(mail.getAttachments().isEmpty())
			{
				DbUtils.close(statement);
				statement = con.prepareStatement("DELETE FROM mail_attachments WHERE message_id = ?");
				statement.setInt(1, mail.getMessageId());
				statement.execute();
			}
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		update.incrementAndGet();
	}
	
	private void delete0(Mail mail) throws SQLException
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM mail WHERE message_id = ?");
			statement.setInt(1, mail.getMessageId());
			statement.execute();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		delete.incrementAndGet();
	}
	
	private List<Mail> getMailByOwnerId(int ownerId, boolean sent)
	{
		List<Integer> messageIds = Collections.emptyList();
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT message_id FROM character_mail WHERE char_id = ? AND is_sender = ?");
			statement.setInt(1, ownerId);
			statement.setBoolean(2, sent);
			rset = statement.executeQuery();
			messageIds = new ArrayList();
			while(rset.next())
			{
				messageIds.add(rset.getInt(1));
			}
		}
		catch(SQLException e)
		{
			_log.error("Error while restore mail of owner : " + ownerId, e);
			messageIds.clear();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return load(messageIds);
	}
	
	private boolean deleteMailByOwnerIdAndMailId(int ownerId, int messageId, boolean sent)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM character_mail WHERE char_id = ? AND message_id = ? AND is_sender = ?");
			statement.setInt(1, ownerId);
			statement.setInt(2, messageId);
			statement.setBoolean(3, sent);
			boolean bl = statement.execute();
			return bl;
		}
		catch(SQLException e)
		{
			_log.error("Error while deleting mail of owner : " + ownerId, e);
			boolean bl = false;
			return bl;
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public List<Mail> getReceivedMailByOwnerId(int receiverId)
	{
		return getMailByOwnerId(receiverId, false);
	}
	
	public List<Mail> getSentMailByOwnerId(int senderId)
	{
		return getMailByOwnerId(senderId, true);
	}
	
	public Mail getReceivedMailByMailId(int receiverId, int messageId)
	{
		List<Mail> list = getMailByOwnerId(receiverId, false);
		for(Mail mail : list)
		{
			if(mail.getMessageId() != messageId)
				continue;
			return mail;
		}
		return null;
	}
	
	public Mail getSentMailByMailId(int senderId, int messageId)
	{
		List<Mail> list = getMailByOwnerId(senderId, true);
		for(Mail mail : list)
		{
			if(mail.getMessageId() != messageId)
				continue;
			return mail;
		}
		return null;
	}
	
	public boolean deleteReceivedMailByMailId(int receiverId, int messageId)
	{
		return deleteMailByOwnerIdAndMailId(receiverId, messageId, false);
	}
	
	public boolean deleteSentMailByMailId(int senderId, int messageId)
	{
		return deleteMailByOwnerIdAndMailId(senderId, messageId, true);
	}
	
	public List<Mail> getExpiredMail(int expireTime)
	{
		List<Integer> messageIds = Collections.emptyList();
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT message_id FROM mail WHERE expire_time <= ?");
			statement.setInt(1, expireTime);
			rset = statement.executeQuery();
			messageIds = new ArrayList<>();
			while(rset.next())
			{
				messageIds.add(rset.getInt(1));
			}
		}
		catch(SQLException e)
		{
			_log.error("Error while restore expired mail!", e);
			messageIds.clear();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return load(messageIds);
	}
	
	@Override
	public Mail load(Integer id)
	{
		Element ce = cache.get(id);
		if(ce != null)
		{
			Mail mail2 = (Mail) ce.getObjectValue();
			return mail2;
		}
		Mail mail;
		try
		{
			mail = load0(id);
			if(mail != null)
			{
				mail.setJdbcState(JdbcEntityState.STORED);
				cache.put(new Element(Integer.valueOf(mail.getMessageId()), mail));
			}
		}
		catch(SQLException e)
		{
			_log.error("Error while restoring mail : " + id, e);
			return null;
		}
		return mail;
	}
	
	public List<Mail> load(Collection<Integer> messageIds)
	{
		if(messageIds.isEmpty())
		{
			return Collections.emptyList();
		}
		ArrayList<Mail> list = new ArrayList<>(messageIds.size());
		for(Integer messageId : messageIds)
		{
			Mail mail = load(messageId);
			if(mail == null)
				continue;
			list.add(mail);
		}
		return list;
	}
	
	@Override
	public void save(Mail mail)
	{
		if(!mail.getJdbcState().isSavable())
		{
			return;
		}
		try
		{
			save0(mail);
			mail.setJdbcState(JdbcEntityState.STORED);
		}
		catch(SQLException e)
		{
			_log.error("Error while saving mail!", e);
			return;
		}
		cache.put(new Element(Integer.valueOf(mail.getMessageId()), mail));
	}
	
	@Override
	public void update(Mail mail)
	{
		if(!mail.getJdbcState().isUpdatable())
		{
			return;
		}
		try
		{
			update0(mail);
			mail.setJdbcState(JdbcEntityState.STORED);
		}
		catch(SQLException e)
		{
			_log.error("Error while updating mail : " + mail.getMessageId(), e);
			return;
		}
		cache.putIfAbsent(new Element(Integer.valueOf(mail.getMessageId()), mail));
	}
	
	@Override
	public void saveOrUpdate(Mail mail)
	{
		if(mail.getJdbcState().isSavable())
		{
			save(mail);
		}
		else if(mail.getJdbcState().isUpdatable())
		{
			update(mail);
		}
	}
	
	@Override
	public void delete(Mail mail)
	{
		if(!mail.getJdbcState().isDeletable())
		{
			return;
		}
		try
		{
			delete0(mail);
			mail.setJdbcState(JdbcEntityState.DELETED);
		}
		catch(SQLException e)
		{
			_log.error("Error while deleting mail : " + mail.getMessageId(), e);
			return;
		}
		cache.remove(Integer.valueOf(mail.getExpireTime()));
	}
}