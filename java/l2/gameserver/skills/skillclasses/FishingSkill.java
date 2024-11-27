package l2.gameserver.skills.skillclasses;

import l2.commons.collections.LazyArrayList;
import l2.commons.util.Rnd;
import l2.gameserver.cache.Msg;
import l2.gameserver.geodata.GeoEngine;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Fishing;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.World;
import l2.gameserver.model.Zone;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.tables.FishTable;
import l2.gameserver.templates.FishTemplate;
import l2.gameserver.templates.StatsSet;
import l2.gameserver.templates.item.WeaponTemplate;
import l2.gameserver.utils.Location;
import l2.gameserver.utils.PositionUtils;

import java.util.List;

public class FishingSkill extends Skill
{
	public FishingSkill(StatsSet set)
	{
		super(set);
	}
	
	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		Player player = (Player) activeChar;
		if(player.getSkillLevel(1315) == -1)
		{
			return false;
		}
		if(player.isFishing())
		{
			player.stopFishing();
			player.sendPacket(Msg.CANCELS_FISHING);
			return false;
		}
		if(player.isInBoat())
		{
			activeChar.sendPacket(Msg.YOU_CANT_FISH_WHILE_YOU_ARE_ON_BOARD);
			return false;
		}
		if(player.getPrivateStoreType() != 0)
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_FISH_WHILE_USING_A_RECIPE_BOOK_PRIVATE_MANUFACTURE_OR_PRIVATE_STORE);
			return false;
		}
		Zone fishingZone = player.getZone(Zone.ZoneType.FISHING);
		if(fishingZone == null)
		{
			player.sendPacket(Msg.YOU_CANT_FISH_HERE);
			return false;
		}
		if(player.isInWater())
		{
			player.sendPacket(Msg.YOU_CANT_FISH_HERE);
			return false;
		}
		WeaponTemplate weaponItem = player.getActiveWeaponItem();
		if(weaponItem == null || weaponItem.getItemType() != WeaponTemplate.WeaponType.ROD)
		{
			player.sendPacket(Msg.FISHING_POLES_ARE_NOT_INSTALLED);
			return false;
		}
		ItemInstance lure = player.getInventory().getPaperdollItem(8);
		if(lure == null || lure.getCount() < 1)
		{
			player.sendPacket(Msg.BAITS_ARE_NOT_PUT_ON_A_HOOK);
			return false;
		}
		int rnd = Rnd.get(50) + 150;
		double angle = PositionUtils.convertHeadingToDegree(player.getHeading());
		double radian = Math.toRadians(angle - 90.0);
		double sin = Math.sin(radian);
		double cos = Math.cos(radian);
		int x1 = -(int) (sin * (double) rnd);
		int y1 = (int) (cos * (double) rnd);
		int x = player.getX() + x1;
		int y = player.getY() + y1;
		int z = GeoEngine.getHeight(x, y, player.getZ(), player.getGeoIndex()) + 1;
		boolean isInWater = fishingZone.getParams().getInteger("fishing_place_type") == 2;
		LazyArrayList<Zone> zones = LazyArrayList.newInstance();
		World.getZones(zones, new Location(x, y, z), player.getReflection());
		for(Zone zone : zones)
		{
			if(zone.getType() != Zone.ZoneType.water)
				continue;
			z = zone.getTerritory().getZmax();
			isInWater = true;
			break;
		}
		LazyArrayList.recycle(zones);
		if(!isInWater)
		{
			player.sendPacket(Msg.YOU_CANT_FISH_HERE);
			return false;
		}
		player.getFishing().setFishLoc(new Location(x, y, z));
		return super.checkCondition(activeChar, target, forceUse, dontMove, first);
	}
	
	@Override
	public void useSkill(Creature caster, List<Creature> targets)
	{
		if(caster == null || !caster.isPlayer())
		{
			return;
		}
		Player player = (Player) caster;
		ItemInstance lure = player.getInventory().getPaperdollItem(8);
		if(lure == null || lure.getCount() < 1)
		{
			player.sendPacket(Msg.BAITS_ARE_NOT_PUT_ON_A_HOOK);
			return;
		}
		Zone zone = player.getZone(Zone.ZoneType.FISHING);
		if(zone == null)
		{
			return;
		}
		int distributionId = zone.getParams().getInteger("distribution_id");
		int lureId = lure.getItemId();
		int fishLvl = Fishing.getRandomFishLvl(player);
		int group = Fishing.getFishGroup(lureId);
		int type = Fishing.getRandomFishType(lureId, fishLvl, distributionId);
		List<FishTemplate> fishs = FishTable.getInstance().getFish(group, type, fishLvl);
		if(fishs == null || fishs.size() == 0)
		{
			player.sendPacket(Msg.SYSTEM_ERROR);
			return;
		}
		if(!player.getInventory().destroyItemByObjectId(player.getInventory().getPaperdollObjectId(8), 1))
		{
			player.sendPacket(Msg.NOT_ENOUGH_BAIT);
			return;
		}
		int check = Rnd.get(fishs.size());
		FishTemplate fish = fishs.get(check);
		player.startFishing(fish, lureId);
	}
}