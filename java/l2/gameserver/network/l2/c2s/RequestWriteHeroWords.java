package l2.gameserver.network.l2.c2s;

import l2.gameserver.model.Player;
import l2.gameserver.model.entity.oly.HeroController;

public class RequestWriteHeroWords extends L2GameClientPacket
{
	private String _heroWords;
	
	@Override
	protected void readImpl()
	{
		_heroWords = readS();
	}
	
	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null || !player.isHero())
		{
			return;
		}
		if(_heroWords == null || _heroWords.length() > 300)
		{
			return;
		}
		HeroController.getInstance().setHeroMessage(player.getObjectId(), _heroWords);
	}
}