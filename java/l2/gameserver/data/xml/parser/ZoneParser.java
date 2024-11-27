package l2.gameserver.data.xml.parser;

import l2.commons.data.xml.AbstractDirParser;
import l2.commons.geometry.Circle;
import l2.commons.geometry.Polygon;
import l2.commons.geometry.Rectangle;
import l2.gameserver.Config;
import l2.gameserver.data.xml.holder.ZoneHolder;
import l2.gameserver.model.Territory;
import l2.gameserver.model.World;
import l2.gameserver.templates.StatsSet;
import l2.gameserver.templates.ZoneTemplate;
import l2.gameserver.utils.Location;
import org.dom4j.Element;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

public class ZoneParser extends AbstractDirParser<ZoneHolder>
{
	private static final ZoneParser _instance = new ZoneParser();
	
	protected ZoneParser()
	{
		super(ZoneHolder.getInstance());
	}
	
	public static ZoneParser getInstance()
	{
		return _instance;
	}
	
	public static Rectangle parseRectangle(Element n) throws Exception
	{
		int zmin = World.MAP_MIN_Z;
		int zmax = World.MAP_MAX_Z;
		Iterator i = n.elementIterator();
		Element d = (Element) i.next();
		String[] coord = d.attributeValue("loc").split("[\\s,;]+");
		int x1 = Integer.parseInt(coord[0]);
		int y1 = Integer.parseInt(coord[1]);
		if(coord.length > 2)
		{
			zmin = Integer.parseInt(coord[2]);
			zmax = Integer.parseInt(coord[3]);
		}
		d = (Element) i.next();
		coord = d.attributeValue("loc").split("[\\s,;]+");
		int x2 = Integer.parseInt(coord[0]);
		int y2 = Integer.parseInt(coord[1]);
		if(coord.length > 2)
		{
			zmin = Integer.parseInt(coord[2]);
			zmax = Integer.parseInt(coord[3]);
		}
		Rectangle rectangle = new Rectangle(x1, y1, x2, y2);
		rectangle.setZmin(zmin);
		rectangle.setZmax(zmax);
		return rectangle;
	}
	
	public static Polygon parsePolygon(Element shape) throws Exception
	{
		Polygon poly = new Polygon();
		Iterator i = shape.elementIterator();
		while(i.hasNext())
		{
			Element d = (Element) i.next();
			if(!"coords".equals(d.getName()))
				continue;
			String[] coord = d.attributeValue("loc").split("[\\s,;]+");
			if(coord.length < 4)
			{
				poly.add(Integer.parseInt(coord[0]), Integer.parseInt(coord[1])).setZmin(World.MAP_MIN_Z).setZmax(World.MAP_MAX_Z);
				continue;
			}
			poly.add(Integer.parseInt(coord[0]), Integer.parseInt(coord[1])).setZmin(Integer.parseInt(coord[2])).setZmax(Integer.parseInt(coord[3]));
		}
		return poly;
	}
	
	public static Circle parseCircle(Element shape) throws Exception
	{
		String[] coord = shape.attribute("loc").getValue().split("[\\s,;]+");
		Circle circle = coord.length < 5 ? new Circle(Integer.parseInt(coord[0]), Integer.parseInt(coord[1]), Integer.parseInt(coord[2])).setZmin(World.MAP_MIN_Z).setZmax(World.MAP_MAX_Z) : new Circle(Integer.parseInt(coord[0]), Integer.parseInt(coord[1]), Integer.parseInt(coord[2])).setZmin(Integer.parseInt(coord[3])).setZmax(Integer.parseInt(coord[4]));
		return circle;
	}
	
	@Override
	public File getXMLDir()
	{
		return new File(Config.DATAPACK_ROOT, "data/zone/");
	}
	
	@Override
	public boolean isIgnored(File f)
	{
		return false;
	}
	
	@Override
	public String getDTDFileName()
	{
		return "zone.dtd";
	}
	
	@Override
	protected void readData(Element rootElement) throws Exception
	{
		Iterator iterator = rootElement.elementIterator();
		while(iterator.hasNext())
		{
			Element zoneElement = (Element) iterator.next();
			if(!"zone".equals(zoneElement.getName()))
				continue;
			StatsSet zoneDat = new StatsSet();
			zoneDat.set("name", zoneElement.attribute("name").getValue());
			zoneDat.set("type", zoneElement.attribute("type").getValue());
			Territory territory = null;
			Iterator i = zoneElement.elementIterator();
			while(i.hasNext())
			{
				Element n = (Element) i.next();
				if("set".equals(n.getName()))
				{
					zoneDat.set(n.attributeValue("name"), n.attributeValue("val"));
					continue;
				}
				Element d;
				Iterator ii;
				Location loc;
				if("restart_point".equals(n.getName()))
				{
					ii = n.elementIterator();
					ArrayList<Location> restartPoints = new ArrayList<>();
					while(ii.hasNext())
					{
						d = (Element) ii.next();
						if(!"coords".equalsIgnoreCase(d.getName()))
							continue;
						loc = Location.parseLoc(d.attribute("loc").getValue());
						restartPoints.add(loc);
					}
					zoneDat.set("restart_points", restartPoints);
					continue;
				}
				if("PKrestart_point".equals(n.getName()))
				{
					ii = n.elementIterator();
					ArrayList<Location> PKrestartPoints = new ArrayList<>();
					while(ii.hasNext())
					{
						d = (Element) ii.next();
						if(!"coords".equalsIgnoreCase(d.getName()))
							continue;
						loc = Location.parseLoc(d.attribute("loc").getValue());
						PKrestartPoints.add(loc);
					}
					zoneDat.set("PKrestart_points", PKrestartPoints);
					continue;
				}
				boolean isShape = "rectangle".equalsIgnoreCase(n.getName());
				if(isShape || "banned_rectangle".equalsIgnoreCase(n.getName()))
				{
					Rectangle shape = parseRectangle(n);
					if(territory == null)
					{
						territory = new Territory();
						zoneDat.set("territory", territory);
					}
					if(isShape)
					{
						territory.add(shape);
						continue;
					}
					territory.addBanned(shape);
					continue;
				}
				isShape = "circle".equalsIgnoreCase(n.getName());
				if(isShape || "banned_cicrcle".equalsIgnoreCase(n.getName()))
				{
					Circle shape = parseCircle(n);
					if(territory == null)
					{
						territory = new Territory();
						zoneDat.set("territory", territory);
					}
					if(isShape)
					{
						territory.add(shape);
						continue;
					}
					territory.addBanned(shape);
					continue;
				}
				isShape = "polygon".equalsIgnoreCase(n.getName());
				if(!isShape && !"banned_polygon".equalsIgnoreCase(n.getName()))
					continue;
				Polygon shape = parsePolygon(n);
				if(!shape.validate())
				{
					error("ZoneParser: invalid territory data : " + shape + ", zone: " + zoneDat.getString("name") + "!");
				}
				if(territory == null)
				{
					territory = new Territory();
					zoneDat.set("territory", territory);
				}
				if(isShape)
				{
					territory.add(shape);
					continue;
				}
				territory.addBanned(shape);
			}
			if(territory == null || territory.getTerritories().isEmpty())
			{
				error("Empty territory for zone: " + zoneDat.get("name"));
			}
			ZoneTemplate template = new ZoneTemplate(zoneDat);
			getHolder().addTemplate(template);
		}
	}
}