package l2.commons.net.nio.impl;

import java.nio.ByteBuffer;

public abstract class ReceivablePacket<T extends MMOClient> extends l2.commons.net.nio.ReceivablePacket<T>
{
	protected T _client;
	protected ByteBuffer _buf;
	
	@Override
	protected ByteBuffer getByteBuffer()
	{
		return _buf;
	}
	
	protected void setByteBuffer(ByteBuffer buf)
	{
		_buf = buf;
	}
	
	@Override
	public T getClient()
	{
		return _client;
	}
	
	protected void setClient(T client)
	{
		_client = client;
	}
	
	@Override
	protected abstract boolean read();
}