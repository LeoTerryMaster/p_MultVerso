package l2.gameserver.utils;

import l2.gameserver.data.xml.holder.ResidenceHolder;
import l2.gameserver.model.Player;
import l2.gameserver.model.entity.residence.Residence;
import l2.gameserver.tables.SkillTable;

public class SiegeUtils
{
	public static void addSiegeSkills(Player character)
	{
		character.addSkill(SkillTable.getInstance().getInfo(246, 1), false);
		character.addSkill(SkillTable.getInstance().getInfo(247, 1), false);
		if(character.isNoble())
		{
			character.addSkill(SkillTable.getInstance().getInfo(326, 1), false);
		}
	}
	
	public static void removeSiegeSkills(Player character)
	{
		character.removeSkill(SkillTable.getInstance().getInfo(246, 1), false);
		character.removeSkill(SkillTable.getInstance().getInfo(247, 1), false);
		character.removeSkill(SkillTable.getInstance().getInfo(326, 1), false);
	}
	
	public static boolean getCanRide()
	{
		for(Residence residence : ResidenceHolder.getInstance().getResidences())
		{
			if(residence == null || !residence.getSiegeEvent().isInProgress())
				continue;
			return false;
		}
		return true;
	}
}