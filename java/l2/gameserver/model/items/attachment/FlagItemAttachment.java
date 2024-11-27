package l2.gameserver.model.items.attachment;

import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;

public interface FlagItemAttachment extends PickableAttachment
{
	void onLogout(Player player);
	
	void onDeath(Player owner, Creature killer);
	
	void onEnterPeace(Player owner);
	
	boolean canAttack(Player player);
	
	boolean canCast(Player player, Skill skill);
}