package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Player;
import l2.gameserver.model.pledge.RankPrivs;

public class ManagePledgePower extends L2GameServerPacket
{
	private final int _action;
	private final int _clanId;
	private final int privs;
	
	public ManagePledgePower(Player player, int action, int rank)
	{
		_clanId = player.getClanId();
		_action = action;
		RankPrivs temp = player.getClan().getRankPrivs(rank);
		privs = temp == null ? 0 : temp.getPrivs();
		player.sendPacket(new PledgeReceiveUpdatePower(privs));
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(48);
		writeD(_clanId);
		writeD(_action);
		writeD(privs);
	}
}