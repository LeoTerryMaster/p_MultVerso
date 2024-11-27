package l2.gameserver.network.l2.s2c;

import l2.gameserver.templates.PlayerTemplate;

import java.util.ArrayList;
import java.util.List;

public class NewCharacterSuccess extends L2GameServerPacket
{
	private final List<PlayerTemplate> _chars = new ArrayList<>();
	
	public void addChar(PlayerTemplate template)
	{
		_chars.add(template);
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(23);
		writeD(_chars.size());
		for(PlayerTemplate temp : _chars)
		{
			writeD(temp.race.ordinal());
			writeD(temp.classId.getId());
			writeD(70);
			writeD(temp.getBaseSTR());
			writeD(10);
			writeD(70);
			writeD(temp.baseDEX);
			writeD(10);
			writeD(70);
			writeD(temp.baseCON);
			writeD(10);
			writeD(70);
			writeD(temp.baseINT);
			writeD(10);
			writeD(70);
			writeD(temp.baseWIT);
			writeD(10);
			writeD(70);
			writeD(temp.baseMEN);
			writeD(10);
		}
	}
}