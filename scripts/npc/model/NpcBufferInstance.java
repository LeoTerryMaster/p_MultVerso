package npc.model;

import l2.gameserver.model.Player;
import l2.gameserver.model.instances.MerchantInstance;
import l2.gameserver.templates.npc.NpcTemplate;
import services.Buffer;

public class NpcBufferInstance extends MerchantInstance
{
	public NpcBufferInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void showChatWindow(Player player, int val, Object... replace)
	{
		if(val == 0)
		{
			Buffer.showPage(player, "npc-" + getNpcId(), this);
			return;
		}
		super.showChatWindow(player, val, replace);
	}
}