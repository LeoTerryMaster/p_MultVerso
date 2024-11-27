package l2.gameserver.skills;

import l2.gameserver.data.xml.holder.EnchantSkillHolder;
import l2.gameserver.model.Skill;
import l2.gameserver.stats.conditions.Condition;
import l2.gameserver.templates.SkillEnchant;
import l2.gameserver.templates.StatsSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.io.File;
import java.util.*;

@Deprecated
public final class DocumentSkill extends DocumentBase
{
	private static final Logger _log = LoggerFactory.getLogger(DocumentSkill.class);
	private static final String SKILL_ENCHANT_NODE_NAME = "enchant";
	private static final Comparator<Integer> INTEGER_KEY_ASC_COMPARATOR = new Comparator<Integer>()
	{
		@Override
		public int compare(Integer o1, Integer o2)
		{
			return o1 - o2;
		}
	};
	private final Set<String> usedTables = new HashSet<>();
	private final List<Skill> skillsInFile = new LinkedList<>();
	protected Map<String, Map<Integer, Object>> tables = new LinkedHashMap<>();
	private SkillLoad currentSkill;
	
	DocumentSkill(File file)
	{
		super(file);
	}
	
	protected void resetTable()
	{
		if(!usedTables.isEmpty())
		{
			for(String table : tables.keySet())
			{
				if(usedTables.contains(table))
					continue;
				_log.warn("Unused table " + table + " for skill " + currentSkill.id);
			}
		}
		usedTables.clear();
		tables.clear();
	}
	
	private void setCurrentSkill(SkillLoad skill)
	{
		currentSkill = skill;
	}
	
	protected List<Skill> getSkills()
	{
		return skillsInFile;
	}
	
	@Override
	protected Object getTableValue(String name)
	{
		Map<Integer, Object> values = tables.get(name);
		if(values == null)
		{
			_log.error("No table " + name + " for skill " + currentSkill.id);
			return 0;
		}
		if(!values.containsKey(currentSkill.currentLevel))
		{
			_log.error("No value in table " + name + " for skill " + currentSkill.id + " at level " + currentSkill.currentLevel);
			return 0;
		}
		usedTables.add(name);
		return values.get(currentSkill.currentLevel);
	}
	
	@Override
	protected Object getTableValue(String name, int level)
	{
		Map<Integer, Object> values = tables.get(name);
		if(values == null)
		{
			_log.error("No table " + name + " for skill " + currentSkill.id);
			return 0;
		}
		if(!values.containsKey(level))
		{
			_log.error("No value in table " + name + " for skill " + currentSkill.id + " at level " + level);
			return 0;
		}
		usedTables.add(name);
		return values.get(level);
	}
	
	@Override
	protected void parseDocument(Document doc)
	{
		for(Node n = doc.getFirstChild();n != null;n = n.getNextSibling())
		{
			if("list".equalsIgnoreCase(n.getNodeName()))
			{
				for(Node d = n.getFirstChild();d != null;d = d.getNextSibling())
				{
					if(!"skill".equalsIgnoreCase(d.getNodeName()))
						continue;
					parseSkill(d);
					skillsInFile.addAll(currentSkill.skills);
					resetTable();
				}
				continue;
			}
			if(!"skill".equalsIgnoreCase(n.getNodeName()))
				continue;
			parseSkill(n);
			skillsInFile.addAll(currentSkill.skills);
		}
	}
	
	private void loadTable(Node tableNode, int skillLevelOffset, int levels)
	{
		NamedNodeMap tableNodeAttrs = tableNode.getAttributes();
		String tableName = tableNodeAttrs.getNamedItem("name").getNodeValue();
		Object[] tableContent = fillTableToSize(parseTable(tableNode), levels);
		Map<Integer, Object> globalTableLevels = tables.get(tableName);
		if(globalTableLevels == null)
		{
			globalTableLevels = new TreeMap<>(INTEGER_KEY_ASC_COMPARATOR);
			tables.put(tableName, globalTableLevels);
		}
		for(int tblContIdx = 0;tblContIdx < tableContent.length;++tblContIdx)
		{
			int skillLvl = skillLevelOffset + tblContIdx;
			if(globalTableLevels.containsKey(skillLvl))
			{
				_log.error("Duplicate skill level " + skillLvl + " in table " + tableName + " in skill " + currentSkill.id);
				return;
			}
			globalTableLevels.put(skillLvl, tableContent[tblContIdx]);
		}
	}
	
