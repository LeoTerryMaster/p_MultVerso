package l2.gameserver.network.l2.c2s;

import l2.gameserver.cache.Msg;
import l2.gameserver.handler.items.IRefineryHandler;
import l2.gameserver.model.Player;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.network.l2.s2c.ExPutCommissionResultForVariationMake;

public final class RequestRefine extends L2GameClientPacket
{
	private static final int GEMSTONE_D = 2130;
	private static final int GEMSTONE_C = 2131;
	private static final int GEMSTONE_B = 2132;
	private int _targetItemObjId;
	private int _refinerItemObjId;
	private int _gemstoneItemObjId;
	private long _gemstoneCount;
	
	@Override
	protected void readImpl()
	{
		_targetItemObjId = readD();
		_refinerItemObjId = readD();
		_gemstoneItemObjId = readD();
		_gemstoneCount = readD();
	}
	
	@Override
	protected void runImpl()
	{
		if(_gemstoneCount <= 0)
		{
			return;
		}
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		ItemInstance targetItem = activeChar.getInventory().getItemByObjectId(_targetItemObjId);
		ItemInstance refinerItem = activeChar.getInventory().getItemByObjectId(_refinerItemObjId);
		ItemInstance gemstoneItem = activeChar.getInventory().getItemByObjectId(_gemstoneItemObjId);
		IRefineryHandler IRefineryHandler2 = activeChar.getRefineryHandler();
		if(targetItem == null || refinerItem == null || gemstoneItem == null || IRefineryHandler2 == null)
		{
			activeChar.sendPacket(Msg.THIS_IS_NOT_A_SUITABLE_ITEM, ExPutCommissionResultForVariationMake.FAIL_PACKET);
			return;
		}
		IRefineryHandler2.onRequestRefine(activeChar, targetItem, refinerItem, gemstoneItem, _gemstoneCount);
	}
}