package l2.gameserver.model.quest;

import l2.commons.threading.RunnableImpl;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.model.instances.NpcInstance;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class QuestTimer extends RunnableImpl
{
	private final String _name;
	private final NpcInstance _npc;
	private long _time;
	private QuestState _qs;
	private ScheduledFuture<?> _schedule;
	
	public QuestTimer(String name, long time, NpcInstance npc)
	{
		_name = name;
		_time = time;
		_npc = npc;
	}
	
	QuestState getQuestState()
	{
		return _qs;
	}
	
	void setQuestState(QuestState qs)
	{
		_qs = qs;
	}
	
	void start()
	{
		_schedule = ThreadPoolManager.getInstance().schedule(this, _time);
	}
	
	@Override
	public void runImpl() throws Exception
	{
		QuestState qs = getQuestState();
		if(qs != null)
		{
			qs.removeQuestTimer(getName());
			qs.getQuest().notifyEvent(getName(), qs, getNpc());
		}
	}
	
	void pause()
	{
		if(_schedule != null)
		{
			_time = _schedule.getDelay(TimeUnit.SECONDS);
			_schedule.cancel(false);
		}
	}
	
	void stop()
	{
		if(_schedule != null)
		{
			_schedule.cancel(false);
		}
	}
	
	public boolean isActive()
	{
		return _schedule != null && !_schedule.isDone();
	}
	
	public String getName()
	{
		return _name;
	}
	
	public long getTime()
	{
		return _time;
	}
	
	public NpcInstance getNpc()
	{
		return _npc;
	}
	
	@Override
	public final String toString()
	{
		return _name;
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
		return ((QuestTimer) o).getName().equals(getName());
	}
}