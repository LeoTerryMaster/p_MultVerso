package l2.gameserver.network.l2.c2s;

import l2.commons.util.Rnd;
import l2.gameserver.cache.Msg;
import l2.gameserver.data.xml.holder.EnchantSkillHolder;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.actor.instances.player.ShortCut;
import l2.gameserver.model.base.Experience;
import l2.gameserver.network.l2.s2c.ExEnchantSkillList;
import l2.gameserver.network.l2.s2c.ShortCutRegister;
import l2.gameserver.network.l2.s2c.SkillList;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.scripts.Functions;
import l2.gameserver.skills.TimeStamp;
import l2.gameserver.tables.SkillTable;
import l2.gameserver.templates.SkillEnchant;
import l2.gameserver.utils.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class RequestExEnchantSkill extends L2GameClientPacket
{
	private static final Logger LOG = LoggerFactory.getLogger(RequestExEnchantSkill.class);
	private int _skillId;
	private int _skillLvl;
	
	protected static void updateSkillShortcuts(Player player, int skillId, int skillLevel)
	{
		for(ShortCut sc : player.getAllShortCuts())
		{
			if(sc.getId() != skillId || sc.getType() != 2)
				continue;
			ShortCut newsc = new ShortCut(sc.getSlot(), sc.getPage(), sc.getType(), sc.getId(), skillLevel, 1);
			player.sendPacket(new ShortCutRegister(player, newsc));
			player.registerShortCut(newsc);
		}
	}
	
	@Override
	protected void readImpl()
	{
		_skillId = readD();
		_skillLvl = readD();
	}
	
	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
		{
			return;
		}
		if(player.getClassId().getLevel() < 4 || player.getLevel() < 76)
		{
			player.sendPacket(new SystemMessage(1438));
			return;
		}
		Skill currSkill = player.getKnownSkill(_skillId);
		if(currSkill == null)
		{
			player.sendPacket(new SystemMessage(1438));
			return;
		}
		int currSkillLevel = currSkill.getLevel();
		int currSkillBaseLevel = currSkill.getBaseLevel();
		Map<Integer, SkillEnchant> skillEnchLevels = EnchantSkillHolder.getInstance().getLevelsOf(_skillId);
		if(skillEnchLevels == null || skillEnchLevels.isEmpty())
		{
			player.sendPacket(new SystemMessage(1438));
			return;
		}
		SkillEnchant currSkillEnch = skillEnchLevels.get(currSkillLevel);
		SkillEnchant newSkillEnch = skillEnchLevels.get(_skillLvl);
		if(newSkillEnch == null)
		{
			player.sendPacket(new SystemMessage(1438));
			return;
		}
		if(currSkillEnch != null)
		{
			if(currSkillEnch.getRouteId() != newSkillEnch.getRouteId() || newSkillEnch.getEnchantLevel() != currSkillEnch.getEnchantLevel() + 1)
			{
				player.sendPacket(new SystemMessage(1438));
				return;
			}
		}
		else if(newSkillEnch.getEnchantLevel() != 1 || currSkillLevel != currSkillBaseLevel)
		{
			player.sendPacket(new SystemMessage(1438));
			LOG.warn("Player \"" + player + "\" trying to use enchant  exploit" + currSkill + " to " + _skillLvl + "(enchant level " + newSkillEnch.getEnchantLevel() + ")");
			return;
		}
		int[] chances = newSkillEnch.getChances();
		int minPlayerLevel = Experience.LEVEL.length - chances.length - 1;
		if(player.getLevel() < minPlayerLevel)
		{
			sendPacket(new SystemMessage(607).addNumber(minPlayerLevel));
			return;
		}
		if(player.getSp() < (long) newSkillEnch.getSp())
		{
			sendPacket(new SystemMessage(1443));
			return;
		}
		if(player.getExp() < newSkillEnch.getExp())
		{
			sendPacket(new SystemMessage(1444));
			return;
		}
		if(newSkillEnch.getItemId() > 0 && newSkillEnch.getItemCount() > 0 && Functions.removeItem(player, newSkillEnch.getItemId(), newSkillEnch.getItemCount()) < newSkillEnch.getItemCount())
		{
			sendPacket(Msg.ITEMS_REQUIRED_FOR_SKILL_ENCHANT_ARE_INSUFFICIENT);
			return;
		}
		int chanceIdx = Math.max(0, Math.min(player.getLevel() - minPlayerLevel, chances.length - 1));
		int chance = chances[chanceIdx];
		player.addExpAndSp(-1 * newSkillEnch.getExp(), -1 * newSkillEnch.getSp());
		player.sendPacket(new SystemMessage(538).addNumber(newSkillEnch.getSp()));
		player.sendPacket(new SystemMessage(539).addNumber(newSkillEnch.getExp()));
		TimeStamp currSkillReuseTimeStamp = player.getSkillReuse(currSkill);
		Skill newSkill;
		if(Rnd.chance(chance))
		{
			newSkill = SkillTable.getInstance().getInfo(newSkillEnch.getSkillId(), newSkillEnch.getSkillLevel());
			player.sendPacket(new SystemMessage(1440).addSkillName(_skillId, _skillLvl));
			Log.add(player.getName() + "|Successfully enchanted|" + _skillId + "|to+" + _skillLvl + "|" + chance, "enchant_skills");
		}
		else
		{
			newSkill = SkillTable.getInstance().getInfo(currSkill.getId(), currSkill.getBaseLevel());
			player.sendPacket(new SystemMessage(1441).addSkillName(_skillId, _skillLvl));
			Log.add(player.getName() + "|Failed to enchant|" + _skillId + "|to+" + _skillLvl + "|" + chance, "enchant_skills");
		}
		if(currSkillReuseTimeStamp != null && currSkillReuseTimeStamp.hasNotPassed())
		{
			player.disableSkill(newSkill, currSkillReuseTimeStamp.getReuseCurrent());
		}
		player.addSkill(newSkill, true);
		player.sendPacket(new SkillList(player));
		updateSkillShortcuts(player, _skillId, _skillLvl);
		player.sendPacket(ExEnchantSkillList.packetFor(player));
	}
}