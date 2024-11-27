package npc.model;

import l2.gameserver.Config;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Party;
import l2.gameserver.model.Player;
import l2.gameserver.model.entity.oly.NoblesController;
import l2.gameserver.model.instances.BossInstance;
import l2.gameserver.network.l2.s2c.SkillList;
import l2.gameserver.templates.npc.NpcTemplate;

public class NoblesMainRewardBossInstance extends BossInstance
{
	public NoblesMainRewardBossInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	protected void onDeath(Creature killer)
	{
		super.onDeath(killer);
		if(killer != null && killer.isPlayable())
		{
			Player player = killer.getPlayer();
			if(player.isDead() || player.getParty() == null)
			{
				return;
			}
			Party playerParty = player.getParty();
			for(Player partyPlayer : playerParty)
			{
				if(partyPlayer == null || partyPlayer.isDead() || partyPlayer.isNoble() || partyPlayer.isSubClassActive() || partyPlayer.getLevel() < 76 || getDistance3D(partyPlayer) > (double) Config.ALT_PARTY_DISTRIBUTION_RANGE)
					continue;
				NoblesController.getInstance().addNoble(partyPlayer.getPlayer());
				partyPlayer.setNoble(true);
				partyPlayer.updatePledgeClass();
				partyPlayer.updateNobleSkills();
				partyPlayer.getPlayer().sendPacket(new SkillList(partyPlayer.getPlayer()));
				partyPlayer.getPlayer().broadcastUserInfo(true);
			}
		}
	}
}