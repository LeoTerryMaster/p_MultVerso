package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.SkillLearn;
import l2.gameserver.model.base.AcquireType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AcquireSkillInfo extends L2GameServerPacket
{
	private final SkillLearn _learn;
	private final AcquireType _type;
	private List<Require> _reqs = Collections.emptyList();
	
	public AcquireSkillInfo(AcquireType type, SkillLearn learn)
	{
		this(type, learn, learn.getItemId(), (int) learn.getItemCount());
	}
	
	public AcquireSkillInfo(AcquireType type, SkillLearn learn, int itemId, int itemCount)
	{
		_type = type;
		_learn = learn;
		if(itemId != 0)
		{
			_reqs = new ArrayList<>(1);
			_reqs.add(new Require(99, itemId, itemCount, 50));
		}
	}
	
	@Override
	public void writeImpl()
	{
		writeC(139);
		writeD(_learn.getId());
		writeD(_learn.getLevel());
		writeD(_learn.getCost());
		writeD(_type.ordinal());
		writeD(_reqs.size());
		for(Require temp : _reqs)
		{
			writeD(temp.type);
			writeD(temp.itemId);
			writeD((int) temp.count);
			writeD(temp.unk);
		}
	}
	
	private static class Require
	{
		public int itemId;
		public long count;
		public int type;
		public int unk;
		
		public Require(int pType, int pItemId, long pCount, int pUnk)
		{
			itemId = pItemId;
			type = pType;
			count = pCount;
			unk = pUnk;
		}
	}
}