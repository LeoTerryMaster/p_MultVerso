package l2.gameserver.model.items.listeners;

import l2.gameserver.data.xml.holder.ArmorSetsHolder;
import l2.gameserver.listener.inventory.OnEquipListener;
import l2.gameserver.model.ArmorSet;
import l2.gameserver.model.Playable;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.network.l2.s2c.SkillList;

import java.util.ArrayList;
import java.util.List;

public final class ArmorSetListener implements OnEquipListener
{
	private static final ArmorSetListener _instance = new ArmorSetListener();
	
	public static ArmorSetListener getInstance()
	{
		return _instance;
	}
	
	@Override
	public void onEquip(int slot, ItemInstance item, Playable actor)
	{
		if(!item.isEquipable())
		{
			return;
		}
		Player player = (Player) actor;
		ItemInstance chestItem = player.getInventory().getPaperdollItem(10);
		if(chestItem == null)
		{
			return;
		}
		ArmorSet armorSet = ArmorSetsHolder.getInstance().getArmorSet(chestItem.getItemId());
		if(armorSet == null)
		{
			return;
		}
		boolean update = false;
		if(armorSet.containItem(slot, item.getItemId()))
		{
			if(armorSet.containAll(player))
			{
				List<Skill> skills = armorSet.getSkills();
				for(Skill skill : skills)
				{
					player.addSkill(skill, false);
					update = true;
				}
				if(armorSet.containShield(player))
				{
					skills = armorSet.getShieldSkills();
					for(Skill skill : skills)
					{
						player.addSkill(skill, false);
						update = true;
					}
				}
				if(armorSet.isEnchanted6(player))
				{
					skills = armorSet.getEnchant6skills();
					for(Skill skill : skills)
					{
						player.addSkill(skill, false);
						update = true;
					}
				}
			}
		}
		else if(armorSet.containShield(item.getItemId()) && armorSet.containAll(player))
		{
			List<Skill> skills = armorSet.getShieldSkills();
			for(Skill skill : skills)
			{
				player.addSkill(skill, false);
				update = true;
			}
		}
		if(update)
		{
			player.sendPacket(new SkillList(player));
			player.updateStats();
		}
	}
	
	@Override
	public void onUnequip(int slot, ItemInstance item, Playable actor)
	{
		if(!item.isEquipable())
		{
			return;
		}
		Player player = (Player) actor;
		boolean remove = false;
		List<Skill> removeSkillId1 = new ArrayList(1);
		List<Skill> removeSkillId2 = new ArrayList(1);
		List<Skill> removeSkillId3 = new ArrayList(1);
		if(slot == 10)
		{
			ArmorSet armorSet = ArmorSetsHolder.getInstance().getArmorSet(item.getItemId());
			if(armorSet == null)
			{
				return;
			}
			remove = true;
			removeSkillId1 = armorSet.getSkills();
			removeSkillId2 = armorSet.getShieldSkills();
			removeSkillId3 = armorSet.getEnchant6skills();
		}
		else
		{
			ItemInstance chestItem = player.getInventory().getPaperdollItem(10);
			if(chestItem == null)
			{
				return;
			}
			ArmorSet armorSet = ArmorSetsHolder.getInstance().getArmorSet(chestItem.getItemId());
			if(armorSet == null)
			{
				return;
			}
			if(armorSet.containItem(slot, item.getItemId()))
			{
				remove = true;
				removeSkillId1 = armorSet.getSkills();
				removeSkillId2 = armorSet.getShieldSkills();
				removeSkillId3 = armorSet.getEnchant6skills();
			}
			else if(armorSet.containShield(item.getItemId()))
			{
				remove = true;
				removeSkillId2 = armorSet.getShieldSkills();
			}
		}
		boolean update = false;
		if(remove)
		{
			for(Skill skill : removeSkillId1)
			{
				player.removeSkill(skill, false);
				update = true;
			}
			for(Skill skill : removeSkillId2)
			{
				player.removeSkill(skill);
				update = true;
			}
			for(Skill skill : removeSkillId3)
			{
				player.removeSkill(skill);
				update = true;
			}
		}
		if(update)
		{
			player.sendPacket(new SkillList(player));
			player.updateStats();
		}
	}
}