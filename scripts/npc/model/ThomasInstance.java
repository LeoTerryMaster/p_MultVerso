package npc.model;

import events.SavingSnowman.SavingSnowman;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Skill;
import l2.gameserver.model.instances.MonsterInstance;
import l2.gameserver.templates.npc.NpcTemplate;

public class ThomasInstance extends MonsterInstance
{
	public ThomasInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void reduceCurrentHp(double i, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp, boolean canReflect, boolean transferDamage, boolean isDot, boolean sendMessage)
	{
		i = 10.0;
		if(attacker.getActiveWeaponInstance() != null)
		{
			switch(attacker.getActiveWeaponInstance().getItemId())
			{
				case 4202:
				case 5133:
				case 5817:
				case 7058:
				case 8350:
				{
					i = 100.0;
					break;
				}
				default:
				{
					i = 10.0;
				}
			}
		}
		super.reduceCurrentHp(i, attacker, skill, awake, standUp, directHp, canReflect, transferDamage, isDot, sendMessage);
	}
	
	@Override
	protected void onDeath(Creature killer)
	{
		Creature topdam = getAggroList().getTopDamager();
		if(topdam == null)
		{
			topdam = killer;
		}
		SavingSnowman.freeSnowman(topdam);
		super.onDeath(killer);
	}
	
	@Override
	public boolean canChampion()
	{
		return false;
	}
}