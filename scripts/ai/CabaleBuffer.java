package ai;

import l2.commons.util.Rnd;
import l2.gameserver.ai.DefaultAI;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.World;
import l2.gameserver.model.entity.SevenSigns;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.scripts.Functions;
import l2.gameserver.tables.SkillTable;

import java.util.List;

public class CabaleBuffer extends DefaultAI
{
	private static final int PREACHER_FIGHTER_SKILL_ID = 4361;
	private static final int PREACHER_MAGE_SKILL_ID = 4362;
	private static final int ORATOR_FIGHTER_SKILL_ID = 4364;
	private static final int ORATOR_MAGE_SKILL_ID = 4365;
	private static final long castDelay = 60000;
	private static final long buffDelay = 1000;
	private static final String[] preacherText = {"This world will soon be annihilated!", "All is lost! Prepare to meet the goddess of death!", "All is lost! The prophecy of destruction has been fulfilled!", "The end of time has come! The prophecy of destruction has been fulfilled!"};
	private static final String[] oratorText = {"The day of judgment is near!", "The prophecy of darkness has been fulfilled!", "As foretold in the prophecy of darkness, the era of chaos has begun!", "The prophecy of darkness has come to pass!"};
	private long _castVar;
	private long _buffVar;
	
	public CabaleBuffer(NpcInstance actor)
	{
		super(actor);
	}
	
	@Override
	protected boolean thinkActive()
	{
		NpcInstance actor = getActor();
		if(actor.isDead())
		{
			return true;
		}
		int winningCabal = SevenSigns.getInstance().getCabalHighestScore();
		if(winningCabal == 0)
		{
			return true;
		}
		int losingCabal = 0;
		if(winningCabal == 2)
		{
			losingCabal = 1;
		}
		else if(winningCabal == 1)
		{
			losingCabal = 2;
		}
		if(_castVar + 60000 < System.currentTimeMillis())
		{
			_castVar = System.currentTimeMillis();
			Functions.npcSayInRange(actor, actor.getNpcId() == 31094 ? oratorText[Rnd.get(oratorText.length)] : preacherText[Rnd.get(preacherText.length)], 300);
		}
		if(_buffVar + 1000 < System.currentTimeMillis())
		{
			_buffVar = System.currentTimeMillis();
			for(Player player : World.getAroundPlayers(actor, 300, 200))
			{
				if(player == null)
					continue;
				int playerCabal = SevenSigns.getInstance().getPlayerCabal(player);
				int i0 = Rnd.get(100);
				int i1 = Rnd.get(10000);
				List effects;
				Skill skill;
				if(playerCabal == winningCabal && actor.getNpcId() == 31094)
				{
					if(player.isMageClass())
					{
						effects = player.getEffectList().getEffectsBySkillId(4365);
						if(effects == null || effects.size() <= 0)
						{
							if(i1 < 1)
							{
								Functions.npcSayInRangeCustomMessage(actor, 300, "scripts.ai.CabaleBuffer.I_BESTOW_UPON_YOU_A_BLESSING", (Object[]) new Object[0]);
							}
							if((skill = SkillTable.getInstance().getInfo(4365, 1)) == null)
								continue;
							actor.altUseSkill(skill, player);
							continue;
						}
						if(i0 >= 5)
							continue;
						if(i1 < 500)
						{
							Functions.npcSayCustomMessage(actor, "scripts.ai.CabaleBuffer.S1__I_GIVE_YOU_THE_BLESSING_OF_PROPHECY", (Object[]) new Object[] {player.getName()});
						}
						if((skill = SkillTable.getInstance().getInfo(4365, 2)) == null)
							continue;
						actor.altUseSkill(skill, player);
						continue;
					}
					effects = player.getEffectList().getEffectsBySkillId(4364);
					if(effects == null || effects.size() <= 0)
					{
						if(i1 < 1)
						{
							Functions.npcSayInRangeCustomMessage(actor, 300, "scripts.ai.CabaleBuffer.HERALD_OF_THE_NEW_ERA__OPEN_YOUR_EYES", (Object[]) new Object[0]);
						}
						if((skill = SkillTable.getInstance().getInfo(4364, 1)) == null)
							continue;
						actor.altUseSkill(skill, player);
						continue;
					}
					if(i0 >= 5)
						continue;
					if(i1 < 500)
					{
						Functions.npcSayCustomMessage(actor, "scripts.ai.CabaleBuffer.S1__I_BESTOW_UPON_YOU_THE_AUTHORITY_OF_THE_ABYSS", (Object[]) new Object[] {player.getName()});
					}
					if((skill = SkillTable.getInstance().getInfo(4364, 2)) == null)
						continue;
					actor.altUseSkill(skill, player);
					continue;
				}
				if(playerCabal != losingCabal || actor.getNpcId() != 31093)
					continue;
				if(player.isMageClass())
				{
					effects = player.getEffectList().getEffectsBySkillId(4362);
					if(effects == null || effects.size() <= 0)
					{
						if(i1 < 1)
						{
							Functions.npcSayInRangeCustomMessage(actor, 300, "scripts.ai.CabaleBuffer.YOU_DONT_HAVE_ANY_HOPE__YOUR_END_HAS_COME", (Object[]) new Object[0]);
						}
						if((skill = SkillTable.getInstance().getInfo(4362, 1)) == null)
							continue;
						actor.altUseSkill(skill, player);
						continue;
					}
					if(i0 >= 5)
						continue;
					if(i1 < 500)
					{
						Functions.npcSayInRangeCustomMessage(actor, 300, "scripts.ai.CabaleBuffer.A_CURSE_UPON_YOU", (Object[]) new Object[0]);
					}
					if((skill = SkillTable.getInstance().getInfo(4362, 2)) == null)
						continue;
					actor.altUseSkill(skill, player);
					continue;
				}
				effects = player.getEffectList().getEffectsBySkillId(4361);
				if(effects == null || effects.size() <= 0)
				{
					if(i1 < 1)
					{
						Functions.npcSayCustomMessage(actor, "scripts.ai.CabaleBuffer.S1__YOU_BRING_AN_ILL_WIND", (Object[]) new Object[] {player.getName()});
					}
					if((skill = SkillTable.getInstance().getInfo(4361, 1)) == null)
						continue;
					actor.altUseSkill(skill, player);
					continue;
				}
				if(i0 >= 5)
					continue;
				if(i1 < 500)
				{
					Functions.npcSayCustomMessage(actor, "scripts.ai.CabaleBuffer.S1__YOU_MIGHT_AS_WELL_GIVE_UP", (Object[]) new Object[] {player.getName()});
				}
				if((skill = SkillTable.getInstance().getInfo(4361, 2)) == null)
					continue;
				actor.altUseSkill(skill, player);
			}
		}
		return false;
	}
}