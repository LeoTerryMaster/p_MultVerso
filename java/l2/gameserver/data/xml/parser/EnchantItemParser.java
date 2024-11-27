package l2.gameserver.data.xml.parser;

import l2.commons.data.xml.AbstractFileParser;
import l2.gameserver.Config;
import l2.gameserver.data.xml.holder.EnchantItemHolder;
import l2.gameserver.templates.item.ItemTemplate;
import l2.gameserver.templates.item.support.EnchantScroll;
import l2.gameserver.templates.item.support.EnchantScrollOnFailAction;
import l2.gameserver.templates.item.support.EnchantTargetType;
import org.dom4j.Element;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

public class EnchantItemParser extends AbstractFileParser<EnchantItemHolder>
{
	private static final EnchantItemParser _instance = new EnchantItemParser();
	
	private EnchantItemParser()
	{
		super(EnchantItemHolder.getInstance());
	}
	
	public static EnchantItemParser getInstance()
	{
		return _instance;
	}
	
	@Override
	public File getXMLFile()
	{
		return new File(Config.DATAPACK_ROOT, "data/enchant_items.xml");
	}
	
	@Override
	public String getDTDFileName()
	{
		return "enchant_items.dtd";
	}
	
	@Override
	protected void readData(Element rootElement) throws Exception
	{
		Iterator it = rootElement.elementIterator();
		while(it.hasNext())
		{
			Element el = (Element) it.next();
			if(el.getName().equals("scroll"))
			{
				int scroll_id = Integer.parseInt(el.attributeValue("id"));
				boolean infallible = Boolean.parseBoolean(el.attributeValue("infallible", "false"));
				EnchantScrollOnFailAction esofa = EnchantScrollOnFailAction.NONE;
				int rfl = 0;
				if(!infallible)
				{
					esofa = EnchantScrollOnFailAction.valueOf(el.attributeValue("on_fail"));
					rfl = Integer.parseInt(el.attributeValue("reset_lvl", "0"));
				}
				double chance_bonus = Double.parseDouble(el.attributeValue("chance_bonus", "0"));
				ItemTemplate.Grade grade = ItemTemplate.Grade.valueOf(el.attributeValue("grade"));
				int minLvl = 0;
				int maxLvl = Config.ENCHANT_MAX;
				int increment = Integer.parseInt(el.attributeValue("increment", "1"));
				EnchantTargetType ett = EnchantTargetType.ALL;
				ArrayList<Integer> itemRestricted = new ArrayList<>();
				Iterator it2 = el.elementIterator();
				while(it2.hasNext())
				{
					Element el2 = (Element) it2.next();
					if(el2.getName().equals("levels"))
					{
						minLvl = Integer.parseInt(el2.attributeValue("min"));
						maxLvl = Integer.parseInt(el2.attributeValue("max"));
						continue;
					}
					if(!el2.getName().equals("items_restrict"))
						continue;
					ett = EnchantTargetType.valueOf(el2.attributeValue("type"));
					Iterator irit = el2.elementIterator("item");
					while(irit.hasNext())
					{
						itemRestricted.add(Integer.valueOf(((Element) irit.next()).attributeValue("id")));
					}
				}
				EnchantScroll es = new EnchantScroll(scroll_id, increment, chance_bonus, grade, minLvl, maxLvl, ett, esofa, rfl, infallible, false);
				if(!itemRestricted.isEmpty())
				{
					for(Integer itid : itemRestricted)
					{
						es.addItemRestrict(itid);
					}
				}
				getHolder().addEnchantItem(es);
				continue;
			}
			error("Unknown entry " + el.getName());
		}
	}
}