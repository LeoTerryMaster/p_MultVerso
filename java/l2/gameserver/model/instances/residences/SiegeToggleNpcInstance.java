package l2.gameserver.model.instances.residences;

import l2.gameserver.data.xml.holder.NpcHolder;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.Spawner;
import l2.gameserver.model.entity.events.impl.SiegeEvent;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.pledge.Clan;
import l2.gameserver.templates.npc.NpcTemplate;

import java.util.Set;

public abstract class SiegeToggleNpcInstance extends NpcInstance
{
	private NpcInstance _fakeInstance;
	private int _maxHp;
	
	public SiegeToggleNpcInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		setHasChatWindow(false);
	}
	
	public void setZoneList(Set<String> set)
	{
	}
	
	public void register(Spawner spawn)
	{
	}
	
	public void initFake(int fakeNpcId)
	{
		_fakeInstance = NpcHolder.getInstance().getTemplate(fakeNpcId).getNewInstance();
		_fakeInstance.setCurrentHpMp(1.0, _fakeInstance.getMaxMp());
		_fakeInstance.setHasChatWindow(false);
	}
	
	public abstract void onDeathImpl(Creature killer);
	
	@Override
	protected void onReduceCurrentHp(double damage, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp)
	{
		setCurrentHp(Math.max(getCurrentHp() - damage, 0.0), false);
		if(getCurrentHp() < 0.5)
		{
			doDie(attacker);
			onDeathImpl(attacker);
			decayMe();
			_fakeInstance.spawnMe(getLoc());
		}
	}
	
	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		if(attacker == null)
		{
			return false;
		}
		Player player = attacker.getPlayer();
		if(player == null)
		{
			return false;
		}
		SiegeEvent siegeEvent = getEvent(SiegeEvent.class);
		return siegeEvent != null && siegeEvent.isInProgress();
	}
	
	@Override
	public boolean isAttackable(Creature attacker)
	{
		return isAutoAttackable(attacker);
	}
	
	@Override
	public boolean isInvul()
	{
		return false;
	}
	
	@Override
	public boolean hasRandomAnimation()
	{
		return false;
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
	
	public void decayFake()
	{
		_fakeInstance.decayMe();
	}
	
	@Override
	public int getMaxHp()
	{
		return _maxHp;
	}
	
	public void setMaxHp(int maxHp)
	{
		_maxHp = maxHp;
	}
	
	@Override
	protected void onDecay()
	{
		decayMe();
		_spawnAnimation = 2;
	}
	
	@Override
	public Clan getClan()
	{
		return null;
	}
}