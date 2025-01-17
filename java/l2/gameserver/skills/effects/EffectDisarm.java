package l2.gameserver.skills.effects;

import l2.gameserver.model.Effect;
import l2.gameserver.model.Player;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.stats.Env;

public final class EffectDisarm extends Effect
{
	public EffectDisarm(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public boolean checkCondition()
	{
		if(!_effected.isPlayer())
		{
			return false;
		}
		Player player = _effected.getPlayer();
		if(player.isCursedWeaponEquipped() || player.getActiveWeaponFlagAttachment() != null)
		{
			return false;
		}
		return super.checkCondition();
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		Player player = (Player) _effected;
		ItemInstance wpn = player.getActiveWeaponInstance();
		if(wpn != null)
		{
			player.getInventory().unEquipItem(wpn);
			player.sendDisarmMessage(wpn);
		}
		player.startWeaponEquipBlocked();
	}
	
	@Override
	public void onExit()
	{
		super.onExit();
		_effected.stopWeaponEquipBlocked();
	}
	
	@Override
	public boolean onActionTime()
	{
		return false;
	}
}