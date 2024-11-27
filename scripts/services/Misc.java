package services;

import l2.gameserver.model.Party;
import l2.gameserver.model.Player;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.scripts.Functions;
import l2.gameserver.utils.Location;
import l2.gameserver.utils.MapUtils;

import java.util.ArrayList;

public class Misc extends Functions
{
	private static final Location SUMMON_LOCATION = new Location(11416, -23432, -3640);
	
	public void doSummon()
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(player == null || npc == null || !NpcInstance.canBypassCheck(player, player.getLastNpc()))
		{
			return;
		}
		Party party = player.getParty();
		ArrayList<Player> membersToRecall = new ArrayList<>();
		if(party != null)
		{
			for(Player member : party.getPartyMembers())
			{
				if(!member.isDead() || MapUtils.regionX(member) != 20 || MapUtils.regionY(member) != 17)
					continue;
				membersToRecall.add(member);
			}
		}
		if(membersToRecall.isEmpty() || !player.reduceAdena(200000, true))
		{
			show("default/32104-fail.htm", player);
			return;
		}
		for(Player member : membersToRecall)
		{
			member.teleToLocation(SUMMON_LOCATION);
		}
		show("default/32104-success.htm", player);
	}
}