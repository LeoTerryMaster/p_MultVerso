package l2.gameserver.model;

import java.util.ArrayList;
import java.util.Calendar;

public class ProductItem
{
	public static final long NOT_LIMITED_START_TIME = 315547200000L;
	public static final long NOT_LIMITED_END_TIME = 2127445200000L;
	public static final int NOT_LIMITED_START_HOUR = 0;
	public static final int NOT_LIMITED_END_HOUR = 23;
	public static final int NOT_LIMITED_START_MIN = 0;
	public static final int NOT_LIMITED_END_MIN = 59;
	private final int _productId;
	private final int _category;
	private final int _points;
	private final int _tabId;
	private final long _startTimeSale;
	private final long _endTimeSale;
	private final int _startHour;
	private final int _endHour;
	private final int _startMin;
	private final int _endMin;
	private ArrayList<ProductItemComponent> _components;
	
	public ProductItem(int productId, int category, int points, int tabId, long startTimeSale, long endTimeSale)
	{
		_productId = productId;
		_category = category;
		_points = points;
		_tabId = tabId;
		Calendar calendar;
		if(startTimeSale > 0)
		{
			calendar = Calendar.getInstance();
			calendar.setTimeInMillis(startTimeSale);
			_startTimeSale = startTimeSale;
			_startHour = calendar.get(11);
			_startMin = calendar.get(12);
		}
		else
		{
			_startTimeSale = 315547200000L;
			_startHour = 0;
			_startMin = 0;
		}
		if(endTimeSale > 0)
		{
			calendar = Calendar.getInstance();
			calendar.setTimeInMillis(endTimeSale);
			_endTimeSale = endTimeSale;
			_endHour = calendar.get(11);
			_endMin = calendar.get(12);
		}
		else
		{
			_endTimeSale = 2127445200000L;
			_endHour = 23;
			_endMin = 59;
		}
	}
	
	public ArrayList<ProductItemComponent> getComponents()
	{
		if(_components == null)
		{
			_components = new ArrayList();
		}
		return _components;
	}
	
	public void setComponents(ArrayList<ProductItemComponent> a)
	{
		_components = a;
	}
	
	public int getProductId()
	{
		return _productId;
	}
	
	public int getCategory()
	{
		return _category;
	}
	
	public int getPoints()
	{
		return _points;
	}
	
	public int getTabId()
	{
		return _tabId;
	}
	
	public long getStartTimeSale()
	{
		return _startTimeSale;
	}
	
	public int getStartHour()
	{
		return _startHour;
	}
	
	public int getStartMin()
	{
		return _startMin;
	}
	
	public long getEndTimeSale()
	{
		return _endTimeSale;
	}
	
	public int getEndHour()
	{
		return _endHour;
	}
	
	public int getEndMin()
	{
		return _endMin;
	}
}