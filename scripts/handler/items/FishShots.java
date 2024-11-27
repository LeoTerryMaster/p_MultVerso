package handler.items;

import l2.gameserver.cache.Msg;
import l2.gameserver.model.Playable;
import l2.gameserver.model.Player;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.network.l2.s2c.ExAutoSoulShot;
import l2.gameserver.network.l2.s2c.MagicSkillUse;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.templates.item.WeaponTemplate;

public class FishShots extends ScriptItemHandler
{
	private static final int[] _itemIds = {6535, 6536, 6537, 6538, 6539, 6540};
	private static final int[] _skillIds = {2181, 2182, 2183, 2184, 2185, 2186};
	
	@Override
	public boolean useItem(Playable playable, ItemInstance item, boolean ctrl)
	{
		if(playable == null || !playable.isPlayer())
		{
			return false;
		}
		Player player = (Player) playable;
		int FishshotId = item.getItemId();
		boolean isAutoSoulShot = false;
		if(player.getAutoSoulShot().contains(FishshotId))
		{
			isAutoSoulShot = true;
		}
		ItemInstance weaponInst = player.getActiveWeaponInstance();
		WeaponTemplate weaponItem = player.getActiveWeaponItem();
		if(weaponInst == null || weaponItem.getItemType() != WeaponTemplate.WeaponType.ROD)
		{
			if(!isAutoSoulShot)
			{
				player.sendPacket(Msg.CANNOT_USE_SOULSHOTS);
			}
			return false;
		}
		if(weaponInst.getChargedFishshot())
		{
			return false;
		}
		if(item.getCount() < 1)
		{
			if(isAutoSoulShot)
			{
				player.removeAutoSoulShot(Integer.valueOf(FishshotId));
				player.sendPacket(new ExAutoSoulShot(FishshotId, false), new SystemMessage(1434).addString(item.getName()));
				return false;
			}
			player.sendPacket(Msg.NOT_ENOUGH_SPIRITSHOTS);
			return false;
		}
		int grade = weaponItem.getCrystalType().gradeOrd();
		if(grade == 0 && FishshotId != 6535 || grade == 1 && FishshotId != 6536 || grade == 2 && FishshotId != 6537 || grade == 3 && FishshotId != 6538 || grade == 4 && FishshotId != 6539 || grade == 5 && FishshotId != 6540)
		{
			if(isAutoSoulShot)
			{
				return false;
			}
			player.sendPacket(Msg.THIS_FISHING_SHOT_IS_NOT_FIT_FOR_THE_FISHING_POLE_CRYSTAL);
			return false;
		}
		if(player.getInventory().destroyItem(item, 1))
		{
			weaponInst.setChargedFishshot(true);
			player.sendPacket(Msg.POWER_OF_MANA_ENABLED);
			player.broadcastPacket(new MagicSkillUse(player, player, _skillIds[grade], 1, 0, 0));
		}
		return true;
	}
	
	@Override
	protected boolean isShotsHandler()
	{
		return true;
	}
	
	@Override
	public int[] getItemIds()
	{
		return _itemIds;
	}
}