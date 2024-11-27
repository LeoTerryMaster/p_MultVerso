package l2.gameserver.data.xml.parser;

import l2.commons.data.xml.AbstractFileParser;
import l2.gameserver.Config;
import l2.gameserver.data.xml.holder.VariationGroupHolder;
import l2.gameserver.templates.item.support.VariationGroupData;
import org.dom4j.Element;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

public class VariationGroupParser extends AbstractFileParser<VariationGroupHolder>
{
	private static final VariationGroupParser _instance = new VariationGroupParser();
	private final HashMap<Integer, VariationGroupData> _byMineralId = new HashMap();
	
	private VariationGroupParser()
	{
		super(VariationGroupHolder.getInstance());
	}
	
	public static VariationGroupParser getInstance()
	{
		return _instance;
	}
	
	@Override
	public File getXMLFile()
	{
		return new File(Config.DATAPACK_ROOT, "data/variation_group.xml");
	}
	
	@Override
	public String getDTDFileName()
	{
		return "variation_group.dtd";
	}
	
	@Override
	protected void readData(Element rootElement) throws Exception
	{
		Iterator it = rootElement.elementIterator();
		while(it.hasNext())
		{
			Element vge = (Element) it.next();
			String groupName = vge.attributeValue("name");
			ArrayList<Integer> itemsList = new ArrayList<>();
			ArrayList<VariationGroupData> variationGroupDataList = new ArrayList<>();
			Iterator it2 = vge.elementIterator();
			while(it2.hasNext())
			{
				Element vge2 = (Element) it2.next();
				String vge2Name = vge2.getName();
				if("items".equalsIgnoreCase(vge2Name))
				{
					StringTokenizer stItems = new StringTokenizer(vge2.getStringValue());
					while(stItems.hasMoreTokens())
					{
						itemsList.add(Integer.parseInt(stItems.nextToken()));
					}
					continue;
				}
				if(!"fee".equalsIgnoreCase(vge2Name))
					continue;
				long cancelPrice = Long.parseLong(vge2.attributeValue("cancelPrice"));
				Iterator it3 = vge2.elementIterator();
				while(it3.hasNext())
				{
					Element vge3 = (Element) it3.next();
					if(!"mineral".equalsIgnoreCase(vge3.getName()))
						continue;
					int mineralItemId = Integer.parseInt(vge3.attributeValue("itemId"));
					int gemstoneItemId = Integer.parseInt(vge3.attributeValue("gemstoneItemId"));
					long gemstoneItemCnt = Long.parseLong(vge3.attributeValue("gemstoneItemCnt"));
					variationGroupDataList.add(new VariationGroupData(mineralItemId, gemstoneItemId, gemstoneItemCnt, cancelPrice));
				}
			}
			if(variationGroupDataList.isEmpty())
			{
				throw new RuntimeException("Undefined fee for group " + groupName);
			}
			int[] itemIds = new int[itemsList.size()];
			for(int i = 0;i < itemsList.size();++i)
			{
				itemIds[i] = itemsList.get(i);
			}
			Arrays.sort(itemIds);
			for(VariationGroupData variationGroupData : variationGroupDataList)
			{
				getHolder().addSorted(itemIds, variationGroupData);
			}
		}
	}
}