package l2.gameserver.network.authcomm.gs2as;

import l2.gameserver.model.Player;
import l2.gameserver.network.authcomm.SendablePacket;

public class IGPwdCng extends SendablePacket
{
	private final int _requestor_oid;
	private final String _account;
	private final String _old_pass;
	private final String _new_pass;
	
	public IGPwdCng(Player requestor, String old_pass, String new_pass)
	{
		_requestor_oid = requestor.getObjectId();
		_account = requestor.getAccountName();
		_old_pass = old_pass;
		_new_pass = new_pass;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(160);
		writeD(_requestor_oid);
		writeS(_account);
		writeS(_old_pass);
		writeS(_new_pass);
	}
}