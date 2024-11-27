package zones;

import gnu.trove.TIntHashSet;
import l2.gameserver.instancemanager.ReflectionManager;
import l2.gameserver.listener.zone.OnZoneEnterLeaveListener;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.Summon;
import l2.gameserver.model.Zone;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.scripts.ScriptFile;
import l2.gameserver.utils.Location;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class ClassIdLimitZone implements ScriptFile
{
	private static final Logger LOG = LoggerFactory.getLogger(ClassIdLimitZone.class);
	private static final String PLAYER_CLASS_ID_ZONE_PARAM_NAME = "playerClassIdsLimit";
	private static final String PLAYER_CLASS_ID_LOC_ZONE_PARAM_NAME = "playerClassIdsLimitBackLoc";
	
	private static void init()
	{
		int count = 0;
		Collection<Zone> zones = ReflectionManager.DEFAULT.getZones();
		for(Zone zone : zones)
		{
			boolean isClassIdsSet = zone.getParams().isSet("playerClassIdsLimit");
			boolean isBackLocSet = zone.getParams().isSet("playerClassIdsLimitBackLoc");
			if(!isClassIdsSet && !isBackLocSet)
				continue;
			if(!isClassIdsSet)
			{
				LOG.warn("Class ids not set for zone " + zone);
				continue;
			}
			if(!isBackLocSet)
			{
				LOG.warn("Back location not set for сlassId limit zone " + zone);
				continue;
			}
			int[] playerClassIds = zone.getParams().getIntegerArray("playerClassIdsLimit", ArrayUtils.EMPTY_INT_ARRAY);
			Location backLoc = Location.parseLoc(zone.getParams().getString("playerClassIdsLimitBackLoc"));
			zone.addListener(new ClassIdLimitZoneListener(playerClassIds, backLoc));
			++count;
		}
		LOG.info("ClassIdLimitZone: Loaded " + count + " player class id limit zone(s).");
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
	
	private static class ClassIdLimitZoneListener implements OnZoneEnterLeaveListener
	{
		private final TIntHashSet _playerClassIds;
		private final Location _playerBackLoc;
		
		private ClassIdLimitZoneListener(int[] playerClassIds, Location playerBackLoc)
		{
			_playerClassIds = new TIntHashSet(playerClassIds);
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
				if(_playerClassIds.contains(player.getActiveClassId()))
				{
					player.sendMessage(new CustomMessage("scripts.zones.epic.banishClassMsg", player));
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