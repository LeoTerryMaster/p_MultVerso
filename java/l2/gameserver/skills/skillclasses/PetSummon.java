package l2.gameserver.skills.skillclasses;

import l2.gameserver.cache.Msg;
import l2.gameserver.model.Creature;
import l2.gameserver.model.GameObject;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.World;
import l2.gameserver.tables.PetDataTable;
import l2.gameserver.templates.StatsSet;

import java.util.List;

public class PetSummon extends Skill
{
	public PetSummon(StatsSet set)
	{
		super(set);
	}
	
	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		Player player = activeChar.getPlayer();
		if(player == null)
		{
			return false;
		}
		if(player.getPetControlItem() == null)
		{
			return false;
		}
		int npcId = PetDataTable.getSummonId(player.getPetControlItem());
		if(npcId == 0)
		{
			return false;
		}
		if(player.isInCombat())
		{
			player.sendPacket(Msg.YOU_CANNOT_SUMMON_DURING_COMBAT);
			return false;
		}
		if(player.isProcessingRequest())
		{
			player.sendPacket(Msg.PETS_AND_SERVITORS_ARE_NOT_AVAILABLE_AT_THIS_TIME);
			return false;
		}
		if(player.isMounted() || player.getPet() != null)
		{
			player.sendPacket(Msg.YOU_ALREADY_HAVE_A_PET);
			return false;
		}
		if(player.isInBoat())
		{
			player.sendPacket(Msg.YOU_MAY_NOT_CALL_FORTH_A_PET_OR_SUMMONED_CREATURE_FROM_THIS_LOCATION);
			return false;
		}
		if(player.isInFlyingTransform())
		{
			return false;
		}
		if(player.isOlyParticipant())
		{
			player.sendPacket(Msg.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
			return false;
		}
		if(player.isCursedWeaponEquipped())
		{
			player.sendPacket(Msg.YOU_MAY_NOT_USE_MULTIPLE_PETS_OR_SERVITORS_AT_THE_SAME_TIME);
			return false;
		}
		for(GameObject o : World.getAroundObjects(player, 120, 200))
		{
			if(!o.isDoor())
				continue;
			player.sendPacket(Msg.YOU_MAY_NOT_SUMMON_FROM_YOUR_CURRENT_LOCATION);
			return false;
		}
		return super.checkCondition(activeChar, target, forceUse, dontMove, first);
	}
	
	@Override
	public void useSkill(Creature caster, List<Creature> targets)
	{
		Player activeChar = caster.getPlayer();
		activeChar.summonPet();
		if(isSSPossible())
		{
			caster.unChargeShots(isMagic());
		}
	}
}