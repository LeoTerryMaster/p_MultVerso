package l2.gameserver.data.xml.parser;

import l2.commons.data.xml.AbstractFileParser;
import l2.commons.geometry.Polygon;
import l2.gameserver.Config;
import l2.gameserver.instancemanager.MapRegionManager;
import l2.gameserver.model.Territory;
import l2.gameserver.templates.mapregion.DomainArea;
import org.dom4j.Element;

import java.io.File;
import java.util.Iterator;

public class DomainParser extends AbstractFileParser<MapRegionManager>
{
	private static final DomainParser _instance = new DomainParser();
	
	protected DomainParser()
	{
		super(MapRegionManager.getInstance());
	}
	
	public static DomainParser getInstance()
	{
		return _instance;
	}
	
	@Override
	public File getXMLFile()
	{
		return new File(Config.DATAPACK_ROOT, "data/mapregion/domains.xml");
	}
	
	@Override
	public String getDTDFileName()
	{
		return "domains.dtd";
	}
	
	@Override
	protected void readData(Element rootElement) throws Exception
	{
		Iterator iterator = rootElement.elementIterator();
		while(iterator.hasNext())
		{
			Element listElement = (Element) iterator.next();
			if(!"domain".equals(listElement.getName()))
				continue;
			int id = Integer.parseInt(listElement.attributeValue("id"));
			Territory territory = null;
			Iterator i = listElement.elementIterator();
			while(i.hasNext())
			{
				Element n = (Element) i.next();
				if(!"polygon".equalsIgnoreCase(n.getName()))
					continue;
				Polygon shape = ZoneParser.parsePolygon(n);
				if(!shape.validate())
				{
					error("DomainParser: invalid territory data : " + shape + "!");
				}
				if(territory == null)
				{
					territory = new Territory();
				}
				territory.add(shape);
			}
			if(territory == null)
			{
				throw new RuntimeException("DomainParser: empty territory!");
			}
			getHolder().addRegionData(new DomainArea(id, territory));
		}
	}
}