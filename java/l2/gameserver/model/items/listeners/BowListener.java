package l2.gameserver.model.items.listeners;

import l2.gameserver.listener.inventory.OnEquipListener;
import l2.gameserver.model.Playable;
import l2.gameserver.model.Player;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.templates.item.WeaponTemplate;

public final class BowListener implements OnEquipListener
{
	private static final BowListener _instance = new BowListener();
	
	public static BowListener getInstance()
	{
		return _instance;
	}
	
	@Override
	public void onUnequip(int slot, ItemInstance item, Playable actor)
	{
		if(!item.isEquipable() || slot != 7)
		{
			return;
		}
		Player player = (Player) actor;
		if(item.getItemType() == WeaponTemplate.WeaponType.BOW || item.getItemType() == WeaponTemplate.WeaponType.ROD)
		{
			player.getInventory().setPaperdollItem(8, null);
		}
	}
	
	@Override
	public void onEquip(int slot, ItemInstance item, Playable actor)
	{
		if(!item.isEquipable() || slot != 7)
		{
			return;
		}
		Player player = (Player) actor;
		ItemInstance arrow;
		if(item.getItemType() == WeaponTemplate.WeaponType.BOW && (arrow = player.getInventory().findArrowForBow(item.getTemplate())) != null)
		{
			player.getInventory().setPaperdollItem(8, arrow);
		}
		ItemInstance bait;
		if(item.getItemType() == WeaponTemplate.WeaponType.ROD && (bait = player.getInventory().findEquippedLure()) != null)
		{
			player.getInventory().setPaperdollItem(8, bait);
		}
	}
}