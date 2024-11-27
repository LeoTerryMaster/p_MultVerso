package l2.gameserver.model.items.listeners;

import l2.gameserver.listener.inventory.OnEquipListener;
import l2.gameserver.model.Playable;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.network.l2.s2c.SkillCoolTime;
import l2.gameserver.network.l2.s2c.SkillList;
import l2.gameserver.stats.Formulas;
import l2.gameserver.tables.SkillTable;
import l2.gameserver.templates.item.ItemTemplate;

public final class ItemSkillsListener implements OnEquipListener
{
	private static final ItemSkillsListener _instance = new ItemSkillsListener();
	
	public static ItemSkillsListener getInstance()
	{
		return _instance;
	}
	
	@Override
	public void onUnequip(int slot, ItemInstance item, Playable actor)
	{
		Player player = (Player) actor;
		ItemTemplate it = item.getTemplate();
		Skill[] itemSkills = it.getAttachedSkills();
		Skill enchant4Skill = it.getEnchant4Skill();
		player.removeTriggers(it);
		if(itemSkills != null && itemSkills.length > 0)
		{
			for(Skill itemSkill : itemSkills)
			{
				if(itemSkill.getId() >= 26046 && itemSkill.getId() <= 26048)
				{
					int level = player.getSkillLevel(itemSkill.getId());
					int newlevel = level - 1;
					if(newlevel > 0)
					{
						player.addSkill(SkillTable.getInstance().getInfo(itemSkill.getId(), newlevel), false);
						continue;
					}
					player.removeSkillById(itemSkill.getId());
					continue;
				}
				player.removeSkill(itemSkill, false);
			}
		}
		if(enchant4Skill != null)
		{
			player.removeSkill(enchant4Skill, false);
		}
		if(itemSkills.length > 0 || enchant4Skill != null)
		{
			player.sendPacket(new SkillList(player));
			player.updateStats();
		}
	}
	
	@Override
	public void onEquip(int slot, ItemInstance item, Playable actor)
	{
		Player player = (Player) actor;
		ItemTemplate it = item.getTemplate();
		Skill[] itemSkills = it.getAttachedSkills();
		Skill enchant4Skill = null;
		if(item.getEnchantLevel() >= 4)
		{
			enchant4Skill = it.getEnchant4Skill();
		}
		if(it.getType2() == 0 && player.getGradePenalty() > 0)
		{
			return;
		}
		player.addTriggers(it);
		boolean needSendInfo = false;
		if(itemSkills.length > 0)
		{
			for(Skill itemSkill : itemSkills)
			{
				if(itemSkill.getId() >= 26046 && itemSkill.getId() <= 26048)
				{
					int level;
					int newlevel = level = player.getSkillLevel(itemSkill.getId());
					if(level > 0)
					{
						if(SkillTable.getInstance().getInfo(itemSkill.getId(), level + 1) != null)
						{
							newlevel = level + 1;
						}
					}
					else
					{
						newlevel = 1;
					}
					if(newlevel == level)
						continue;
					player.addSkill(SkillTable.getInstance().getInfo(itemSkill.getId(), newlevel), false);
					continue;
				}
				if(player.getSkillLevel(itemSkill.getId()) >= itemSkill.getLevel())
					continue;
				player.addSkill(itemSkill, false);
				if(!itemSkill.isActive())
					continue;
				long reuseDelay = Formulas.calcSkillReuseDelay(player, itemSkill);
				if((reuseDelay = Math.min(reuseDelay, 30000)) <= 0 || player.isSkillDisabled(itemSkill))
					continue;
				player.disableSkill(itemSkill, reuseDelay);
				needSendInfo = true;
			}
		}
		if(enchant4Skill != null)
		{
			player.addSkill(enchant4Skill, false);
		}
		if(itemSkills.length > 0 || enchant4Skill != null)
		{
			player.sendPacket(new SkillList(player));
			player.updateStats();
			if(needSendInfo)
			{
				player.sendPacket(new SkillCoolTime(player));
			}
		}
	}
}