package l2.gameserver.model.entity.oly;

import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.network.l2.s2c.L2GameServerPacket;

public abstract class Participant
{
	public static int SIDE_RED = 2;
	public static int SIDE_BLUE = 1;
	private final int _side;
	private final Competition _comp;
	
	protected Participant(int side, Competition comp)
	{
		_side = side;
		_comp = comp;
	}
	
	public final Competition getCompetition()
	{
		return _comp;
	}
	
	public final int getSide()
	{
		return _side;
	}
	
	public abstract void OnStart();
	
	public abstract void OnFinish();
	
	public abstract void OnDamaged(Player player, Creature attacker, double damage, double hp);
	
	public abstract void OnDisconnect(Player player);
	
	public abstract void sendPacket(L2GameServerPacket gsp);
	
	public abstract String getName();
	
	public abstract boolean isAlive();
	
	public abstract boolean isPlayerLoose(Player player);
	
	public abstract double getDamageOf(Player player);
	
	public abstract Player[] getPlayers();
	
	public abstract double getTotalDamage();
	
	public abstract boolean validateThis();
}