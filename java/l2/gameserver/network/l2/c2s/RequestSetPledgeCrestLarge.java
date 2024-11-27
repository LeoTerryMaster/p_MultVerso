package l2.gameserver.network.l2.c2s;

import l2.gameserver.cache.CrestCache;
import l2.gameserver.cache.Msg;
import l2.gameserver.model.Player;
import l2.gameserver.model.pledge.Clan;

public class RequestSetPledgeCrestLarge extends L2GameClientPacket
{
	private int _length;
	private byte[] _data;
	
	@Override
	protected void readImpl()
	{
		_length = readD();
		if(_length == 2176 && _length == _buf.remaining())
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
		if(clan == null)
		{
			return;
		}
		if((activeChar.getClanPrivileges() & 128) == 128)
		{
			if(clan.getCastle() == 0 && clan.getHasHideout() == 0)
			{
				activeChar.sendPacket(Msg.THE_CLANS_EMBLEM_WAS_SUCCESSFULLY_REGISTERED__ONLY_A_CLAN_THAT_OWNS_A_CLAN_HALL_OR_A_CASTLE_CAN_GET_THEIR_EMBLEM_DISPLAYED_ON_CLAN_RELATED_ITEMS);
				return;
			}
			int crestId = 0;
			if(_data != null && CrestCache.isValidCrestData(_data))
			{
				crestId = CrestCache.getInstance().savePledgeCrestLarge(clan.getClanId(), _data);
				activeChar.sendPacket(Msg.THE_CLANS_EMBLEM_WAS_SUCCESSFULLY_REGISTERED__ONLY_A_CLAN_THAT_OWNS_A_CLAN_HALL_OR_A_CASTLE_CAN_GET_THEIR_EMBLEM_DISPLAYED_ON_CLAN_RELATED_ITEMS);
			}
			else if(clan.hasCrestLarge())
			{
				CrestCache.getInstance().removePledgeCrestLarge(clan.getClanId());
			}
			clan.setCrestLargeId(crestId);
			clan.broadcastClanStatus(false, true, false);
		}
	}
}