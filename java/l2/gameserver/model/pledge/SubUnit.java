package l2.gameserver.model.pledge;

import l2.commons.dbutils.DbUtils;
import l2.gameserver.database.DatabaseFactory;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.network.l2.s2c.ExSubPledgeSkillAdd;
import l2.gameserver.tables.SkillTable;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.CHashIntObjectMap;
import org.napile.primitive.maps.impl.CTreeIntObjectMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;

public class SubUnit
{
	private static final Logger _log = LoggerFactory.getLogger(SubUnit.class);
	private final IntObjectMap<Skill> _skills = new CTreeIntObjectMap();
	private final IntObjectMap<UnitMember> _members = new CHashIntObjectMap();
	private final int _type;
	private final Clan _clan;
	private int _leaderObjectId;
	private UnitMember _leader;
	private int _nextLeaderObjectId;
	private String _name;
	
	public SubUnit(Clan c, int type, UnitMember leader, String name)
	{
		_clan = c;
		_type = type;
		_name = name;
		setLeader(leader, false);
	}
	
	public SubUnit(Clan c, int type, int leader, String name)
	{
		_clan = c;
		_type = type;
		_leaderObjectId = leader;
		_name = name;
	}
	
	private static void removeMemberInDatabase(UnitMember member)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE characters SET clanid=0, pledge_type=?, pledge_rank=0, lvl_joined_academy=0, apprentice=0, title='', leaveclan=? WHERE obj_Id=?");
			statement.setInt(1, -128);
			statement.setLong(2, System.currentTimeMillis() / 1000);
			statement.setInt(3, member.getObjectId());
			statement.execute();
		}
		catch(Exception e)
		{
			_log.warn("Exception: " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public int getType()
	{
		return _type;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public UnitMember getLeader()
	{
		return _leader;
	}
	
	public boolean isUnitMember(int obj)
	{
		return _members.containsKey(obj);
	}
	
	public void addUnitMember(UnitMember member)
	{
		_members.put(member.getObjectId(), member);
	}
	
	public UnitMember getUnitMember(int obj)
	{
		if(obj == 0)
		{
			return null;
		}
		return _members.get(obj);
	}
	
	public UnitMember getUnitMember(String obj)
	{
		for(UnitMember m : getUnitMembers())
		{
			if(!m.getName().equalsIgnoreCase(obj))
				continue;
			return m;
		}
		return null;
	}
	
	public void removeUnitMember(int objectId)
	{
		UnitMember m = _members.remove(objectId);
		if(m == null)
		{
			return;
		}
		if(objectId == getLeaderObjectId())
		{
			setLeader(null, true);
		}
		if(m.hasSponsor())
		{
			_clan.getAnyMember(m.getSponsor()).setApprentice(0);
		}
		removeMemberInDatabase(m);
		m.setPlayerInstance(null, true);
	}
	
	public void replace(int objectId, int newUnitId)
	{
		SubUnit newUnit = _clan.getSubUnit(newUnitId);
		if(newUnit == null)
		{
			return;
		}
		UnitMember m = _members.remove(objectId);
		if(m == null)
		{
			return;
		}
		m.setPledgeType(newUnitId);
		newUnit.addUnitMember(m);
		if(m.getPowerGrade() > 5)
		{
			m.setPowerGrade(_clan.getAffiliationRank(m.getPledgeType()));
		}
	}
	
	public int getLeaderObjectId()
	{
		return _leader == null ? 0 : _leader.getObjectId();
	}
	
	public int size()
	{
		return _members.size();
	}
	
	public Collection<UnitMember> getUnitMembers()
	{
		return _members.values();
	}
	
	public void updateDbLeader(UnitMember leaderUnitMember)
	{
		if(getType() == 0)
		{
			_nextLeaderObjectId = leaderUnitMember != _leader ? leaderUnitMember.getObjectId() : 0;
		}
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE clan_subpledges SET leader_id=? WHERE clan_id=? and type=?");
			statement.setInt(1, leaderUnitMember.getObjectId());
			statement.setInt(2, _clan.getClanId());
			statement.setInt(3, _type);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.error("Exception: " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public void setLeader(UnitMember newLeader, boolean updateDB)
	{
		UnitMember old = _leader;
		if(old != null)
		{
			old.setLeaderOf(-128);
		}
		_leader = newLeader;
		int n = _leaderObjectId = newLeader == null ? 0 : newLeader.getObjectId();
		if(newLeader != null)
		{
			newLeader.setLeaderOf(_type);
		}
		if(updateDB)
		{
			updateDbLeader(_leader);
		}
	}
	
	public void setName(String name, boolean updateDB)
	{
		_name = name;
		if(updateDB)
		{
			Connection con = null;
			PreparedStatement statement = null;
			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("UPDATE clan_subpledges SET name=? WHERE clan_id=? and type=?");
				statement.setString(1, _name);
				statement.setInt(2, _clan.getClanId());
				statement.setInt(3, _type);
				statement.execute();
			}
			catch(Exception e)
			{
				_log.error("Exception: " + e, e);
			}
			finally
			{
				DbUtils.closeQuietly(con, statement);
			}
		}
	}
	
	public String getLeaderName()
	{
		return _leader == null ? "" : _leader.getName();
	}
	
	public Skill addSkill(Skill newSkill, boolean store)
	{
		Skill oldSkill = null;
		if(newSkill != null)
		{
			oldSkill = _skills.put(newSkill.getId(), newSkill);
			if(store)
			{
				Connection con = null;
				PreparedStatement statement = null;
				try
				{
					con = DatabaseFactory.getInstance().getConnection();
					if(oldSkill != null)
					{
						statement = con.prepareStatement("UPDATE clan_subpledges_skills SET skill_level=? WHERE skill_id=? AND clan_id=? AND type=?");
						statement.setInt(1, newSkill.getLevel());
						statement.setInt(2, oldSkill.getId());
						statement.setInt(3, _clan.getClanId());
						statement.setInt(4, _type);
						statement.execute();
					}
					else
					{
						statement = con.prepareStatement("INSERT INTO clan_subpledges_skills (clan_id,type,skill_id,skill_level) VALUES (?,?,?,?)");
						statement.setInt(1, _clan.getClanId());
						statement.setInt(2, _type);
						statement.setInt(3, newSkill.getId());
						statement.setInt(4, newSkill.getLevel());
						statement.execute();
					}
				}
				catch(Exception e)
				{
					_log.warn("Exception: " + e, e);
				}
				finally
				{
					DbUtils.closeQuietly(con, statement);
				}
			}
			ExSubPledgeSkillAdd packet = new ExSubPledgeSkillAdd(_type, newSkill.getId(), newSkill.getLevel());
			for(UnitMember temp : _clan)
			{
				Player player;
				if(!temp.isOnline() || (player = temp.getPlayer()) == null)
					continue;
				player.sendPacket(packet);
				if(player.getPledgeType() != _type)
					continue;
				addSkill(player, newSkill);
			}
		}
		return oldSkill;
	}
	
	public int getNextLeaderObjectId()
	{
		return _nextLeaderObjectId;
	}
	
	public void addSkillsQuietly(Player player)
	{
		for(Skill skill : _skills.values())
		{
			addSkill(player, skill);
		}
	}
	
	public void enableSkills(Player player)
	{
		for(Skill skill : _skills.values())
		{
			if(skill.getMinRank() > player.getPledgeClass())
				continue;
			player.removeUnActiveSkill(skill);
		}
	}
	
	public void disableSkills(Player player)
	{
		for(Skill skill : _skills.values())
		{
			player.addUnActiveSkill(skill);
		}
	}
	
	private void addSkill(Player player, Skill skill)
	{
		if(skill.getMinRank() <= player.getPledgeClass())
		{
			player.addSkill(skill, false);
			if(_clan.getReputationScore() < 0 || player.isOlyParticipant())
			{
				player.addUnActiveSkill(skill);
			}
		}
	}
	
	public Collection<Skill> getSkills()
	{
		return _skills.values();
	}
	
	public void restore()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT `c`.`char_name` AS `char_name`,`s`.`level` AS `level`,`s`.`class_id` AS `classid`,`c`.`obj_Id` AS `obj_id`,`c`.`title` AS `title`,`c`.`pledge_rank` AS `pledge_rank`,`c`.`apprentice` AS `apprentice`, `c`.`sex` AS `sex` FROM `characters` `c` LEFT JOIN `character_subclasses` `s` ON (`s`.`char_obj_id` = `c`.`obj_Id` AND `s`.`isBase` = '1') WHERE `c`.`clanid`=? AND `c`.`pledge_type`=? ORDER BY `c`.`lastaccess` DESC");
			statement.setInt(1, _clan.getClanId());
			statement.setInt(2, _type);
			rset = statement.executeQuery();
			while(rset.next())
			{
				UnitMember member = new UnitMember(_clan, rset.getString("char_name"), rset.getString("title"), rset.getInt("level"), rset.getInt("classid"), rset.getInt("obj_Id"), _type, rset.getInt("pledge_rank"), rset.getInt("apprentice"), rset.getInt("sex"), -128);
				addUnitMember(member);
			}
			if(_type != -1)
			{
				SubUnit mainClan = _clan.getSubUnit(0);
				UnitMember leader = mainClan.getUnitMember(_leaderObjectId);
				if(leader != null)
				{
					setLeader(leader, false);
				}
				else if(_type == 0)
				{
					_log.error("Clan " + _name + " have no leader!");
				}
			}
		}
		catch(Exception e)
		{
			_log.warn("Error while restoring clan members for clan: " + _clan.getClanId() + " " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}
	
	public void restoreSkills()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT skill_id,skill_level FROM clan_subpledges_skills WHERE clan_id=? AND type=?");
			statement.setInt(1, _clan.getClanId());
			statement.setInt(2, _type);
			rset = statement.executeQuery();
			while(rset.next())
			{
				int id = rset.getInt("skill_id");
				int level = rset.getInt("skill_level");
				Skill skill = SkillTable.getInstance().getInfo(id, level);
				_skills.put(skill.getId(), skill);
			}
		}
		catch(Exception e)
		{
			_log.warn("Exception: " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}
	
	public int getSkillLevel(int id, int def)
	{
		Skill skill = _skills.get(id);
		return skill == null ? def : skill.getLevel();
	}
	
	public int getSkillLevel(int id)
	{
		return getSkillLevel(id, -1);
	}
}