package l2.gameserver.templates;

import l2.gameserver.data.xml.holder.ItemHolder;
import l2.gameserver.model.base.ClassId;
import l2.gameserver.model.base.Race;
import l2.gameserver.templates.item.ItemTemplate;
import l2.gameserver.utils.Location;

import java.util.ArrayList;
import java.util.List;

public class PlayerTemplate extends CharTemplate
{
	public final ClassId classId;
	public final Race race;
	public final String className;
	public final Location spawnLoc = new Location();
	public final boolean isMale;
	private final List<ItemTemplate> _items = new ArrayList<>();
	
	public PlayerTemplate(StatsSet set)
	{
		super(set);
		classId = ClassId.VALUES[set.getInteger("classId")];
		race = Race.values()[set.getInteger("raceId")];
		className = set.getString("className");
		spawnLoc.set(new Location(set.getInteger("spawnX"), set.getInteger("spawnY"), set.getInteger("spawnZ")));
		isMale = set.getBool("isMale", true);
	}
	
	public void addItem(int itemId)
	{
		ItemTemplate item = ItemHolder.getInstance().getTemplate(itemId);
		if(item != null)
		{
			_items.add(item);
		}
	}
	
	public ItemTemplate[] getItems()
	{
		return _items.toArray(new ItemTemplate[_items.size()]);
	}
}