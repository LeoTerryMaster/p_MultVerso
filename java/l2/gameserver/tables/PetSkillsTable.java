package l2.gameserver.tables;

import gnu.trove.TIntObjectHashMap;
import l2.commons.dbutils.DbUtils;
import l2.gameserver.database.DatabaseFactory;
import l2.gameserver.model.SkillLearn;
import l2.gameserver.model.Summon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class PetSkillsTable
{
	private static final Logger _log = LoggerFactory.getLogger(PetSkillsTable.class);
	private static PetSkillsTable _instance = new PetSkillsTable();
	private final TIntObjectHashMap<List<SkillLearn>> _skillTrees = new TIntObjectHashMap();
	
	private PetSkillsTable()
	{
		load();
	}
	
	public static PetSkillsTable getInstance()
	{
		return _instance;
	}
	
	public void reload()
	{
		_instance = new PetSkillsTable();
	}
	
	private void load()
	{
		int count = 0;
		int npcId = 0;
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM pets_skills ORDER BY templateId");
			rset = statement.executeQuery();
			while(rset.next())
			{
				npcId = rset.getInt("templateId");
				int id = rset.getInt("skillId");
				int lvl = rset.getInt("skillLvl");
				int minLvl = rset.getInt("minLvl");
				ArrayList<SkillLearn> list = (ArrayList<SkillLearn>) _skillTrees.get(npcId);
				if(list == null)
				{
					list = new ArrayList<>();
					_skillTrees.put(npcId, list);
				}
				SkillLearn skillLearn = new SkillLearn(id, lvl, minLvl, 0, 0, 0, false);
				list.add(skillLearn);
				++count;
			}
		}
		catch(Exception e)
		{
			_log.error("Error while creating pet skill tree (Pet ID " + npcId + ")", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		_log.info("PetSkillsTable: Loaded " + count + " skills.");
	}
	
	public int getAvailableLevel(Summon cha, int skillId)
	{
		List<SkillLearn> skills = _skillTrees.get(cha.getNpcId());
		if(skills == null)
		{
			return 0;
		}
		int lvl = 0;
		for(SkillLearn temp : skills)
		{
			if(temp.getId() != skillId)
				continue;
			if(temp.getLevel() == 0)
			{
				if(cha.getLevel() < 70)
				{
					lvl = cha.getLevel() / 10;
					if(lvl <= 0)
					{
						lvl = 1;
					}
				}
				else
				{
					lvl = 7 + (cha.getLevel() - 70) / 5;
				}
				int maxLvl;
				if(lvl <= (maxLvl = SkillTable.getInstance().getMaxLevel(temp.getId())))
					break;
				lvl = maxLvl;
				break;
			}
			if(temp.getMinLevel() > cha.getLevel() || temp.getLevel() <= lvl)
				continue;
			lvl = temp.getLevel();
		}
		return lvl;
	}
}