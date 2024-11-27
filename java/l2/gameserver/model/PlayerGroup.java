package l2.gameserver.model;

import l2.commons.collections.EmptyIterator;
import l2.gameserver.network.l2.components.IStaticPacket;

import java.util.Iterator;

public interface PlayerGroup extends Iterable<Player>
{
	PlayerGroup EMPTY = new PlayerGroup()
	{
		@Override
		public void broadCast(IStaticPacket... packet)
		{
		}
		
		@Override
		public Iterator<Player> iterator()
		{
			return EmptyIterator.getInstance();
		}
	};
	
	void broadCast(IStaticPacket... packet);
}