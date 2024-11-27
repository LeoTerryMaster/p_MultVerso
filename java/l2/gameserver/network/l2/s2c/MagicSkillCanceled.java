package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;

public class MagicSkillCanceled extends L2GameServerPacket
{
	private final int _casterX;
	private final int _casterY;
	private final int _casterId;
	
	public MagicSkillCanceled(Creature caster)
	{
		_casterId = caster.getObjectId();
		_casterX = caster.getX();
		_casterY = caster.getY();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(73);
		writeD(_casterId);
	}
	
	@Override
	public L2GameServerPacket packet(Player player)
	{
		if(player != null && !player.isInObserverMode())
		{
			if(player.buffAnimRange() < 0)
			{
				return null;
			}
			if(player.buffAnimRange() == 0)
			{
				return _casterId == player.getObjectId() ? super.packet(player) : null;
			}
			return player.getDistance(_casterX, _casterY) < (double) player.buffAnimRange() ? super.packet(player) : null;
		}
		return super.packet(player);
	}
}