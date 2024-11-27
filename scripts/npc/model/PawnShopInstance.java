package npc.model;

import l2.gameserver.Config;
import l2.gameserver.model.Player;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.templates.npc.NpcTemplate;
import services.pawnshop.PawnShop;

public class PawnShopInstance extends NpcInstance
{
	public PawnShopInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void showChatWindow(Player player, int val, Object... replace)
	{
		if(val == 0 && Config.PAWNSHOP_ENABLED)
		{
			PawnShop.showStartPage(player, this);
			return;
		}
		super.showChatWindow(player, val, replace);
	}
}