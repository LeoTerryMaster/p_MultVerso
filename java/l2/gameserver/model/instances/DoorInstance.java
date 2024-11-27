package l2.gameserver.model.instances;

import l2.commons.geometry.Shape;
import l2.commons.listener.Listener;
import l2.commons.threading.RunnableImpl;
import l2.commons.util.Rnd;
import l2.gameserver.Config;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.ai.CtrlIntention;
import l2.gameserver.ai.DoorAI;
import l2.gameserver.geodata.GeoCollision;
import l2.gameserver.geodata.GeoEngine;
import l2.gameserver.listener.actor.door.OnOpenCloseListener;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.World;
import l2.gameserver.model.entity.SevenSigns;
import l2.gameserver.model.entity.events.impl.SiegeEvent;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.network.l2.s2c.DoorInfo;
import l2.gameserver.network.l2.s2c.DoorStatusUpdate;
import l2.gameserver.network.l2.s2c.L2GameServerPacket;
import l2.gameserver.network.l2.s2c.MyTargetSelected;
import l2.gameserver.network.l2.s2c.ValidateLocation;
import l2.gameserver.scripts.Events;
import l2.gameserver.templates.DoorTemplate;
import l2.gameserver.templates.item.WeaponTemplate;
import l2.gameserver.utils.Location;
import l2.gameserver.utils.Log;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class DoorInstance extends Creature implements GeoCollision
{
	private final Lock _openLock = new ReentrantLock();
	protected ScheduledFuture<?> _autoActionTask;
	private boolean _open = true;
	private boolean _geoOpen = true;
	private int _upgradeHp;
	private byte[][] _geoAround;
	
	public DoorInstance(int objectId, DoorTemplate template)
	{
		super(objectId, template);
	}
	
	public boolean isUnlockable()
	{
		return getTemplate().isUnlockable();
	}
	
	@Override
	public String getName()
	{
		return getTemplate().getName();
	}
	
	@Override
	public int getLevel()
	{
		return 1;
	}
	
	public int getDoorId()
	{
		return getTemplate().getNpcId();
	}
	
	public boolean isOpen()
	{
		return _open;
	}
	
	protected boolean setOpen(boolean open)
	{
		if(_open == open)
		{
			return false;
		}
		_open = open;
		return true;
	}
	
	public void scheduleAutoAction(boolean open, long actionDelay)
	{
		if(_autoActionTask != null)
		{
			_autoActionTask.cancel(false);
			_autoActionTask = null;
		}
		_autoActionTask = ThreadPoolManager.getInstance().schedule(new AutoOpenClose(open), actionDelay);
	}
	
	public int getDamage()
	{
		int dmg = 6 - (int) Math.ceil(getCurrentHpRatio() * 6.0);
		return Math.max(0, Math.min(6, dmg));
	}
	
	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return isAttackable(attacker);
	}
	
	@Override
	public boolean isAttackable(Creature attacker)
	{
		if(attacker == null || isOpen())
		{
			return false;
		}
		SiegeEvent siegeEvent = getEvent(SiegeEvent.class);
		return !isInvul();
	}
	
	@Override
	public void sendChanges()
	{
	}
	
	@Override
	public ItemInstance getActiveWeaponInstance()
	{
		return null;
	}
	
	@Override
	public WeaponTemplate getActiveWeaponItem()
	{
		return null;
	}
	
	@Override
	public ItemInstance getSecondaryWeaponInstance()
	{
		return null;
	}
	
	@Override
	public WeaponTemplate getSecondaryWeaponItem()
	{
		return null;
	}
	
	public Location getCenterPoint()
	{
		Shape shape = getShape();
		return new Location(shape.getXmin() + (shape.getXmax() - shape.getXmin() >> 1), shape.getYmin() + (shape.getYmax() - shape.getYmin() >> 1), shape.getZmin() + (shape.getZmax() - shape.getZmin() >> 1));
	}
	
	@Override
	public void onAction(Player player, boolean shift)
	{
		if(Events.onAction(player, this, shift))
		{
			return;
		}
		if(this != player.getTarget())
		{
			player.setTarget(this);
			player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel()));
			if(isAutoAttackable(player))
			{
				player.sendPacket(new DoorInfo(this, player));
			}
			player.sendPacket(new ValidateLocation(this));
		}
		else
		{
			player.sendPacket(new MyTargetSelected(getObjectId(), 0));
			if(isAutoAttackable(player))
			{
				player.getAI().Attack(this, false, shift);
				return;
			}
			if(!isInActingRange(player))
			{
				if(!player.getAI().isIntendingInteract(this))
				{
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
				}
				return;
			}
			getAI().onEvtTwiceClick(player);
		}
	}
	
	@Override
	public int getActingRange()
	{
		return 150;
	}
	
	@Override
	public DoorAI getAI()
	{
		if(_ai == null)
		{
			DoorInstance doorInstance = this;
			synchronized(doorInstance)
			{
				if(_ai == null)
				{
					_ai = getTemplate().getNewAI(this);
				}
			}
		}
		return (DoorAI) _ai;
	}
	
	@Override
	public void broadcastStatusUpdate()
	{
		for(Player player : World.getAroundPlayers(this))
		{
			if(player == null)
				continue;
			player.sendPacket(new DoorStatusUpdate(this, player));
		}
	}
	
	public boolean openMe()
	{
		return openMe(null, true);
	}
	
	public boolean openMe(Player opener, boolean autoClose)
	{
		_openLock.lock();
		try
		{
			if(!setOpen(true))
			{
				boolean bl = false;
				return bl;
			}
			setGeoOpen(true);
		}
		finally
		{
			_openLock.unlock();
		}
		broadcastStatusUpdate();
		if(autoClose && getTemplate().getCloseTime() > 0)
		{
			scheduleAutoAction(false, (long) getTemplate().getCloseTime() * 1000);
		}
		getAI().onEvtOpen(opener);
		for(Listener l : getListeners().getListeners())
		{
			if(!(l instanceof OnOpenCloseListener))
				continue;
			((OnOpenCloseListener) l).onOpen(this);
		}
		return true;
	}
	
	public boolean closeMe()
	{
		return closeMe(null, true);
	}
	
	public boolean closeMe(Player closer, boolean autoOpen)
	{
		if(isDead())
		{
			return false;
		}
		_openLock.lock();
		try
		{
			if(!setOpen(false))
			{
				boolean bl = false;
				return bl;
			}
			setGeoOpen(false);
		}
		finally
		{
			_openLock.unlock();
		}
		broadcastStatusUpdate();
		if(autoOpen && getTemplate().getOpenTime() > 0)
		{
			long openDelay = (long) getTemplate().getOpenTime() * 1000;
			if(getTemplate().getRandomTime() > 0)
			{
				openDelay += (long) Rnd.get(0, getTemplate().getRandomTime()) * 1000;
			}
			scheduleAutoAction(true, openDelay);
		}
		getAI().onEvtClose(closer);
		for(Listener l : getListeners().getListeners())
		{
			if(!(l instanceof OnOpenCloseListener))
				continue;
			((OnOpenCloseListener) l).onClose(this);
		}
		return true;
	}
	
	@Override
	public String toString()
	{
		return "[Door " + getDoorId() + "]";
	}
	
	@Override
	protected void onDeath(Creature killer)
	{
		_openLock.lock();
		try
		{
			setGeoOpen(true);
		}
		finally
		{
			_openLock.unlock();
		}
		SiegeEvent siegeEvent = getEvent(SiegeEvent.class);
		if(siegeEvent != null && siegeEvent.isInProgress())
		{
			Log.add(toString(), getDoorType() + " destroyed by " + killer + ", " + siegeEvent);
		}
		super.onDeath(killer);
	}
	
	@Override
	protected void onRevive()
	{
		super.onRevive();
		_openLock.lock();
		try
		{
			if(!isOpen())
			{
				setGeoOpen(false);
			}
		}
		finally
		{
			_openLock.unlock();
		}
	}
	
	@Override
	protected void onSpawn()
	{
		super.onSpawn();
		setCurrentHpMp(getMaxHp(), getMaxMp(), true);
		closeMe(null, true);
	}
	
	@Override
	protected void onDespawn()
	{
		if(_autoActionTask != null)
		{
			_autoActionTask.cancel(false);
			_autoActionTask = null;
		}
		super.onDespawn();
	}
	
	public boolean isHPVisible()
	{
		return getTemplate().isHPVisible();
	}
	
	@Override
	public int getMaxHp()
	{
		return super.getMaxHp() + _upgradeHp;
	}
	
	public int getUpgradeHp()
	{
		return _upgradeHp;
	}
	
	public void setUpgradeHp(int hp)
	{
		_upgradeHp = hp;
	}
	
	@Override
	public int getPDef(Creature target)
	{
		switch(SevenSigns.getInstance().getSealOwner(3))
		{
			case 2:
			{
				return (int) ((double) super.getPDef(target) * 1.2);
			}
			case 1:
			{
				return (int) ((double) super.getPDef(target) * 0.3);
			}
		}
		return super.getPDef(target);
	}
	
	@Override
	public int getMDef(Creature target, Skill skill)
	{
		switch(SevenSigns.getInstance().getSealOwner(3))
		{
			case 2:
			{
				return (int) ((double) super.getMDef(target, skill) * 1.2);
			}
			case 1:
			{
				return (int) ((double) super.getMDef(target, skill) * 0.3);
			}
		}
		return super.getMDef(target, skill);
	}
	
	@Override
	public boolean isInvul()
	{
		if(!getTemplate().isHPVisible())
		{
			return true;
		}
		SiegeEvent siegeEvent = getEvent(SiegeEvent.class);
		if(siegeEvent != null && siegeEvent.isInProgress())
		{
			return false;
		}
		return super.isInvul();
	}
	
	protected boolean setGeoOpen(boolean open)
	{
		if(_geoOpen == open)
		{
			return false;
		}
		_geoOpen = open;
		if(Config.ALLOW_GEODATA)
		{
			if(open)
			{
				GeoEngine.removeGeoCollision(this, getGeoIndex());
			}
			else
			{
				GeoEngine.applyGeoCollision(this, getGeoIndex());
			}
		}
		return true;
	}
	
	@Override
	public boolean isMovementDisabled()
	{
		return true;
	}
	
	@Override
	public boolean isActionsDisabled()
	{
		return true;
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
	public boolean isConcrete()
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
	
	@Override
	public List<L2GameServerPacket> addPacketList(Player forPlayer, Creature dropper)
	{
		return Arrays.asList(new DoorInfo(this, forPlayer), new DoorStatusUpdate(this, forPlayer));
	}
	
	@Override
	public boolean isDoor()
	{
		return true;
	}
	
	@Override
	public Shape getShape()
	{
		return getTemplate().getPolygon();
	}
	
	@Override
	public byte[][] getGeoAround()
	{
		return _geoAround;
	}
	
	@Override
	public void setGeoAround(byte[][] geo)
	{
		_geoAround = geo;
	}
	
	@Override
	public DoorTemplate getTemplate()
	{
		return (DoorTemplate) super.getTemplate();
	}
	
	public DoorTemplate.DoorType getDoorType()
	{
		return getTemplate().getDoorType();
	}
	
	public int getKey()
	{
		return getTemplate().getKey();
	}
	
	private class AutoOpenClose extends RunnableImpl
	{
		private final boolean _open;
		
		public AutoOpenClose(boolean open)
		{
			_open = open;
		}
		
		@Override
		public void runImpl() throws Exception
		{
			if(_open)
			{
				openMe(null, true);
			}
			else
			{
				closeMe(null, true);
			}
		}
	}
}