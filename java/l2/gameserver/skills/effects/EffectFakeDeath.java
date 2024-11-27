package l2.gameserver.skills.effects;

import l2.commons.util.Rnd;
import l2.gameserver.ai.CtrlEvent;
import l2.gameserver.cache.Msg;
import l2.gameserver.model.Effect;
import l2.gameserver.model.Player;
import l2.gameserver.network.l2.s2c.ChangeWaitType;
import l2.gameserver.network.l2.s2c.Revive;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.stats.Env;

public final class EffectFakeDeath extends Effect
{
	public static final int FAKE_DEATH_OFF = 0;
	public static final int FAKE_DEATH_ON = 1;
	public static final int FAKE_DEATH_FAILED = 2;
	private final int _failChance;
	
	public EffectFakeDeath(Env env, EffectTemplate template)
	{
		super(env, template);
		_failChance = template.getParam().getInteger("failChance", 0);
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		Player player = (Player) getEffected();
		player.abortAttack(true, false);
		if(player.isMoving())
		{
			player.stopMove();
		}
		if(_failChance > 0 && Rnd.chance(_failChance))
		{
			player.setFakeDeath(2);
		}
		else
		{
			player.setFakeDeath(1);
			player.getAI().notifyEvent(CtrlEvent.EVT_FAKE_DEATH, null, null);
		}
		player.broadcastPacket(new ChangeWaitType(player, 2));
		player.broadcastCharInfo();
	}
	
	@Override
	public void onExit()
	{
		super.onExit();
		Player player = (Player) getEffected();
		player.setNonAggroTime(System.currentTimeMillis() + 5000);
		player.setFakeDeath(0);
		player.broadcastPacket(new ChangeWaitType(player, 3));
		player.broadcastPacket(new Revive(player));
		player.broadcastCharInfo();
	}
	
	@Override
	public boolean onActionTime()
	{
		if(getEffected().isDead())
		{
			return false;
		}
		double manaDam = calc();
		if(manaDam > getEffected().getCurrentMp() && getSkill().isToggle())
		{
			getEffected().sendPacket(Msg.NOT_ENOUGH_MP);
			getEffected().sendPacket(new SystemMessage(749).addSkillName(getSkill().getId(), getSkill().getDisplayLevel()));
			return false;
		}
		getEffected().reduceCurrentMp(manaDam, null);
		return true;
	}
}