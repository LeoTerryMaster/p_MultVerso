package l2.commons.net.nio.impl;

import java.nio.ByteBuffer;

public interface IPacketHandler<T extends MMOClient>
{
	ReceivablePacket<T> handlePacket(ByteBuffer buf, T client);
}