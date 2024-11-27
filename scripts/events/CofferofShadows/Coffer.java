package events.CofferofShadows;

import handler.items.ScriptItemHandler;
import l2.commons.util.Rnd;
import l2.gameserver.Config;
import l2.gameserver.model.Playable;
import l2.gameserver.model.Player;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.model.reward.RewardData;
import l2.gameserver.network.l2.s2c.SystemMessage2;
import l2.gameserver.utils.ItemFunctions;

import java.util.HashMap;
import java.util.Map;

public class Coffer extends ScriptItemHandler
{
	protected static final RewardData[] _dropmats = {new RewardData(4041, 1, 1, 250.0), new RewardData(4042, 1, 1, 450.0), new RewardData(4040, 1, 1, 500.0), new RewardData(1890, 1, 3, 833.0), new RewardData(5550, 1, 3, 833.0), new RewardData(4039, 1, 1, 833.0), new RewardData(4043, 1, 1, 833.0), new RewardData(4044, 1, 1, 833.0), new RewardData(1888, 1, 3, 1000.0), new RewardData(1877, 1, 3, 1000.0), new RewardData(1894, 1, 3, 3000.0), new RewardData(1874, 1, 5, 3000.0), new RewardData(1875, 1, 5, 3000.0), new RewardData(1887, 1, 3, 3000.0), new RewardData(1866, 1, 10, 16666.0), new RewardData(1882, 1, 10, 16666.0), new RewardData(1881, 1, 10, 10000.0), new RewardData(1873, 1, 10, 10000.0), new RewardData(1879, 1, 5, 10000.0), new RewardData(1880, 1, 5, 10000.0), new RewardData(1876, 1, 5, 10000.0), new RewardData(1864, 1, 20, 25000.0), new RewardData(1865, 1, 20, 25000.0), new RewardData(1868, 1, 15, 25000.0), new RewardData(1869, 1, 15, 25000.0), new RewardData(1870, 1, 15, 25000.0), new RewardData(1871, 1, 15, 25000.0), new RewardData(1872, 1, 20, 30000.0), new RewardData(1867, 1, 20, 33333.0)};
	protected static final RewardData[] _dropacc = {new RewardData(8660, 1, 1, 1000.0), new RewardData(8661, 1, 1, 1000.0), new RewardData(4393, 1, 1, 300.0), new RewardData(5590, 1, 1, 200.0), new RewardData(7058, 1, 1, 50.0), new RewardData(8350, 1, 1, 50.0), new RewardData(5133, 1, 1, 50.0), new RewardData(5817, 1, 1, 50.0), new RewardData(9140, 1, 1, 30.0), new RewardData(9177, 1, 1, 100.0), new RewardData(9178, 1, 1, 100.0), new RewardData(9179, 1, 1, 100.0), new RewardData(9180, 1, 1, 100.0), new RewardData(9181, 1, 1, 100.0), new RewardData(9182, 1, 1, 100.0), new RewardData(9183, 1, 1, 100.0), new RewardData(9184, 1, 1, 100.0), new RewardData(9185, 1, 1, 100.0), new RewardData(9186, 1, 1, 100.0), new RewardData(9187, 1, 1, 100.0), new RewardData(9188, 1, 1, 100.0), new RewardData(9189, 1, 1, 100.0), new RewardData(9190, 1, 1, 100.0), new RewardData(9191, 1, 1, 100.0), new RewardData(9192, 1, 1, 100.0), new RewardData(9193, 1, 1, 100.0), new RewardData(9194, 1, 1, 100.0), new RewardData(9195, 1, 1, 100.0), new RewardData(9196, 1, 1, 100.0), new RewardData(9197, 1, 1, 100.0), new RewardData(9198, 1, 1, 100.0), new RewardData(9199, 1, 1, 100.0)};
	protected static final RewardData[] _dropevents = {new RewardData(9146, 1, 1, 3000.0), new RewardData(9147, 1, 1, 3000.0), new RewardData(9148, 1, 1, 3000.0), new RewardData(9149, 1, 1, 3000.0), new RewardData(9150, 1, 1, 3000.0), new RewardData(9151, 1, 1, 3000.0), new RewardData(9152, 1, 1, 3000.0), new RewardData(9153, 1, 1, 3000.0), new RewardData(9154, 1, 1, 3000.0), new RewardData(9155, 1, 1, 3000.0), new RewardData(9156, 1, 1, 2000.0), new RewardData(9157, 1, 1, 1000.0), new RewardData(5234, 1, 5, 25000.0), new RewardData(7609, 50, 100, 24000.0), new RewardData(7562, 2, 4, 10000.0), new RewardData(6415, 1, 3, 20000.0), new RewardData(1461, 1, 3, 15000.0), new RewardData(6406, 1, 3, 20000.0), new RewardData(6407, 1, 1, 20000.0), new RewardData(6403, 1, 5, 20000.0), new RewardData(6036, 1, 5, 30000.0), new RewardData(5595, 1, 1, 21000.0), new RewardData(1374, 1, 5, 20000.0), new RewardData(1375, 1, 5, 20000.0), new RewardData(1540, 1, 3, 20000.0), new RewardData(5126, 1, 1, 1000.0)};
	protected static final RewardData[] _dropench = {new RewardData(955, 1, 1, 400.0), new RewardData(956, 1, 1, 2000.0), new RewardData(951, 1, 1, 300.0), new RewardData(952, 1, 1, 1500.0), new RewardData(947, 1, 1, 200.0), new RewardData(948, 1, 1, 1000.0), new RewardData(729, 1, 1, 100.0), new RewardData(730, 1, 1, 500.0), new RewardData(959, 1, 1, 50.0), new RewardData(960, 1, 1, 300.0), new RewardData(5577, 1, 1, 90.0), new RewardData(5578, 1, 1, 90.0), new RewardData(5579, 1, 1, 90.0), new RewardData(5580, 1, 1, 70.0), new RewardData(5581, 1, 1, 70.0), new RewardData(5582, 1, 1, 70.0)};
	private static final int[] _itemIds = {8659};
	
