package l2.gameserver.data.xml.parser;

import gnu.trove.TIntIntHashMap;
import l2.commons.data.xml.AbstractFileParser;
import l2.gameserver.Config;
import l2.gameserver.data.xml.holder.CubicHolder;
import l2.gameserver.model.Skill;
import l2.gameserver.tables.SkillTable;
import l2.gameserver.templates.CubicTemplate;
import org.dom4j.Element;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

public final class CubicParser extends AbstractFileParser<CubicHolder>
{
	private static final CubicParser _instance = new CubicParser();
	
	protected CubicParser()
	{
		super(CubicHolder.getInstance());
	}
	
	public static CubicParser getInstance()
	{
		return _instance;
	}
	
	@Override
	public File getXMLFile()
	{
		return new File(Config.DATAPACK_ROOT, "data/cubics.xml");
	}
	
	@Override
	public String getDTDFileName()
	{
		return "cubics.dtd";
	}
	
	@Override
	protected void readData(Element rootElement) throws Exception
	{
		Iterator iterator = rootElement.elementIterator();
		while(iterator.hasNext())
		{
			Element cubicElement = (Element) iterator.next();
			int id = Integer.parseInt(cubicElement.attributeValue("id"));
			int level = Integer.parseInt(cubicElement.attributeValue("level"));
			int delay = Integer.parseInt(cubicElement.attributeValue("delay"));
			CubicTemplate template = new CubicTemplate(id, level, delay);
			getHolder().addCubicTemplate(template);
			Iterator skillsIterator = cubicElement.elementIterator();
			while(skillsIterator.hasNext())
			{
				Element skillsElement = (Element) skillsIterator.next();
				int chance = Integer.parseInt(skillsElement.attributeValue("chance"));
				ArrayList<CubicTemplate.SkillInfo> skills = new ArrayList<>(1);
				Iterator skillIterator = skillsElement.elementIterator();
				while(skillIterator.hasNext())
				{
					Element skillElement = (Element) skillIterator.next();
					int id2 = Integer.parseInt(skillElement.attributeValue("id"));
					int level2 = Integer.parseInt(skillElement.attributeValue("level"));
					String val = skillElement.attributeValue("chance");
					int chance2 = val == null ? 0 : Integer.parseInt(val);
					boolean canAttackDoor = Boolean.parseBoolean(skillElement.attributeValue("can_attack_door"));
					val = skillElement.attributeValue("min_hp");
					int minHp = val == null ? 0 : Integer.parseInt(val);
					val = skillElement.attributeValue("min_hp_per");
					int minHpPer = val == null ? 0 : Integer.parseInt(val);
					CubicTemplate.ActionType type = CubicTemplate.ActionType.valueOf(skillElement.attributeValue("action_type"));
					TIntIntHashMap set = new TIntIntHashMap();
					Iterator chanceIterator = skillElement.elementIterator();
					while(chanceIterator.hasNext())
					{
						Element chanceElement = (Element) chanceIterator.next();
						int min = Integer.parseInt(chanceElement.attributeValue("min"));
						int max = Integer.parseInt(chanceElement.attributeValue("max"));
						int value = Integer.parseInt(chanceElement.attributeValue("value"));
						for(int i = min;i <= max;++i)
						{
							set.put(i, value);
						}
					}
					if(chance2 == 0 && set.isEmpty())
					{
						warn("Wrong skill chance. Cubic: " + id + "/" + level);
					}
					Skill skill;
					if((skill = SkillTable.getInstance().getInfo(id2, level2)) == null)
						continue;
					skill.setCubicSkill(true);
					skills.add(new CubicTemplate.SkillInfo(skill, chance2, type, canAttackDoor, minHp, minHpPer, set));
				}
				template.putSkills(chance, skills);
			}
		}
	}
}