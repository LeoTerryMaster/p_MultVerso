package l2.gameserver.network.l2.c2s;

import l2.gameserver.data.xml.holder.ResidenceHolder;
import l2.gameserver.instancemanager.CastleManorManager;
import l2.gameserver.model.Player;
import l2.gameserver.model.entity.residence.Castle;
import l2.gameserver.templates.manor.CropProcure;

import java.util.ArrayList;

public class RequestSetCrop extends L2GameClientPacket
{
	private int _count;
	private int _manorId;
	private long[] _items;
	
	@Override
	protected void readImpl()
	{
		_manorId = readD();
		_count = readD();
		if(_count * 13 > _buf.remaining() || _count > 32767 || _count < 1)
		{
			_count = 0;
			return;
		}
		_items = new long[_count * 4];
		for(int i = 0;i < _count;++i)
		{
			_items[i * 4 + 0] = readD();
			_items[i * 4 + 1] = readD();
			_items[i * 4 + 2] = readD();
			_items[i * 4 + 3] = readC();
			if(_items[i * 4 + 0] >= 1 && _items[i * 4 + 1] >= 0 && _items[i * 4 + 2] >= 0)
				continue;
			_count = 0;
			return;
		}
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null || _count == 0)
		{
			return;
		}
		if(activeChar.getClan() == null)
		{
			activeChar.sendActionFailed();
			return;
		}
		Castle caslte = ResidenceHolder.getInstance().getResidence(Castle.class, _manorId);
		if(caslte.getOwnerId() != activeChar.getClanId() || (activeChar.getClanPrivileges() & 65536) != 65536)
		{
			activeChar.sendActionFailed();
			return;
		}
		ArrayList<CropProcure> crops = new ArrayList<>(_count);
		for(int i = 0;i < _count;++i)
		{
			int id = (int) _items[i * 4 + 0];
			long sales = _items[i * 4 + 1];
			long price = _items[i * 4 + 2];
			int type = (int) _items[i * 4 + 3];
			if(id <= 0)
				continue;
			CropProcure s = CastleManorManager.getInstance().getNewCropProcure(id, sales, type, price, sales);
			crops.add(s);
		}
		caslte.setCropProcure(crops, 1);
		caslte.saveCropData(1);
	}
}