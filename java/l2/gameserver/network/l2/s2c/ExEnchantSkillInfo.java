package l2.gameserver.network.l2.s2c;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.LinkedList;
import java.util.List;

public class ExEnchantSkillInfo extends L2GameServerPacket
{
	private final int _skillId;
	private final int _skillLevel;
	private final int _sp;
	private final long _exp;
	private final int _chance;
	private final List<Pair<Integer, Long>> _itemsNeeded;
	
	public ExEnchantSkillInfo(int skillId, int skillLvl, int sp, long exp, int chance)
	{
		_skillId = skillId;
		_skillLevel = skillLvl;
		_sp = sp;
		_exp = exp;
		_chance = chance;
		_itemsNeeded = new LinkedList<>();
	}
	
	public void addNeededItem(int itemId, long itemCount)
	{
		_itemsNeeded.add(ImmutablePair.of(itemId, itemCount));
	}
	
	@Override
	protected void writeImpl()
	{
		writeEx(24);
		writeD(_skillId);
		writeD(_skillLevel);
		writeD(_sp);
		writeQ(_exp);
		writeD(_chance);
		if(_itemsNeeded.isEmpty())
		{
			writeD(0);
			writeD(0);
			writeD(0);
			writeD(0);
			writeD(0);
		}
		else
		{
			writeD(_itemsNeeded.size());
			for(Pair<Integer, Long> itemNeeded : _itemsNeeded)
			{
				writeD(4);
				writeD(itemNeeded.getKey());
				writeD(itemNeeded.getValue().intValue());
				writeD(0);
			}
		}
	}
}