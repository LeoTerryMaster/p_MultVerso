package l2.gameserver.data.xml.parser;

import gnu.trove.TIntObjectHashMap;
import l2.commons.data.xml.AbstractDirParser;
import l2.gameserver.Config;
import l2.gameserver.data.xml.holder.SkillAcquireHolder;
import l2.gameserver.model.SkillLearn;
import l2.gameserver.model.base.ClassType2;
import org.dom4j.Element;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class SkillAcquireParser extends AbstractDirParser<SkillAcquireHolder>
{
	private static final SkillAcquireParser _instance = new SkillAcquireParser();
	
	protected SkillAcquireParser()
	{
		super(SkillAcquireHolder.getInstance());
	}
	
	public static SkillAcquireParser getInstance()
	{
		return _instance;
	}
	
	@Override
	public File getXMLDir()
	{
		return new File(Config.DATAPACK_ROOT, "data/skill_tree/");
	}
	
	@Override
	public boolean isIgnored(File b)
	{
		return false;
	}
	
	@Override
	public String getDTDFileName()
	{
		return "tree.dtd";
	}
	
	@Override
	protected void readData(Element rootElement) throws Exception
	{
		Iterator iterator = rootElement.elementIterator("pledge_skill_tree");
		while(iterator.hasNext())
		{
			getHolder().addAllPledgeLearns(parseSkillLearn((Element) iterator.next()));
		}
		iterator = rootElement.elementIterator("fishing_skill_tree");
		while(iterator.hasNext())
		{
			Element nxt = (Element) iterator.next();
			Iterator classIterator = nxt.elementIterator("race");
			while(classIterator.hasNext())
			{
				Element classElement = (Element) classIterator.next();
				int race = Integer.parseInt(classElement.attributeValue("id"));
				List<SkillLearn> learns = parseSkillLearn(classElement);
				getHolder().addAllFishingLearns(race, learns);
			}
		}
		iterator = rootElement.elementIterator("normal_skill_tree");
		while(iterator.hasNext())
		{
			TIntObjectHashMap map = new TIntObjectHashMap();
			Element nxt = (Element) iterator.next();
			Iterator classIterator = nxt.elementIterator("class");
			while(classIterator.hasNext())
			{
				Element classElement = (Element) classIterator.next();
				int classId = Integer.parseInt(classElement.attributeValue("id"));
				List<SkillLearn> learns = parseSkillLearn(classElement);
				map.put(classId, learns);
			}
			getHolder().addAllNormalSkillLearns(map);
		}
	}
	
	private List<SkillLearn> parseSkillLearn(Element tree)
	{
		ArrayList<SkillLearn> skillLearns = new ArrayList<>();
		Iterator iterator = tree.elementIterator("skill");
		while(iterator.hasNext())
		{
			Element element = (Element) iterator.next();
			int id = Integer.parseInt(element.attributeValue("id"));
			int level = Integer.parseInt(element.attributeValue("level"));
			int cost = element.attributeValue("cost") == null ? 0 : Integer.parseInt(element.attributeValue("cost"));
			int min_level = Integer.parseInt(element.attributeValue("min_level"));
			int item_id = element.attributeValue("item_id") == null ? 0 : Integer.parseInt(element.attributeValue("item_id"));
			long item_count = element.attributeValue("item_count") == null ? 1 : Long.parseLong(element.attributeValue("item_count"));
			boolean clicked = element.attributeValue("clicked") != null && Boolean.parseBoolean(element.attributeValue("clicked"));
			ClassType2 classtype2 = ClassType2.valueOf(element.attributeValue("classtype2", "None"));
			skillLearns.add(new SkillLearn(id, level, min_level, cost * Config.SKILL_COST_RATE, item_id, item_count, clicked, classtype2));
		}
		return skillLearns;
	}
}