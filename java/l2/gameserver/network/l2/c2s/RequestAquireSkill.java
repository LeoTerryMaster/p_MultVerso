package l2.gameserver.network.l2.c2s;

import l2.commons.lang.ArrayUtils;
import l2.gameserver.Config;
import l2.gameserver.data.xml.holder.SkillAcquireHolder;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.SkillLearn;
import l2.gameserver.model.base.AcquireType;
import l2.gameserver.model.base.ClassId;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.instances.VillageMasterInstance;
import l2.gameserver.model.pledge.Clan;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.SkillList;
import l2.gameserver.network.l2.s2c.SystemMessage2;
import l2.gameserver.tables.SkillTable;

public class RequestAquireSkill extends L2GameClientPacket
{
	private AcquireType _type;
	private int _id;
	private int _level;
	
	private static void learnSimpleNextLevel(Player player, SkillLearn skillLearn, Skill skill)
	{
		int skillLevel = player.getSkillLevel(skillLearn.getId(), 0);
		if(skillLevel != skillLearn.getLevel() - 1)
		{
			return;
		}
		learnSimple(player, skillLearn, skill);
	}
	
	private static void learnSimpleNextFishingLevel(Player player, SkillLearn skillLearn, Skill skill)
	{
		int skillLevel = player.getSkillLevel(skillLearn.getId(), 0);
		if(skillLevel != skillLearn.getLevel() - 1)
		{
			return;
		}
		learnSimpleFishing(player, skillLearn, skill);
	}
	
	private static void learnSimple(Player player, SkillLearn skillLearn, Skill skill)
	{
		if(player.getSp() < (long) skillLearn.getCost())
		{
			player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_SP_TO_LEARN_THIS_SKILL);
			return;
		}
		if(!Config.ALT_DISABLE_SPELLBOOKS && skillLearn.getItemId() > 0 && !player.consumeItem(skillLearn.getItemId(), skillLearn.getItemCount()))
		{
			player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_THE_NECESSARY_MATERIALS_OR_PREREQUISITES_TO_LEARN_THIS_SKILL);
			return;
		}
		player.sendPacket(new SystemMessage2(SystemMsg.YOU_HAVE_EARNED_S1_SKILL).addSkillName(skill.getId(), skill.getLevel()));
		player.setSp(player.getSp() - (long) skillLearn.getCost());
		player.addSkill(skill, true);
		player.sendUserInfo();
		player.updateStats();
		player.sendPacket(new SkillList(player));
		RequestExEnchantSkill.updateSkillShortcuts(player, skill.getId(), skill.getLevel());
	}
	
	private static void learnSimpleFishing(Player player, SkillLearn skillLearn, Skill skill)
	{
		if(player.getSp() < (long) skillLearn.getCost())
		{
			player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_SP_TO_LEARN_THIS_SKILL);
			return;
		}
		if(skillLearn.getItemId() > 0 && !player.consumeItem(skillLearn.getItemId(), skillLearn.getItemCount()))
		{
			player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_THE_NECESSARY_MATERIALS_OR_PREREQUISITES_TO_LEARN_THIS_SKILL);
			return;
		}
		player.sendPacket(new SystemMessage2(SystemMsg.YOU_HAVE_EARNED_S1_SKILL).addSkillName(skill.getId(), skill.getLevel()));
		player.setSp(player.getSp() - (long) skillLearn.getCost());
		player.addSkill(skill, true);
		player.sendUserInfo();
		player.updateStats();
		player.sendPacket(new SkillList(player));
		RequestExEnchantSkill.updateSkillShortcuts(player, skill.getId(), skill.getLevel());
	}
	
	private static void learnClanSkill(Player player, SkillLearn skillLearn, NpcInstance trainer, Skill skill)
	{
		if(!(trainer instanceof VillageMasterInstance))
		{
			return;
		}
		if(!player.isClanLeader())
		{
			player.sendPacket(SystemMsg.ONLY_THE_CLAN_LEADER_IS_ENABLED);
			return;
		}
		Clan clan = player.getClan();
		int skillLevel = clan.getSkillLevel(skillLearn.getId(), 0);
		if(skillLevel != skillLearn.getLevel() - 1)
		{
			return;
		}
		if(clan.getReputationScore() < skillLearn.getCost())
		{
			player.sendPacket(SystemMsg.THE_CLAN_REPUTATION_SCORE_IS_TOO_LOW);
			return;
		}
		if(skillLearn.getItemId() != 0 && !player.consumeItem(skillLearn.getItemId(), skillLearn.getItemCount()))
		{
			player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_THE_NECESSARY_MATERIALS_OR_PREREQUISITES_TO_LEARN_THIS_SKILL);
			return;
		}
		clan.incReputation(-skillLearn.getCost(), false, "AquireSkill: " + skillLearn.getId() + ", lvl " + skillLearn.getLevel());
		clan.addSkill(skill, true);
		clan.broadcastToOnlineMembers(new SystemMessage2(SystemMsg.THE_CLAN_SKILL_S1_HAS_BEEN_ADDED).addSkillName(skill));
		NpcInstance.showClanSkillList(player);
	}
	
	private static boolean checkSpellbook(Player player, SkillLearn skillLearn)
	{
		if(Config.ALT_DISABLE_SPELLBOOKS)
		{
			return true;
		}
		if(skillLearn.getItemId() == 0)
		{
			return true;
		}
		if(skillLearn.isClicked())
		{
			return false;
		}
		return player.getInventory().getCountOf(skillLearn.getItemId()) >= skillLearn.getItemCount();
	}
	
	@Override
	protected void readImpl()
	{
		_id = readD();
		_level = readD();
		_type = ArrayUtils.valid(AcquireType.VALUES, readD());
	}
	
	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null || player.getTransformation() != 0 || _type == null)
		{
			return;
		}
		NpcInstance trainer = player.getLastNpc();
		if(trainer == null || !trainer.isInActingRange(player))
		{
			return;
		}
		Skill skill = SkillTable.getInstance().getInfo(_id, _level);
		if(skill == null)
		{
			return;
		}
		int clsId = player.getVarInt("AcquireSkillClassId", player.getClassId().getId());
		ClassId classId;
		ClassId classId2 = classId = clsId >= 0 && clsId < ClassId.VALUES.length ? ClassId.VALUES[clsId] : null;
		if(!SkillAcquireHolder.getInstance().isSkillPossible(player, classId, skill, _type))
		{
			return;
		}
		SkillLearn skillLearn = SkillAcquireHolder.getInstance().getSkillLearn(player, classId, _id, _level, _type);
		if(skillLearn == null)
		{
			return;
		}
		if(!checkSpellbook(player, skillLearn))
		{
			player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_THE_NECESSARY_MATERIALS_OR_PREREQUISITES_TO_LEARN_THIS_SKILL);
			return;
		}
		switch(_type)
		{
			case NORMAL:
			{
				learnSimpleNextLevel(player, skillLearn, skill);
				if(trainer == null)
					break;
				trainer.showSkillList(player, classId);
				break;
			}
			case FISHING:
			{
				learnSimpleNextFishingLevel(player, skillLearn, skill);
				if(trainer == null)
					break;
				NpcInstance.showFishingSkillList(player);
				break;
			}
			case CLAN:
			{
				learnClanSkill(player, skillLearn, trainer, skill);
			}
		}
	}
}