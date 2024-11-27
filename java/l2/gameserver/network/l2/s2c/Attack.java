package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Creature;
import l2.gameserver.model.GameObject;

public class Attack extends L2GameServerPacket
{
	public final int _attackerId;
	public final boolean _soulshot;
	private final int _grade;
	private final int _x;
	private final int _y;
	private final int _z;
	private final int _tx;
	private final int _ty;
	private final int _tz;
	private Hit[] hits;
	
	public Attack(Creature attacker, Creature target, boolean ss, int grade)
	{
		_attackerId = attacker.getObjectId();
		_soulshot = ss;
		_grade = grade;
		_x = attacker.getX();
		_y = attacker.getY();
		_z = attacker.getZ();
		_tx = target.getX();
		_ty = target.getY();
		_tz = target.getZ();
		hits = new Hit[0];
	}
	
	public void addHit(GameObject target, int damage, boolean miss, boolean crit, boolean shld)
	{
		int pos = hits.length;
		Hit[] tmp = new Hit[pos + 1];
		System.arraycopy(hits, 0, tmp, 0, hits.length);
		tmp[pos] = new Hit(target, damage, miss, crit, shld);
		hits = tmp;
	}
	
	public boolean hasHits()
	{
		return hits.length > 0;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(5);
		writeD(_attackerId);
		writeD(hits[0]._targetId);
		writeD(hits[0]._damage);
		writeC(hits[0]._flags);
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeH(hits.length - 1);
		for(int i = 1;i < hits.length;++i)
		{
			writeD(hits[i]._targetId);
			writeD(hits[i]._damage);
			writeC(hits[i]._flags);
		}
	}
	
	private class Hit
	{
		int _targetId;
		int _damage;
		int _flags;
		
		Hit(GameObject target, int damage, boolean miss, boolean crit, boolean shld)
		{
			_targetId = target.getObjectId();
			_damage = damage;
			if(_soulshot)
			{
				_flags |= 16 | _grade;
			}
			if(crit)
			{
				_flags |= 32;
			}
			if(shld)
			{
				_flags |= 64;
			}
			if(miss)
			{
				_flags |= 128;
			}
		}
	}
}