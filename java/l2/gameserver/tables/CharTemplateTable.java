package l2.gameserver.tables;

import l2.commons.dbutils.DbUtils;
import l2.gameserver.Config;
import l2.gameserver.database.DatabaseFactory;
import l2.gameserver.model.base.ClassId;
import l2.gameserver.model.base.Experience;
import l2.gameserver.templates.PlayerTemplate;
import l2.gameserver.templates.StatsSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class CharTemplateTable
{
	private static final Logger _log = LoggerFactory.getLogger(CharTemplateTable.class);
	private static CharTemplateTable _instance;
	private final Map<Integer, PlayerTemplate> _templates;
	
	private CharTemplateTable()
	{
		_templates = new HashMap<>();
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM class_list, char_templates WHERE class_list.id = char_templates.classId ORDER BY class_list.id");
			rset = statement.executeQuery();
			while(rset.next())
			{
				StatsSet set = new StatsSet();
				ClassId classId = ClassId.VALUES[rset.getInt("class_list.id")];
				set.set("classId", rset.getInt("class_list.id"));
				set.set("className", rset.getString("char_templates.className"));
				set.set("raceId", rset.getInt("char_templates.RaceId"));
				set.set("baseSTR", rset.getInt("char_templates.STR"));
				set.set("baseCON", rset.getInt("char_templates.CON"));
				set.set("baseDEX", rset.getInt("char_templates.DEX"));
				set.set("baseINT", rset.getInt("char_templates._INT"));
				set.set("baseWIT", rset.getInt("char_templates.WIT"));
				set.set("baseMEN", rset.getInt("char_templates.MEN"));
				set.set("baseHpMax", 0);
				set.set("baseMpMax", 0);
				set.set("baseCpMax", 0);
				set.set("baseHpReg", 0.01);
				set.set("baseCpReg", 0.01);
				set.set("baseMpReg", 0.01);
				set.set("basePAtk", rset.getInt("char_templates.p_atk"));
				set.set("basePDef", rset.getInt("char_templates.p_def"));
				set.set("baseMAtk", rset.getInt("char_templates.m_atk"));
				set.set("baseMDef", 41);
				set.set("basePAtkSpd", rset.getInt("char_templates.p_spd"));
				set.set("baseMAtkSpd", classId.isMage() ? Config.BASE_MAGE_CAST_SPEED : Config.BASE_WARRIOR_CAST_SPEED);
				set.set("baseCritRate", rset.getInt("char_templates.critical"));
				set.set("baseWalkSpd", rset.getInt("char_templates.walk_spd"));
				set.set("baseRunSpd", rset.getInt("char_templates.run_spd"));
				set.set("baseShldDef", 0);
				set.set("baseShldRate", 0);
				switch(set.getInteger("raceId"))
				{
					case 3:
					{
						set.set("baseAtkRange", 25);
						break;
					}
					default:
					{
						set.set("baseAtkRange", 20);
					}
				}
				set.set("baseExp", Experience.getExpForLevel(rset.getInt("char_templates.level")));
				set.set("spawnX", rset.getInt("char_templates.x"));
				set.set("spawnY", rset.getInt("char_templates.y"));
				set.set("spawnZ", rset.getInt("char_templates.z"));
				set.set("isMale", true);
				set.set("collision_radius", rset.getDouble("char_templates.m_col_r"));
				set.set("collision_height", rset.getDouble("char_templates.m_col_h"));
				PlayerTemplate ct = new PlayerTemplate(set);
				int x;
				for(x = 1;x < 15;++x)
				{
					if(rset.getInt("char_templates.items" + x) == 0)
						continue;
					ct.addItem(rset.getInt("char_templates.items" + x));
				}
				_templates.put(ct.classId.getId(), ct);
				set.set("isMale", false);
				set.set("collision_radius", rset.getDouble("char_templates.f_col_r"));
				set.set("collision_height", rset.getDouble("char_templates.f_col_h"));
				ct = new PlayerTemplate(set);
				for(x = 1;x < 15;++x)
				{
					int itemId = rset.getInt("char_templates.items" + x);
					if(itemId == 0)
						continue;
					ct.addItem(itemId);
				}
				_templates.put(ct.classId.getId() | 256, ct);
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
		_log.info("CharTemplateTable: Loaded " + _templates.size() + " Character Templates.");
	}
	
	public static CharTemplateTable getInstance()
	{
		if(_instance == null)
		{
			_instance = new CharTemplateTable();
		}
		return _instance;
	}
	
	public PlayerTemplate getTemplate(ClassId classId, boolean female)
	{
		return getTemplate(classId.getId(), female);
	}
	
	public PlayerTemplate getTemplate(int classId, boolean female)
	{
		int key = classId;
		if(female)
		{
			key |= 256;
		}
		return _templates.get(key);
	}
}