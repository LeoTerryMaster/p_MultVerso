package npc.model;

import l2.gameserver.listener.zone.OnZoneEnterLeaveListener;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.Zone;
import l2.gameserver.model.entity.Reflection;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.network.l2.s2c.EventTrigger;
import l2.gameserver.templates.npc.NpcTemplate;
import l2.gameserver.utils.ReflectionUtils;

public final class OddGlobeInstance extends NpcInstance
{
	private static final int instancedZoneId = 151;
	
	public OddGlobeInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
		{
			return;
		}
		if(command.equalsIgnoreCase("monastery_enter"))
		{
			Reflection r = player.getActiveReflection();
			if(r != null)
			{
				if(player.canReenterInstance(151))
				{
					player.teleToLocation(r.getTeleportLoc(), r);
				}
			}
			else if(player.canEnterInstance(151))
			{
				Reflection newfew = ReflectionUtils.enterReflection(player, 151);
				ZoneListener zoneL = new ZoneListener();
				newfew.getZone("[ssq_holy_burial_ground]").addListener(zoneL);
				ZoneListener2 zoneL2 = new ZoneListener2();
				newfew.getZone("[ssq_holy_seal]").addListener(zoneL2);
			}
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
	
	public class ZoneListener2 implements OnZoneEnterLeaveListener
	{
		private boolean done;
		
		public ZoneListener2()
		{
			done = false;
		}
		
		@Override
		public void onZoneEnter(Zone zone, Creature cha)
		{
			Player player = cha.getPlayer();
			if(player == null || !cha.isPlayer())
			{
				return;
			}
			player.broadcastPacket(new EventTrigger(21100100, true));
			if(!done)
			{
				done = true;
				player.showQuestMovie(26);
			}
		}
		
		@Override
		public void onZoneLeave(Zone zone, Creature cha)
		{
		}
	}
	
	public class ZoneListener implements OnZoneEnterLeaveListener
	{
		private boolean done;
		
		public ZoneListener()
		{
			done = false;
		}
		
		@Override
		public void onZoneEnter(Zone zone, Creature cha)
		{
			Player player = cha.getPlayer();
			if(player == null || !cha.isPlayer() || done)
			{
				return;
			}
			done = true;
			player.showQuestMovie(24);
		}
		
		@Override
		public void onZoneLeave(Zone zone, Creature cha)
		{
		}
	}
}