package npc.model.residences.castle;

import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.templates.npc.NpcTemplate;

public class CastleFakeTowerInstance extends NpcInstance
{
	public CastleFakeTowerInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public boolean isAutoAttackable(Creature player)
	{
		return false;
	}
	
	@Override
	public void showChatWindow(Player player, int val, Object... arg)
	{
	}
	
	@Override
	public void showChatWindow(Player player, String filename, Object... replace)
	{
	}
	
	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}
	
	@Override
	public boolean isInvul()
	{
		return true;
	}
	
	@Override
	public boolean isFearImmune()
	{
		return true;
	}
	
	@Override
	public boolean isParalyzeImmune()
	{
		return true;
	}
	
	@Override
	public boolean isLethalImmune()
	{
		return true;
	}
}