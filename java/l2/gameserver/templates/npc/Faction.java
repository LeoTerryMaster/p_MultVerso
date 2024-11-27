package l2.gameserver.templates.npc;

import gnu.trove.TIntArrayList;
import l2.commons.util.TroveUtils;

public class Faction
{
	public static final String none = "none";
	public static final Faction NONE = new Faction("none");
	public final String factionId;
	public int factionRange;
	public TIntArrayList ignoreId = TroveUtils.EMPTY_INT_ARRAY_LIST;
	
	public Faction(String factionId)
	{
		this.factionId = factionId;
	}
	
	public String getName()
	{
		return factionId;
	}
	
	public int getRange()
	{
		return factionRange;
	}
	
	public void setRange(int factionRange)
	{
		this.factionRange = factionRange;
	}
	
	public void addIgnoreNpcId(int npcId)
	{
		if(ignoreId.isEmpty())
		{
			ignoreId = new TIntArrayList();
		}
		ignoreId.add(npcId);
	}
	
	public boolean isIgnoreNpcId(int npcId)
	{
		return ignoreId.contains(npcId);
	}
	
	public boolean isNone()
	{
		return factionId.isEmpty() || factionId.equals("none");
	}
	
	public boolean equals(Faction faction)
	{
		return !isNone() && faction.getName().equalsIgnoreCase(factionId);
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
		return equals((Faction) o);
	}
	
	@Override
	public String toString()
	{
		return isNone() ? "none" : factionId;
	}
}