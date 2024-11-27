package l2.gameserver.instancemanager;

import l2.gameserver.Config;
import l2.gameserver.GameTimeController;
import l2.gameserver.data.xml.holder.NpcHolder;
import l2.gameserver.data.xml.holder.SpawnHolder;
import l2.gameserver.listener.game.OnDayNightChangeListener;
import l2.gameserver.listener.game.OnSSPeriodListener;
import l2.gameserver.model.HardSpawner;
import l2.gameserver.model.Spawner;
import l2.gameserver.model.entity.SevenSigns;
import l2.gameserver.model.instances.MonsterInstance;
import l2.gameserver.templates.npc.NpcTemplate;
import l2.gameserver.templates.spawn.PeriodOfDay;
import l2.gameserver.templates.spawn.SpawnTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SpawnManager
{
	private static final Logger _log = LoggerFactory.getLogger(SpawnManager.class);
	private static final String SPAWN_EVENT_NAME_SSQ_EVENT = "[ssq_event]";
	private static final String SPAWN_EVENT_NAME_AVARICE_NONE = "[ssq_seal1_none]";
	private static final String SPAWN_EVENT_NAME_AVARICE_DUSK = "[ssq_seal1_twilight]";
	private static final String SPAWN_EVENT_NAME_AVARICE_DAWN = "[ssq_seal1_dawn]";
	private static final String SPAWN_EVENT_NAME_GNOSIS_NONE = "[ssq_seal2_none]";
	private static final String SPAWN_EVENT_NAME_GNOSIS_DUSK = "[ssq_seal2_twilight]";
	private static final String SPAWN_EVENT_NAME_GNOSIS_DAWN = "[ssq_seal2_dawn]";
	private static final String DAWN_GROUP = "dawn_spawn";
	private static final String DUSK_GROUP = "dusk_spawn";
	private static final SpawnManager _instance = new SpawnManager();
	private final Map<String, List<Spawner>> _spawns = new ConcurrentHashMap<>();
	private final Listeners _listeners;
	
	private SpawnManager()
	{
		_listeners = new Listeners();
		for(Map.Entry<String, List<SpawnTemplate>> entry : SpawnHolder.getInstance().getSpawns().entrySet())
		{
			fillSpawn(entry.getKey(), entry.getValue());
		}
		GameTimeController.getInstance().addListener(_listeners);
		SevenSigns.getInstance().addListener(_listeners);
	}
	
	public static SpawnManager getInstance()
	{
		return _instance;
	}
	
	public List<Spawner> fillSpawn(String group, List<SpawnTemplate> templateList)
	{
		if(Config.DONTLOADSPAWN)
		{
			return Collections.emptyList();
		}
		List<Spawner> spawnerList = _spawns.get(group);
		if(spawnerList == null)
		{
			spawnerList = new ArrayList<>(templateList.size());
			_spawns.put(group, spawnerList);
		}
		for(SpawnTemplate template : templateList)
		{
			try
			{
				HardSpawner spawner = new HardSpawner(template);
				spawnerList.add(spawner);
				NpcTemplate npcTemplate = NpcHolder.getInstance().getTemplate(spawner.getCurrentNpcId());
				if(Config.RATE_MOB_SPAWN > 1 && npcTemplate.getInstanceClass() == MonsterInstance.class && npcTemplate.level >= Config.RATE_MOB_SPAWN_MIN_LEVEL && npcTemplate.level <= Config.RATE_MOB_SPAWN_MAX_LEVEL)
				{
					spawner.setAmount(template.getCount() * Config.RATE_MOB_SPAWN);
				}
				else
				{
					spawner.setAmount(template.getCount());
				}
				spawner.setRespawnDelay(template.getRespawn(), template.getRespawnRandom());
				spawner.setRespawnCron(template.getRespawnCron());
				spawner.setReflection(ReflectionManager.DEFAULT);
				spawner.setRespawnTime(0);
				if(!npcTemplate.isRaid || !group.equals(PeriodOfDay.ALL.name()))
					continue;
				RaidBossSpawnManager.getInstance().addNewSpawn(npcTemplate.getNpcId(), spawner);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		return spawnerList;
	}
	
	public void spawnAll()
	{
		spawn(PeriodOfDay.ALL.name());
		if(Config.ALLOW_EVENT_GATEKEEPER)
		{
			spawn("[event_gate]");
		}
		if(Config.ALLOW_GLOBAL_GK)
		{
			spawn("[global_gatekeeper]");
		}
		if(Config.ALLOW_BUFFER)
		{
			spawn("[npc_buffer]");
		}
		if(Config.ALLOW_GMSHOP)
		{
			spawn("[gm_shop]");
		}
		if(Config.ALLOW_AUCTIONER)
		{
			spawn("[auctioner]");
		}
		if(Config.ALLOW_GLOBAL_SERVICES)
		{
			spawn("[global_services]");
		}
		if(Config.ALLOW_PVP_EVENT_MANAGER)
		{
			spawn("[pvp_event_manager]");
		}
		if(Config.ALLOW_TREASURE_BOX)
		{
			spawn("[treasure_box]");
		}
		if(Config.SERVICES_ALLOW_LOTTERY)
		{
			spawn("[lotto_manager]");
		}
		if(!Config.ALLOW_CLASS_MASTERS_LIST.isEmpty())
		{
			spawn("class_master");
		}
	}
	
	public void spawn(String group)
	{
		List<Spawner> spawnerList = getSpawners(group);
		if(spawnerList == null)
		{
			return;
		}
		int npcSpawnCount = 0;
		for(Spawner spawner : spawnerList)
		{
			if((npcSpawnCount += spawner.init()) % 1000 != 0 || npcSpawnCount == 0)
				continue;
			_log.info("SpawnManager: spawned " + npcSpawnCount + " npc for group: " + group);
		}
		_log.info("SpawnManager: spawned " + npcSpawnCount + " npc; spawns: " + spawnerList.size() + "; group: " + group);
	}
	
	public void despawn(String group)
	{
		List<Spawner> spawnerList = _spawns.get(group);
		if(spawnerList == null)
		{
			return;
		}
		for(Spawner spawner : spawnerList)
		{
			spawner.deleteAll();
		}
	}
	
	public List<Spawner> getSpawners(String group)
	{
		List list = _spawns.get(group);
		return list == null ? Collections.emptyList() : list;
	}
	
	public void reloadAll()
	{
		RaidBossSpawnManager.getInstance().cleanUp();
		for(List<Spawner> spawnerList : _spawns.values())
		{
			for(Spawner spawner : spawnerList)
			{
				spawner.deleteAll();
			}
		}
		RaidBossSpawnManager.getInstance().reloadBosses();
		spawnAll();
		if(SevenSigns.getInstance().getCurrentPeriod() == 3)
		{
			SevenSigns.getInstance().getCabalHighestScore();
		}
		_listeners.onPeriodChange(SevenSigns.getInstance().getCurrentPeriod());
		if(GameTimeController.getInstance().isNowNight())
		{
			_listeners.onNight();
		}
		else
		{
			_listeners.onDay();
		}
	}
	
	private class Listeners implements OnDayNightChangeListener, OnSSPeriodListener
	{
		@Override
		public void onDay()
		{
			despawn(PeriodOfDay.NIGHT.name());
			spawn(PeriodOfDay.DAY.name());
		}
		
		@Override
		public void onNight()
		{
			despawn(PeriodOfDay.DAY.name());
			spawn(PeriodOfDay.NIGHT.name());
		}
		
		@Override
		public void onPeriodChange(int mode)
		{
			despawn("[ssq_event]");
			despawn("[ssq_seal1_none]");
			despawn("[ssq_seal1_twilight]");
			despawn("[ssq_seal1_dawn]");
			despawn("[ssq_seal2_none]");
			despawn("[ssq_seal2_twilight]");
			despawn("[ssq_seal2_dawn]");
			switch(SevenSigns.getInstance().getCurrentPeriod())
			{
				case 0:
				case 2:
				{
					break;
				}
				case 1:
				{
					spawn("[ssq_event]");
					break;
				}
				case 3:
				{
					switch(SevenSigns.getInstance().getSealOwner(1))
					{
						case 0:
						{
							spawn("[ssq_seal1_none]");
							break;
						}
						case 1:
						{
							spawn("[ssq_seal1_twilight]");
							break;
						}
						case 2:
						{
							spawn("[ssq_seal1_dawn]");
						}
					}
					switch(SevenSigns.getInstance().getSealOwner(2))
					{
						case 0:
						{
							spawn("[ssq_seal2_none]");
							break;
						}
						case 1:
						{
							spawn("[ssq_seal2_twilight]");
							break;
						}
						case 2:
						{
							spawn("[ssq_seal2_dawn]");
						}
					}
				}
			}
		}
	}
}