package l2.gameserver.network.l2.c2s;

import l2.gameserver.data.xml.holder.ResidenceHolder;
import l2.gameserver.instancemanager.CastleManorManager;
import l2.gameserver.model.Player;
import l2.gameserver.model.entity.residence.Castle;
import l2.gameserver.templates.manor.SeedProduction;

import java.util.ArrayList;

public class RequestSetSeed extends L2GameClientPacket
{
	private int _count;
	private int _manorId;
	private long[] _items;
	
	@Override
	protected void readImpl()
	{
		_manorId = readD();
		_count = readD();
		if(_count * 12 > _buf.remaining() || _count > 32767 || _count < 1)
		{
			_count = 0;
			return;
		}
		_items = new long[_count * 3];
		for(int i = 0;i < _count;++i)
		{
			_items[i * 3 + 0] = readD();
			_items[i * 3 + 1] = readD();
			_items[i * 3 + 2] = readD();
			if(_items[i * 3 + 0] >= 1 && _items[i * 3 + 1] >= 0 && _items[i * 3 + 2] >= 0)
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
		ArrayList<SeedProduction> seeds = new ArrayList<>(_count);
		for(int i = 0;i < _count;++i)
		{
			int id = (int) _items[i * 3 + 0];
			long sales = _items[i * 3 + 1];
			long price = _items[i * 3 + 2];
			if(id <= 0)
				continue;
			SeedProduction s = CastleManorManager.getInstance().getNewSeedProduction(id, sales, price, sales);
			seeds.add(s);
		}
		caslte.setSeedProduction(seeds, 1);
		caslte.saveSeedData(1);
	}
}