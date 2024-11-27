package l2.gameserver.dao;

import l2.commons.dbutils.DbUtils;
import l2.gameserver.database.DatabaseFactory;
import l2.gameserver.model.Effect;
import l2.gameserver.model.Playable;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.instances.SummonInstance;
import l2.gameserver.skills.EffectType;
import l2.gameserver.skills.effects.EffectTemplate;
import l2.gameserver.stats.Env;
import l2.gameserver.tables.SkillTable;
import l2.gameserver.utils.SqlBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

public class EffectsDAO
{
	private static final int SUMMON_SKILL_OFFSET = 100000;
	private static final Logger _log = LoggerFactory.getLogger(EffectsDAO.class);
	private static final EffectsDAO _instance = new EffectsDAO();
	
	EffectsDAO()
	{
	}
	
	public static EffectsDAO getInstance()
	{
		return _instance;
	}
	
	public void restoreEffects(Playable playable)
	{
		int objectId;
		int id;
		if(playable.isPlayer())
		{
			objectId = playable.getObjectId();
			id = ((Player) playable).getActiveClassId();
		}
		else if(playable.isSummon())
		{
			objectId = playable.getPlayer().getObjectId();
			id = ((SummonInstance) playable).getEffectIdentifier() + 100000;
		}
		else
		{
			return;
		}
		Connection con = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT `skill_id`,`skill_level`,`effect_count`,`effect_cur_time`,`duration` FROM `character_effects_save` WHERE `object_id`=? AND `id`=? ORDER BY `order` ASC");
			statement.setInt(1, objectId);
			statement.setInt(2, id);
			ResultSet rset = statement.executeQuery();
			while(rset.next())
			{
				int skillId = rset.getInt("skill_id");
				int skillLvl = rset.getInt("skill_level");
				int effectCount = rset.getInt("effect_count");
				long effectCurTime = rset.getLong("effect_cur_time");
				long duration = rset.getLong("duration");
				Skill skill = SkillTable.getInstance().getInfo(skillId, skillLvl);
				if(skill == null)
					continue;
				for(EffectTemplate et : skill.getEffectTemplates())
				{
					if(et == null)
						continue;
					Env env = new Env(playable, playable, skill);
					Effect effect = et.getEffect(env);
					if(effect == null || effect.isOneTime())
						continue;
					
					effect.setCount(effectCount);
					effect.setPeriod(effectCount == 1 ? duration - effectCurTime : duration);
					playable.getEffectList().addEffect(effect);
				}
			}
			DbUtils.closeQuietly(statement, rset);
			statement = con.prepareStatement("DELETE FROM character_effects_save WHERE object_id = ? AND id=?");
			statement.setInt(1, objectId);
			statement.setInt(2, id);
			statement.execute();
			DbUtils.close(statement);
		}
		catch(Exception e)
		{
			_log.error("Could not restore active effects data!", e);
		}
		finally
		{
			DbUtils.closeQuietly(con);
		}
	}
	
	public void deleteEffects(int objectId, int skillId)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM character_effects_save WHERE object_id = ? AND id=?");
			statement.setInt(1, objectId);
			statement.setInt(2, 100000 + skillId);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.error("Could not delete effects active effects data!" + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public void insert(Playable playable)
	{
		int id;
		int objectId;
		if(playable.isPlayer())
		{
			objectId = playable.getObjectId();
			id = ((Player) playable).getActiveClassId();
		}
		else if(playable.isSummon())
		{
			objectId = playable.getPlayer().getObjectId();
			id = ((SummonInstance) playable).getEffectIdentifier() + 100000;
		}
		else
		{
			return;
		}
		List<Effect> effects = playable.getEffectList().getAllEffects();
		if(effects.isEmpty())
		{
			return;
		}
		Connection con = null;
		Statement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.createStatement();
			int order = 0;
			SqlBatch b = new SqlBatch("INSERT IGNORE INTO `character_effects_save` (`object_id`,`skill_id`,`skill_level`,`effect_count`,`effect_cur_time`,`duration`,`order`,`id`) VALUES");
			for(Effect effect : effects)
			{
				if(effect == null || !effect.isInUse() || effect.getSkill().isToggle() || effect.getEffectType() == EffectType.HealOverTime || effect.getEffectType() == EffectType.CombatPointHealOverTime)
					continue;
				StringBuilder sb;
				if(effect.isSaveable())
				{
					sb = new StringBuilder("(");
					sb.append(objectId).append(",");
					sb.append(effect.getSkill().getId()).append(",");
					sb.append(effect.getSkill().getLevel()).append(",");
					sb.append(effect.getCount()).append(",");
					sb.append(effect.getTime()).append(",");
					sb.append(effect.getPeriod()).append(",");
					sb.append(order).append(",");
					sb.append(id).append(")");
					b.write(sb.toString());
				}
				while((effect = effect.getNext()) != null && effect.isSaveable())
				{
					sb = new StringBuilder("(");
					sb.append(objectId).append(",");
					sb.append(effect.getSkill().getId()).append(",");
					sb.append(effect.getSkill().getLevel()).append(",");
					sb.append(effect.getCount()).append(",");
					sb.append(effect.getTime()).append(",");
					sb.append(effect.getPeriod()).append(",");
					sb.append(order).append(",");
					sb.append(id).append(")");
					b.write(sb.toString());
				}
				++order;
			}
			if(!b.isEmpty())
			{
				statement.executeUpdate(b.close());
			}
		}
		catch(Exception e)
		{
			_log.error("Could not store active effects data!", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
}