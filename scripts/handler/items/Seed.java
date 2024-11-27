package handler.items;

import l2.gameserver.cache.Msg;
import l2.gameserver.instancemanager.MapRegionManager;
import l2.gameserver.model.Manor;
import l2.gameserver.model.Playable;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.instances.ChestInstance;
import l2.gameserver.model.instances.MinionInstance;
import l2.gameserver.model.instances.MonsterInstance;
import l2.gameserver.model.instances.RaidBossInstance;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.tables.SkillTable;
import l2.gameserver.templates.mapregion.DomainArea;

public class Seed extends ScriptItemHandler
{
	private static int[] _itemIds = new int[0];
	
	public Seed()
	{
		_itemIds = new int[Manor.getInstance().getAllSeeds().size()];
		int id = 0;
		for(Integer s : Manor.getInstance().getAllSeeds().keySet())
		{
			_itemIds[id++] = s.shortValue();
		}
	}
	
	@Override
	public boolean useItem(Playable playable, ItemInstance item, boolean ctrl)
	{
		if(playable == null || !playable.isPlayer())
		{
			return false;
		}
		Player player = (Player) playable;
		if(playable.getTarget() == null)
		{
			player.sendActionFailed();
			return false;
		}
		if(!player.getTarget().isMonster() || player.getTarget() instanceof RaidBossInstance || player.getTarget() instanceof MinionInstance && ((MinionInstance) player.getTarget()).getLeader() instanceof RaidBossInstance || player.getTarget() instanceof ChestInstance || ((MonsterInstance) playable.getTarget()).getChampion() > 0 && !item.isAltSeed())
		{
			player.sendPacket(SystemMsg.THE_TARGET_IS_UNAVAILABLE_FOR_SEEDING);
			return false;
		}
		MonsterInstance target = (MonsterInstance) playable.getTarget();
		if(target == null)
		{
			player.sendPacket(Msg.INVALID_TARGET);
			return false;
		}
		if(target.isDead())
		{
			player.sendPacket(Msg.INVALID_TARGET);
			return false;
		}
		if(target.isSeeded())
		{
			player.sendPacket(SystemMsg.THE_SEED_HAS_BEEN_SOWN);
			return false;
		}
		int seedId = item.getItemId();
		if(seedId == 0 || player.getInventory().getItemByItemId(item.getItemId()) == null)
		{
			player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
			return false;
		}
		DomainArea domain = MapRegionManager.getInstance().getRegionData(DomainArea.class, player);
		int castleId;
		int n = castleId = domain == null ? 0 : domain.getId();
		if(Manor.getInstance().getCastleIdForSeed(seedId) != castleId)
		{
			player.sendPacket(SystemMsg.THIS_SEED_MAY_NOT_BE_SOWN_HERE);
			return false;
		}
		Skill skill = SkillTable.getInstance().getInfo(2097, 1);
		if(skill == null)
		{
			player.sendActionFailed();
			return false;
		}
		if(skill.checkCondition(player, target, false, false, true))
		{
			player.setUseSeed(seedId);
			player.getAI().Cast(skill, target);
		}
		return true;
	}
	
	@Override
	public final int[] getItemIds()
	{
		return _itemIds;
	}
}