	@Override
	public boolean useItem(Playable playable, ItemInstance item, boolean ctrl)
	{
		if(!playable.isPlayer())
		{
			return false;
		}
		Player activeChar = playable.getPlayer();
		if(!activeChar.isQuestContinuationPossible(true))
		{
			return false;
		}
		HashMap<Integer, Long> items = new HashMap<>();
		long count = 0;
		do
		{
			getGroupItem(activeChar, _dropmats, items);
			getGroupItem(activeChar, _dropacc, items);
			getGroupItem(activeChar, _dropevents, items);
			getGroupItem(activeChar, _dropench, items);
		}
		while(ctrl && item.getCount() > ++count && activeChar.isQuestContinuationPossible(false));
		activeChar.getInventory().destroyItem(item, count);
		activeChar.sendPacket(SystemMessage2.removeItems(item.getItemId(), count));
		for(Map.Entry<Integer, Long> e : items.entrySet())
		{
			activeChar.sendPacket(SystemMessage2.obtainItems(e.getKey(), e.getValue(), 0));
		}
		return true;
	}
	
	public void getGroupItem(Player activeChar, RewardData[] dropData, Map<Integer, Long> report)
	{
		for(RewardData d : dropData)
		{
			if((double) Rnd.get(1, 1000000) > d.getChance() * Config.EVENT_CofferOfShadowsRewardRate)
				continue;
			long count = Rnd.get(d.getMinDrop(), d.getMaxDrop());
			ItemInstance item = ItemFunctions.createItem(d.getItemId());
			item.setCount(count);
			activeChar.getInventory().addItem(item);
			Long old = report.get(d.getItemId());
			report.put(d.getItemId(), old != null ? old + count : count);
		}
	}
	
	@Override
	public final int[] getItemIds()
	{
		return _itemIds;
	}
}