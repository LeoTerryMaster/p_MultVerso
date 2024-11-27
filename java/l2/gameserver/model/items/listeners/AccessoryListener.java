package l2.gameserver.model.items.listeners;

import l2.gameserver.listener.inventory.OnEquipListener;
import l2.gameserver.model.Playable;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.items.ItemInstance;

public final class AccessoryListener implements OnEquipListener
{
	private static final AccessoryListener _instance = new AccessoryListener();
	
	public static AccessoryListener getInstance()
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
		Player player = (Player) actor;
		if(item.getBodyPart() == 2097152 && item.getTemplate().getAttachedSkills().length > 0)
		{
			int agathionId = player.getAgathionId();
			int transformNpcId = player.getTransformationTemplate();
			for(Skill skill : item.getTemplate().getAttachedSkills())
			{
				if(agathionId > 0 && skill.getNpcId() == agathionId)
				{
					player.setAgathion(0);
				}
				if(skill.getNpcId() != transformNpcId || skill.getSkillType() != Skill.SkillType.TRANSFORMATION)
					continue;
				player.setTransformation(0);
			}
		}
		if(item.isAccessory() || item.getTemplate().isTalisman() || item.getTemplate().isBracelet())
		{
			player.sendUserInfo(true);
		}
		else
		{
			player.broadcastCharInfo();
		}
	}
	
	@Override
	public void onEquip(int slot, ItemInstance item, Playable actor)
	{
		if(!item.isEquipable())
		{
			return;
		}
		Player player = (Player) actor;
		if(item.isAccessory() || item.getTemplate().isTalisman() || item.getTemplate().isBracelet())
		{
			player.sendUserInfo(true);
		}
		else
		{
			player.broadcastCharInfo();
		}
	}
}