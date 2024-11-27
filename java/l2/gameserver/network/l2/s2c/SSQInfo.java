package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.entity.SevenSigns;

public class SSQInfo extends L2GameServerPacket
{
	private int _state;
	
	public SSQInfo()
	{
		int compWinner = SevenSigns.getInstance().getCabalHighestScore();
		if(SevenSigns.getInstance().isSealValidationPeriod())
		{
			if(compWinner == 2)
			{
				_state = 2;
			}
			else if(compWinner == 1)
			{
				_state = 1;
			}
		}
	}
	
	public SSQInfo(int state)
	{
		_state = state;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(248);
		switch(_state)
		{
			case 1:
			{
				writeH(257);
				break;
			}
			case 2:
			{
				writeH(258);
				break;
			}
			default:
			{
				writeH(256);
			}
		}
	}
}