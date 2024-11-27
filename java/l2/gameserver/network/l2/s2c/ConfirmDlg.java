package l2.gameserver.network.l2.s2c;

import l2.gameserver.network.l2.components.SystemMsg;

public class ConfirmDlg extends SysMsgContainer<ConfirmDlg>
{
	private final int _time;
	private int _requestId;
	
	public ConfirmDlg(SystemMsg msg, int time)
	{
		super(msg);
		_time = time;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(237);
		writeElements();
		writeD(_time);
		writeD(_requestId);
	}
	
	public void setRequestId(int requestId)
	{
		_requestId = requestId;
	}
}