package handler.items;

import l2.gameserver.Config;
import l2.gameserver.cache.Msg;
import l2.gameserver.model.Playable;
import l2.gameserver.model.Player;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.network.l2.s2c.ExAutoSoulShot;
import l2.gameserver.network.l2.s2c.MagicSkillUse;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.templates.item.WeaponTemplate;

public class SpiritShot extends ScriptItemHandler
{
	private static final int[] _itemIds = {5790, 2509, 2510, 2511, 2512, 2513, 2514};
	private static final int[] _skillIds = {2061, 2155, 2156, 2157, 2158, 2159};
	
	@Override
	public boolean useItem(Playable playable, ItemInstance item, boolean ctrl)
	{
		if(playable == null || !playable.isPlayer())
		{
			return false;
		}
		Player player = (Player) playable;
		ItemInstance weaponInst = player.getActiveWeaponInstance();
		WeaponTemplate weaponItem = player.getActiveWeaponItem();
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
				player.sendPacket(Msg.CANNOT_USE_SPIRITSHOTS);
			}
			return false;
		}
		if(weaponInst.getChargedSpiritshot() != 0)
		{
			return false;
		}
		int SpiritshotId = item.getItemId();
		int grade = weaponItem.getCrystalType().gradeOrd();
		int soulSpiritConsumption = weaponItem.getSpiritShotCount();
		long count = item.getCount();
		if(soulSpiritConsumption == 0)
		{
			if(isAutoSoulShot)
			{
				player.removeAutoSoulShot(Integer.valueOf(SoulshotId));
				player.sendPacket(new ExAutoSoulShot(SoulshotId, false), new SystemMessage(1434).addItemName(SoulshotId));
				return false;
			}
			player.sendPacket(Msg.CANNOT_USE_SPIRITSHOTS);
			return false;
		}
		if(grade == 0 && SpiritshotId != 5790 && SpiritshotId != 2509 || grade == 1 && SpiritshotId != 2510 || grade == 2 && SpiritshotId != 2511 || grade == 3 && SpiritshotId != 2512 || grade == 4 && SpiritshotId != 2513 || grade == 5 && SpiritshotId != 2514)
		{
			if(isAutoSoulShot)
			{
				return false;
			}
			player.sendPacket(Msg.SPIRITSHOT_DOES_NOT_MATCH_WEAPON_GRADE);
			return false;
		}
		if(count < (long) soulSpiritConsumption)
		{
			if(isAutoSoulShot)
			{
				player.removeAutoSoulShot(Integer.valueOf(SoulshotId));
				player.sendPacket(new ExAutoSoulShot(SoulshotId, false), new SystemMessage(1434).addItemName(SoulshotId));
				return false;
			}
			player.sendPacket(Msg.NOT_ENOUGH_SPIRITSHOTS);
			return false;
		}
		if(Config.ALT_CONSUME_SPIRITSHOTS && !player.getInventory().destroyItem(item, (long) soulSpiritConsumption))
		{
			player.sendPacket(Msg.NOT_ENOUGH_SPIRITSHOTS);
			return false;
		}
		weaponInst.setChargedSpiritshot(1);
		player.sendPacket(Msg.POWER_OF_MANA_ENABLED);
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