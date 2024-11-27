package l2.gameserver.model.items.listeners;

import l2.gameserver.data.xml.holder.OptionDataHolder;
import l2.gameserver.listener.inventory.OnEquipListener;
import l2.gameserver.model.Playable;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.network.l2.s2c.SkillCoolTime;
import l2.gameserver.network.l2.s2c.SkillList;
import l2.gameserver.templates.OptionDataTemplate;

public final class ItemAugmentationListener implements OnEquipListener
{
	private static final ItemAugmentationListener _instance = new ItemAugmentationListener();
	
	public static ItemAugmentationListener getInstance()
	{
		return _instance;
	}
	
	@Override
	public void onUnequip(int slot, ItemInstance item, Playable actor)
	{
		if(!item.isEquipable())
		{
			return;
		}
		if(!item.isAugmented())
		{
			return;
		}
		Player player = actor.getPlayer();
		int[] stats = {item.getVariationStat1(), item.getVariationStat2()};
		boolean sendList = false;
		for(int i : stats)
		{
			OptionDataTemplate template = OptionDataHolder.getInstance().getTemplate(i);
			if(template == null)
				continue;
			player.removeStatsOwner(template);
			for(Skill skill : template.getSkills())
			{
				sendList = true;
				player.removeSkill(skill);
			}
			player.removeTriggers(template);
		}
		if(sendList)
		{
			player.sendPacket(new SkillList(player));
		}
		player.updateStats();
	}
	
	@Override
	public void onEquip(int slot, ItemInstance item, Playable actor)
	{
		if(!item.isEquipable())
		{
			return;
		}
		if(!item.isAugmented())
		{
			return;
		}
		Player player = actor.getPlayer();
		if(player.getExpertisePenalty(item) > 0)
		{
			return;
		}
		int[] stats = {item.getVariationStat1(), item.getVariationStat2()};
		boolean sendList = false;
		boolean sendReuseList = false;
		for(int i : stats)
		{
			OptionDataTemplate template = OptionDataHolder.getInstance().getTemplate(i);
			if(template == null)
				continue;
			player.addStatFuncs(template.getStatFuncs(template));
			for(Skill skill : template.getSkills())
			{
				sendList = true;
				player.addSkill(skill);
				if(!player.isSkillDisabled(skill))
					continue;
				sendReuseList = true;
			}
			player.addTriggers(template);
		}
		if(sendList)
		{
			player.sendPacket(new SkillList(player));
		}
		if(sendReuseList)
		{
			player.sendPacket(new SkillCoolTime(player));
		}
		player.updateStats();
	}
}