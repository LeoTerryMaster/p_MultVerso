package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Player;

import java.util.ArrayList;
import java.util.List;

public class ExEventMatchSpelledInfo extends L2GameServerPacket
{
	private final List<Effect> _effects = new ArrayList<>();
	private int char_obj_id;
	
	public void addEffect(int skillId, int dat, int duration)
	{
		_effects.add(new Effect(skillId, dat, duration));
	}
	
	public void addSpellRecivedPlayer(Player cha)
	{
		if(cha != null)
		{
			char_obj_id = cha.getObjectId();
		}
	}
	
	@Override
	protected void writeImpl()
	{
		writeEx(4);
		writeD(char_obj_id);
		writeD(_effects.size());
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