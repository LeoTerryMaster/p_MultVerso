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

public class BlessedSpiritShot extends ScriptItemHandler
{
	private static final int[] _itemIds = {3947, 3948, 3949, 3950, 3951, 3952};
	private static final int[] _skillIds = {2061, 2160, 2161, 2162, 2163, 2164};
	
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
		if(weaponInst.getChargedSpiritshot() == 2)
		{
			return false;
		}
		int spiritshotId = item.getItemId();
		int grade = weaponItem.getCrystalType().gradeOrd();
		int blessedsoulSpiritConsumption = weaponItem.getSpiritShotCount();
		if(blessedsoulSpiritConsumption == 0)
		{
			if(isAutoSoulShot)
			{
				player.removeAutoSoulShot(Integer.valueOf(SoulshotId));
				player.sendPacket(new ExAutoSoulShot(SoulshotId, false), new SystemMessage(1434).addItemName(spiritshotId));
				return false;
			}
			player.sendPacket(Msg.CANNOT_USE_SPIRITSHOTS);
			return false;
		}
		if(grade == 0 && spiritshotId != 3947 || grade == 1 && spiritshotId != 3948 || grade == 2 && spiritshotId != 3949 || grade == 3 && spiritshotId != 3950 || grade == 4 && spiritshotId != 3951 || grade == 5 && spiritshotId != 3952)
		{
			if(isAutoSoulShot)
			{
				return false;
			}
			player.sendPacket(Msg.SPIRITSHOT_DOES_NOT_MATCH_WEAPON_GRADE);
			return false;
		}
		if(Config.ALT_CONSUME_SPIRITSHOTS && !player.getInventory().destroyItem(item, (long) blessedsoulSpiritConsumption))
		{
			if(isAutoSoulShot)
			{
				player.removeAutoSoulShot(Integer.valueOf(SoulshotId));
				player.sendPacket(new ExAutoSoulShot(SoulshotId, false), new SystemMessage(1434).addItemName(spiritshotId));
				return false;
			}
			player.sendPacket(Msg.NOT_ENOUGH_SPIRITSHOTS);
			return false;
		}
		weaponInst.setChargedSpiritshot(2);
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