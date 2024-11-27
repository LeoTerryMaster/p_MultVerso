package l2.gameserver.model.instances;

import l2.commons.lang.reference.HardReference;
import l2.commons.threading.RunnableImpl;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.model.Creature;
import l2.gameserver.model.GameObjectTasks;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.network.l2.s2c.L2GameServerPacket;
import l2.gameserver.network.l2.s2c.MyTargetSelected;
import l2.gameserver.network.l2.s2c.NpcInfo;
import l2.gameserver.taskmanager.EffectTaskManager;
import l2.gameserver.templates.npc.NpcTemplate;
import l2.gameserver.utils.Location;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

public final class TrapInstance extends NpcInstance
{
	private final HardReference<? extends Creature> _ownerRef;
	private final Skill _skill;
	private ScheduledFuture<?> _targetTask;
	private ScheduledFuture<?> _destroyTask;
	private boolean _detected;
	
	public TrapInstance(int objectId, NpcTemplate template, Creature owner, Skill skill)
	{
		this(objectId, template, owner, skill, owner.getLoc());
	}
	
	public TrapInstance(int objectId, NpcTemplate template, Creature owner, Skill skill, Location loc)
	{
		super(objectId, template);
		_ownerRef = owner.getRef();
		_skill = skill;
		setReflection(owner.getReflection());
		setLevel(owner.getLevel());
		setTitle(owner.getName());
		setLoc(loc);
	}
	
	@Override
	public boolean isTrap()
	{
		return true;
	}
	
	public Creature getOwner()
	{
		return _ownerRef.get();
	}
	
	@Override
	protected void onSpawn()
	{
		super.onSpawn();
		_destroyTask = ThreadPoolManager.getInstance().schedule(new GameObjectTasks.DeleteTask(this), 120000);
		_targetTask = EffectTaskManager.getInstance().scheduleAtFixedRate(new CastTask(this), 250, 250);
	}
	
	@Override
	public void broadcastCharInfo()
	{
		if(!isDetected())
		{
			return;
		}
		super.broadcastCharInfo();
	}
	
	@Override
	protected void onDelete()
	{
		Creature owner = getOwner();
		if(owner != null && owner.isPlayer())
		{
			((Player) owner).removeTrap(this);
		}
		if(_destroyTask != null)
		{
			_destroyTask.cancel(false);
		}
		_destroyTask = null;
		if(_targetTask != null)
		{
			_targetTask.cancel(false);
		}
		_targetTask = null;
		super.onDelete();
	}
	
	public boolean isDetected()
	{
		return _detected;
	}
	
	public void setDetected(boolean detected)
	{
		_detected = detected;
	}
	
	@Override
	public int getPAtk(Creature target)
	{
		Creature owner = getOwner();
		return owner == null ? 0 : owner.getPAtk(target);
	}
	
	@Override
	public int getMAtk(Creature target, Skill skill)
	{
		Creature owner = getOwner();
		return owner == null ? 0 : owner.getMAtk(target, skill);
	}
	
	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}
	
	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return false;
	}
	
	@Override
	public boolean isAttackable(Creature attacker)
	{
		return false;
	}
	
	@Override
	public boolean isInvul()
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
	public void showChatWindow(Player player, int val, Object... arg)
	{
	}
	
	@Override
	public void showChatWindow(Player player, String filename, Object... replace)
	{
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
	}
	
	@Override
	public void onAction(Player player, boolean shift)
	{
		if(player.getTarget() != this)
		{
			player.setTarget(this);
			if(player.getTarget() == this)
			{
				player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel()));
			}
		}
		player.sendActionFailed();
	}
	
	@Override
	public List<L2GameServerPacket> addPacketList(Player forPlayer, Creature dropper)
	{
		if(!isDetected() && getOwner() != forPlayer)
		{
			return Collections.emptyList();
		}
		return Collections.singletonList(new NpcInfo(this, forPlayer));
	}
	
	private static class CastTask extends RunnableImpl
	{
		private final HardReference<NpcInstance> _trapRef;
		
		public CastTask(TrapInstance trap)
		{
			_trapRef = trap.getRef();
		}
		
		@Override
		public void runImpl() throws Exception
		{
			TrapInstance trap = (TrapInstance) _trapRef.get();
			if(trap == null)
			{
				return;
			}
			Creature owner = trap.getOwner();
			if(owner == null)
			{
				return;
			}
			for(Creature target : trap.getAroundCharacters(200, 200))
			{
				if(target == owner || trap._skill.checkTarget(owner, target, null, false, false) != null)
					continue;
				ArrayList<Creature> targets = new ArrayList<>();
				if(trap._skill.getTargetType() != Skill.SkillTargetType.TARGET_AREA)
				{
					targets.add(target);
				}
				else
				{
					for(Creature t : trap.getAroundCharacters(trap._skill.getSkillRadius(), 128))
					{
						if(trap._skill.checkTarget(owner, t, null, false, false) != null)
							continue;
						targets.add(target);
					}
				}
				trap._skill.useSkill(trap, targets);
				if(target.isPlayer())
				{
					target.sendMessage(new CustomMessage("common.Trap", target.getPlayer()));
				}
				trap.deleteMe();
				break;
			}
		}
	}
}