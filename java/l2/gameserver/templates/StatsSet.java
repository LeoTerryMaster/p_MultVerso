package l2.gameserver.templates;

import l2.commons.collections.MultiValueSet;

public class StatsSet extends MultiValueSet<String>
{
	public static final StatsSet EMPTY = new StatsSet()
	{
		@Override
		public Object put(String a, Object a2)
		{
			throw new UnsupportedOperationException();
		}
	};
	private static final long serialVersionUID = -2209589233655930756L;
	
	public StatsSet()
	{
	}
	
	public StatsSet(StatsSet set)
	{
		super(set);
	}
	
	@Override
	public StatsSet clone()
	{
		return new StatsSet(this);
	}
}