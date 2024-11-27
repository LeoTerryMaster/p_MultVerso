package events.TheFallHarvest;

import handler.items.ScriptItemHandler;
import l2.commons.threading.RunnableImpl;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.data.xml.holder.NpcHolder;
import l2.gameserver.model.Playable;
import l2.gameserver.model.Player;
import l2.gameserver.model.SimpleSpawner;
import l2.gameserver.model.Zone;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.templates.npc.NpcTemplate;
import l2.gameserver.utils.Location;
import npc.model.SquashInstance;

public class Seed extends ScriptItemHandler
{
	private static final int[] _itemIds = {6389, 6390};
	private static final int[] _npcIds = {12774, 12777};
	
	@Override
	public boolean useItem(Playable playable, ItemInstance item, boolean ctrl)
	{
		Player activeChar = (Player) playable;
		if(activeChar.isInZone(Zone.ZoneType.RESIDENCE))
		{
			return false;
		}
		if(activeChar.isOlyParticipant())
		{
			activeChar.sendMessage("Нельзя взращивать тыкву на стадионе.");
			return false;
		}
		if(!activeChar.getReflection().isDefault())
		{
			activeChar.sendMessage("Нельзя взращивать тыкву в инстансе.");
			return false;
		}
		NpcTemplate template = null;
		int itemId = item.getItemId();
		for(int i = 0;i < _itemIds.length;++i)
		{
			if(_itemIds[i] != itemId)
				continue;
			template = NpcHolder.getInstance().getTemplate(_npcIds[i]);
			break;
		}
		if(template == null)
		{
			return false;
		}
		if(!activeChar.getInventory().destroyItem(item, 1))
		{
			return false;
		}
		SimpleSpawner spawn = new SimpleSpawner(template);
		spawn.setLoc(Location.findPointToStay(activeChar, 30, 70));
		NpcInstance npc = spawn.doSpawn(true);
		npc.setAI(new SquashAI(npc));
		((SquashInstance) npc).setSpawner(activeChar);
		ThreadPoolManager.getInstance().schedule(new DeSpawnScheduleTimerTask(spawn), 180000);
		return true;
	}
	
	@Override
	public int[] getItemIds()
	{
		return _itemIds;
	}
	
	public class DeSpawnScheduleTimerTask extends RunnableImpl
	{
		SimpleSpawner spawnedPlant;
		
		public DeSpawnScheduleTimerTask(SimpleSpawner spawn)
		{
			spawnedPlant = null;
			spawnedPlant = spawn;
		}
		
		@Override
		public void runImpl() throws Exception
		{
			spawnedPlant.deleteAll();
		}
	}
}