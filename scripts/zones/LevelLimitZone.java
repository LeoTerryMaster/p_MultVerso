package zones;

import l2.gameserver.instancemanager.ReflectionManager;
import l2.gameserver.listener.zone.OnZoneEnterLeaveListener;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.Summon;
import l2.gameserver.model.Zone;
import l2.gameserver.model.base.Experience;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.scripts.ScriptFile;
import l2.gameserver.utils.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class LevelLimitZone implements ScriptFile
{
	private static final Logger LOG = LoggerFactory.getLogger(LevelLimitZone.class);
	private static final String MIN_PLAYER_LEVEL_ZONE_PARAM_NAME = "playerMinLevel";
	private static final String MAX_PLAYER_LEVEL_ZONE_PARAM_NAME = "playerMaxLevel";
	private static final String XYZ_PLAYER_LEVEL_ZONE_PARAM_NAME = "playerLevelLimitBackLoc";
	
	private static void init()
	{
		int count = 0;
		Collection<Zone> zones = ReflectionManager.DEFAULT.getZones();
		for(Zone zone : zones)
		{
			boolean isMinSet = zone.getParams().isSet("playerMinLevel");
			boolean isMaxSet = zone.getParams().isSet("playerMaxLevel");
			boolean isCoordSet = zone.getParams().isSet("playerLevelLimitBackLoc");
			if(isMinSet != isMaxSet)
			{
				LOG.warn("Min or max level not set for zone " + zone);
			}
			if(!isMinSet || !isMaxSet)
				continue;
			if(!isCoordSet)
			{
				LOG.warn("Back coord not set for player level limited zone " + zone);
				continue;
			}
			int minPlayerLevel = zone.getParams().getInteger("playerMinLevel", 1);
			int maxPlayerLevel = zone.getParams().getInteger("playerMaxLevel", Experience.getMaxLevel());
			Location backLoc = Location.parseLoc(zone.getParams().getString("playerLevelLimitBackLoc"));
			zone.addListener(new LevelLimitZoneListener(minPlayerLevel, maxPlayerLevel, backLoc));
			++count;
		}
		LOG.info("LevelLimitZone: Loaded " + count + " player level limit zone(s).");
	}
	
	@Override
	public void onLoad()
	{
		init();
	}
	
	@Override
	public void onReload()
	{
	}
	
	@Override
	public void onShutdown()
	{
	}
	
	private static class LevelLimitZoneListener implements OnZoneEnterLeaveListener
	{
		private final int _minPlayerLevel;
		private final int _maxPlayerLevel;
		private final Location _playerBackLoc;
		
		private LevelLimitZoneListener(int minPlayerLevel, int maxPlayerLevel, Location playerBackLoc)
		{
			_minPlayerLevel = minPlayerLevel;
			_maxPlayerLevel = maxPlayerLevel;
			_playerBackLoc = playerBackLoc;
		}
		
		@Override
		public void onZoneEnter(Zone zone, Creature actor)
		{
			if(actor != null && actor.isPlayer())
			{
				Player player = actor.getPlayer();
				if(player.isGM())
				{
					return;
				}
				if(player.getLevel() < _minPlayerLevel || player.getLevel() > _maxPlayerLevel)
				{
					player.sendMessage(new CustomMessage("scripts.zones.epic.banishMsg", player));
					player.teleToLocation(_playerBackLoc);
					Summon summon = player.getPet();
					if(summon != null)
					{
						summon.teleToLocation(_playerBackLoc);
					}
				}
			}
		}
		
		@Override
		public void onZoneLeave(Zone zone, Creature actor)
		{
		}
	}
}