package l2.gameserver.data.xml.holder;

import gnu.trove.TIntObjectHashMap;
import gnu.trove.TIntObjectIterator;
import l2.commons.data.xml.AbstractHolder;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.SkillLearn;
import l2.gameserver.model.SubClass;
import l2.gameserver.model.base.AcquireType;
import l2.gameserver.model.base.ClassId;
import l2.gameserver.model.base.ClassType2;
import l2.gameserver.model.pledge.Clan;
import l2.gameserver.model.pledge.SubUnit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class SkillAcquireHolder extends AbstractHolder
{
	private static final SkillAcquireHolder _instance = new SkillAcquireHolder();
	private final TIntObjectHashMap<List<SkillLearn>> _normalSkillTree = new TIntObjectHashMap();
	private final TIntObjectHashMap<List<SkillLearn>> _fishingSkillTree = new TIntObjectHashMap();
	private final List<SkillLearn> _pledgeSkillTree = new ArrayList<>();
	
	public static SkillAcquireHolder getInstance()
	{
		return _instance;
	}
	
	public int getMinLevelForNewSkill(ClassId classId, int currLevel, AcquireType type)
	{
		List<SkillLearn> skills;
		switch(type)
		{
			case NORMAL:
			{
				skills = _normalSkillTree.get(classId.getId());
				if(skills != null)
					break;
				info("skill tree for class " + classId.getId() + " is not defined !");
				return 0;
			}
			default:
			{
				return 0;
			}
		}
		int minlevel = 0;
		for(SkillLearn temp : skills)
		{
			if(temp.getMinLevel() <= currLevel || minlevel != 0 && temp.getMinLevel() >= minlevel)
				continue;
			minlevel = temp.getMinLevel();
		}
		return minlevel;
	}
	
	public Collection<SkillLearn> getAvailableSkills(Player player, AcquireType type)
	{
		return getAvailableSkills(player, player.getClassId(), type, null);
	}
	
	public Collection<SkillLearn> getAvailableSkills(Player player, ClassId classId, AcquireType type, SubUnit subUnit)
	{
		switch(type)
		{
			case NORMAL:
			{
				Collection skills = _normalSkillTree.get(classId.getId());
				if(skills == null)
				{
					info("skill tree for class " + classId + " is not defined !");
					return Collections.emptyList();
				}
				return getAvaliableList(skills, player.getAllSkillsArray(), player.getLevel());
			}
			case FISHING:
			{
				Collection skills = _fishingSkillTree.get(player.getRace().ordinal());
				if(skills == null)
				{
					info("skill tree for race " + player.getRace().ordinal() + " is not defined !");
					return Collections.emptyList();
				}
				return getAvaliableList(skills, player.getAllSkillsArray(), player.getLevel());
			}
			case CLAN:
			{
				List<SkillLearn> skills = _pledgeSkillTree;
				Collection<Skill> skls = player.getClan().getSkills();
				return getAvaliableList(skills, skls.toArray(new Skill[skls.size()]), player.getClan().getLevel());
			}
		}
		return Collections.emptyList();
	}
	
	private Collection<SkillLearn> getAvaliableList(Collection<SkillLearn> skillLearns, Skill[] skills, int level)
	{
		return getAvaliableList(skillLearns, skills, level, null);
	}
	
	private Collection<SkillLearn> getAvaliableList(Collection<SkillLearn> skillLearns, Skill[] skills, int level, Player target)
	{
		TreeMap<Integer, SkillLearn> skillLearnMap = new TreeMap<>();
		for(SkillLearn temp : skillLearns)
		{
			if(temp.getMinLevel() > level)
				continue;
			if(target != null && temp.getClassType2() != ClassType2.None)
			{
				boolean learnable = false;
				for(Map.Entry<Integer, SubClass> e : target.getSubClasses().entrySet())
				{
					if(e.getValue().isBase())
						continue;
					for(ClassId ci : ClassId.values())
					{
						if(ci.getId() != e.getKey().intValue() || ci.getType2() != temp.getClassType2())
							continue;
						learnable = true;
					}
				}
				if(!learnable)
					continue;
			}
			boolean knownSkill = false;
			for(int j = 0;j < skills.length && !knownSkill;++j)
			{
				if(skills[j].getId() != temp.getId())
					continue;
				knownSkill = true;
				if(skills[j].getLevel() != temp.getLevel() - 1)
					continue;
				skillLearnMap.put(temp.getId(), temp);
			}
			if(knownSkill || temp.getLevel() != 1)
				continue;
			skillLearnMap.put(temp.getId(), temp);
		}
		return skillLearnMap.values();
	}
	
	public SkillLearn getSkillLearn(Player player, ClassId classId, int id, int level, AcquireType type)
	{
		List<SkillLearn> skills;
		switch(type)
		{
			case NORMAL:
			{
				skills = _normalSkillTree.get(classId.getId());
				break;
			}
			case FISHING:
			{
				skills = _fishingSkillTree.get(player.getRace().ordinal());
				break;
			}
			case CLAN:
			{
				skills = _pledgeSkillTree;
				break;
			}
			default:
			{
				return null;
			}
		}
		if(skills == null)
		{
			return null;
		}
		for(SkillLearn temp : skills)
		{
			if(temp.getLevel() != level || temp.getId() != id)
				continue;
			return temp;
		}
		return null;
	}
	
	public boolean isSkillPossible(Player player, Skill skill, AcquireType type)
	{
		List<SkillLearn> skills;
		switch(type)
		{
			case NORMAL:
			{
				skills = _normalSkillTree.get(player.getActiveClassId());
				break;
			}
			case FISHING:
			{
				skills = _fishingSkillTree.get(player.getRace().ordinal());
				break;
			}
			case CLAN:
			{
				Clan clan = player.getClan();
				if(clan == null)
				{
					return false;
				}
				skills = _pledgeSkillTree;
				break;
			}
			default:
			{
				return false;
			}
		}
		return isSkillPossible(skills, skill);
	}
	
	public boolean isSkillPossible(Player player, ClassId classId, Skill skill, AcquireType type)
	{
		List<SkillLearn> skills;
		switch(type)
		{
			case NORMAL:
			{
				skills = _normalSkillTree.get(classId.getId());
				break;
			}
			case FISHING:
			{
				skills = _fishingSkillTree.get(player.getRace().ordinal());
				break;
			}
			case CLAN:
			{
				Clan clan = player.getClan();
				if(clan == null)
				{
					return false;
				}
				skills = _pledgeSkillTree;
				break;
			}
			default:
			{
				return false;
			}
		}
		return isSkillPossible(skills, skill);
	}
	
	private boolean isSkillPossible(Collection<SkillLearn> skills, Skill skill)
	{
		for(SkillLearn learn : skills)
		{
			if(learn.getId() != skill.getId() || learn.getLevel() > skill.getLevel())
				continue;
			return true;
		}
		return false;
	}
	
	public boolean isSkillPossible(Player player, Skill skill)
	{
		for(AcquireType aq : AcquireType.VALUES)
		{
			if(!isSkillPossible(player, skill, aq))
				continue;
			return true;
		}
		return false;
	}
	
	public boolean isSkillPossible(Player player, ClassId classId, Skill skill)
	{
		for(AcquireType aq : AcquireType.VALUES)
		{
			if(!isSkillPossible(player, classId, skill, aq))
				continue;
			return true;
		}
		return false;
	}
	
	public List<SkillLearn> getSkillLearnListByItemId(Player player, int itemId)
	{
		List<SkillLearn> learns = _normalSkillTree.get(player.getActiveClassId());
		if(learns == null)
		{
			return Collections.emptyList();
		}
		ArrayList<SkillLearn> l = new ArrayList<>(1);
		for(SkillLearn sl : learns)
		{
			if(sl.getItemId() != itemId)
				continue;
			l.add(sl);
		}
		return l;
	}
	
	public List<SkillLearn> getAllNormalSkillTreeWithForgottenScrolls()
	{
		ArrayList<SkillLearn> a = new ArrayList<>();
		TIntObjectIterator<List<SkillLearn>> i = _normalSkillTree.iterator();
		while(i.hasNext())
		{
			i.advance();
			for(SkillLearn learn : i.value())
			{
				if(learn.getItemId() <= 0 || !learn.isClicked())
					continue;
				a.add(learn);
			}
		}
		return a;
	}
	
	public void addAllNormalSkillLearns(TIntObjectHashMap<List<SkillLearn>> map)
	{
		for(ClassId classId : ClassId.VALUES)
		{
			if(classId.getRace() == null)
				continue;
			int classID = classId.getId();
			List<SkillLearn> temp = map.get(classID);
			if(temp == null)
			{
				info("Not found NORMAL skill learn for class " + classID);
				continue;
			}
			_normalSkillTree.put(classId.getId(), temp);
			ClassId secondparent = classId.getParent(1);
			if(secondparent == classId.getParent(0))
			{
				secondparent = null;
			}
			classId = classId.getParent(0);
			while(classId != null)
			{
				List<SkillLearn> parentList = _normalSkillTree.get(classId.getId());
				temp.addAll(parentList);
				if((classId = classId.getParent(0)) != null || secondparent == null)
					continue;
				classId = secondparent;
				secondparent = secondparent.getParent(1);
			}
		}
	}
	
	public void addAllFishingLearns(int race, List<SkillLearn> s)
	{
		_fishingSkillTree.put(race, s);
	}
	
	public void addAllPledgeLearns(List<SkillLearn> s)
	{
		_pledgeSkillTree.addAll(s);
	}
	
	@Override
	public void log()
	{
		info("load " + sizeTroveMap(_normalSkillTree) + " normal learns for " + _normalSkillTree.size() + " classes.");
		info("load " + sizeTroveMap(_fishingSkillTree) + " fishing learns for " + _fishingSkillTree.size() + " races.");
		info("load " + _pledgeSkillTree.size() + " pledge learns.");
	}
	
	@Deprecated
	@Override
	public int size()
	{
		return 0;
	}
	
	@Override
	public void clear()
	{
		_normalSkillTree.clear();
		_fishingSkillTree.clear();
		_pledgeSkillTree.clear();
	}
	
	private int sizeTroveMap(TIntObjectHashMap<List<SkillLearn>> a)
	{
		int i = 0;
		TIntObjectIterator iterator = a.iterator();
		while(iterator.hasNext())
		{
			iterator.advance();
			i += ((List) iterator.value()).size();
		}
		return i;
	}
}