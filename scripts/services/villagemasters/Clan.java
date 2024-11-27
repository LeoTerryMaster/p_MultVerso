package services.villagemasters;

import l2.gameserver.Config;
import l2.gameserver.model.Player;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.scripts.Functions;

public class Clan extends Functions
{
	public void CheckCreateClan()
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(npc == null || player == null)
		{
			return;
		}
		if(player.getLevel() < Config.CHARACTER_MIN_LEVEL_FOR_CLAN_CREATE)
		{
			show("villagemaster/clan-06.htm", player, npc, (Object[]) new Object[] {"%min_level%", Config.CHARACTER_MIN_LEVEL_FOR_CLAN_CREATE});
		}
		else if(player.isClanLeader())
		{
			show("villagemaster/clan-07.htm", player, npc, (Object[]) new Object[0]);
		}
		else if(player.getClan() != null)
		{
			show("villagemaster/clan-09.htm", player, npc, (Object[]) new Object[0]);
		}
		else
		{
			show("villagemaster/clan-02.htm", player, npc, (Object[]) new Object[0]);
		}
	}
	
	public void CheckDissolveClan()
	{
		if(getNpc() == null || getSelf() == null)
		{
			return;
		}
		Player pl = getSelf();
		String htmltext = pl.isClanLeader() ? "clan-04.htm" : pl.getClan() != null ? "9000-08.htm" : "9000-11.htm";
		getNpc().showChatWindow(pl, "villagemaster/" + htmltext);
	}
}