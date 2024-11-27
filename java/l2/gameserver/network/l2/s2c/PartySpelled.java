package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Playable;

import java.util.ArrayList;
import java.util.List;

public class PartySpelled extends L2GameServerPacket
{
	private final int _type;
	private final int _objId;
	private final List<Effect> _effects;
	
	public PartySpelled(Playable activeChar, boolean full)
	{
		_objId = activeChar.getObjectId();
		_type = activeChar.isPet() ? 1 : activeChar.isSummon() ? 2 : 0;
		_effects = new ArrayList<>();
		if(full)
		{
			l2.gameserver.model.Effect[] effects = activeChar.getEffectList().getAllFirstEffects();
			for(l2.gameserver.model.Effect.EEffectSlot ees : l2.gameserver.model.Effect.EEffectSlot.VALUES)
			{
				for(l2.gameserver.model.Effect effect : effects)
				{
					if(effect == null || !effect.isInUse() || effect.getEffectSlot() != ees)
						continue;
					effect.addPartySpelledIcon(this);
				}
			}
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(238);
		writeD(_type);
		writeD(_objId);
		writeD(_effects.size());
		for(Effect temp : _effects)
		{
			writeD(temp._skillId);
			writeH(temp._level);
			writeD(temp._duration);
		}
	}
	
	public void addPartySpelledEffect(int skillId, int level, int duration)
	{
		_effects.add(new Effect(skillId, level, duration));
	}
	
	static class Effect
	{
		final int _skillId;
		final int _level;
		final int _duration;
		
		public Effect(int skillId, int level, int duration)
		{
			_skillId = skillId;
			_level = level;
			_duration = duration;
		}
	}
}