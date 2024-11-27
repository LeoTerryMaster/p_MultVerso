package services.community.custom;

import l2.gameserver.model.Player;
import l2.gameserver.model.base.TeamType;

public class CommunityTools
{
	public static boolean checkConditions(Player player)
	{
		return ACbConfigManager.ALLOW_PVPCB_ABNORMAL || (!player.isDead() && !player.isAlikeDead() && !player.isCastingNow() && !player.isInCombat() && !player.isInBoat() && !player.isAttackingNow() && !player.isOlyParticipant() && !player.isFlying() && !player.isInFlyingTransform() && !player.isSitting() && player.getTeam() == TeamType.NONE);
	}
}