package l2.gameserver.data.xml.parser;

import l2.commons.collections.MultiValueSet;
import l2.commons.data.xml.AbstractDirParser;
import l2.gameserver.Config;
import l2.gameserver.data.xml.holder.EventHolder;
import l2.gameserver.model.entity.events.EventAction;
import l2.gameserver.model.entity.events.EventType;
import l2.gameserver.model.entity.events.GlobalEvent;
import l2.gameserver.model.entity.events.actions.*;
import l2.gameserver.model.entity.events.objects.BoatPoint;
import l2.gameserver.model.entity.events.objects.CTBTeamObject;
import l2.gameserver.model.entity.events.objects.CastleDamageZoneObject;
import l2.gameserver.model.entity.events.objects.DoorObject;
import l2.gameserver.model.entity.events.objects.SiegeToggleNpcObject;
import l2.gameserver.model.entity.events.objects.SpawnExObject;
import l2.gameserver.model.entity.events.objects.StaticObjectObject;
import l2.gameserver.model.entity.events.objects.ZoneObject;
import l2.gameserver.network.l2.components.ChatType;
import l2.gameserver.network.l2.components.NpcString;
import l2.gameserver.network.l2.components.SysString;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.PlaySound;
import l2.gameserver.utils.Location;
import org.dom4j.Element;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public final class EventParser extends AbstractDirParser<EventHolder>
{
	private static final EventParser _instance = new EventParser();
	
	protected EventParser()
	{
		super(EventHolder.getInstance());
	}
	
	public static EventParser getInstance()
	{
		return _instance;
	}
	
	@Override
	public File getXMLDir()
	{
		return new File(Config.DATAPACK_ROOT, "data/events/");
	}
	
	@Override
	public boolean isIgnored(File f)
	{
		return false;
	}
	
	@Override
	public String getDTDFileName()
	{
		return "events.dtd";
	}
	
	@Override
	protected void readData(Element rootElement) throws Exception
	{
		Iterator iterator = rootElement.elementIterator("event");
		while(iterator.hasNext())
		{
			Element eventElement = (Element) iterator.next();
			int id = Integer.parseInt(eventElement.attributeValue("id"));
			String name = eventElement.attributeValue("name");
			String impl = eventElement.attributeValue("impl");
			EventType type = EventType.valueOf(eventElement.attributeValue("type"));
			Class eventClass;
			try
			{
				eventClass = Class.forName("l2.gameserver.model.entity.events.impl." + impl + "Event");
			}
			catch(ClassNotFoundException e)
			{
				info("Not found impl class: " + impl + "; File: " + getCurrentFileName());
				continue;
			}
			Constructor constructor = eventClass.getConstructor(MultiValueSet.class);
			MultiValueSet<String> set = new MultiValueSet<>();
			set.set("id", id);
			set.set("name", name);
			Iterator parameterIterator = eventElement.elementIterator("parameter");
			while(parameterIterator.hasNext())
			{
				Element parameterElement = (Element) parameterIterator.next();
				set.set(parameterElement.attributeValue("name"), parameterElement.attributeValue("value"));
			}
			GlobalEvent event = (GlobalEvent) constructor.newInstance(set);
			event.addOnStartActions(parseActions(eventElement.element("on_start"), Integer.MAX_VALUE));
			event.addOnStopActions(parseActions(eventElement.element("on_stop"), Integer.MAX_VALUE));
			event.addOnInitActions(parseActions(eventElement.element("on_init"), Integer.MAX_VALUE));
			Element onTime = eventElement.element("on_time");
			if(onTime != null)
			{
				Iterator onTimeIterator = onTime.elementIterator("on");
				while(onTimeIterator.hasNext())
				{
					Element on = (Element) onTimeIterator.next();
					int time = Integer.parseInt(on.attributeValue("time"));
					List<EventAction> actions = parseActions(on, time);
					event.addOnTimeActions(time, actions);
				}
			}
			Iterator objectIterator = eventElement.elementIterator("objects");
			while(objectIterator.hasNext())
			{
				Element objectElement = (Element) objectIterator.next();
				String objectsName = objectElement.attributeValue("name");
				List<Serializable> objects = parseObjects(objectElement);
				event.addObjects(objectsName, objects);
			}
			getHolder().addEvent(type, event);
		}
	}
	
	private List<Serializable> parseObjects(Element element)
	{
		if(element == null)
		{
			return Collections.emptyList();
		}
		ArrayList<Serializable> objects = new ArrayList<>(2);
		Iterator objectsIterator = element.elementIterator();
		while(objectsIterator.hasNext())
		{
			Element objectsElement = (Element) objectsIterator.next();
			String nodeName = objectsElement.getName();
			if(nodeName.equalsIgnoreCase("boat_point"))
			{
				objects.add(BoatPoint.parse(objectsElement));
				continue;
			}
			if(nodeName.equalsIgnoreCase("point"))
			{
				objects.add(Location.parse(objectsElement));
				continue;
			}
			if(nodeName.equalsIgnoreCase("spawn_ex"))
			{
				objects.add(new SpawnExObject(objectsElement.attributeValue("name")));
				continue;
			}
			if(nodeName.equalsIgnoreCase("door"))
			{
				objects.add(new DoorObject(Integer.parseInt(objectsElement.attributeValue("id"))));
				continue;
			}
			if(nodeName.equalsIgnoreCase("static_object"))
			{
				objects.add(new StaticObjectObject(Integer.parseInt(objectsElement.attributeValue("id"))));
				continue;
			}
			if(nodeName.equalsIgnoreCase("siege_toggle_npc"))
			{
				int id = Integer.parseInt(objectsElement.attributeValue("id"));
				int fakeId = Integer.parseInt(objectsElement.attributeValue("fake_id"));
				int x = Integer.parseInt(objectsElement.attributeValue("x"));
				int y = Integer.parseInt(objectsElement.attributeValue("y"));
				int z = Integer.parseInt(objectsElement.attributeValue("z"));
				int hp = Integer.parseInt(objectsElement.attributeValue("hp"));
				Set<String> set = Collections.emptySet();
				Iterator oIterator = objectsElement.elementIterator();
				while(oIterator.hasNext())
				{
					Element sub = (Element) oIterator.next();
					if(set.isEmpty())
					{
						set = new HashSet();
					}
					set.add(sub.attributeValue("name"));
				}
				objects.add(new SiegeToggleNpcObject(id, fakeId, new Location(x, y, z), hp, set));
				continue;
			}
			if(nodeName.equalsIgnoreCase("castle_zone"))
			{
				long price = Long.parseLong(objectsElement.attributeValue("price"));
				objects.add(new CastleDamageZoneObject(objectsElement.attributeValue("name"), price));
				continue;
			}
			if(nodeName.equalsIgnoreCase("zone"))
			{
				objects.add(new ZoneObject(objectsElement.attributeValue("name")));
				continue;
			}
			if(!nodeName.equalsIgnoreCase("ctb_team"))
				continue;
			int mobId = Integer.parseInt(objectsElement.attributeValue("mob_id"));
			int flagId = Integer.parseInt(objectsElement.attributeValue("id"));
			Location loc = Location.parse(objectsElement);
			objects.add(new CTBTeamObject(mobId, flagId, loc));
		}
		return objects;
	}
	
	private List<EventAction> parseActions(Element element, int time)
	{
		if(element == null)
		{
			return Collections.emptyList();
		}
		IfElseAction lastIf = null;
		List actions = new ArrayList(0);
		Iterator iterator = element.elementIterator();
		while(iterator.hasNext())
		{
			String name;
			Element actionElement = (Element) iterator.next();
			if(actionElement.getName().equalsIgnoreCase("start"))
			{
				name = actionElement.attributeValue("name");
				StartStopAction startStopAction = new StartStopAction(name, true);
				actions.add(startStopAction);
				continue;
			}
			if(actionElement.getName().equalsIgnoreCase("stop"))
			{
				name = actionElement.attributeValue("name");
				StartStopAction startStopAction = new StartStopAction(name, false);
				actions.add(startStopAction);
				continue;
			}
			if(actionElement.getName().equalsIgnoreCase("spawn"))
			{
				name = actionElement.attributeValue("name");
				SpawnDespawnAction spawnDespawnAction = new SpawnDespawnAction(name, true);
				actions.add(spawnDespawnAction);
				continue;
			}
			if(actionElement.getName().equalsIgnoreCase("despawn"))
			{
				name = actionElement.attributeValue("name");
				SpawnDespawnAction spawnDespawnAction = new SpawnDespawnAction(name, false);
				actions.add(spawnDespawnAction);
				continue;
			}
			if(actionElement.getName().equalsIgnoreCase("open"))
			{
				name = actionElement.attributeValue("name");
				OpenCloseAction a = new OpenCloseAction(true, name);
				actions.add(a);
				continue;
			}
			if(actionElement.getName().equalsIgnoreCase("close"))
			{
				name = actionElement.attributeValue("name");
				OpenCloseAction a = new OpenCloseAction(false, name);
				actions.add(a);
				continue;
			}
			if(actionElement.getName().equalsIgnoreCase("active"))
			{
				name = actionElement.attributeValue("name");
				ActiveDeactiveAction a = new ActiveDeactiveAction(true, name);
				actions.add(a);
				continue;
			}
			if(actionElement.getName().equalsIgnoreCase("deactive"))
			{
				name = actionElement.attributeValue("name");
				ActiveDeactiveAction a = new ActiveDeactiveAction(false, name);
				actions.add(a);
				continue;
			}
			if(actionElement.getName().equalsIgnoreCase("refresh"))
			{
				name = actionElement.attributeValue("name");
				RefreshAction a = new RefreshAction(name);
				actions.add(a);
				continue;
			}
			if(actionElement.getName().equalsIgnoreCase("init"))
			{
				name = actionElement.attributeValue("name");
				InitAction a = new InitAction(name);
				actions.add(a);
				continue;
			}
			if(actionElement.getName().equalsIgnoreCase("npc_say"))
			{
				int npc = Integer.parseInt(actionElement.attributeValue("npc"));
				ChatType chat = ChatType.valueOf(actionElement.attributeValue("chat"));
				int range = Integer.parseInt(actionElement.attributeValue("range"));
				NpcString string = NpcString.valueOf(actionElement.attributeValue("text"));
				NpcSayAction action3 = new NpcSayAction(npc, range, chat, string);
				actions.add(action3);
				continue;
			}
			if(actionElement.getName().equalsIgnoreCase("play_sound"))
			{
				int range = Integer.parseInt(actionElement.attributeValue("range"));
				String sound = actionElement.attributeValue("sound");
				PlaySound.Type type = PlaySound.Type.valueOf(actionElement.attributeValue("type"));
				PlaySoundAction action2 = new PlaySoundAction(range, sound, type);
				actions.add(action2);
				continue;
			}
			if(actionElement.getName().equalsIgnoreCase("give_item"))
			{
				int itemId = Integer.parseInt(actionElement.attributeValue("id"));
				long count = Integer.parseInt(actionElement.attributeValue("count"));
				GiveItemAction action = new GiveItemAction(itemId, count);
				actions.add(action);
				continue;
			}
			if(actionElement.getName().equalsIgnoreCase("announce"))
			{
				String val = actionElement.attributeValue("val");
				if(val == null && time == Integer.MAX_VALUE)
				{
					info("Can't get announce time." + getCurrentFileName());
					continue;
				}
				int val2 = val == null ? time : Integer.parseInt(val);
				AnnounceAction action4 = new AnnounceAction(val2);
				actions.add(action4);
				continue;
			}
			if(actionElement.getName().equalsIgnoreCase("if"))
			{
				name = actionElement.attributeValue("name");
				IfElseAction action = new IfElseAction(name, false);
				action.setIfList(parseActions(actionElement, time));
				actions.add(action);
				lastIf = action;
				continue;
			}
			if(actionElement.getName().equalsIgnoreCase("ifnot"))
			{
				name = actionElement.attributeValue("name");
				IfElseAction action = new IfElseAction(name, true);
				action.setIfList(parseActions(actionElement, time));
				actions.add(action);
				lastIf = action;
				continue;
			}
			if(actionElement.getName().equalsIgnoreCase("else"))
			{
				if(lastIf == null)
				{
					info("Not find <if> for <else> tag");
					continue;
				}
				lastIf.setElseList(parseActions(actionElement, time));
				continue;
			}
			if(actionElement.getName().equalsIgnoreCase("say"))
			{
				ChatType chat = ChatType.valueOf(actionElement.attributeValue("chat"));
				int range = Integer.parseInt(actionElement.attributeValue("range"));
				String how = actionElement.attributeValue("how");
				String text = actionElement.attributeValue("text");
				SysString sysString = SysString.valueOf2(how);
				SayAction sayAction = sysString != null ? new SayAction(range, chat, sysString, SystemMsg.valueOf(text)) : new SayAction(range, chat, how, NpcString.valueOf(text));
				actions.add(sayAction);
				continue;
			}
			if(!actionElement.getName().equalsIgnoreCase("teleport_players"))
				continue;
			name = actionElement.attributeValue("id");
			TeleportPlayersAction a = new TeleportPlayersAction(name);
			actions.add(a);
		}
		return actions.isEmpty() ? Collections.emptyList() : actions;
	}
}