	protected void parseSkill(Node n)
	{
		NamedNodeMap attrs = n.getAttributes();
		int skillId = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
		String skillName = attrs.getNamedItem("name").getNodeValue();
		int skillBaseLevels = Integer.parseInt(attrs.getNamedItem("levels").getNodeValue());
		setCurrentSkill(new SkillLoad(skillId, skillName));
		try
		{
			ArrayList<Integer> skillLevelsList = new ArrayList<>();
			for(int skillLvl = 1;skillLvl <= skillBaseLevels;++skillLvl)
			{
				skillLevelsList.add(skillLvl);
			}
			Node skillRootNode = n.cloneNode(true);
			int skillChildNodesLen = skillRootNode.getChildNodes().getLength();
			for(int skillChildNodeIdx = 0;skillChildNodeIdx < skillChildNodesLen;++skillChildNodeIdx)
			{
				Node skillEnchRootNode = skillRootNode.getChildNodes().item(skillChildNodeIdx);
				String skillEnchNodeName = skillEnchRootNode.getNodeName();
				if(!skillEnchNodeName.startsWith("enchant"))
					continue;
				int skillCurrEnchantRoute;
				try
				{
					skillCurrEnchantRoute = Integer.parseInt(skillEnchNodeName.substring("enchant".length()));
				}
				catch(NumberFormatException e)
				{
					_log.error("Wrong enchant " + skillEnchNodeName + " in skill " + skillId);
					break;
				}
				int skillEnchRouteFirstSkillLevel = EnchantSkillHolder.getInstance().getFirstSkillLevelOf(skillId, skillCurrEnchantRoute);
				Node skillEnchLevelsNode = skillEnchRootNode.getAttributes().getNamedItem("levels");
				int skillEnchantLevels;
				if(skillEnchLevelsNode != null)
				{
					skillEnchantLevels = Integer.parseInt(skillEnchLevelsNode.getNodeValue());
				}
				else
				{
					_log.warn("Skill " + skillId + " have no enchant levels in route " + skillCurrEnchantRoute + ".");
					skillEnchantLevels = EnchantSkillHolder.getInstance().getMaxEnchantLevelOf(skillId);
				}
				int skillRouteMaxEnchantLevel = EnchantSkillHolder.getInstance().getMaxEnchantLevelOf(skillId);
				if(skillEnchantLevels != skillRouteMaxEnchantLevel)
				{
					_log.warn("Unknown enchant levels " + skillEnchantLevels + " for skill " + skillId + ". Actual " + skillRouteMaxEnchantLevel);
				}
				for(int skillEnchantLevel = 1;skillEnchantLevel <= skillEnchantLevels;++skillEnchantLevel)
				{
					SkillEnchant skillEnchant = EnchantSkillHolder.getInstance().getSkillEnchant(skillId, skillCurrEnchantRoute, skillEnchantLevel);
					if(skillEnchant == null)
					{
						_log.error("No enchant level " + skillEnchantLevel + " in route " + skillCurrEnchantRoute + " for skill " + skillId);
						break;
					}
					skillLevelsList.add(skillEnchant.getSkillLevel());
				}
				for(Node skillEnchNode = skillEnchRootNode.getFirstChild();skillEnchNode != null;skillEnchNode = skillEnchNode.getNextSibling())
				{
					if("table".equalsIgnoreCase(skillEnchNode.getNodeName()))
					{
						Node skillEnchTableNode = skillEnchNode;
						loadTable(skillEnchNode, skillEnchRouteFirstSkillLevel, skillEnchantLevels);
						continue;
					}
					if(skillEnchNode.getNodeType() != 1)
						continue;
					_log.error("Unknown element of enchant \"" + skillEnchNode.getNodeName() + "\" in skill " + skillId);
				}
			}
			for(Node skillTableNode = n.getFirstChild();skillTableNode != null;skillTableNode = skillTableNode.getNextSibling())
			{
				if(!"table".equalsIgnoreCase(skillTableNode.getNodeName()))
					continue;
				loadTable(skillTableNode, 1, skillBaseLevels);
			}
			for(Map.Entry tableEntry : tables.entrySet())
			{
				Map table = (Map) tableEntry.getValue();
				Object baseEnchantValue = table.get(skillBaseLevels);
				for(Integer skillLevel : skillLevelsList)
				{
					if(skillLevel <= skillBaseLevels || table.containsKey(skillLevel))
						continue;
					table.put(skillLevel, baseEnchantValue);
				}
			}
			for(Integer skillLevel : skillLevelsList)
			{
				StatsSet currLevelStatSet = new StatsSet();
				currLevelStatSet.set("skill_id", currentSkill.id);
				currLevelStatSet.set("level", skillLevel);
				currLevelStatSet.set("name", currentSkill.name);
				currLevelStatSet.set("base_level", skillBaseLevels);
				currentSkill.sets.put(skillLevel, currLevelStatSet);
			}
			for(Integer skillLevel : skillLevelsList)
			{
				for(Node skillSetNode = n.getFirstChild();skillSetNode != null;skillSetNode = skillSetNode.getNextSibling())
				{
					if(!"set".equalsIgnoreCase(skillSetNode.getNodeName()))
						continue;
					StatsSet skillCurrLevelSet = currentSkill.sets.get(skillLevel);
					currentSkill.currentLevel = skillLevel;
					parseBeanSet(skillSetNode, skillCurrLevelSet, skillLevel);
				}
			}
			for(StatsSet currStatsSet : currentSkill.sets.values())
			{
				Skill newSkill = currStatsSet.getEnum("skillType", Skill.SkillType.class).makeSkill(currStatsSet);
				currentSkill.currentSkills.put(newSkill.getLevel(), newSkill);
			}
			for(Integer skillLevel : skillLevelsList)
			{
				currentSkill.currentLevel = skillLevel;
				Skill currSkill = currentSkill.currentSkills.get(skillLevel);
				if(currSkill == null)
				{
					_log.error("Undefined skill id " + skillId + " level " + skillLevel);
					return;
				}
				currSkill.setDisplayLevel(skillLevel);
				for(Node skillNode = n.getFirstChild();skillNode != null;skillNode = skillNode.getNextSibling())
				{
					String skillNodeName = skillNode.getNodeName();
					if("cond".equalsIgnoreCase(skillNodeName))
					{
						Condition condition = parseCondition(skillNode.getFirstChild());
						if(condition == null)
							continue;
						Node sysMsgIdAttr = skillNode.getAttributes().getNamedItem("msgId");
						if(sysMsgIdAttr != null)
						{
							int sysMsgId = parseNumber(sysMsgIdAttr.getNodeValue()).intValue();
							condition.setSystemMsg(sysMsgId);
						}
						currSkill.attach(condition);
						continue;
					}
					if("for".equalsIgnoreCase(skillNodeName))
					{
						parseTemplate(skillNode, currSkill);
						continue;
					}
					if(!"triggers".equalsIgnoreCase(skillNodeName))
						continue;
					parseTrigger(skillNode, currSkill);
				}
			}
			currentSkill.skills.addAll(currentSkill.currentSkills.values());
		}
		catch(Exception e)
		{
			_log.error("Error loading skill " + skillId, e);
		}
	}
	
