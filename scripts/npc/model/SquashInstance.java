package npc.model;

import l2.commons.lang.reference.HardReference;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.instances.SpecialMonsterInstance;
import l2.gameserver.templates.npc.NpcTemplate;

public class SquashInstance extends SpecialMonsterInstance
{
	public static final int Young_Squash = 12774;
	public static final int High_Quality_Squash = 12775;
	public static final int Low_Quality_Squash = 12776;
	public static final int Large_Young_Squash = 12777;
	public static final int High_Quality_Large_Squash = 12778;
	public static final int Low_Quality_Large_Squash = 12779;
	public static final int King_Squash = 13016;
	public static final int Emperor_Squash = 13017;
	private HardReference<Player> _spawnerRef;
	
	public SquashInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	public Player getSpawner()
	{
		return _spawnerRef.get();
	}
	
	public void setSpawner(Player spawner)
	{
		_spawnerRef = spawner.getRef();
	}
	
	@Override
	public void reduceCurrentHp(double i, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp, boolean canReflect, boolean transferDamage, boolean isDot, boolean sendMessage)
	{
		if(attacker.getActiveWeaponInstance() == null)
		{
			return;
		}
		int weaponId = attacker.getActiveWeaponInstance().getItemId();
		if((getNpcId() == 12779 || getNpcId() == 12778 || getNpcId() == 13017) && weaponId != 4202 && weaponId != 5133 && weaponId != 5817 && weaponId != 7058 && weaponId != 8350)
		{
			return;
		}
		i = 1.0;
		super.reduceCurrentHp(i, attacker, skill, awake, standUp, directHp, canReflect, transferDamage, isDot, sendMessage);
	}
	
	@Override
	public long getRegenTick()
	{
		return 0;
	}
	
	@Override
	public boolean canChampion()
	{
		return false;
	}
}