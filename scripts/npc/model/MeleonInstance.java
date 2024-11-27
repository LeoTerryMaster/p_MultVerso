package npc.model;

import l2.commons.lang.reference.HardReference;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.instances.SpecialMonsterInstance;
import l2.gameserver.templates.npc.NpcTemplate;

public class MeleonInstance extends SpecialMonsterInstance
{
	public static final int Young_Watermelon = 13271;
	public static final int Rain_Watermelon = 13273;
	public static final int Defective_Watermelon = 13272;
	public static final int Young_Honey_Watermelon = 13275;
	public static final int Rain_Honey_Watermelon = 13277;
	public static final int Defective_Honey_Watermelon = 13276;
	public static final int Large_Rain_Watermelon = 13274;
	public static final int Large_Rain_Honey_Watermelon = 13278;
	private HardReference<Player> _spawnerRef;
	
	public MeleonInstance(int objectId, NpcTemplate template)
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
		if(getNpcId() == 13276 || getNpcId() == 13277 || getNpcId() == 13278)
		{
			if(weaponId != 4202 && weaponId != 5133 && weaponId != 5817 && weaponId != 7058 && weaponId != 8350)
			{
				return;
			}
			i = 1.0;
		}
		else if(getNpcId() == 13273 || getNpcId() == 13272 || getNpcId() == 13274)
		{
			i = 5.0;
		}
		else
		{
			return;
		}
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