	protected Object[] parseTable(Node n)
	{
		NamedNodeMap attrs = n.getAttributes();
		String name = attrs.getNamedItem("name").getNodeValue();
		if(name.charAt(0) != '#')
		{
			throw new IllegalArgumentException("Table name must start with #");
		}
		StringTokenizer data = new StringTokenizer(n.getFirstChild().getNodeValue());
		ArrayList<String> array = new ArrayList<>();
		while(data.hasMoreTokens())
		{
			array.add(data.nextToken());
		}
		Object[] res = array.toArray(new Object[array.size()]);
		return res;
	}
	
	private Object[] fillTableToSize(Object[] table, int size)
	{
		if(table.length < size)
		{
			Object[] ret = new Object[size];
			System.arraycopy(table, 0, ret, 0, table.length);
			table = ret;
		}
		for(int j = 1;j < size;++j)
		{
			if(table[j] != null)
				continue;
			table[j] = table[j - 1];
		}
		return table;
	}
	
	public class SkillLoad
	{
		public final int id;
		public final String name;
		public final Map<Integer, StatsSet> sets;
		public final List<Skill> skills;
		public final Map<Integer, Skill> currentSkills;
		public int currentLevel;
		
		public SkillLoad(int id_, String name_)
		{
			id = id_;
			name = name_;
			sets = new TreeMap<>(INTEGER_KEY_ASC_COMPARATOR);
			skills = new ArrayList<>();
			currentSkills = new TreeMap<>(INTEGER_KEY_ASC_COMPARATOR);
		}
	}
}