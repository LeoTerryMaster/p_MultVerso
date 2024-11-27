package services;

import l2.gameserver.Config;
import l2.gameserver.cache.Msg;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.pledge.Clan;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2.gameserver.network.l2.s2c.SystemMessage2;
import l2.gameserver.scripts.Functions;
import l2.gameserver.tables.SkillTable;

import java.util.Arrays;
import java.util.List;

public class ClanSkillSell extends Functions
{
	private static final List<Skill> CLAN_SKILL_BUY_LIST = Arrays.asList(SkillTable.getInstance().getInfo(370, 3), SkillTable.getInstance().getInfo(373, 3), SkillTable.getInstance().getInfo(379, 3), SkillTable.getInstance().getInfo(391, 1), SkillTable.getInstance().getInfo(371, 3), SkillTable.getInstance().getInfo(374, 3), SkillTable.getInstance().getInfo(376, 3), SkillTable.getInstance().getInfo(377, 3), SkillTable.getInstance().getInfo(383, 3), SkillTable.getInstance().getInfo(380, 3), SkillTable.getInstance().getInfo(382, 3), SkillTable.getInstance().getInfo(384, 3), SkillTable.getInstance().getInfo(385, 3), SkillTable.getInstance().getInfo(386, 3), SkillTable.getInstance().getInfo(387, 3), SkillTable.getInstance().getInfo(388, 3), SkillTable.getInstance().getInfo(390, 3), SkillTable.getInstance().getInfo(372, 3), SkillTable.getInstance().getInfo(375, 3), SkillTable.getInstance().getInfo(378, 3), SkillTable.getInstance().getInfo(381, 3), SkillTable.getInstance().getInfo(389, 3));
	
	public void clan_skill_sell_page()
	{
		Player player = getSelf();
		if(player == null)
		{
			return;
		}
		if(!Config.SERVICES_CLANSKILL_SELL_ENABLED)
		{
			player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/service_disabled.htm"));
			return;
		}
		if(!player.isInPeaceZone())
		{
			player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/service_peace_zone.htm"));
			return;
		}
		NpcHtmlMessage msg = new NpcHtmlMessage(5).setFile("scripts/services/clan_skills_sell.htm");
		msg.replace("%item_id%", String.valueOf(Config.SERVICES_CLAN_SKILL_SELL_ITEM));
		msg.replace("%item_count%", String.valueOf(Config.SERVICES_CLAN_SKILL_SELL_PRICE));
		msg.replace("%clan_min_level%", String.valueOf(Config.SERVICES_CLANSKIL_SELL_MIN_LEVEL));
		player.sendPacket(msg);
	}
	
	public void clanSkillBuy()
	{
		Player player = getSelf();
		if(player == null)
		{
			return;
		}
		if(!Config.SERVICES_CLANSKILL_SELL_ENABLED)
		{
			player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/service_disabled.htm"));
			return;
		}
		if(!player.isInPeaceZone())
		{
			player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/service_peace_zone.htm"));
			return;
		}
		Clan clan = player.getClan();
		if(clan == null)
		{
			player.sendMessage("Get clan first.");
			return;
		}
		if(!player.isClanLeader())
		{
			player.sendPacket(SystemMsg.ONLY_THE_CLAN_LEADER_IS_ENABLED);
			return;
		}
		if(clan.getLevel() < Config.SERVICES_CLANSKIL_SELL_MIN_LEVEL)
		{
			player.sendMessage("Clan level to low.");
			return;
		}
		if(getItemCount(player, Config.SERVICES_CLAN_SKILL_SELL_ITEM) < (long) Config.SERVICES_CLAN_SKILL_SELL_PRICE)
		{
			if(Config.SERVICES_CLAN_SKILL_SELL_ITEM == 57)
			{
				player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			}
			else
			{
				player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
			}
			return;
		}
		Functions.removeItem(player, Config.SERVICES_CLAN_SKILL_SELL_ITEM, (long) Config.SERVICES_CLAN_SKILL_SELL_PRICE);
		for(Skill aNewClanSkill : CLAN_SKILL_BUY_LIST)
		{
			clan.addSkill(aNewClanSkill, true);
			clan.broadcastToOnlineMembers(new SystemMessage2(SystemMsg.THE_CLAN_SKILL_S1_HAS_BEEN_ADDED).addSkillName(aNewClanSkill));
		}
	}
}