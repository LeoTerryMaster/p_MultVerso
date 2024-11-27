package handler.items;

import l2.commons.util.Rnd;
import l2.gameserver.Config;
import l2.gameserver.cache.Msg;
import l2.gameserver.model.Playable;
import l2.gameserver.model.Player;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.network.l2.s2c.ExAutoSoulShot;
import l2.gameserver.network.l2.s2c.MagicSkillUse;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.stats.Stats;
import l2.gameserver.templates.item.WeaponTemplate;

public class SoulShots extends ScriptItemHandler
{
	private static final int[] _itemIds = {5789, 1835, 1463, 1464, 1465, 1466, 1467};
	private static final int[] _skillIds = {2039, 2150, 2151, 2152, 2153, 2154};
	
	@Override
	public boolean useItem(Playable playable, ItemInstance item, boolean ctrl)
	{
		if(playable == null || !playable.isPlayer())
		{
			return false;
		}
		Player player = (Player) playable;
		WeaponTemplate weaponItem = player.getActiveWeaponItem();
		ItemInstance weaponInst = player.getActiveWeaponInstance();
		int SoulshotId = item.getItemId();
		boolean isAutoSoulShot = false;
		if(player.getAutoSoulShot().contains(SoulshotId))
		{
			isAutoSoulShot = true;
		}
		if(weaponInst == null)
		{
			if(!isAutoSoulShot)
			{
				player.sendPacket(Msg.CANNOT_USE_SOULSHOTS);
			}
			return false;
		}
		if(weaponInst.getChargedSoulshot() != 0)
		{
			return false;
		}
		int grade = weaponItem.getCrystalType().gradeOrd();
		int soulShotConsumption = weaponItem.getSoulShotCount();
		if(soulShotConsumption == 0)
		{
			if(isAutoSoulShot)
			{
				player.removeAutoSoulShot(Integer.valueOf(SoulshotId));
				player.sendPacket(new ExAutoSoulShot(SoulshotId, false), new SystemMessage(1434).addItemName(SoulshotId));
				return false;
			}
			player.sendPacket(Msg.CANNOT_USE_SOULSHOTS);
			return false;
		}
		if(grade == 0 && SoulshotId != 5789 && SoulshotId != 1835 || grade == 1 && SoulshotId != 1463 || grade == 2 && SoulshotId != 1464 || grade == 3 && SoulshotId != 1465 || grade == 4 && SoulshotId != 1466 || grade == 5 && SoulshotId != 1467)
		{
			if(isAutoSoulShot)
			{
				return false;
			}
			player.sendPacket(Msg.SOULSHOT_DOES_NOT_MATCH_WEAPON_GRADE);
			return false;
		}
		int newSS;
		if(weaponItem.getItemType() == WeaponTemplate.WeaponType.BOW && (newSS = (int) player.calcStat(Stats.SS_USE_BOW, (double) soulShotConsumption, null, null)) < soulShotConsumption && Rnd.chance(player.calcStat(Stats.SS_USE_BOW_CHANCE, (double) soulShotConsumption, null, null)))
		{
			soulShotConsumption = newSS;
		}
		if(Config.ALT_CONSUME_SOULSHOTS && !player.getInventory().destroyItem(item, (long) soulShotConsumption))
		{
			player.sendPacket(Msg.NOT_ENOUGH_SOULSHOTS);
			return false;
		}
		weaponInst.setChargedSoulshot(1);
		player.sendPacket(Msg.POWER_OF_THE_SPIRITS_ENABLED);
		player.broadcastPacket(new MagicSkillUse(player, player, _skillIds[grade], 1, 0, 0));
		return true;
	}
	
	@Override
	public final int[] getItemIds()
	{
		return _itemIds;
	}
	
	@Override
	protected boolean isShotsHandler()
	{
		return true;
	}
}