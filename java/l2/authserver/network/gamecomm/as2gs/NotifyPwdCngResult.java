package l2.authserver.network.gamecomm.as2gs;

import l2.authserver.network.gamecomm.SendablePacket;

public class NotifyPwdCngResult extends SendablePacket
{
	public static final int RESULT_OK = 1;
	public static final int RESULT_WRONG_OLD_PASSWORD = 2;
	public static final int RESULT_WRONG_NEW_PASSWORD = 3;
	public static final int RESULT_WRONG_ACCOUNT = 4;
	private final int _requestor_oid;
	private final int _result;
	
	public NotifyPwdCngResult(int requestor_oid, int result)
	{
		_requestor_oid = requestor_oid;
		_result = result;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(161);
		writeD(_requestor_oid);
		writeD(_result);
	}
}