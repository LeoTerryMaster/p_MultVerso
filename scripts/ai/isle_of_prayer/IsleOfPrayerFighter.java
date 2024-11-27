package ai.isle_of_prayer;

import l2.commons.util.Rnd;
import l2.gameserver.ai.CtrlEvent;
import l2.gameserver.ai.Fighter;
import l2.gameserver.data.xml.holder.NpcHolder;
import l2.gameserver.idfactory.IdFactory;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Party;
import l2.gameserver.model.Player;
import l2.gameserver.model.instances.MonsterInstance;
import l2.gameserver.model.instances.NpcInstance;

public class IsleOfPrayerFighter extends Fighter
{
	private static final int[] PENALTY_MOBS = {18364, 18365, 18366};
	private static final int YELLOW_CRYSTAL = 9593;
	private static final int GREEN_CRYSTAL = 9594;
	private boolean _penaltyMobsNotSpawned = true;
	
	public IsleOfPrayerFighter(NpcInstance actor)
	{
		super(actor);
	}
	
	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		Party party;
		NpcInstance actor = getActor();
		if(_penaltyMobsNotSpawned && attacker.isPlayable() && attacker.getPlayer() != null && (party = attacker.getPlayer().getParty()) != null && party.getMemberCount() > 2)
		{
			_penaltyMobsNotSpawned = false;
			for(int i = 0;i < 2;++i)
			{
				MonsterInstance npc = new MonsterInstance(IdFactory.getInstance().getNextId(), NpcHolder.getInstance().getTemplate(PENALTY_MOBS[Rnd.get(PENALTY_MOBS.length)]));
				npc.setSpawnedLoc(((MonsterInstance) actor).getMinionPosition());
				npc.setReflection(actor.getReflection());
				npc.setCurrentHpMp((double) npc.getMaxHp(), (double) npc.getMaxMp(), true);
				npc.spawnMe(npc.getSpawnedLoc());
				npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, Rnd.get(1, 100));
			}
		}
		super.onEvtAttacked(attacker, damage);
	}
	
	@Override
	protected void onEvtDead(Creature killer)
	{
		_penaltyMobsNotSpawned = true;
		Player player;
		if(killer != null && (player = killer.getPlayer()) != null)
		{
			NpcInstance actor = getActor();
			switch(actor.getNpcId())
			{
				case 22259:
				{
					if(!Rnd.chance(26))
						break;
					actor.dropItem(player, 9593, 1);
					break;
				}
				case 22263:
				{
					if(!Rnd.chance(14))
						break;
					actor.dropItem(player, 9594, 1);
				}
			}
		}
		super.onEvtDead(killer);
	}
}