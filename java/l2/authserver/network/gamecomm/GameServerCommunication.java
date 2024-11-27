package l2.authserver.network.gamecomm;

import l2.authserver.ThreadPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.locks.Lock;

public class GameServerCommunication extends Thread
{
	private static final Logger _log = LoggerFactory.getLogger(GameServerCommunication.class);
	private static final GameServerCommunication instance = new GameServerCommunication();
	private final ByteBuffer writeBuffer = ByteBuffer.allocate(65536).order(ByteOrder.LITTLE_ENDIAN);
	private Selector selector;
	private boolean shutdown;
	
	private GameServerCommunication()
	{
	}
	
	public static GameServerCommunication getInstance()
	{
		return instance;
	}
	
	public void openServerSocket(InetAddress address, int tcpPort) throws IOException
	{
		selector = Selector.open();
		ServerSocketChannel selectable = ServerSocketChannel.open();
		selectable.configureBlocking(false);
		selectable.socket().bind(address == null ? new InetSocketAddress(tcpPort) : new InetSocketAddress(address, tcpPort));
		selectable.register(selector, selectable.validOps());
	}
	
	@Override
	public void run()
	{
		SelectionKey key = null;
		while(!isShutdown())
		{
			try
			{
				selector.select();
				Set<SelectionKey> keys = selector.selectedKeys();
				Iterator<SelectionKey> iterator = keys.iterator();
				while(iterator.hasNext())
				{
					key = iterator.next();
					iterator.remove();
					if(!key.isValid())
					{
						close(key);
						continue;
					}
					int opts = key.readyOps();
					switch(opts)
					{
						case 8:
						{
							close(key);
							break;
						}
						case 16:
						{
							accept(key);
							break;
						}
						case 4:
						{
							write(key);
							break;
						}
						case 1:
						{
							read(key);
							break;
						}
						case 5:
						{
							write(key);
							read(key);
						}
					}
				}
			}
			catch(ClosedSelectorException e)
			{
				_log.error("Selector " + selector + " closed!");
				return;
			}
			catch(IOException e)
			{
				_log.error("Gameserver I/O error: " + e.getMessage());
				close(key);
			}
			catch(Exception e)
			{
				_log.error("", e);
			}
		}
	}
	
	public void accept(SelectionKey key) throws IOException
	{
		ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
		SocketChannel sc = ssc.accept();
		sc.configureBlocking(false);
		SelectionKey clientKey = sc.register(selector, 1);
		GameServerConnection conn = new GameServerConnection(clientKey);
		clientKey.attach(conn);
		conn.setGameServer(new GameServer(conn));
	}
	
	public void read(SelectionKey key) throws IOException
	{
		SocketChannel channel = (SocketChannel) key.channel();
		GameServerConnection conn = (GameServerConnection) key.attachment();
		GameServer gs = conn.getGameServer();
		ByteBuffer buf = conn.getReadBuffer();
		int count = channel.read(buf);
		if(count == -1)
		{
			close(key);
			return;
		}
		if(count == 0)
		{
			return;
		}
		buf.flip();
		while(tryReadPacket(key, gs, buf))
		{
		}
	}
	
	protected boolean tryReadPacket(SelectionKey key, GameServer gs, ByteBuffer buf) throws IOException
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
				ReceivablePacket rp = PacketHandler.handlePacket(gs, buf);
				if(rp != null)
				{
					rp.setByteBuffer(buf);
					rp.setClient(gs);
					if(rp.read())
					{
						ThreadPoolManager.getInstance().execute(rp);
					}
					rp.setByteBuffer(null);
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
	
	public void write(SelectionKey key) throws IOException
	{
		GameServerConnection conn = (GameServerConnection) key.attachment();
		GameServer gs = conn.getGameServer();
		SocketChannel channel = (SocketChannel) key.channel();
		ByteBuffer buf = getWriteBuffer();
		conn.disableWriteInterest();
		Queue<SendablePacket> sendQueue = conn.sendQueue;
		Lock sendLock = conn.sendLock;
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
				sp.setByteBuffer(buf);
				sp.setClient(gs);
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
				conn.disableWriteInterest();
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
		if(!done && conn.enableWriteInterest())
		{
			selector.wakeup();
		}
	}
	
	private ByteBuffer getWriteBuffer()
	{
		return writeBuffer;
	}
	
	public void close(SelectionKey key)
	{
		if(key == null)
		{
			return;
		}
		try
		{
			try
			{
				GameServerConnection conn = (GameServerConnection) key.attachment();
				if(conn != null)
				{
					conn.onDisconnection();
				}
			}
			finally
			{
				key.channel().close();
				key.cancel();
			}
		}
		catch(IOException e)
		{
			_log.error("", e);
		}
	}
	
	public boolean isShutdown()
	{
		return shutdown;
	}
	
	public void setShutdown(boolean shutdown)
	{
		this.shutdown = shutdown;
	}
}