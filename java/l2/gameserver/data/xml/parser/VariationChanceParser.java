package l2.gameserver.data.xml.parser;

import l2.commons.data.xml.AbstractFileParser;
import l2.commons.util.RandomUtils;
import l2.gameserver.Config;
import l2.gameserver.data.xml.holder.VariationChanceHolder;
import l2.gameserver.templates.item.support.VariationChanceData;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.dom4j.Element;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class VariationChanceParser extends AbstractFileParser<VariationChanceHolder>
{
	private static final VariationChanceParser _instance = new VariationChanceParser();
	
	private VariationChanceParser()
	{
		super(VariationChanceHolder.getInstance());
	}
	
	public static VariationChanceParser getInstance()
	{
		return _instance;
	}
	
	@Override
	public File getXMLFile()
	{
		return new File(Config.DATAPACK_ROOT, "data/variation_data.xml");
	}
	
	@Override
	public String getDTDFileName()
	{
		return "variation_data.dtd";
	}
	
	private List<Pair<List<Pair<Integer, Double>>, Double>> readGroups(Element groupRootElement) throws Exception
	{
		ArrayList<Pair<List<Pair<Integer, Double>>, Double>> variation = new ArrayList<>();
		Iterator it = groupRootElement.elementIterator();
		while(it.hasNext())
		{
			Element ge = (Element) it.next();
			if(!"group".equalsIgnoreCase(ge.getName()))
				continue;
			double groupChance = Double.parseDouble(ge.attributeValue("chance"));
			ArrayList<ImmutablePair<Integer, Double>> groupOptions = new ArrayList<>();
			Iterator it2 = ge.elementIterator();
			while(it2.hasNext())
			{
				Element oe = (Element) it2.next();
				int id = Integer.parseInt(oe.attributeValue("id"));
				double chance = Double.parseDouble(oe.attributeValue("chance"));
				groupOptions.add(new ImmutablePair(id, chance));
			}
			Collections.sort(groupOptions, RandomUtils.DOUBLE_GROUP_COMPARATOR);
			variation.add(new ImmutablePair(groupOptions, groupChance));
		}
		return variation;
	}
	
	@Override
	protected void readData(Element rootElement) throws Exception
	{
		Iterator it = rootElement.elementIterator();
		while(it.hasNext())
		{
			Element vde = (Element) it.next();
			int mineralId = Integer.parseInt(vde.attributeValue("mineralId"));
			boolean isMage = false;
			VariationChanceData warriorData = null;
			VariationChanceData mageData = null;
			Iterator it2 = vde.elementIterator();
			while(it2.hasNext())
			{
				Element oe = (Element) it2.next();
				String typeStr = oe.attributeValue("type");
				List<Pair<List<Pair<Integer, Double>>, Double>> variation1 = null;
				List<Pair<List<Pair<Integer, Double>>, Double>> variation2 = null;
				Iterator it3 = oe.elementIterator();
				while(it3.hasNext())
				{
					List<Pair<List<Pair<Integer, Double>>, Double>> variation;
					Element ve = (Element) it3.next();
					if("variation1".equalsIgnoreCase(ve.getName()))
					{
						variation = readGroups(ve);
						Collections.sort(variation, RandomUtils.DOUBLE_GROUP_COMPARATOR);
						variation1 = variation;
						continue;
					}
					if(!"variation2".equalsIgnoreCase(ve.getName()))
						continue;
					variation = readGroups(ve);
					Collections.sort(variation, RandomUtils.DOUBLE_GROUP_COMPARATOR);
					variation2 = variation;
				}
				if(typeStr.equalsIgnoreCase("WARRIOR"))
				{
					warriorData = new VariationChanceData(mineralId, variation1 != null ? variation1 : Collections.emptyList(), variation2 != null ? variation2 : Collections.emptyList());
					continue;
				}
				if(typeStr.equalsIgnoreCase("MAGE"))
				{
					mageData = new VariationChanceData(mineralId, variation1 != null ? variation1 : Collections.emptyList(), variation2 != null ? variation2 : Collections.emptyList());
					continue;
				}
				throw new RuntimeException("Unknown type " + typeStr);
			}
			getHolder().add((Pair<VariationChanceData, VariationChanceData>) new ImmutablePair(warriorData, mageData));
		}
	}
}