package l2.gameserver.model.entity.events.actions;

import l2.gameserver.Config;
import l2.gameserver.model.GameObjectsStorage;
import l2.gameserver.model.Player;
import l2.gameserver.model.World;
import l2.gameserver.model.entity.events.EventAction;
import l2.gameserver.model.entity.events.GlobalEvent;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.network.l2.components.ChatType;
import l2.gameserver.network.l2.components.NpcString;
import l2.gameserver.network.l2.s2c.NpcSay;
import l2.gameserver.utils.MapUtils;

public class NpcSayAction implements EventAction
{
	private final int _npcId;
	private final int _range;
	private final ChatType _chatType;
	private final NpcString _text;
	
	public NpcSayAction(int npcId, int range, ChatType type, NpcString string)
	{
		_npcId = npcId;
		_range = range;
		_chatType = type;
		_text = string;
	}
	
	@Override
	public void call(GlobalEvent event)
	{
		NpcInstance npc = GameObjectsStorage.getByNpcId(_npcId);
		if(npc == null)
		{
			return;
		}
		if(_range <= 0)
		{
			int rx = MapUtils.regionX(npc);
			int ry = MapUtils.regionY(npc);
			int offset = Config.SHOUT_OFFSET;
			for(Player player : GameObjectsStorage.getAllPlayersForIterate())
			{
				if(npc.getReflection() != player.getReflection())
					continue;
				int tx = MapUtils.regionX(player);
				int ty = MapUtils.regionY(player);
				if(tx < rx - offset || tx > rx + offset || ty < ry - offset || ty > ry + offset)
					continue;
				packet(npc, player);
			}
		}
		else
		{
			for(Player player : World.getAroundPlayers(npc, _range, Math.max(_range / 2, 200)))
			{
				if(npc.getReflection() != player.getReflection())
					continue;
				packet(npc, player);
			}
		}
	}
	
	private void packet(NpcInstance npc, Player player)
	{
		player.sendPacket(new NpcSay(npc, _chatType, _text));
	}
}