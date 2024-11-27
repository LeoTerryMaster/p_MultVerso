package l2.gameserver.data.xml.parser;

import l2.gameserver.Config;
import l2.gameserver.data.xml.holder.OptionDataHolder;
import l2.gameserver.model.Skill;
import l2.gameserver.stats.triggers.TriggerInfo;
import l2.gameserver.stats.triggers.TriggerType;
import l2.gameserver.tables.SkillTable;
import l2.gameserver.templates.OptionDataTemplate;
import org.dom4j.Element;

import java.io.File;
import java.util.Iterator;

public final class OptionDataParser extends StatParser<OptionDataHolder>
{
	private static final OptionDataParser _instance = new OptionDataParser();
	
	protected OptionDataParser()
	{
		super(OptionDataHolder.getInstance());
	}
	
	public static OptionDataParser getInstance()
	{
		return _instance;
	}
	
	@Override
	public File getXMLDir()
	{
		return new File(Config.DATAPACK_ROOT, "data/optiondata");
	}
	
	@Override
	public boolean isIgnored(File f)
	{
		return false;
	}
	
	@Override
	public String getDTDFileName()
	{
		return "optiondata.dtd";
	}
	
	@Override
	protected void readData(Element rootElement) throws Exception
	{
		Iterator itemIterator = rootElement.elementIterator();
		while(itemIterator.hasNext())
		{
			Element optionDataElement = (Element) itemIterator.next();
			OptionDataTemplate template = new OptionDataTemplate(Integer.parseInt(optionDataElement.attributeValue("id")));
			Iterator subIterator = optionDataElement.elementIterator();
			while(subIterator.hasNext())
			{
				Element subElement = (Element) subIterator.next();
				String subName = subElement.getName();
				if(subName.equalsIgnoreCase("for"))
				{
					parseFor(subElement, template);
					continue;
				}
				int level;
				int id;
				if(subName.equalsIgnoreCase("trigger"))
				{
					id = parseNumber(subElement.attributeValue("id")).intValue();
					level = parseNumber(subElement.attributeValue("level")).intValue();
					TriggerType t = TriggerType.valueOf(subElement.attributeValue("type"));
					double chance = parseNumber(subElement.attributeValue("chance")).doubleValue();
					TriggerInfo trigger = new TriggerInfo(id, level, t, chance);
					template.addTrigger(trigger);
					continue;
				}
				if(!subName.equalsIgnoreCase("skill"))
					continue;
				id = Integer.parseInt(subElement.attributeValue("id"));
				level = Integer.parseInt(subElement.attributeValue("level"));
				Skill skill = SkillTable.getInstance().getInfo(id, level);
				if(skill != null)
				{
					template.addSkill(skill);
					continue;
				}
				info("Skill not found(" + id + "," + level + ") for option data:" + template.getId() + "; file:" + getCurrentFileName());
			}
			getHolder().addTemplate(template);
		}
	}
	
	@Override
	protected Object getTableValue(String name)
	{
		return null;
	}
}