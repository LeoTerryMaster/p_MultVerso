package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.base.Element;
import l2.gameserver.model.items.ItemInstance;

public class ExBaseAttributeCancelResult extends L2GameServerPacket
{
	private final boolean _result;
	private final int _objectId;
	private final Element _element;
	
	public ExBaseAttributeCancelResult(boolean result, ItemInstance item, Element element)
	{
		_result = result;
		_objectId = item.getObjectId();
		_element = element;
	}
	
	@Override
	protected void writeImpl()
	{
		writeEx(117);
		writeD(_result);
		writeD(_objectId);
		writeD(_element.getId());
	}
}