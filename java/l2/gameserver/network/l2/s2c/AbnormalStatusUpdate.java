package l2.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.List;

public class AbnormalStatusUpdate extends L2GameServerPacket
{
	public static final int INFINITIVE_EFFECT = -1;
	private final List<Effect> _effects = new ArrayList<>();
	
	public void addEffect(int skillId, int dat, int duration)
	{
		_effects.add(new Effect(skillId, dat, duration));
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(127);
		writeH(_effects.size());
		for(Effect temp : _effects)
		{
			writeD(temp.skillId);
			writeH(temp.dat);
			writeD(temp.duration);
		}
	}
	
	class Effect
	{
		int skillId;
		int dat;
		int duration;
		
		public Effect(int skillId, int dat, int duration)
		{
			this.skillId = skillId;
			this.dat = dat;
			this.duration = duration;
		}
	}
}