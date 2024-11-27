package l2.commons.dao;

public enum JdbcEntityState
{
	CREATED(true, false, false, false),
	STORED(false, true, false, true),
	UPDATED(false, true, true, true),
	DELETED(false, false, false, false);
	
	private final boolean savable;
	private final boolean deletable;
	private final boolean updatable;
	private final boolean persisted;
	
	JdbcEntityState(boolean savable, boolean deletable, boolean updatable, boolean persisted)
	{
		this.savable = savable;
		this.deletable = deletable;
		this.updatable = updatable;
		this.persisted = persisted;
	}
	
	public boolean isSavable()
	{
		return savable;
	}
	
	public boolean isDeletable()
	{
		return deletable;
	}
	
	public boolean isUpdatable()
	{
		return updatable;
	}
	
	public boolean isPersisted()
	{
		return persisted;
	}
}