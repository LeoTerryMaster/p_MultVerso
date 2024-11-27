package l2.gameserver.data.xml.parser;

import l2.commons.collections.MultiValueSet;
import l2.commons.data.xml.AbstractFileParser;
import l2.gameserver.Config;
import l2.gameserver.data.xml.holder.FishDataHolder;
import l2.gameserver.templates.item.support.FishGroup;
import l2.gameserver.templates.item.support.FishTemplate;
import l2.gameserver.templates.item.support.LureTemplate;
import l2.gameserver.templates.item.support.LureType;
import org.dom4j.Attribute;
import org.dom4j.Element;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

public class FishDataParser extends AbstractFileParser<FishDataHolder>
{
	private static final FishDataParser _instance = new FishDataParser();
	
	private FishDataParser()
	{
		super(FishDataHolder.getInstance());
	}
	
	public static FishDataParser getInstance()
	{
		return _instance;
	}
	
	@Override
	public File getXMLFile()
	{
		return new File(Config.DATAPACK_ROOT, "data/fishdata.xml");
	}
	
	@Override
	public String getDTDFileName()
	{
		return "fishdata.dtd";
	}
	
	@Override
	protected void readData(Element rootElement) throws Exception
	{
		Iterator iterator = rootElement.elementIterator();
		while(iterator.hasNext())
		{
			Iterator attributeIterator;
			MultiValueSet<String> map;
			Attribute attribute;
			Element e = (Element) iterator.next();
			if("fish".equals(e.getName()))
			{
				map = new MultiValueSet<>();
				attributeIterator = e.attributeIterator();
				while(attributeIterator.hasNext())
				{
					attribute = (Attribute) attributeIterator.next();
					map.put(attribute.getName(), attribute.getValue());
				}
				getHolder().addFish(new FishTemplate(map));
				continue;
			}
			if("lure".equals(e.getName()))
			{
				map = new MultiValueSet();
				attributeIterator = e.attributeIterator();
				while(attributeIterator.hasNext())
				{
					attribute = (Attribute) attributeIterator.next();
					map.put(attribute.getName(), attribute.getValue());
				}
				HashMap<FishGroup, Integer> chances = new HashMap<>();
				Iterator elementIterator = e.elementIterator();
				while(elementIterator.hasNext())
				{
					Element chanceElement = (Element) elementIterator.next();
					chances.put(FishGroup.valueOf(chanceElement.attributeValue("type")), Integer.parseInt(chanceElement.attributeValue("value")));
				}
				map.put("chances", chances);
				getHolder().addLure(new LureTemplate(map));
				continue;
			}
			if(!"distribution".equals(e.getName()))
				continue;
			int id = Integer.parseInt(e.attributeValue("id"));
			Iterator forLureIterator = e.elementIterator();
			while(forLureIterator.hasNext())
			{
				Element forLureElement = (Element) forLureIterator.next();
				LureType lureType = LureType.valueOf(forLureElement.attributeValue("type"));
				HashMap<FishGroup, Integer> chances = new HashMap<>();
				Iterator chanceIterator = forLureElement.elementIterator();
				while(chanceIterator.hasNext())
				{
					Element chanceElement = (Element) chanceIterator.next();
					chances.put(FishGroup.valueOf(chanceElement.attributeValue("type")), Integer.parseInt(chanceElement.attributeValue("value")));
				}
				getHolder().addDistribution(id, lureType, chances);
			}
		}
	}
}