package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Player;

import java.util.ArrayList;

public class ExOlympiadSpelledInfo extends L2GameServerPacket
{
	private final ArrayList<Effect> _effects = new ArrayList();
	private int char_obj_id;
	
	public void addEffect(int skillId, int level, int duration)
	{
		_effects.add(new Effect(skillId, level, duration));
	}
	
	public void addSpellRecivedPlayer(Player cha)
	{
		if(cha != null)
		{
			char_obj_id = cha.getObjectId();
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		if(char_obj_id == 0)
		{
			return;
		}
		writeEx(42);
		writeD(char_obj_id);
		writeD(_effects.size());
		for(Effect temp : _effects)
		{
			writeD(temp.skillId);
			writeH(temp.level);
			writeD(temp.duration);
		}
	}
	
	class Effect
	{
		int skillId;
		int level;
		int duration;
		
		public Effect(int skillId, int level, int duration)
		{
			this.skillId = skillId;
			this.level = level;
			this.duration = duration;
		}
	}
}