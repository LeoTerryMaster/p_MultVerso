package l2.gameserver.handler.petition;

import l2.gameserver.model.Player;

public interface IPetitionHandler
{
	void handle(Player player, int typeId, String txt);
}