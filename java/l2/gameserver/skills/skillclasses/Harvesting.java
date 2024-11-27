package l2.gameserver.skills.skillclasses;

import l2.commons.util.Rnd;
import l2.gameserver.Config;
import l2.gameserver.cache.Msg;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.instances.MonsterInstance;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.model.reward.RewardItem;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.templates.StatsSet;
import l2.gameserver.utils.ItemFunctions;

import java.util.List;

public class Harvesting extends Skill
{
	public Harvesting(StatsSet set)
	{
		super(set);
	}
	
	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		if(!activeChar.isPlayer())
		{
			return;
		}
		Player player = (Player) activeChar;
		for(Creature target : targets)
		{
			if(target == null || !target.isMonster())
				continue;
			MonsterInstance monster = (MonsterInstance) target;
			if(!monster.isSeeded())
			{
				activeChar.sendPacket(Msg.THE_HARVEST_FAILED_BECAUSE_THE_SEED_WAS_NOT_SOWN);
				continue;
			}
			if(!monster.isSeeded(player))
			{
				activeChar.sendPacket(Msg.YOU_ARE_NOT_AUTHORIZED_TO_HARVEST);
				continue;
			}
			double SuccessRate = Config.MANOR_HARVESTING_BASIC_SUCCESS;
			int diffPlayerTarget = Math.abs(activeChar.getLevel() - monster.getLevel());
			if(diffPlayerTarget > Config.MANOR_DIFF_PLAYER_TARGET)
			{
				SuccessRate -= (double) (diffPlayerTarget - Config.MANOR_DIFF_PLAYER_TARGET) * Config.MANOR_DIFF_PLAYER_TARGET_PENALTY;
			}
			if(SuccessRate < 1.0)
			{
				SuccessRate = 1.0;
			}
			if(player.isGM())
			{
				player.sendMessage(new CustomMessage("l2p.gameserver.skills.skillclasses.Harvesting.Chance", player).addNumber((long) SuccessRate));
			}
			if(!Rnd.chance(SuccessRate))
			{
				activeChar.sendPacket(Msg.THE_HARVEST_HAS_FAILED);
				monster.clearHarvest();
				continue;
			}
			RewardItem item = monster.takeHarvest();
			if(item == null)
				continue;
			if(!player.getInventory().validateCapacity(item.itemId, item.count) || !player.getInventory().validateWeight(item.itemId, item.count))
			{
				ItemInstance harvest = ItemFunctions.createItem(item.itemId);
				harvest.setCount(item.count);
				harvest.dropToTheGround(player, monster);
				continue;
			}
			player.getInventory().addItem(item.itemId, (long) ((double) item.count * Config.MANOR_HARVESTING_REWARD_RATE));
			player.sendPacket(new SystemMessage(1137).addName(player).addNumber((long) ((double) item.count * Config.MANOR_HARVESTING_REWARD_RATE)).addItemName(item.itemId));
			if(!player.isInParty())
				continue;
			SystemMessage smsg = new SystemMessage(1137).addString(player.getName()).addNumber((long) ((double) item.count * Config.MANOR_HARVESTING_REWARD_RATE)).addItemName(item.itemId);
			player.getParty().broadcastToPartyMembers(player, smsg);
		}
	}
}