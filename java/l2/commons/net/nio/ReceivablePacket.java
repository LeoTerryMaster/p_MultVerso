package l2.commons.net.nio;

public abstract class ReceivablePacket<T> extends AbstractPacket<T> implements Runnable
{
	protected int getAvaliableBytes()
	{
		return getByteBuffer().remaining();
	}
	
	protected void readB(byte[] dst)
	{
		getByteBuffer().get(dst);
	}
	
	protected void readB(byte[] dst, int offset, int len)
	{
		getByteBuffer().get(dst, offset, len);
	}
	
	protected int readC()
	{
		return getByteBuffer().get() & 255;
	}
	
	protected int readH()
	{
		return getByteBuffer().getShort() & 65535;
	}
	
	protected int readD()
	{
		return getByteBuffer().getInt();
	}
	
	protected long readUD()
	{
		return (long) getByteBuffer().getInt() & 0xFFFFFFFFL;
	}
	
	protected long readQ()
	{
		return getByteBuffer().getLong();
	}
	
	protected double readF()
	{
		return getByteBuffer().getDouble();
	}
	
	protected String readS()
	{
		char ch;
		StringBuilder sb = new StringBuilder();
		while((ch = getByteBuffer().getChar()) != '\u0000')
		{
			sb.append(ch);
		}
		return sb.toString();
	}
	
	protected abstract boolean read();
}