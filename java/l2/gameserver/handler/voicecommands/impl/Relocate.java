package l2.gameserver.handler.voicecommands.impl;

import l2.gameserver.Config;
import l2.gameserver.cache.Msg;
import l2.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2.gameserver.model.Player;
import l2.gameserver.model.pledge.Clan;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.scripts.Functions;
import l2.gameserver.skills.skillclasses.Call;
import l2.gameserver.utils.Location;

import java.util.List;

public class Relocate extends Functions implements IVoicedCommandHandler
{
	private final String[] _commandList = {"summon_clan"};
	
	@Override
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}
	
	@Override
	public boolean useVoicedCommand(String command, Player player, String args)
	{
		if(!Config.SERVICES_CLAN_SUMMON_COMMAND_ENABLE)
		{
			return false;
		}
		Clan cl = player.getClan();
		if(cl == null)
		{
			player.sendMessage("You are not a clan member.");
			return false;
		}
		if(cl.getLeaderId() != player.getObjectId())
		{
			player.sendPacket(Msg.ONLY_THE_CLAN_LEADER_IS_ENABLED);
			return false;
		}
		SystemMessage msg = Call.canSummonHere(player);
		if(msg != null)
		{
			player.sendMessage("Clan Summon is started");
			player.sendPacket(msg);
			return false;
		}
		List<Player> clanMembersOnline = cl.getOnlineMembers(player.getObjectId());
		if(clanMembersOnline.size() < 1)
		{
			player.sendMessage("No clan members online");
			return false;
		}
		if(getItemCount(player, Config.SERVICES_CLAN_SUMMON_COMMAND_SELL_ITEM) < (long) Config.SERVICES_CLAN_SUMMON_COMMAND_SELL_PRICE)
		{
			player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
			return false;
		}
		removeItem(player, Config.SERVICES_CLAN_SUMMON_COMMAND_SELL_ITEM, Config.SERVICES_CLAN_SUMMON_COMMAND_SELL_PRICE);
		player.sendMessage("Clan Summon is started");
		for(Player member : clanMembersOnline)
		{
			if(Call.canBeSummoned(member) != null)
				continue;
			member.summonCharacterRequest(player, Location.findPointToStay(player.getX(), player.getY(), player.getZ(), 100, 150, player.getReflection().getGeoIndex()), Config.SERVICES_CLAN_SUMMON_COMMAND_SUMMON_CRYSTAL_COUNT);
		}
		return true;
	}
}