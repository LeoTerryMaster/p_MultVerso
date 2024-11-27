package l2.gameserver.network.l2.c2s;

import l2.gameserver.Config;
import l2.gameserver.cache.Msg;
import l2.gameserver.model.Player;
import l2.gameserver.model.items.ManufactureItem;
import l2.gameserver.network.l2.s2c.RecipeShopMsg;
import l2.gameserver.utils.TradeHelper;

import java.util.concurrent.CopyOnWriteArrayList;

public class RequestRecipeShopListSet extends L2GameClientPacket
{
	private int[] _recipes;
	private long[] _prices;
	private int _count;
	
	@Override
	protected void readImpl()
	{
		_count = readD();
		if(_count * 8 > _buf.remaining() || _count > 32767 || _count < 1)
		{
			_count = 0;
			return;
		}
		_recipes = new int[_count];
		_prices = new long[_count];
		for(int i = 0;i < _count;++i)
		{
			_recipes[i] = readD();
			_prices[i] = readD();
			if(_prices[i] >= 0)
				continue;
			_count = 0;
			return;
		}
	}
	
	@Override
	protected void runImpl()
	{
		Player manufacturer = getClient().getActiveChar();
		if(manufacturer == null || _count == 0)
		{
			return;
		}
		if(!TradeHelper.checksIfCanOpenStore(manufacturer, 5))
		{
			manufacturer.sendActionFailed();
			return;
		}
		if(_count > Config.MAX_PVTCRAFT_SLOTS)
		{
			sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
			return;
		}
		CopyOnWriteArrayList<ManufactureItem> createList = new CopyOnWriteArrayList<>();
		for(int i = 0;i < _count;++i)
		{
			int recipeId = _recipes[i];
			long price = _prices[i];
			if(!manufacturer.findRecipe(recipeId))
				continue;
			ManufactureItem mi = new ManufactureItem(recipeId, price);
			createList.add(mi);
		}
		if(!createList.isEmpty())
		{
			manufacturer.setCreateList(createList);
			manufacturer.saveTradeList();
			manufacturer.setPrivateStoreType(5);
			manufacturer.broadcastPacket(new RecipeShopMsg(manufacturer));
			manufacturer.sitDown(null);
			manufacturer.broadcastCharInfo();
		}
		manufacturer.sendActionFailed();
	}
}