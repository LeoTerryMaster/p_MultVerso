package l2.gameserver.data.xml.holder;

import gnu.trove.TIntObjectHashMap;
import l2.commons.data.xml.AbstractHolder;
import l2.gameserver.templates.SoulCrystal;

public final class SoulCrystalHolder extends AbstractHolder
{
	private static final SoulCrystalHolder _instance = new SoulCrystalHolder();
	private final TIntObjectHashMap<SoulCrystal> _crystals = new TIntObjectHashMap();
	
	public static SoulCrystalHolder getInstance()
	{
		return _instance;
	}
	
	public void addCrystal(SoulCrystal crystal)
	{
		_crystals.put(crystal.getItemId(), crystal);
	}
	
	public SoulCrystal getCrystal(int item)
	{
		return _crystals.get(item);
	}
	
	public SoulCrystal[] getCrystals()
	{
		return (SoulCrystal[]) _crystals.getValues((Object[]) new SoulCrystal[_crystals.size()]);
	}
	
	@Override
	public int size()
	{
		return _crystals.size();
	}
	
	@Override
	public void clear()
	{
		_crystals.clear();
	}
}