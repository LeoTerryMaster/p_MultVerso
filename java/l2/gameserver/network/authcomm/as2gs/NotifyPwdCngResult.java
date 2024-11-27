package l2.gameserver.network.authcomm.as2gs;

import l2.gameserver.model.Player;
import l2.gameserver.model.World;
import l2.gameserver.network.authcomm.ReceivablePacket;

public class NotifyPwdCngResult extends ReceivablePacket
{
	public static final int RESULT_OK = 1;
	public static final int RESULT_WRONG_OLD_PASSWORD = 2;
	public static final int RESULT_WRONG_NEW_PASSWORD = 3;
	public static final int RESULT_WRONG_ACCOUNT = 4;
	private int _requestor_oid;
	private int _result;
	
	@Override
	protected void readImpl()
	{
		_requestor_oid = readD();
		_result = readD();
	}
	
	@Override
	protected void runImpl()
	{
		Player player = World.getPlayer(_requestor_oid);
		if(player == null)
		{
			return;
		}
		switch(_result)
		{
			case 1:
			{
				player.sendMessage("Password succesfuly changed.");
				player.setVar("LastPwdChng", Long.toString(System.currentTimeMillis() / 1000), -1);
				break;
			}
			case 2:
			{
				player.sendMessage("Can't change password! Wrong old password.");
				break;
			}
			case 3:
			{
				player.sendMessage("Can't change password! Wrong new password.");
				break;
			}
			case 4:
			{
				player.sendMessage("Can't change password! Wrong account.");
				break;
			}
			default:
			{
				player.sendMessage("Can't change password! System error. Try later.");
			}
		}
	}
}