package l2.gameserver.dao;

import l2.commons.dbutils.DbUtils;
import l2.commons.listener.Listener;
import l2.commons.listener.ListenerList;
import l2.gameserver.GameServer;
import l2.gameserver.database.DatabaseFactory;
import l2.gameserver.instancemanager.RaidBossSpawnManager;
import l2.gameserver.listener.game.OnCharacterDeleteListener;
import l2.gameserver.model.Player;
import l2.gameserver.tables.LevelUpTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

public class CharacterDAO
{
	private static final Logger LOG = LoggerFactory.getLogger(CharacterDAO.class);
	private static final CharacterDAO INSTANCE = new CharacterDAO();
	private static final String INSERT_CHARACTE = "INSERT INTO `characters` (    `account_name`,  `obj_Id`, `char_name`, `face`, `hairStyle`, `hairColor`, `sex`, `karma`,    `pvpkills`, `pkkills`, `clanid`, `createtime`, `deletetime`, `title`, `accesslevel`,  `online`,    `leaveclan`, `deleteclan`, `nochannel`, `pledge_type`, `pledge_rank`, `lvl_joined_academy`, `apprentice` ) VALUES (     ?, ?, ?, ?, ?, ?, ?, ?,     ?, ?, ?, ?, ?, ?, ?, ?,     ?, ?, ?, ?, ?, ?, ? )";
	private static final String INSERT_SUBCLASS = "INSERT INTO `character_subclasses` (    `char_obj_id`, `class_id`, `exp`, `sp`, `curHp`, `curMp`, `curCp`,     `maxHp`, `maxMp`, `maxCp`, `level`, `active`, `isBase`, `death_penalty`) VALUES (    ?, ?, ?, ?, ?, ?, ?,     ?, ?, ?, ?, ?, ?, ? )";
	private final CharacterDeleteListenerList _characterDeleteListenerList;
	
	public CharacterDAO()
	{
		_characterDeleteListenerList = new CharacterDeleteListenerList();
	}
	
	public static CharacterDAO getInstance()
	{
		return INSTANCE;
	}
	
	public CharacterDeleteListenerList getCharacterDeleteListenerList()
	{
		return _characterDeleteListenerList;
	}
	
