package l2.gameserver.model.entity.events;

import l2.commons.collections.MultiValueSet;
import l2.commons.listener.Listener;
import l2.commons.listener.ListenerList;
import l2.commons.logging.LoggerObject;
import l2.gameserver.dao.ItemsDAO;
import l2.gameserver.instancemanager.ReflectionManager;
import l2.gameserver.listener.event.OnStartStopListener;
import l2.gameserver.model.Creature;
import l2.gameserver.model.GameObject;
import l2.gameserver.model.GameObjectsStorage;
import l2.gameserver.model.Playable;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.base.RestartType;
import l2.gameserver.model.entity.Reflection;
import l2.gameserver.model.entity.events.objects.DoorObject;
import l2.gameserver.model.entity.events.objects.InitableObject;
import l2.gameserver.model.entity.events.objects.SpawnableObject;
import l2.gameserver.model.entity.events.objects.ZoneObject;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.network.l2.components.IStaticPacket;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.L2GameServerPacket;
import l2.gameserver.network.l2.s2c.SystemMessage2;
import l2.gameserver.scripts.Functions;
import l2.gameserver.taskmanager.actionrunner.ActionRunner;
import l2.gameserver.utils.Location;
import l2.gameserver.utils.TimeUtils;
import org.napile.primitive.Containers;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.CHashIntObjectMap;
import org.napile.primitive.maps.impl.TreeIntObjectMap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class GlobalEvent extends LoggerObject
{
	public static final String EVENT = "event";
	protected final IntObjectMap<List<EventAction>> _onTimeActions = new TreeIntObjectMap();
	protected final List<EventAction> _onStartActions = new ArrayList<>(0);
	protected final List<EventAction> _onStopActions = new ArrayList<>(0);
	protected final List<EventAction> _onInitActions = new ArrayList<>(0);
	protected final Map<String, List<Serializable>> _objects = new HashMap<>(0);
	protected final int _id;
	protected final String _name;
	protected final String _timerName;
	protected final ListenerListImpl _listenerList;
	protected IntObjectMap<ItemInstance> _banishedItems;
	
	protected GlobalEvent(MultiValueSet<String> set)
	{
		this(set.getInteger("id"), set.getString("name"));
	}
	
	protected GlobalEvent(int id, String name)
	{
		_listenerList = new ListenerListImpl();
		_banishedItems = Containers.emptyIntObjectMap();
		_id = id;
		_name = name;
		_timerName = "" + id + "_" + name.toLowerCase().replace(" ", "_");
	}
	
	public void initEvent()
	{
		callActions(_onInitActions);
		reCalcNextTime(true);
		printInfo();
	}
	
	public void startEvent()
	{
		callActions(_onStartActions);
		_listenerList.onStart();
	}
	
	public void stopEvent()
	{
		callActions(_onStopActions);
		_listenerList.onStop();
	}
	
	protected void printInfo()
	{
		info(getName() + " time - " + TimeUtils.toSimpleFormat(startTimeMillis()));
	}
	
	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "[" + getId() + ";" + getName() + "]";
	}
	
	protected void callActions(List<EventAction> actions)
	{
		for(EventAction action : actions)
		{
			action.call(this);
		}
	}
	
	public void addOnStartActions(List<EventAction> start)
	{
		_onStartActions.addAll(start);
	}
	
	public void addOnStopActions(List<EventAction> start)
	{
		_onStopActions.addAll(start);
	}
	
	public void addOnInitActions(List<EventAction> start)
	{
		_onInitActions.addAll(start);
	}
	
	public void addOnTimeAction(int time, EventAction action)
	{
		List list = _onTimeActions.get(time);
		if(list != null)
		{
			list.add(action);
		}
		else
		{
			ArrayList<EventAction> actions = new ArrayList<>(1);
			actions.add(action);
			_onTimeActions.put(time, actions);
		}
	}
	
	public void addOnTimeActions(int time, List<EventAction> actions)
	{
		if(actions.isEmpty())
		{
			return;
		}
		List list = _onTimeActions.get(time);
		if(list != null)
		{
			list.addAll(actions);
		}
		else
		{
			_onTimeActions.put(time, new ArrayList<>(actions));
		}
	}
	
	public void timeActions(int time)
	{
		List actions = _onTimeActions.get(time);
		if(actions == null)
		{
			info("Undefined time : " + time + " for " + this);
			return;
		}
		callActions(actions);
	}
	
	public int[] timeActions()
	{
		return _onTimeActions.keySet().toArray();
	}
	
	public void registerActions()
	{
		long t = startTimeMillis();
		if(t == 0)
		{
			return;
		}
		for(int key : _onTimeActions.keySet().toArray())
		{
			ActionRunner.getInstance().register(t + (long) key * 1000, new EventWrapper(_timerName, this, key));
		}
	}
	
	public void clearActions()
	{
		ActionRunner.getInstance().clear(_timerName);
	}
	
	public <O extends Serializable> List<O> getObjects(String name)
	{
		List objects = _objects.get(name);
		return objects == null ? Collections.emptyList() : objects;
	}
	
	public <O extends Serializable> O getFirstObject(String name)
	{
		List<O> objects = getObjects(name);
		return objects.size() > 0 ? objects.get(0) : null;
	}
	
	public void addObject(String name, Serializable object)
	{
		if(object == null)
		{
			return;
		}
		List<Serializable> list = _objects.get(name);
		if(list != null)
		{
			list.add(object);
		}
		else
		{
			list = new CopyOnWriteArrayList<>();
			list.add(object);
			_objects.put(name, list);
		}
	}
	
	public void removeObject(String name, Serializable o)
	{
		if(o == null)
		{
			return;
		}
		List<Serializable> list = _objects.get(name);
		if(list != null)
		{
			list.remove(o);
		}
	}
	
	public <O extends Serializable> List<O> removeObjects(String name)
	{
		List objects = _objects.remove(name);
		return objects == null ? Collections.emptyList() : objects;
	}
	
	public void addObjects(String name, List<? extends Serializable> objects)
	{
		if(objects.isEmpty())
		{
			return;
		}
		List<Serializable> list = _objects.get(name);
		if(list != null)
		{
			list.addAll(objects);
		}
		else
		{
			_objects.put(name, (List<Serializable>) objects);
		}
	}
	
	public Map<String, List<Serializable>> getObjects()
	{
		return _objects;
	}
	
	public void spawnAction(String name, boolean spawn)
	{
		List<Serializable> objects = getObjects(name);
		if(objects.isEmpty())
		{
			info("Undefined objects: " + name);
			return;
		}
		for(Serializable object : objects)
		{
			if(!(object instanceof SpawnableObject))
				continue;
			if(spawn)
			{
				((SpawnableObject) object).spawnObject(this);
				continue;
			}
			((SpawnableObject) object).despawnObject(this);
		}
	}
	
	public void doorAction(String name, boolean open)
	{
		List<Serializable> objects = getObjects(name);
		if(objects.isEmpty())
		{
			info("Undefined objects: " + name);
			return;
		}
		for(Serializable object : objects)
		{
			if(!(object instanceof DoorObject))
				continue;
			if(open)
			{
				((DoorObject) object).open(this);
				continue;
			}
			((DoorObject) object).close(this);
		}
	}
	
	public void zoneAction(String name, boolean active)
	{
		List<Serializable> objects = getObjects(name);
		if(objects.isEmpty())
		{
			info("Undefined objects: " + name);
			return;
		}
		for(Serializable object : objects)
		{
			if(object instanceof ZoneObject)
			{
				((ZoneObject) object).setActive(active, this);
			}
			if(!(object instanceof String))
				continue;
			zoneAction((String) object, active);
		}
	}
	
	public void initAction(String name)
	{
		List<Serializable> objects = getObjects(name);
		if(objects.isEmpty())
		{
			info("Undefined objects: " + name);
			return;
		}
		for(Serializable object : objects)
		{
			if(!(object instanceof InitableObject))
				continue;
			((InitableObject) object).initObject(this);
		}
	}
	
	public void action(String name, boolean start)
	{
		if(name.equalsIgnoreCase("event"))
		{
			if(start)
			{
				startEvent();
			}
			else
			{
				stopEvent();
			}
		}
	}
	
	public void refreshAction(String name)
	{
		List<Serializable> objects = getObjects(name);
		if(objects.isEmpty())
		{
			info("Undefined objects: " + name);
			return;
		}
		for(Serializable object : objects)
		{
			if(!(object instanceof SpawnableObject))
				continue;
			((SpawnableObject) object).refreshObject(this);
		}
	}
	
	public abstract void reCalcNextTime(boolean onInit);
	
	protected abstract long startTimeMillis();
	
	public void broadcastToWorld(IStaticPacket packet)
	{
		for(Player player : GameObjectsStorage.getAllPlayersForIterate())
		{
			if(player == null)
				continue;
			player.sendPacket(packet);
		}
	}
	
	public void broadcastToWorld(L2GameServerPacket packet)
	{
		for(Player player : GameObjectsStorage.getAllPlayersForIterate())
		{
			if(player == null)
				continue;
			player.sendPacket(packet);
		}
	}
	
	public int getId()
	{
		return _id;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public GameObject getCenterObject()
	{
		return null;
	}
	
	public Reflection getReflection()
	{
		return ReflectionManager.DEFAULT;
	}
	
	public int getRelation(Player thisPlayer, Player target, int oldRelation)
	{
		return oldRelation;
	}
	
	public int getUserRelation(Player thisPlayer, int oldRelation)
	{
		return oldRelation;
	}
	
	public void checkRestartLocs(Player player, Map<RestartType, Boolean> r)
	{
	}
	
	public Location getRestartLoc(Player player, RestartType type)
	{
		return null;
	}
	
	public boolean canAttack(Creature target, Creature attacker, Skill skill, boolean force)
	{
		return false;
	}
	
	public SystemMsg checkForAttack(Creature target, Creature attacker, Skill skill, boolean force)
	{
		return null;
	}
	
	public boolean isInProgress()
	{
		return false;
	}
	
	public boolean isParticle(Player player)
	{
		return false;
	}
	
	public void announce(int a)
	{
		throw new UnsupportedOperationException();
	}
	
	public void teleportPlayers(String teleportWho)
	{
		throw new UnsupportedOperationException();
	}
	
	public boolean ifVar(String name)
	{
		throw new UnsupportedOperationException();
	}
	
	public List<Player> itemObtainPlayers()
	{
		throw new UnsupportedOperationException();
	}
	
	public void giveItem(Player player, int itemId, long count)
	{
		Functions.addItem(player, itemId, count);
	}
	
	public List<Player> broadcastPlayers(int range)
	{
		throw new UnsupportedOperationException();
	}
	
	public boolean canResurrect(Player resurrectPlayer, Creature creature, boolean force)
	{
		return true;
	}
	
	public void onAddEvent(GameObject o)
	{
	}
	
	public void onRemoveEvent(GameObject o)
	{
	}
	
	public void addBanishItem(ItemInstance item)
	{
		if(_banishedItems.isEmpty())
		{
			_banishedItems = new CHashIntObjectMap();
		}
		_banishedItems.put(item.getObjectId(), item);
	}
	
	public void removeBanishItems()
	{
		Iterator<IntObjectMap.Entry<ItemInstance>> iterator = _banishedItems.entrySet().iterator();
		while(iterator.hasNext())
		{
			IntObjectMap.Entry entry = iterator.next();
			iterator.remove();
			ItemInstance item = ItemsDAO.getInstance().load(entry.getKey());
			if(item != null)
			{
				GameObject object;
				if(item.getOwnerId() > 0 && (object = GameObjectsStorage.findObject(item.getOwnerId())) != null && object.isPlayable())
				{
					((Playable) object).getInventory().destroyItem(item);
					object.getPlayer().sendPacket(SystemMessage2.removeItems(item));
				}
				item.delete();
			}
			else
			{
				item = (ItemInstance) entry.getValue();
			}
			item.deleteMe();
		}
	}
	
	public void addListener(Listener<GlobalEvent> l)
	{
		_listenerList.add(l);
	}
	
	public void removeListener(Listener<GlobalEvent> l)
	{
		_listenerList.remove(l);
	}
	
	public void cloneTo(GlobalEvent e)
	{
		for(EventAction a : _onInitActions)
		{
			e._onInitActions.add(a);
		}
		for(EventAction a : _onStartActions)
		{
			e._onStartActions.add(a);
		}
		for(EventAction a : _onStopActions)
		{
			e._onStopActions.add(a);
		}
		for(IntObjectMap.Entry entry : _onTimeActions.entrySet())
		{
			e.addOnTimeActions(entry.getKey(), (List) entry.getValue());
		}
	}
	
	private class ListenerListImpl extends ListenerList<GlobalEvent>
	{
		public void onStart()
		{
			for(Listener listener : getListeners())
			{
				if(!OnStartStopListener.class.isInstance(listener))
					continue;
				((OnStartStopListener) listener).onStart(GlobalEvent.this);
			}
		}
		
		public void onStop()
		{
			for(Listener listener : getListeners())
			{
				if(!OnStartStopListener.class.isInstance(listener))
					continue;
				((OnStartStopListener) listener).onStop(GlobalEvent.this);
			}
		}
	}
}