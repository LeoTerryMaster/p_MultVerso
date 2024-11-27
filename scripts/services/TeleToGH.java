package services;

import l2.gameserver.Config;
import l2.gameserver.cache.Msg;
import l2.gameserver.instancemanager.ReflectionManager;
import l2.gameserver.listener.zone.OnZoneEnterLeaveListener;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.Zone;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.scripts.Functions;
import l2.gameserver.scripts.ScriptFile;
import l2.gameserver.utils.Location;
import l2.gameserver.utils.PositionUtils;
import l2.gameserver.utils.ReflectionUtils;

public class TeleToGH extends Functions implements ScriptFile
{
	private static final Zone _zone = ReflectionUtils.getZone((String) "[giran_harbor_offshore]");
	private static final String en;
	private static final String ru;
	private static final String en2 = "Altar Gatekeeper:<center><br>[scripts_services.ManaRegen:DoManaRegen|Full MP Regeneration. (1 MP for 5 Adena)]<br>[scripts_services.TeleToGH:fromGH @811;From Giran Harbor|\"Exit the Giran Harbor.\"]<br></center>";
	private static final String ru2 = "Altar Gatekeeper:<center><br>[scripts_services.ManaRegen:DoManaRegen|Полное восстановление MP. (1 MP за 5 Adena)]<br>[scripts_services.TeleToGH:fromGH @811;From Giran Harbor|\"Покинуть Giran Harbor.\"]<br></center>";
	private static ZoneListener _zoneListener;
	
	static
	{
		en = "<br>[scripts_services.TeleToGH:toGH @811;Giran Harbor|\"Move to Giran Harbor (offshore zone) - " + Config.SERVICES_GIRAN_HARBOR_PRICE + " Adena.\"]";
		ru = "<br>[scripts_services.TeleToGH:toGH @811;Giran Harbor|\"Giran Harbor (торговая зона без налогов) - " + Config.SERVICES_GIRAN_HARBOR_PRICE + " Adena.\"]";
	}
	
	@Override
	public void onLoad()
	{
		if(!Config.SERVICES_GIRAN_HARBOR_ENABLED)
		{
			return;
		}
		ReflectionManager.GIRAN_HARBOR.setCoreLoc(new Location(47416, 186568, -3480));
		_zoneListener = new ZoneListener();
		_zone.addListener(_zoneListener);
		_zone.setReflection(ReflectionManager.GIRAN_HARBOR);
		_zone.setActive(true);
		Zone zone = ReflectionUtils.getZone("[giran_harbor_peace_alt]");
		zone.setReflection(ReflectionManager.GIRAN_HARBOR);
		zone.setActive(true);
		zone = ReflectionUtils.getZone("[giran_harbor_no_trade]");
		zone.setReflection(ReflectionManager.GIRAN_HARBOR);
		zone.setActive(true);
	}
	
	@Override
	public void onReload()
	{
		_zone.removeListener(_zoneListener);
	}
	
	@Override
	public void onShutdown()
	{
	}
	
	public void toGH()
	{
		Player player = getSelf();
		if(player == null)
		{
			return;
		}
		if(player.getAdena() < (long) Config.SERVICES_GIRAN_HARBOR_PRICE)
		{
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			return;
		}
		player.reduceAdena((long) Config.SERVICES_GIRAN_HARBOR_PRICE, true);
		player.setVar("backCoords", player.getLoc().toXYZString(), -1);
		player.teleToLocation(Location.findPointToStay(_zone.getSpawn(), 30, 200, ReflectionManager.GIRAN_HARBOR.getGeoIndex()), ReflectionManager.GIRAN_HARBOR);
	}
	
