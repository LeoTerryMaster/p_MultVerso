package l2.gameserver.network.authcomm;

import l2.gameserver.Config;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.network.authcomm.gs2as.AuthRequest;
import l2.gameserver.network.l2.GameClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class AuthServerCommunication extends Thread
{
	private static final Logger _log = LoggerFactory.getLogger(AuthServerCommunication.class);
	private static final AuthServerCommunication instance = new AuthServerCommunication();
	private final Map<String, GameClient> waitingClients = new HashMap<>();
	private final Map<String, GameClient> authedClients = new HashMap<>();
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private final Lock readLock = lock.readLock();
	private final Lock writeLock = lock.writeLock();
	private final ByteBuffer readBuffer = ByteBuffer.allocate(65536).order(ByteOrder.LITTLE_ENDIAN);
	private final ByteBuffer writeBuffer = ByteBuffer.allocate(65536).order(ByteOrder.LITTLE_ENDIAN);
	private final Queue<SendablePacket> sendQueue = new ArrayDeque<>();
	private final Lock sendLock = new ReentrantLock();
	private final AtomicBoolean isPengingWrite = new AtomicBoolean();
	private SelectionKey key;
	private Selector selector;
	private boolean shutdown;
	private boolean restart;
	
	private AuthServerCommunication()
	{
		try
		{
			selector = Selector.open();
		}
		catch(IOException e)
		{
			_log.error("", e);
		}
	}
	
	public static final AuthServerCommunication getInstance()
	{
		return instance;
	}
	
	private void connect() throws IOException
	{
		_log.info("Connecting to authserver on " + Config.GAME_SERVER_LOGIN_HOST + ":" + Config.GAME_SERVER_LOGIN_PORT);
		SocketChannel channel = SocketChannel.open();
		channel.configureBlocking(false);
		key = channel.register(selector, 8);
		channel.connect(new InetSocketAddress(Config.GAME_SERVER_LOGIN_HOST, Config.GAME_SERVER_LOGIN_PORT));
	}
	
	public void sendPacket(SendablePacket packet)
	{
		if(isShutdown())
		{
			return;
		}
		sendLock.lock();
		boolean wakeUp;
		try
		{
			sendQueue.add(packet);
			wakeUp = enableWriteInterest();
		}
		catch(CancelledKeyException e)
		{
			return;
		}
		finally
		{
			sendLock.unlock();
		}
		if(wakeUp)
		{
			selector.wakeup();
		}
	}
	
	private boolean disableWriteInterest() throws CancelledKeyException
	{
		if(isPengingWrite.compareAndSet(true, false))
		{
			key.interestOps(key.interestOps() & -5);
			return true;
		}
		return false;
	}
	
	private boolean enableWriteInterest() throws CancelledKeyException
	{
		if(!isPengingWrite.getAndSet(true))
		{
			key.interestOps(key.interestOps() | 4);
			return true;
		}
		return false;
	}
	
	protected ByteBuffer getReadBuffer()
	{
		return readBuffer;
	}
	
	protected ByteBuffer getWriteBuffer()
	{
		return writeBuffer;
	}
	
	@Override
	public void run()
	{
		while(!shutdown)
		{
			restart = false;
			
			try
			{
				Set keys;
				Iterator iterator;
				SelectionKey key;
				int opts;
				label80:
				while(!isShutdown())
				{
					connect();
					long elapsed = System.currentTimeMillis();
					int selected = selector.select(5000L);
					elapsed = System.currentTimeMillis() - elapsed;
					if(selected == 0 && elapsed < 5000L)
					{
						Iterator keyIter = selector.keys().iterator();
						
						while(keyIter.hasNext())
						{
							key = (SelectionKey) keyIter.next();
							if(key.isValid())
							{
								SocketChannel channel = (SocketChannel) key.channel();
								if(channel != null && (key.interestOps() & 8) != 0)
								{
									connect(key);
									break label80;
								}
							}
						}
					}
					else
					{
						keys = selector.selectedKeys();
						if(keys.isEmpty())
						{
							throw new IOException("Connection timeout.");
						}
						
						iterator = keys.iterator();
						
						try
						{
							while(iterator.hasNext())
							{
								key = (SelectionKey) iterator.next();
								iterator.remove();
								opts = key.readyOps();
								switch(opts)
								{
									case 8:
										connect(key);
										break label80;
								}
							}
						}
						catch(CancelledKeyException e)
						{
							break;
						}
					}
				}
				
				while(!isShutdown())
				{
					selector.select();
					keys = selector.selectedKeys();
					iterator = keys.iterator();
					
					try
					{
						while(iterator.hasNext())
						{
							key = (SelectionKey) iterator.next();
							iterator.remove();
							opts = key.readyOps();
							switch(opts)
							{
								case 1:
									read(key);
								case 2:
								case 3:
								default:
									break;
								case 4:
									write(key);
									break;
								case 5:
									write(key);
									read(key);
							}
						}
					}
					catch(CancelledKeyException e)
					{
						break;
					}
				}
			}
			catch(IOException e)
			{
				_log.error("AuthServer I/O error: " + e.getMessage());
			}
			
			close();
			
			try
			{
				Thread.sleep(5000L);
			}
			catch(InterruptedException e)
			{
			}
		}
	}
	
	private void read(SelectionKey key) throws IOException
	{
		ByteBuffer buf;
		SocketChannel channel = (SocketChannel) key.channel();
		int count = channel.read(buf = getReadBuffer());
		if(count == -1)
		{
			throw new IOException("End of stream.");
		}
		if(count == 0)
		{
			return;
		}
		buf.flip();
		while(tryReadPacket(key, buf))
		{
		}
	}
	
	private boolean tryReadPacket(SelectionKey key, ByteBuffer buf) throws IOException
	{
		int pos = buf.position();
		if(buf.remaining() > 2)
		{
			int size = buf.getShort() & 65535;
			if(size <= 2)
			{
				throw new IOException("Incorrect packet size: <= 2");
			}
			if((size -= 2) <= buf.remaining())
			{
				int limit = buf.limit();
				buf.limit(pos + size + 2);
				ReceivablePacket rp = PacketHandler.handlePacket(buf);
				if(rp != null && rp.read())
				{
					ThreadPoolManager.getInstance().execute(rp);
				}
				buf.limit(limit);
				buf.position(pos + size + 2);
				if(!buf.hasRemaining())
				{
					buf.clear();
					return false;
				}
				return true;
			}
			buf.position(pos);
		}
		buf.compact();
		return false;
	}
	
	private void write(SelectionKey key) throws IOException
	{
		SocketChannel channel = (SocketChannel) key.channel();
		ByteBuffer buf = getWriteBuffer();
		sendLock.lock();
		boolean done;
		try
		{
			SendablePacket sp;
			int i = 0;
			while(i++ < 64 && (sp = sendQueue.poll()) != null)
			{
				int headerPos = buf.position();
				buf.position(headerPos + 2);
				sp.write();
				int dataSize = buf.position() - headerPos - 2;
				if(dataSize == 0)
				{
					buf.position(headerPos);
					continue;
				}
				buf.position(headerPos);
				buf.putShort((short) (dataSize + 2));
				buf.position(headerPos + dataSize + 2);
			}
			done = sendQueue.isEmpty();
			if(done)
			{
				disableWriteInterest();
			}
		}
		finally
		{
			sendLock.unlock();
		}
		buf.flip();
		channel.write(buf);
		if(buf.remaining() > 0)
		{
			buf.compact();
			done = false;
		}
		else
		{
			buf.clear();
		}
		if(!done && enableWriteInterest())
		{
			selector.wakeup();
		}
	}
	
	private void connect(SelectionKey key) throws IOException
	{
		SocketChannel channel = (SocketChannel) key.channel();
		channel.finishConnect();
		key.interestOps(key.interestOps() & -9);
		key.interestOps(key.interestOps() | 1);
		sendPacket(new AuthRequest());
	}
	
	private void close()
	{
		restart = !shutdown;
		sendLock.lock();
		try
		{
			sendQueue.clear();
		}
		finally
		{
			sendLock.unlock();
		}
		readBuffer.clear();
		writeBuffer.clear();
		isPengingWrite.set(false);
		try
		{
			if(key != null)
			{
				key.channel().close();
				key.cancel();
			}
		}
		catch(IOException e)
		{
			
		}
		writeLock.lock();
		try
		{
			waitingClients.clear();
		}
		finally
		{
			writeLock.unlock();
		}
	}
	
	public void shutdown()
	{
		shutdown = true;
		selector.wakeup();
	}
	
	public boolean isShutdown()
	{
		return shutdown || restart;
	}
	
	public void restart()
	{
		restart = true;
		selector.wakeup();
	}
	
	public GameClient addWaitingClient(GameClient client)
	{
		writeLock.lock();
		try
		{
			GameClient gameClient = waitingClients.put(client.getLogin(), client);
			return gameClient;
		}
		finally
		{
			writeLock.unlock();
		}
	}
	
	public GameClient removeWaitingClient(String account)
	{
		writeLock.lock();
		try
		{
			GameClient gameClient = waitingClients.remove(account);
			return gameClient;
		}
		finally
		{
			writeLock.unlock();
		}
	}
	
	public GameClient addAuthedClient(GameClient client)
	{
		writeLock.lock();
		try
		{
			GameClient gameClient = authedClients.put(client.getLogin(), client);
			return gameClient;
		}
		finally
		{
			writeLock.unlock();
		}
	}
	
	public GameClient removeAuthedClient(String login)
	{
		writeLock.lock();
		try
		{
			GameClient gameClient = authedClients.remove(login);
			return gameClient;
		}
		finally
		{
			writeLock.unlock();
		}
	}
	
	public GameClient getAuthedClient(String login)
	{
		readLock.lock();
		try
		{
			GameClient gameClient = authedClients.get(login);
			return gameClient;
		}
		finally
		{
			readLock.unlock();
		}
	}
	
	public GameClient removeClient(GameClient client)
	{
		writeLock.lock();
		try
		{
			if(client.isAuthed())
			{
				GameClient gameClient = authedClients.remove(client.getLogin());
				return gameClient;
			}
			GameClient gameClient = waitingClients.remove(client.getSessionKey());
			return gameClient;
		}
		finally
		{
			writeLock.unlock();
		}
	}
	
	public String[] getAccounts()
	{
		readLock.lock();
		try
		{
			String[] arrstring = authedClients.keySet().toArray(new String[authedClients.size()]);
			return arrstring;
		}
		finally
		{
			readLock.unlock();
		}
	}
}