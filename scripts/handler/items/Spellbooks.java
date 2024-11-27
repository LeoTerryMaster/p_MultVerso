package handler.items;

import gnu.trove.TIntHashSet;
import l2.gameserver.data.xml.holder.SkillAcquireHolder;
import l2.gameserver.model.Playable;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.SkillLearn;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.MagicSkillUse;
import l2.gameserver.network.l2.s2c.SkillList;
import l2.gameserver.network.l2.s2c.SystemMessage2;
import l2.gameserver.tables.SkillTable;

import java.util.List;

public class Spellbooks extends ScriptItemHandler
{
	private final int[] _itemIds;
	
	public Spellbooks()
	{
		TIntHashSet list = new TIntHashSet();
		List<SkillLearn> l = SkillAcquireHolder.getInstance().getAllNormalSkillTreeWithForgottenScrolls();
		for(SkillLearn learn : l)
		{
			list.add(learn.getItemId());
		}
		_itemIds = list.toArray();
	}
	
	@Override
	public boolean useItem(Playable playable, ItemInstance item, boolean ctrl)
	{
		if(!playable.isPlayer())
		{
			return false;
		}
		Player player = (Player) playable;
		if(item.getCount() < 1)
		{
			player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
			return false;
		}
		List<SkillLearn> list = SkillAcquireHolder.getInstance().getSkillLearnListByItemId(player, item.getItemId());
		if(list.isEmpty())
		{
			player.sendPacket(new SystemMessage2(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(item.getItemId()));
			return false;
		}
		boolean alreadyHas = true;
		for(SkillLearn learn : list)
		{
			if(player.getSkillLevel(Integer.valueOf(learn.getId())) == learn.getLevel())
				continue;
			alreadyHas = false;
			break;
		}
		if(alreadyHas)
		{
			player.sendPacket(new SystemMessage2(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(item.getItemId()));
			return false;
		}
		boolean wrongLvl = false;
		for(SkillLearn learn : list)
		{
			if(player.getLevel() >= learn.getMinLevel())
				continue;
			wrongLvl = true;
		}
		if(wrongLvl)
		{
			player.sendPacket(new SystemMessage2(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(item.getItemId()));
			return false;
		}
		if(!player.consumeItem(item.getItemId(), 1))
		{
			return false;
		}
		for(SkillLearn skillLearn : list)
		{
			Skill skill = SkillTable.getInstance().getInfo(skillLearn.getId(), skillLearn.getLevel());
			if(skill == null)
				continue;
			player.sendPacket(new SystemMessage2(SystemMsg.YOU_HAVE_EARNED_S1_SKILL).addSkillName(skill.getId(), skill.getLevel()));
			player.addSkill(skill, true);
		}
		player.updateStats();
		player.sendPacket(new SkillList(player));
		player.broadcastPacket(new MagicSkillUse(player, player, 2790, 1, 1, 0));
		return true;
	}
	
	@Override
	public int[] getItemIds()
	{
		return _itemIds;
	}
}