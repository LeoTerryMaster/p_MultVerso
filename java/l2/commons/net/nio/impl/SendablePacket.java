package l2.commons.net.nio.impl;

import java.nio.ByteBuffer;

public abstract class SendablePacket<T extends MMOClient> extends l2.commons.net.nio.SendablePacket<T>
{
	@Override
	protected ByteBuffer getByteBuffer()
	{
		return ((SelectorThread) Thread.currentThread()).getWriteBuffer();
	}
	
	@Override
	public T getClient()
	{
		return (T) ((SelectorThread) Thread.currentThread()).getWriteClient();
	}
	
	@Override
	protected abstract boolean write();
}