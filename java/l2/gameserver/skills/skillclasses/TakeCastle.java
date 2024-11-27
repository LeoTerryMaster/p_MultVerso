package l2.gameserver.skills.skillclasses;

import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.entity.events.impl.CastleSiegeEvent;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.SystemMessage2;
import l2.gameserver.templates.StatsSet;

import java.util.List;

public class TakeCastle extends Skill
{
	public TakeCastle(StatsSet set)
	{
		super(set);
	}
	
	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		if(!super.checkCondition(activeChar, target, forceUse, dontMove, first))
		{
			return false;
		}
		if(activeChar == null || !activeChar.isPlayer())
		{
			return false;
		}
		Player player = (Player) activeChar;
		if(player.getClan() == null || !player.isClanLeader())
		{
			activeChar.sendPacket(new SystemMessage2(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
			return false;
		}
		CastleSiegeEvent siegeEvent = player.getEvent(CastleSiegeEvent.class);
		if(siegeEvent == null)
		{
			activeChar.sendPacket(new SystemMessage2(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
			return false;
		}
		if(siegeEvent.getSiegeClan("attackers", player.getClan()) == null)
		{
			activeChar.sendPacket(new SystemMessage2(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
			return false;
		}
		if(player.isMounted())
		{
			activeChar.sendPacket(new SystemMessage2(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
			return false;
		}
		if(!player.isInRangeZ(target, 185))
		{
			player.sendPacket(SystemMsg.YOUR_TARGET_IS_OUT_OF_RANGE);
			return false;
		}
		if(!siegeEvent.getResidence().getZone().checkIfInZone(activeChar) || !siegeEvent.getResidence().getZone().checkIfInZone(target))
		{
			activeChar.sendPacket(new SystemMessage2(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
			return false;
		}
		if(first)
		{
			siegeEvent.broadcastTo(SystemMsg.THE_OPPOSING_CLAN_HAS_STARTED_TO_ENGRAVE_THE_HOLY_ARTIFACT, "defenders");
		}
		return true;
	}
	
	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		for(Creature target : targets)
		{
			Player player;
			CastleSiegeEvent siegeEvent;
			if(target == null || !target.isArtefact() || (siegeEvent = (player = (Player) activeChar).getEvent(CastleSiegeEvent.class)) == null)
				continue;
			siegeEvent.broadcastTo(new SystemMessage2(SystemMsg.CLAN_S1_HAS_SUCCESSFULLY_ENGRAVED_THE_HOLY_ARTIFACT).addString(player.getClan().getName()), "attackers", "defenders");
			siegeEvent.processStep(player.getClan());
		}
		getEffects(activeChar, activeChar, getActivateRate() > 0, false);
	}
}