	public void deleteCharacterDataByObjId(int objid)
	{
		if(objid < 0)
		{
			return;
		}
		RaidBossSpawnManager.getInstance().deletePoints(objid);
		Collection<Integer> charItems = ItemsDAO.getInstance().loadItemObjectIdsByOwner(objid);
		for(Integer charItemObjId : charItems)
		{
			boolean someOtherHaveIt = false;
			if(someOtherHaveIt)
				continue;
			int itemObjId = charItemObjId;
			ItemsDAO.getInstance().delete(itemObjId);
		}
		Connection conn = null;
		try
		{
			conn = DatabaseFactory.getInstance().getConnection();
			PreparedStatement pstmt = conn.prepareStatement("DELETE FROM `characters` WHERE `obj_Id`=?");
			pstmt.setInt(1, objid);
			pstmt.executeUpdate();
			DbUtils.closeQuietly(pstmt);
			pstmt = conn.prepareStatement("DELETE FROM `character_subclasses` WHERE `char_obj_id`=?");
			pstmt.setInt(1, objid);
			pstmt.executeUpdate();
			DbUtils.closeQuietly(pstmt);
			pstmt = conn.prepareStatement("DELETE FROM `character_blocklist` WHERE `obj_Id`=?");
			pstmt.setInt(1, objid);
			pstmt.executeUpdate();
			DbUtils.closeQuietly(pstmt);
			pstmt = conn.prepareStatement("DELETE FROM `character_effects_save` WHERE `object_id`=?");
			pstmt.setInt(1, objid);
			pstmt.executeUpdate();
			DbUtils.closeQuietly(pstmt);
			pstmt = conn.prepareStatement("DELETE FROM `character_friends` WHERE `char_id`=? OR `friend_id`=?");
			pstmt.setInt(1, objid);
			pstmt.setInt(2, objid);
			pstmt.executeUpdate();
			DbUtils.closeQuietly(pstmt);
			pstmt = conn.prepareStatement("DELETE FROM `character_group_reuse` WHERE `object_id`=?");
			pstmt.setInt(1, objid);
			pstmt.executeUpdate();
			DbUtils.closeQuietly(pstmt);
			pstmt = conn.prepareStatement("DELETE FROM `character_hennas` WHERE `char_obj_id`=?");
			pstmt.setInt(1, objid);
			pstmt.executeUpdate();
			DbUtils.closeQuietly(pstmt);
			pstmt = conn.prepareStatement("DELETE FROM `character_macroses` WHERE `char_obj_id`=?");
			pstmt.setInt(1, objid);
			pstmt.executeUpdate();
			DbUtils.closeQuietly(pstmt);
			pstmt = conn.prepareStatement("DELETE FROM `character_post_friends` WHERE `object_id`=? OR `post_friend`=?");
			pstmt.setInt(1, objid);
			pstmt.setInt(2, objid);
			pstmt.executeUpdate();
			DbUtils.closeQuietly(pstmt);
			pstmt = conn.prepareStatement("DELETE FROM `character_quests` WHERE `char_id`=?");
			pstmt.setInt(1, objid);
			pstmt.executeUpdate();
			DbUtils.closeQuietly(pstmt);
			pstmt = conn.prepareStatement("DELETE FROM `character_recipebook` WHERE `char_id`=?");
			pstmt.setInt(1, objid);
			pstmt.executeUpdate();
			DbUtils.closeQuietly(pstmt);
			pstmt = conn.prepareStatement("DELETE FROM `character_recommends` WHERE `objId`=? OR `targetId`=?");
			pstmt.setInt(1, objid);
			pstmt.setInt(2, objid);
			pstmt.executeUpdate();
			DbUtils.closeQuietly(pstmt);
			pstmt = conn.prepareStatement("DELETE FROM `character_shortcuts` WHERE `object_id`=?");
			pstmt.setInt(1, objid);
			pstmt.executeUpdate();
			DbUtils.closeQuietly(pstmt);
			pstmt = conn.prepareStatement("DELETE FROM `character_skills` WHERE `char_obj_id`=?");
			pstmt.setInt(1, objid);
			pstmt.executeUpdate();
			DbUtils.closeQuietly(pstmt);
			pstmt = conn.prepareStatement("DELETE FROM `character_skills_save` WHERE `char_obj_id`=?");
			pstmt.setInt(1, objid);
			pstmt.executeUpdate();
			DbUtils.closeQuietly(pstmt);
			pstmt = conn.prepareStatement("DELETE FROM `character_variables` WHERE `obj_id`=?");
			pstmt.setInt(1, objid);
			pstmt.executeUpdate();
			DbUtils.closeQuietly(pstmt);
			getCharacterDeleteListenerList().onCharacterDelete(objid);
		}
		catch(SQLException e)
		{
			LOG.error("Can't delete character", e);
		}
		finally
		{
			DbUtils.closeQuietly(conn);
		}
	}
	
