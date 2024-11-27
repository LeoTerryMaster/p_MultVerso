package l2.gameserver.model.instances.residences;

import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.entity.events.objects.SiegeClanObject;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.pledge.Clan;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.templates.npc.NpcTemplate;

public class SiegeFlagInstance extends NpcInstance
{
	private SiegeClanObject _owner;
	private long _lastAnnouncedAttackedTime;
	
	public SiegeFlagInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		setHasChatWindow(false);
	}
	
	@Override
	public String getName()
	{
		return _owner.getClan().getName();
	}
	
	@Override
	public Clan getClan()
	{
		return _owner.getClan();
	}
	
	public void setClan(SiegeClanObject owner)
	{
		_owner = owner;
	}
	
	@Override
	public String getTitle()
	{
		return "";
	}
	
	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		Player player = attacker.getPlayer();
		if(player == null || isInvul())
		{
			return false;
		}
		Clan clan = player.getClan();
		return clan == null || _owner.getClan() != clan;
	}
	
	@Override
	public boolean isAttackable(Creature attacker)
	{
		return true;
	}
	
	@Override
	protected void onDeath(Creature killer)
	{
		_owner.setFlag(null);
		super.onDeath(killer);
	}
	
	@Override
	protected void onReduceCurrentHp(double damage, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp)
	{
		if(System.currentTimeMillis() - _lastAnnouncedAttackedTime > 120000)
		{
			_lastAnnouncedAttackedTime = System.currentTimeMillis();
			_owner.getClan().broadcastToOnlineMembers(SystemMsg.YOUR_BASE_IS_BEING_ATTACKED);
		}
		super.onReduceCurrentHp(damage, attacker, skill, awake, standUp, directHp);
	}
	
	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}
	
	@Override
	public boolean isInvul()
	{
		return _isInvul;
	}
	
	@Override
	public boolean isFearImmune()
	{
		return true;
	}
	
	@Override
	public boolean isParalyzeImmune()
	{
		return true;
	}
	
	@Override
	public boolean isLethalImmune()
	{
		return true;
	}
	
	@Override
	public boolean isHealBlocked()
	{
		return true;
	}
	
	@Override
	public boolean isEffectImmune()
	{
		return true;
	}
}