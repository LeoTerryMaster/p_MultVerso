package l2.gameserver.network.l2.c2s;

import l2.gameserver.data.xml.holder.EnchantSkillHolder;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.base.Experience;
import l2.gameserver.network.l2.s2c.ExEnchantSkillInfo;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.templates.SkillEnchant;

import java.util.Map;

public class RequestExEnchantSkillInfo extends L2GameClientPacket
{
	private int _skillId;
	private int _skillLvl;
	
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
		else if(newSkillEnch.getEnchantLevel() != 1)
		{
			player.sendPacket(new SystemMessage(1438));
			return;
		}
		int[] chances = newSkillEnch.getChances();
		int minPlayerLevel = Experience.LEVEL.length - chances.length - 1;
		if(player.getLevel() < minPlayerLevel)
		{
			sendPacket(new SystemMessage(607).addNumber(minPlayerLevel));
			return;
		}
		int chanceIdx = Math.max(0, Math.min(player.getLevel() - minPlayerLevel, chances.length - 1));
		int chance = chances[chanceIdx];
		ExEnchantSkillInfo esi = new ExEnchantSkillInfo(newSkillEnch.getSkillId(), newSkillEnch.getSkillLevel(), newSkillEnch.getSp(), newSkillEnch.getExp(), chance);
		if(newSkillEnch.getItemId() > 0 && newSkillEnch.getItemCount() > 0)
		{
			esi.addNeededItem(newSkillEnch.getItemId(), newSkillEnch.getItemCount());
		}
		player.sendPacket(esi);
	}
}