	public boolean insert(Player player)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO `characters` (    `account_name`,  `obj_Id`, `char_name`, `face`, `hairStyle`, `hairColor`, `sex`, `karma`,    `pvpkills`, `pkkills`, `clanid`, `createtime`, `deletetime`, `title`, `accesslevel`,  `online`,    `leaveclan`, `deleteclan`, `nochannel`, `pledge_type`, `pledge_rank`, `lvl_joined_academy`, `apprentice` ) VALUES (     ?, ?, ?, ?, ?, ?, ?, ?,     ?, ?, ?, ?, ?, ?, ?, ?,     ?, ?, ?, ?, ?, ?, ? )");
			statement.setString(1, player.getAccountName());
			statement.setInt(2, player.getObjectId());
			statement.setString(3, player.getName());
			statement.setInt(4, player.getFace());
			statement.setInt(5, player.getHairStyle());
			statement.setInt(6, player.getHairColor());
			statement.setInt(7, player.getSex());
			statement.setInt(8, player.getKarma());
			statement.setInt(9, player.getPvpKills());
			statement.setInt(10, player.getPkKills());
			statement.setInt(11, player.getClanId());
			statement.setLong(12, player.getCreateTime() / 1000);
			statement.setInt(13, player.getDeleteTimer());
			statement.setString(14, player.getTitle());
			statement.setInt(15, player.getAccessLevel());
			statement.setInt(16, player.isOnline() ? 1 : 0);
			statement.setLong(17, player.getLeaveClanTime() / 1000);
			statement.setLong(18, player.getDeleteClanTime() / 1000);
			statement.setLong(19, player.getNoChannel() > 0 ? player.getNoChannel() / 1000 : player.getNoChannel());
			statement.setInt(20, player.getPledgeType());
			statement.setInt(21, player.getPowerGrade());
			statement.setInt(22, player.getLvlJoinedAcademy());
			statement.setInt(23, player.getApprentice());
			statement.executeUpdate();
			DbUtils.close(statement);
			statement = con.prepareStatement("INSERT INTO `character_subclasses` (    `char_obj_id`, `class_id`, `exp`, `sp`, `curHp`, `curMp`, `curCp`,     `maxHp`, `maxMp`, `maxCp`, `level`, `active`, `isBase`, `death_penalty`) VALUES (    ?, ?, ?, ?, ?, ?, ?,     ?, ?, ?, ?, ?, ?, ? )");
			statement.setInt(1, player.getObjectId());
			statement.setInt(2, player.getTemplate().classId.getId());
			statement.setInt(3, 0);
			statement.setInt(4, 0);
			statement.setDouble(5, LevelUpTable.getInstance().getMaxHP(player));
			statement.setDouble(6, LevelUpTable.getInstance().getMaxMP(player));
			statement.setDouble(7, LevelUpTable.getInstance().getMaxCP(player));
			statement.setDouble(8, LevelUpTable.getInstance().getMaxHP(player));
			statement.setDouble(9, LevelUpTable.getInstance().getMaxMP(player));
			statement.setDouble(10, LevelUpTable.getInstance().getMaxCP(player));
			statement.setInt(11, 1);
			statement.setInt(12, 1);
			statement.setInt(13, 1);
			statement.setInt(14, 0);
			statement.executeUpdate();
		}
		catch(Exception e)
		{
			LOG.error("Can't store character", e);
			boolean bl = false;
			return bl;
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		return true;
	}
	
	public int getObjectIdByName(String name)
	{
		int result = 0;
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT obj_Id FROM characters WHERE char_name=?");
			statement.setString(1, name);
			rset = statement.executeQuery();
			if(rset.next())
			{
				result = rset.getInt(1);
			}
		}
		catch(Exception e)
		{
			LOG.error("Can't get character object id by name" + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return result;
	}
	
	public String getNameByObjectId(int objectId)
	{
		String result = "";
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT char_name FROM characters WHERE obj_Id=?");
			statement.setInt(1, objectId);
			rset = statement.executeQuery();
			if(rset.next())
			{
				result = rset.getString(1);
			}
		}
		catch(Exception e)
		{
			LOG.error("Can't get char name by id" + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return result;
	}
	
	public int accountCharNumber(String account)
	{
		int number = 0;
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT COUNT(char_name) FROM characters WHERE account_name=?");
			statement.setString(1, account);
			rset = statement.executeQuery();
			if(rset.next())
			{
				number = rset.getInt(1);
			}
		}
		catch(Exception e)
		{
			LOG.error("Can't get amount of the account characters", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return number;
	}
	
	public class CharacterDeleteListenerList extends ListenerList<GameServer>
	{
		public void onCharacterDelete(int charObjId)
		{
			try
			{
				if(!getListeners().isEmpty())
				{
					for(Listener listener : getListeners())
					{
						if(!(listener instanceof OnCharacterDeleteListener))
							continue;
						((OnCharacterDeleteListener) listener).onCharacterDelate(charObjId);
					}
				}
			}
			catch(Exception e)
			{
				LOG.warn("Character delete listener", e);
			}
		}
	}
}