	public void fromGH()
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(player == null || npc == null)
		{
			return;
		}
		if(!NpcInstance.canBypassCheck(player, npc))
		{
			return;
		}
		String var = player.getVar("backCoords");
		if(var == null || var.equals(""))
		{
			teleOut();
			return;
		}
		player.teleToLocation(Location.parseLoc(var), 0);
	}
	
	public void teleOut()
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(player == null || npc == null)
		{
			return;
		}
		player.teleToLocation(46776, 185784, -3528, 0);
		show(player.isLangRus() ? "Я не знаю, как Вы попали сюда, но я могу Вас отправить за ограждение." : "I don't know from where you came here, but I can teleport you the another border side.", player, npc, (Object[]) new Object[0]);
	}
	
	public String DialogAppend_30059(Integer val)
	{
		return getHtmlAppends(val);
	}
	
	public String DialogAppend_30080(Integer val)
	{
		return getHtmlAppends(val);
	}
	
	public String DialogAppend_30177(Integer val)
	{
		return getHtmlAppends(val);
	}
	
	public String DialogAppend_30233(Integer val)
	{
		return getHtmlAppends(val);
	}
	
	public String DialogAppend_30256(Integer val)
	{
		return getHtmlAppends(val);
	}
	
	public String DialogAppend_30320(Integer val)
	{
		return getHtmlAppends(val);
	}
	
	public String DialogAppend_30848(Integer val)
	{
		return getHtmlAppends(val);
	}
	
	public String DialogAppend_30878(Integer val)
	{
		return getHtmlAppends(val);
	}
	
	public String DialogAppend_30899(Integer val)
	{
		return getHtmlAppends(val);
	}
	
	public String DialogAppend_31210(Integer val)
	{
		return getHtmlAppends(val);
	}
	
	public String DialogAppend_31275(Integer val)
	{
		return getHtmlAppends(val);
	}
	
	public String DialogAppend_31320(Integer val)
	{
		return getHtmlAppends(val);
	}
	
	public String DialogAppend_31964(Integer val)
	{
		return getHtmlAppends(val);
	}
	
	public String DialogAppend_30006(Integer val)
	{
		return getHtmlAppends(val);
	}
	
	public String DialogAppend_30134(Integer val)
	{
		return getHtmlAppends(val);
	}
	
	public String DialogAppend_30146(Integer val)
	{
		return getHtmlAppends(val);
	}
	
	public String DialogAppend_30576(Integer val)
	{
		return getHtmlAppends(val);
	}
	
	public String DialogAppend_30540(Integer val)
	{
		return getHtmlAppends(val);
	}
	
	public String getHtmlAppends(Integer val)
	{
		if(val != 0 || !Config.SERVICES_GIRAN_HARBOR_ENABLED)
		{
			return "";
		}
		Player player = getSelf();
		if(player == null)
		{
			return "";
		}
		return player.isLangRus() ? ru : en;
	}
	
	public String DialogAppend_40030(Integer val)
	{
		return getHtmlAppends2(val);
	}
	
	public String getHtmlAppends2(Integer val)
	{
		if(val != 0 || !Config.SERVICES_GIRAN_HARBOR_ENABLED)
		{
			return "";
		}
		Player player = getSelf();
		if(player == null || player.getReflectionId() != -1)
		{
			return "";
		}
		return player.isLangRus() ? "Altar Gatekeeper:<center><br>[scripts_services.ManaRegen:DoManaRegen|Полное восстановление MP. (1 MP за 5 Adena)]<br>[scripts_services.TeleToGH:fromGH @811;From Giran Harbor|\"Покинуть Giran Harbor.\"]<br></center>" : "Altar Gatekeeper:<center><br>[scripts_services.ManaRegen:DoManaRegen|Full MP Regeneration. (1 MP for 5 Adena)]<br>[scripts_services.TeleToGH:fromGH @811;From Giran Harbor|\"Exit the Giran Harbor.\"]<br></center>";
	}
	
	public class ZoneListener implements OnZoneEnterLeaveListener
	{
		@Override
		public void onZoneEnter(Zone zone, Creature cha)
		{
		}
		
		@Override
		public void onZoneLeave(Zone zone, Creature cha)
		{
			Player player = cha.getPlayer();
			if(player != null && Config.SERVICES_GIRAN_HARBOR_ENABLED && player.getReflection() == ReflectionManager.GIRAN_HARBOR && player.isVisible())
			{
				double angle = PositionUtils.convertHeadingToDegree(cha.getHeading());
				double radian = Math.toRadians(angle - 90.0);
				cha.teleToLocation((int) ((double) cha.getX() + 50.0 * Math.sin(radian)), (int) ((double) cha.getY() - 50.0 * Math.cos(radian)), cha.getZ());
			}
		}
	}
}