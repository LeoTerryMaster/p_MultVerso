package l2.gameserver.skills.skillclasses;

import l2.gameserver.cache.Msg;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.pledge.Clan;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.templates.StatsSet;

import java.util.List;

public class ClanGate extends Skill
{
	public ClanGate(StatsSet set)
	{
		super(set);
	}
	
	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		if(!activeChar.isPlayer())
		{
			return false;
		}
		Player player = (Player) activeChar;
		Clan clan = player.getClan();
		if(clan == null || !player.isClanLeader() || clan.getCastle() == 0)
		{
			player.sendPacket(Msg.ONLY_THE_CLAN_LEADER_IS_ENABLED);
			return false;
		}
		SystemMessage msg = Call.canSummonHere(player);
		if(msg != null)
		{
			activeChar.sendPacket(msg);
			return false;
		}
		return super.checkCondition(activeChar, target, forceUse, dontMove, first);
	}
	
	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		if(!activeChar.isPlayer())
		{
			return;
		}
		Player player = (Player) activeChar;
		Clan clan = player.getClan();
		clan.broadcastToOtherOnlineMembers(Msg.COURT_MAGICIAN__THE_PORTAL_HAS_BEEN_CREATED, player);
		getEffects(activeChar, activeChar, getActivateRate() > 0, true);
	}
}