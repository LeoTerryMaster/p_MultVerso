package l2.gameserver.skills;

import l2.gameserver.model.Skill;

import java.util.AbstractMap;

public class SkillEntry extends AbstractMap.SimpleImmutableEntry<SkillEntryType, Skill>
{
	private boolean _disabled;
	
	public SkillEntry(SkillEntryType key, Skill value)
	{
		super(key, value);
	}
	
	public boolean isDisabled()
	{
		return _disabled;
	}
	
	public void setDisabled(boolean disabled)
	{
		_disabled = disabled;
	}
}