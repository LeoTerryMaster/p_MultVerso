package l2.gameserver.templates.npc;

public class MinionData
{
	private final int _minionId;
	private final int _minionAmount;
	
	public MinionData(int minionId, int minionAmount)
	{
		_minionId = minionId;
		_minionAmount = minionAmount;
	}
	
	public int getMinionId()
	{
		return _minionId;
	}
	
	public int getAmount()
	{
		return _minionAmount;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if(o == this)
		{
			return true;
		}
		if(o == null)
		{
			return false;
		}
		if(o.getClass() != getClass())
		{
			return false;
		}
		return ((MinionData) o).getMinionId() == getMinionId();
	}
}