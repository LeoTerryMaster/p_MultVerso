package l2.gameserver.model;

import l2.commons.collections.MultiValueSet;
import l2.commons.lang.reference.HardReference;
import l2.commons.threading.RunnableImpl;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.cache.Msg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class Request extends MultiValueSet<String>
{
	private static final long serialVersionUID = 1;
	private static final Logger _log = LoggerFactory.getLogger(Request.class);
	private static final AtomicInteger _nextId = new AtomicInteger();
	private final int _id = _nextId.incrementAndGet();
	private final L2RequestType _type;
	private final HardReference<Player> _requestor;
	private final HardReference<Player> _reciever;
	private boolean _isRequestorConfirmed;
	private boolean _isRecieverConfirmed;
	private boolean _isCancelled;
	private boolean _isDone;
	private long _timeout;
	private Future<?> _timeoutTask;
	
	public Request(L2RequestType type, Player requestor, Player reciever)
	{
		_requestor = requestor.getRef();
		_reciever = reciever.getRef();
		_type = type;
		requestor.setRequest(this);
		reciever.setRequest(this);
	}
	
	public Request setTimeout(long timeout)
	{
		_timeout = timeout > 0 ? System.currentTimeMillis() + timeout : 0;
		_timeoutTask = ThreadPoolManager.getInstance().schedule(new RunnableImpl()
		{
			
			@Override
			public void runImpl() throws Exception
			{
				timeout();
			}
		}, timeout);
		return this;
	}
	
	public int getId()
	{
		return _id;
	}
	
	public void cancel()
	{
		_isCancelled = true;
		if(_timeoutTask != null)
		{
			_timeoutTask.cancel(false);
		}
		_timeoutTask = null;
		Player player = getRequestor();
		if(player != null && player.getRequest() == this)
		{
			player.setRequest(null);
		}
		if((player = getReciever()) != null && player.getRequest() == this)
		{
			player.setRequest(null);
		}
	}
	
	public void done()
	{
		_isDone = true;
		if(_timeoutTask != null)
		{
			_timeoutTask.cancel(false);
		}
		_timeoutTask = null;
		Player player = getRequestor();
		if(player != null && player.getRequest() == this)
		{
			player.setRequest(null);
		}
		if((player = getReciever()) != null && player.getRequest() == this)
		{
			player.setRequest(null);
		}
	}
	
	public void timeout()
	{
		Player player = getReciever();
		if(player != null && player.getRequest() == this)
		{
			player.sendPacket(Msg.TIME_EXPIRED);
		}
		cancel();
	}
	
	public Player getOtherPlayer(Player player)
	{
		if(player == getRequestor())
		{
			return getReciever();
		}
		if(player == getReciever())
		{
			return getRequestor();
		}
		return null;
	}
	
	public Player getRequestor()
	{
		return _requestor.get();
	}
	
	public Player getReciever()
	{
		return _reciever.get();
	}
	
	public boolean isInProgress()
	{
		if(_isCancelled)
		{
			return false;
		}
		if(_isDone)
		{
			return false;
		}
		if(_timeout == 0)
		{
			return true;
		}
		return _timeout > System.currentTimeMillis();
	}
	
	public boolean isTypeOf(L2RequestType type)
	{
		return _type == type;
	}
	
	public void confirm(Player player)
	{
		if(player == getRequestor())
		{
			_isRequestorConfirmed = true;
		}
		else if(player == getReciever())
		{
			_isRecieverConfirmed = true;
		}
	}
	
	public boolean isConfirmed(Player player)
	{
		if(player == getRequestor())
		{
			return _isRequestorConfirmed;
		}
		if(player == getReciever())
		{
			return _isRecieverConfirmed;
		}
		return false;
	}
	
	public enum L2RequestType
	{
		CUSTOM,
		PARTY,
		PARTY_ROOM,
		CLAN,
		ALLY,
		TRADE,
		TRADE_REQUEST,
		FRIEND,
		CHANNEL,
		DUEL;
	}
}