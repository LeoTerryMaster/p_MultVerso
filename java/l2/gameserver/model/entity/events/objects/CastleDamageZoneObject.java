package l2.gameserver.model.entity.events.objects;

public class CastleDamageZoneObject extends ZoneObject
{
	private final long _price;
	
	public CastleDamageZoneObject(String name, long price)
	{
		super(name);
		_price = price;
	}
	
	public long getPrice()
	{
		return _price;
	}
}