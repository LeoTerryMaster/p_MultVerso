package l2.gameserver.data.xml.parser;

import l2.commons.data.xml.AbstractFileParser;
import l2.gameserver.Config;
import l2.gameserver.data.xml.holder.ArmorSetsHolder;
import l2.gameserver.model.ArmorSet;
import org.dom4j.Element;

import java.io.File;
import java.util.Iterator;

public final class ArmorSetsParser extends AbstractFileParser<ArmorSetsHolder>
{
	private static final ArmorSetsParser _instance = new ArmorSetsParser();
	
	private ArmorSetsParser()
	{
		super(ArmorSetsHolder.getInstance());
	}
	
	public static ArmorSetsParser getInstance()
	{
		return _instance;
	}
	
	@Override
	public File getXMLFile()
	{
		return new File(Config.DATAPACK_ROOT, "data/armor_sets.xml");
	}
	
	@Override
	public String getDTDFileName()
	{
		return "armor_sets.dtd";
	}
	
	@Override
	protected void readData(Element rootElement) throws Exception
	{
		Iterator iterator = rootElement.elementIterator("set");
		while(iterator.hasNext())
		{
			String[] chest = null;
			Element element = (Element) iterator.next();
			int id = Integer.parseInt(element.attributeValue("id"));
			if(element.attributeValue("chest") != null)
			{
				chest = element.attributeValue("chest").split(";");
			}
			String[] legs = null;
			if(element.attributeValue("legs") != null)
			{
				legs = element.attributeValue("legs").split(";");
			}
			String[] head = null;
			if(element.attributeValue("head") != null)
			{
				head = element.attributeValue("head").split(";");
			}
			String[] gloves = null;
			if(element.attributeValue("gloves") != null)
			{
				gloves = element.attributeValue("gloves").split(";");
			}
			String[] feet = null;
			if(element.attributeValue("feet") != null)
			{
				feet = element.attributeValue("feet").split(";");
			}
			String[] skills = null;
			if(element.attributeValue("skills") != null)
			{
				skills = element.attributeValue("skills").split(";");
			}
			String[] shield = null;
			if(element.attributeValue("shield") != null)
			{
				shield = element.attributeValue("shield").split(";");
			}
			String[] shield_skills = null;
			if(element.attributeValue("shield_skills") != null)
			{
				shield_skills = element.attributeValue("shield_skills").split(";");
			}
			String[] enchant6skills = null;
			if(element.attributeValue("enchant6skills") != null)
			{
				enchant6skills = element.attributeValue("enchant6skills").split(";");
			}
			getHolder().addArmorSet(new ArmorSet(id, chest, legs, head, gloves, feet, skills, shield, shield_skills, enchant6skills));
		}
	}
}