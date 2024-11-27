package npc.model;

import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.instances.MonsterInstance;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2.gameserver.templates.npc.NpcTemplate;

public class SeducedInvestigatorInstance extends MonsterInstance
{
	public SeducedInvestigatorInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		setHasChatWindow(true);
	}
	
	@Override
	public void showChatWindow(Player player, int val, Object... arg)
	{
		player.sendPacket(new NpcHtmlMessage(player, this, "common/seducedinvestigator.htm", val));
	}
	
	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		Player player = attacker.getPlayer();
		if(player == null)
		{
			return false;
		}
		return !player.isPlayable();
	}
	
	@Override
	public boolean isMovementDisabled()
	{
		return true;
	}
	
	@Override
	public boolean canChampion()
	{
		return false;
	}
}