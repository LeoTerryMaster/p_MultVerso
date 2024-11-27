package ai;

import l2.commons.threading.RunnableImpl;
import l2.commons.util.Rnd;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.ai.Fighter;
import l2.gameserver.data.xml.holder.NpcHolder;
import l2.gameserver.model.Creature;
import l2.gameserver.model.instances.NpcInstance;

public class GuardoftheGrave extends Fighter
{
	private static final int DESPAWN_TIME = 90000;
	private static final int CHIEFTAINS_TREASURE_CHEST = 18816;
	
	public GuardoftheGrave(NpcInstance actor)
	{
		super(actor);
		actor.setIsInvul(true);
		actor.startImmobilized();
	}
	
	@Override
	protected void onEvtSpawn()
	{
		super.onEvtSpawn();
		ThreadPoolManager.getInstance().schedule(new DeSpawnTask(), (long) (90000 + Rnd.get(1, 30)));
	}
	
	@Override
	protected boolean checkTarget(Creature target, int range)
	{
		NpcInstance actor = getActor();
		if(actor != null && target != null && !actor.isInRange(target, (long) actor.getAggroRange()))
		{
			actor.getAggroList().remove(target, true);
			return false;
		}
		return super.checkTarget(target, range);
	}
	
	protected void spawnChest(NpcInstance actor)
	{
		try
		{
			NpcInstance npc = NpcHolder.getInstance().getTemplate(18816).getNewInstance();
			npc.setSpawnedLoc(actor.getLoc());
			npc.setCurrentHpMp((double) npc.getMaxHp(), (double) npc.getMaxMp(), true);
			npc.spawnMe(npc.getSpawnedLoc());
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private class DeSpawnTask extends RunnableImpl
	{
		@Override
		public void runImpl()
		{
			NpcInstance actor = getActor();
			spawnChest(actor);
			actor.deleteMe();
		}
	}
}