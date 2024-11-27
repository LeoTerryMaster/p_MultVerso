package npc.model.residences.clanhall;

import l2.gameserver.model.Creature;
import l2.gameserver.scripts.Functions;
import l2.gameserver.templates.npc.NpcTemplate;
import npc.model.residences.SiegeGuardInstance;

public abstract class _34BossMinionInstance extends SiegeGuardInstance implements _34SiegeGuard
{
	public _34BossMinionInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onDeath(Creature killer)
	{
		setCurrentHp(1.0, true);
	}
	
	@Override
	public void onSpawn()
	{
		super.onSpawn();
		Functions.npcShout(this, spawnChatSay());
	}
	
	public abstract String spawnChatSay();
	
	@Override
	public abstract String teleChatSay();
}