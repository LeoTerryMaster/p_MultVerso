package l2.gameserver.data.xml.parser;

import l2.commons.data.xml.AbstractDirParser;
import l2.commons.geometry.Polygon;
import l2.commons.time.cron.SchedulingPattern;
import l2.gameserver.Config;
import l2.gameserver.data.xml.holder.DoorHolder;
import l2.gameserver.data.xml.holder.InstantZoneHolder;
import l2.gameserver.data.xml.holder.SpawnHolder;
import l2.gameserver.data.xml.holder.ZoneHolder;
import l2.gameserver.model.Territory;
import l2.gameserver.templates.DoorTemplate;
import l2.gameserver.templates.InstantZone;
import l2.gameserver.templates.StatsSet;
import l2.gameserver.templates.ZoneTemplate;
import l2.gameserver.templates.spawn.SpawnTemplate;
import l2.gameserver.utils.Location;
import org.dom4j.Element;
import org.napile.primitive.Containers;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class InstantZoneParser extends AbstractDirParser<InstantZoneHolder>
{
	private static final InstantZoneParser _instance = new InstantZoneParser();
	
	public InstantZoneParser()
	{
		super(InstantZoneHolder.getInstance());
	}
	
	public static InstantZoneParser getInstance()
	{
		return _instance;
	}
	
	@Override
	public File getXMLDir()
	{
		return new File(Config.DATAPACK_ROOT, "data/instances/");
	}
	
	@Override
	public boolean isIgnored(File f)
	{
		return false;
	}
	
	@Override
	public String getDTDFileName()
	{
		return "instances.dtd";
	}
	
	@Override
	protected void readData(Element rootElement) throws Exception
	{
		Iterator iterator = rootElement.elementIterator();
		while(iterator.hasNext())
		{
			Element element = (Element) iterator.next();
			SchedulingPattern resetReuse = new SchedulingPattern("30 6 * * *");
			int timelimit = -1;
			IntObjectMap doors = Containers.emptyIntObjectMap();
			int instanceId = Integer.parseInt(element.attributeValue("id"));
			String name = element.attributeValue("name");
			String n = element.attributeValue("timelimit");
			if(n != null)
			{
				timelimit = Integer.parseInt(n);
			}
			n = element.attributeValue("collapseIfEmpty");
			int collapseIfEmpty = Integer.parseInt(n);
			n = element.attributeValue("maxChannels");
			int maxChannels = Integer.parseInt(n);
			n = element.attributeValue("dispelBuffs");
			boolean dispelBuffs = n != null && Boolean.parseBoolean(n);
			int minLevel = 0;
			int maxLevel = 0;
			int minParty = 1;
			int maxParty = 9;
			List<Location> teleportLocs = Collections.emptyList();
			Location ret = null;
			Iterator subIterator = element.elementIterator();
			int requiredQuestId = 0;
			int givedItemCount = 0;
			int giveItemId = 0;
			int removedItemCount = 0;
			int removedItemId = 0;
			Map<String, InstantZone.SpawnInfo2> spawns2 = Collections.emptyMap();
			Map<String, InstantZone.ZoneInfo> zones = Collections.emptyMap();
			ArrayList<InstantZone.SpawnInfo> spawns = new ArrayList<>();
			StatsSet params = new StatsSet();
			boolean setReuseUponEntry = true;
			boolean removedItemNecessity = false;
			int spawnType = 0;
			int sharedReuseGroup = 0;
			boolean onPartyDismiss = true;
			int mapy = -1;
			int mapx = -1;
			int timer = 60;
			while(subIterator.hasNext())
			{
				Element subElement = (Element) subIterator.next();
				if("level".equalsIgnoreCase(subElement.getName()))
				{
					minLevel = Integer.parseInt(subElement.attributeValue("min"));
					maxLevel = Integer.parseInt(subElement.attributeValue("max"));
					continue;
				}
				if("collapse".equalsIgnoreCase(subElement.getName()))
				{
					onPartyDismiss = Boolean.parseBoolean(subElement.attributeValue("on-party-dismiss"));
					timer = Integer.parseInt(subElement.attributeValue("timer"));
					continue;
				}
				if("party".equalsIgnoreCase(subElement.getName()))
				{
					minParty = Integer.parseInt(subElement.attributeValue("min"));
					maxParty = Integer.parseInt(subElement.attributeValue("max"));
					continue;
				}
				if("return".equalsIgnoreCase(subElement.getName()))
				{
					ret = Location.parseLoc(subElement.attributeValue("loc"));
					continue;
				}
				if("teleport".equalsIgnoreCase(subElement.getName()))
				{
					if(teleportLocs.isEmpty())
					{
						teleportLocs = new ArrayList<>(1);
					}
					teleportLocs.add(Location.parseLoc(subElement.attributeValue("loc")));
					continue;
				}
				if("remove".equalsIgnoreCase(subElement.getName()))
				{
					removedItemId = Integer.parseInt(subElement.attributeValue("itemId"));
					removedItemCount = Integer.parseInt(subElement.attributeValue("count"));
					removedItemNecessity = Boolean.parseBoolean(subElement.attributeValue("necessary"));
					continue;
				}
				if("give".equalsIgnoreCase(subElement.getName()))
				{
					giveItemId = Integer.parseInt(subElement.attributeValue("itemId"));
					givedItemCount = Integer.parseInt(subElement.attributeValue("count"));
					continue;
				}
				if("quest".equalsIgnoreCase(subElement.getName()))
				{
					requiredQuestId = Integer.parseInt(subElement.attributeValue("id"));
					continue;
				}
				if("reuse".equalsIgnoreCase(subElement.getName()))
				{
					resetReuse = new SchedulingPattern(subElement.attributeValue("resetReuse"));
					sharedReuseGroup = Integer.parseInt(subElement.attributeValue("sharedReuseGroup"));
					setReuseUponEntry = Boolean.parseBoolean(subElement.attributeValue("setUponEntry"));
					continue;
				}
				if("geodata".equalsIgnoreCase(subElement.getName()))
				{
					String[] rxy = subElement.attributeValue("map").split("_");
					mapx = Integer.parseInt(rxy[0]);
					mapy = Integer.parseInt(rxy[1]);
					continue;
				}
				if("doors".equalsIgnoreCase(subElement.getName()))
				{
					for(Element e : subElement.elements())
					{
						if(doors.isEmpty())
						{
							doors = new HashIntObjectMap();
						}
						boolean opened = e.attributeValue("opened") != null && Boolean.parseBoolean(e.attributeValue("opened"));
						boolean invul = e.attributeValue("invul") == null || Boolean.parseBoolean(e.attributeValue("invul"));
						DoorTemplate template = DoorHolder.getInstance().getTemplate(Integer.parseInt(e.attributeValue("id")));
						doors.put(template.getNpcId(), new InstantZone.DoorInfo(template, opened, invul));
					}
					continue;
				}
				if("zones".equalsIgnoreCase(subElement.getName()))
				{
					for(Element e : subElement.elements())
					{
						if(zones.isEmpty())
						{
							zones = new HashMap<>();
						}
						boolean active = e.attributeValue("active") != null && Boolean.parseBoolean(e.attributeValue("active"));
						ZoneTemplate template = ZoneHolder.getInstance().getTemplate(e.attributeValue("name"));
						if(template == null)
						{
							error("Zone: " + e.attributeValue("name") + " not found; file: " + getCurrentFileName());
							continue;
						}
						zones.put(template.getName(), new InstantZone.ZoneInfo(template, active));
					}
					continue;
				}
				if("add_parameters".equalsIgnoreCase(subElement.getName()))
				{
					for(Element e : subElement.elements())
					{
						if(!"param".equalsIgnoreCase(e.getName()))
							continue;
						params.set(e.attributeValue("name"), e.attributeValue("value"));
					}
					continue;
				}
				if(!"spawns".equalsIgnoreCase(subElement.getName()))
					continue;
				for(Element e : subElement.elements())
				{
					if("group".equalsIgnoreCase(e.getName()))
					{
						String group = e.attributeValue("name");
						boolean spawned = e.attributeValue("spawned") != null && Boolean.parseBoolean(e.attributeValue("spawned"));
						List<SpawnTemplate> templates = SpawnHolder.getInstance().getSpawn(group);
						if(templates == null)
						{
							info("not find spawn group: " + group + " in file: " + getCurrentFileName());
							continue;
						}
						if(spawns2.isEmpty())
						{
							spawns2 = new Hashtable<>();
						}
						spawns2.put(group, new InstantZone.SpawnInfo2(templates, spawned));
						continue;
					}
					if(!"spawn".equalsIgnoreCase(e.getName()))
						continue;
					String[] mobs = e.attributeValue("mobId").split(" ");
					String respawnNode = e.attributeValue("respawn");
					int respawn = respawnNode != null ? Integer.parseInt(respawnNode) : 0;
					String respawnRndNode = e.attributeValue("respawnRnd");
					int respawnRnd = respawnRndNode != null ? Integer.parseInt(respawnRndNode) : 0;
					String countNode = e.attributeValue("count");
					int count = countNode != null ? Integer.parseInt(countNode) : 1;
					spawnType = 0;
					String spawnTypeNode = e.attributeValue("type");
					if(spawnTypeNode == null || spawnTypeNode.equalsIgnoreCase("point"))
					{
						spawnType = 0;
					}
					else if(spawnTypeNode.equalsIgnoreCase("rnd"))
					{
						spawnType = 1;
					}
					else if(spawnTypeNode.equalsIgnoreCase("loc"))
					{
						spawnType = 2;
					}
					else
					{
						error("Spawn type  '" + spawnTypeNode + "' is unknown!");
					}
					ArrayList<Location> coords = new ArrayList<>();
					for(Element e2 : e.elements())
					{
						if(!"coords".equalsIgnoreCase(e2.getName()))
							continue;
						coords.add(Location.parseLoc(e2.attributeValue("loc")));
					}
					Territory territory = null;
					if(spawnType == 2)
					{
						Polygon poly = new Polygon();
						Iterator<Location> iterator2 = coords.iterator();
						while(iterator2.hasNext())
						{
							Location loc = iterator2.next();
							poly.add(loc.x, loc.y).setZmin(loc.z).setZmax(loc.z);
						}
						if(!poly.validate())
						{
							error("invalid spawn territory for instance id : " + instanceId + " - " + poly + "!");
						}
						territory = new Territory().add(poly);
					}
					for(String mob : mobs)
					{
						int mobId = Integer.parseInt(mob);
						InstantZone.SpawnInfo spawnDat = new InstantZone.SpawnInfo(spawnType, mobId, count, respawn, respawnRnd, coords, territory);
						spawns.add(spawnDat);
					}
				}
			}
			InstantZone instancedZone = new InstantZone(instanceId, name, resetReuse, sharedReuseGroup, timelimit, dispelBuffs, minLevel, maxLevel, minParty, maxParty, timer, onPartyDismiss, teleportLocs, ret, mapx, mapy, doors, zones, spawns2, spawns, collapseIfEmpty, maxChannels, removedItemId, removedItemCount, removedItemNecessity, giveItemId, givedItemCount, requiredQuestId, setReuseUponEntry, params);
			getHolder().addInstantZone(instancedZone);
		}
	}
}