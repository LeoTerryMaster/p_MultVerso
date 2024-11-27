package l2.gameserver.model;

import l2.gameserver.Config;
import l2.gameserver.model.base.ClassId;
import l2.gameserver.model.base.Experience;

public class SubClass
{
	private int _class;
	private long _exp;
	private long minExp = Experience.LEVEL[Config.ALT_LEVEL_AFTER_GET_SUBCLASS];
	private long maxExp = Experience.LEVEL[Experience.LEVEL.length - 1];
	private int _sp;
	private int _level = Config.ALT_LEVEL_AFTER_GET_SUBCLASS;
	private double _Hp = 1.0;
	private double _Mp = 1.0;
	private double _Cp = 1.0;
	private boolean _active;
	private boolean _isBase;
	private DeathPenalty _dp;
	
	public SubClass()
	{
		_exp = Math.max(minExp, Experience.LEVEL[_level]);
	}
	
	public int getClassId()
	{
		return _class;
	}
	
	public void setClassId(int classId)
	{
		_class = classId;
	}
	
	public long getExp()
	{
		return _exp;
	}
	
	public void setExp(long val)
	{
		val = Math.max(val, minExp);
		val = Math.min(val, maxExp);
		_exp = val;
		_level = Experience.getLevel(_exp);
	}
	
	public long getMaxExp()
	{
		return maxExp;
	}
	
	public void addExp(long val)
	{
		setExp(_exp + val);
	}
	
	public long getSp()
	{
		return Math.min(_sp, Integer.MAX_VALUE);
	}
	
	public void setSp(long spValue)
	{
		spValue = Math.max(spValue, 0);
		spValue = Math.min(spValue, Integer.MAX_VALUE);
		_sp = (int) spValue;
	}
	
	public void addSp(long val)
	{
		setSp((long) _sp + val);
	}
	
	public int getLevel()
	{
		return _level;
	}
	
	public double getHp()
	{
		return _Hp;
	}
	
	public void setHp(double hpValue)
	{
		_Hp = hpValue;
	}
	
	public double getMp()
	{
		return _Mp;
	}
	
	public void setMp(double mpValue)
	{
		_Mp = mpValue;
	}
	
	public double getCp()
	{
		return _Cp;
	}
	
	public void setCp(double cpValue)
	{
		_Cp = cpValue;
	}
	
	public boolean isActive()
	{
		return _active;
	}
	
	public void setActive(boolean active)
	{
		_active = active;
	}
	
	public boolean isBase()
	{
		return _isBase;
	}
	
	public void setBase(boolean base)
	{
		_isBase = base;
		minExp = Experience.LEVEL[_isBase ? 1 : Config.ALT_LEVEL_AFTER_GET_SUBCLASS];
		maxExp = Experience.LEVEL[(_isBase ? Experience.getMaxLevel() : Experience.getMaxSubLevel()) + 1] - 1;
	}
	
	public DeathPenalty getDeathPenalty(Player player)
	{
		if(_dp == null)
		{
			_dp = new DeathPenalty(player, 0);
		}
		return _dp;
	}
	
	public void setDeathPenalty(DeathPenalty dp)
	{
		_dp = dp;
	}
	
	@Override
	public String toString()
	{
		return ClassId.VALUES[_class] + " " + _level;
	}
}