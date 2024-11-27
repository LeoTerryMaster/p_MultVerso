package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.base.RestartType;
import l2.gameserver.model.entity.events.GlobalEvent;
import l2.gameserver.model.instances.MonsterInstance;
import l2.gameserver.model.pledge.Clan;

import java.util.HashMap;
import java.util.Map;

public class Die extends L2GameServerPacket
{
	private final int _objectId;
	private final boolean _fake;
	private final Map<RestartType, Boolean> _types = new HashMap<>(RestartType.VALUES.length);
	private boolean _sweepable;
	
	public Die(Creature cha)
	{
		_objectId = cha.getObjectId();
		boolean bl = _fake = !cha.isDead();
		Player player;
		if(cha.isMonster())
		{
			_sweepable = ((MonsterInstance) cha).isSweepActive();
		}
		else if(cha.isPlayer() && !(player = (Player) cha).isOlyCompetitionStarted() && !player.isResurectProhibited())
		{
			put(RestartType.FIXED, player.getPlayerAccess().ResurectFixed || player.getInventory().getCountOf(9218) > 0 && !player.isOnSiegeField());
			put(RestartType.TO_VILLAGE, true);
			Clan clan = null;
			if(get(RestartType.TO_VILLAGE))
			{
				clan = player.getClan();
			}
			if(clan != null)
			{
				put(RestartType.TO_CLANHALL, clan.getHasHideout() > 0);
				put(RestartType.TO_CASTLE, clan.getCastle() > 0);
			}
			for(GlobalEvent e : cha.getEvents())
			{
				e.checkRestartLocs(player, _types);
			}
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		if(_fake)
		{
			return;
		}
		writeC(6);
		writeD(_objectId);
		writeD(get(RestartType.TO_VILLAGE));
		writeD(get(RestartType.TO_CLANHALL));
		writeD(get(RestartType.TO_CASTLE));
		writeD(get(RestartType.TO_FLAG));
		writeD(_sweepable ? 1 : 0);
		writeD(get(RestartType.FIXED));
	}
	
	private void put(RestartType t, boolean b)
	{
		_types.put(t, b);
	}
	
	private boolean get(RestartType t)
	{
		Boolean b = _types.get(t);
		return b != null && b != false;
	}
}