package l2.gameserver.skills.skillclasses;

import l2.commons.util.Rnd;
import l2.gameserver.Config;
import l2.gameserver.cache.Msg;
import l2.gameserver.data.xml.holder.ItemHolder;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Manor;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.instances.MonsterInstance;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.network.l2.s2c.SystemMessage2;
import l2.gameserver.templates.StatsSet;

import java.util.List;

public class Sowing extends Skill
{
	public Sowing(StatsSet set)
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
		int seedId = player.getUseSeed();
		boolean altSeed = ItemHolder.getInstance().getTemplate(seedId).isAltSeed();
		if(!player.getInventory().destroyItemByItemId(seedId, 1))
		{
			activeChar.sendActionFailed();
			return;
		}
		player.sendPacket(SystemMessage2.removeItems(seedId, 1));
		for(Creature target : targets)
		{
			MonsterInstance monster;
			if(target == null || (monster = (MonsterInstance) target).isSeeded())
				continue;
			double SuccessRate = Config.MANOR_SOWING_BASIC_SUCCESS;
			double diffPlayerTarget = Math.abs(activeChar.getLevel() - target.getLevel());
			double diffSeedTarget = Math.abs(Manor.getInstance().getSeedLevel(seedId) - target.getLevel());
			if(diffPlayerTarget > (double) Config.MANOR_DIFF_PLAYER_TARGET)
			{
				SuccessRate -= (diffPlayerTarget - (double) Config.MANOR_DIFF_PLAYER_TARGET) * Config.MANOR_DIFF_PLAYER_TARGET_PENALTY;
			}
			if(diffSeedTarget > (double) Config.MANOR_DIFF_SEED_TARGET)
			{
				SuccessRate -= (diffSeedTarget - (double) Config.MANOR_DIFF_SEED_TARGET) * Config.MANOR_DIFF_SEED_TARGET_PENALTY;
			}
			if(altSeed)
			{
				SuccessRate *= Config.MANOR_SOWING_ALT_BASIC_SUCCESS / Config.MANOR_SOWING_BASIC_SUCCESS;
			}
			if(SuccessRate < 1.0)
			{
				SuccessRate = 1.0;
			}
			if(player.isGM())
			{
				activeChar.sendMessage(new CustomMessage("l2p.gameserver.skills.skillclasses.Sowing.Chance", player).addNumber((long) SuccessRate));
			}
			if(Rnd.chance(SuccessRate) && monster.setSeeded(player, seedId, altSeed))
			{
				activeChar.sendPacket(Msg.THE_SEED_WAS_SUCCESSFULLY_SOWN);
				continue;
			}
			activeChar.sendPacket(Msg.THE_SEED_WAS_NOT_SOWN);
		}
	}
}