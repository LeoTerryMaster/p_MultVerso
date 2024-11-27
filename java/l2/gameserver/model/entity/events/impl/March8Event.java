package l2.gameserver.model.entity.events.impl;

import l2.commons.collections.MultiValueSet;
import l2.gameserver.Announcements;
import l2.gameserver.model.entity.events.GlobalEvent;

import java.util.Calendar;

public class March8Event extends GlobalEvent
{
	private static final long LENGTH = 604800000;
	private final Calendar _calendar = Calendar.getInstance();
	
	public March8Event(MultiValueSet<String> set)
	{
		super(set);
	}
	
	@Override
	public void initEvent()
	{
	}
	
	@Override
	public void startEvent()
	{
		super.startEvent();
		Announcements.getInstance().announceToAll("Test startEvent");
	}
	
	@Override
	public void stopEvent()
	{
		super.stopEvent();
		Announcements.getInstance().announceToAll("Test stopEvent");
	}
	
	@Override
	public void reCalcNextTime(boolean onInit)
	{
		clearActions();
		if(onInit)
		{
			_calendar.set(2, 2);
			_calendar.set(5, 8);
			_calendar.set(11, 0);
			_calendar.set(12, 0);
			_calendar.set(13, 0);
			if(_calendar.getTimeInMillis() + 604800000 < System.currentTimeMillis())
			{
				_calendar.add(1, 1);
			}
		}
		else
		{
			_calendar.add(1, 1);
		}
		registerActions();
	}
	
	@Override
	protected long startTimeMillis()
	{
		return _calendar.getTimeInMillis();
	}
}