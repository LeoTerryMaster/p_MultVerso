package l2.gameserver.network.l2.c2s;

import l2.gameserver.cache.CrestCache;
import l2.gameserver.cache.Msg;
import l2.gameserver.model.Player;
import l2.gameserver.model.pledge.Clan;

public class RequestSetPledgeCrest extends L2GameClientPacket
{
	private int _length;
	private byte[] _data;
	
	@Override
	protected void readImpl()
	{
		_length = readD();
		if(_length == 256 && _length == _buf.remaining())
		{
			_data = new byte[_length];
			readB(_data);
		}
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		Clan clan = activeChar.getClan();
		if((activeChar.getClanPrivileges() & 128) == 128)
		{
			if(clan.getLevel() < 3)
			{
				activeChar.sendPacket(Msg.CLAN_CREST_REGISTRATION_IS_ONLY_POSSIBLE_WHEN_CLANS_SKILL_LEVELS_ARE_ABOVE_3);
				return;
			}
			int crestId = 0;
			if(_data != null && CrestCache.isValidCrestData(_data))
			{
				crestId = CrestCache.getInstance().savePledgeCrest(clan.getClanId(), _data);
			}
			else if(clan.hasCrest())
			{
				CrestCache.getInstance().removePledgeCrest(clan.getClanId());
			}
			clan.setCrestId(crestId);
			clan.broadcastClanStatus(false, true, false);
		}
	}
}