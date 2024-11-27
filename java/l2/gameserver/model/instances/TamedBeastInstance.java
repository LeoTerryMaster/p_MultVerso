package l2.gameserver.model.instances;

import l2.commons.lang.reference.HardReference;
import l2.commons.lang.reference.HardReferences;
import l2.commons.threading.RunnableImpl;
import l2.commons.util.Rnd;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.ai.CtrlIntention;
import l2.gameserver.data.StringHolder;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.World;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.network.l2.s2c.NpcInfo;
import l2.gameserver.tables.SkillTable;
import l2.gameserver.templates.npc.NpcTemplate;

import java.util.concurrent.Future;

public final class TamedBeastInstance extends FeedableBeastInstance
{
	private static final int MAX_DISTANCE_FROM_OWNER = 2000;
	private static final int MAX_DISTANCE_FOR_BUFF = 200;
	private static final int MAX_DURATION = 1200000;
	private static final int DURATION_CHECK_INTERVAL = 60000;
	private static final int DURATION_INCREASE_INTERVAL = 20000;
	private static final int BUFF_INTERVAL = 60000;
	private static final Skill[][] TAMED_SKILLS = new Skill[7][];
	
	static
	{
		TAMED_SKILLS[0] = new Skill[] {SkillTable.getInstance().getInfo(1044, 1), SkillTable.getInstance().getInfo(1044, 1), SkillTable.getInstance().getInfo(1044, 1), SkillTable.getInstance().getInfo(1045, 1), SkillTable.getInstance().getInfo(1086, 1), SkillTable.getInstance().getInfo(1217, 1), SkillTable.getInstance().getInfo(1240, 1), SkillTable.getInstance().getInfo(1268, 1)};
		TAMED_SKILLS[1] = new Skill[] {SkillTable.getInstance().getInfo(1013, 1), SkillTable.getInstance().getInfo(1048, 1), SkillTable.getInstance().getInfo(1059, 1), SkillTable.getInstance().getInfo(1078, 1), SkillTable.getInstance().getInfo(1085, 1), SkillTable.getInstance().getInfo(1160, 1), SkillTable.getInstance().getInfo(1204, 1)};
		TAMED_SKILLS[2] = new Skill[] {SkillTable.getInstance().getInfo(1044, 1), SkillTable.getInstance().getInfo(1045, 1), SkillTable.getInstance().getInfo(1086, 1), SkillTable.getInstance().getInfo(1217, 1), SkillTable.getInstance().getInfo(1240, 1)};
		TAMED_SKILLS[3] = new Skill[] {SkillTable.getInstance().getInfo(1013, 1), SkillTable.getInstance().getInfo(1048, 1), SkillTable.getInstance().getInfo(1059, 1), SkillTable.getInstance().getInfo(1078, 1), SkillTable.getInstance().getInfo(1085, 1), SkillTable.getInstance().getInfo(1204, 1)};
		TAMED_SKILLS[4] = new Skill[] {SkillTable.getInstance().getInfo(1044, 1), SkillTable.getInstance().getInfo(1045, 1), SkillTable.getInstance().getInfo(1086, 1), SkillTable.getInstance().getInfo(1217, 1), SkillTable.getInstance().getInfo(1240, 1), SkillTable.getInstance().getInfo(1268, 1)};
		TAMED_SKILLS[5] = new Skill[] {SkillTable.getInstance().getInfo(1013, 1), SkillTable.getInstance().getInfo(1048, 1), SkillTable.getInstance().getInfo(1059, 1), SkillTable.getInstance().getInfo(1078, 1), SkillTable.getInstance().getInfo(1085, 1), SkillTable.getInstance().getInfo(1204, 1)};
		TAMED_SKILLS[6] = new Skill[] {SkillTable.getInstance().getInfo(1013, 1), SkillTable.getInstance().getInfo(1048, 1), SkillTable.getInstance().getInfo(1059, 1), SkillTable.getInstance().getInfo(1078, 1), SkillTable.getInstance().getInfo(1085, 1), SkillTable.getInstance().getInfo(1204, 1)};
	}
	
	private HardReference<Player> _playerRef = HardReferences.emptyRef();
	private int _foodSkillId;
	private int _remainingTime = 1200000;
	private Future<?> _durationCheckTask;
	private Future<?> _buffTask;
	private Skill[] _skills = new Skill[0];
	
	public TamedBeastInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		_hasRandomWalk = false;
	}
	
	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return false;
	}
	
	private void onReceiveFood()
	{
		_remainingTime += 20000;
		if(_remainingTime > 1200000)
		{
			_remainingTime = 1200000;
		}
	}
	
	public int getRemainingTime()
	{
		return _remainingTime;
	}
	
	public void setRemainingTime(int duration)
	{
		_remainingTime = duration;
	}
	
	public int getFoodType()
	{
		return _foodSkillId;
	}
	
	public void setFoodType(int foodItemId)
	{
		if(foodItemId > 0)
		{
			_foodSkillId = foodItemId;
			if(_durationCheckTask != null)
			{
				_durationCheckTask.cancel(false);
			}
			_durationCheckTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new CheckDuration(this), 60000, 60000);
		}
	}
	
	public void setTameType(Player activeChar)
	{
		switch(getNpcId())
		{
			case 16013:
			{
				setName(StringHolder.getInstance().getNotNull(activeChar, "TamedBeastInstance.NpcString.ALPEN_BUFFALO"));
				break;
			}
			case 16014:
			{
				setName(StringHolder.getInstance().getNotNull(activeChar, "TamedBeastInstance.NpcString.ALPEN_BUFFALO"));
				break;
			}
			case 16015:
			{
				setName(StringHolder.getInstance().getNotNull(activeChar, "TamedBeastInstance.NpcString.ALPEN_COUGAR"));
				break;
			}
			case 16016:
			{
				setName(StringHolder.getInstance().getNotNull(activeChar, "TamedBeastInstance.NpcString.ALPEN_COUGAR"));
				break;
			}
			case 16017:
			{
				setName(StringHolder.getInstance().getNotNull(activeChar, "TamedBeastInstance.NpcString.ALPEN_KOOKABURRA"));
				break;
			}
			case 16018:
			{
				setName(StringHolder.getInstance().getNotNull(activeChar, "TamedBeastInstance.NpcString.ALPEN_KOOKABURRA"));
				break;
			}
			case 16019:
			{
				setName(StringHolder.getInstance().getNotNull(activeChar, "TamedBeastInstance.NpcString.ALPEN_GRENDEL"));
				break;
			}
			case 16020:
			{
				setName(StringHolder.getInstance().getNotNull(activeChar, "TamedBeastInstance.NpcString.ALPEN_GRENDEL"));
				break;
			}
			default:
			{
				setName("");
			}
		}
		Skill[] skills = TAMED_SKILLS[Rnd.get(TAMED_SKILLS.length)];
		_skills = skills.clone();
	}
	
	public void buffOwner()
	{
		if(!isInRange(getPlayer(), 200))
		{
			getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, getPlayer());
			return;
		}
		int delay = 0;
		for(Skill skill : _skills)
		{
			if(getPlayer().getEffectList().getEffectsCountForSkill(skill.getId()) > 0)
				continue;
			ThreadPoolManager.getInstance().schedule(new Buff(this, getPlayer(), skill), delay);
			delay = delay + skill.getHitTime() + 500;
		}
	}
	
	@Override
	protected void onDeath(Creature killer)
	{
		super.onDeath(killer);
		if(_durationCheckTask != null)
		{
			_durationCheckTask.cancel(false);
			_durationCheckTask = null;
		}
		if(_buffTask != null)
		{
			_buffTask.cancel(false);
			_buffTask = null;
		}
		Player owner;
		if((owner = getPlayer()) != null && owner.getTrainedBeast() == this)
		{
			owner.setTrainedBeast(null);
		}
		_foodSkillId = 0;
		_remainingTime = 0;
	}
	
	@Override
	public Player getPlayer()
	{
		return _playerRef.get();
	}
	
	public void setOwner(Player owner)
	{
		HardReference hardReference = _playerRef = owner == null ? HardReferences.emptyRef() : owner.getRef();
		if(owner != null)
		{
			setTitle(owner.getName());
			if(owner.getTrainedBeast() != null)
			{
				owner.getTrainedBeast().doDespawn();
			}
			owner.setTrainedBeast(this);
			for(Player player : World.getAroundPlayers(this))
			{
				player.sendPacket(new NpcInfo(this, player));
			}
			getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, owner);
			_buffTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new RunnableImpl()
			{
				
				@Override
				public void runImpl() throws Exception
				{
					buffOwner();
				}
			}, 60000, 60000);
		}
		else
		{
			doDespawn();
		}
	}
	
	public void despawnWithDelay(int delay)
	{
		ThreadPoolManager.getInstance().schedule(new RunnableImpl()
		{
			
			@Override
			public void runImpl() throws Exception
			{
				doDespawn();
			}
		}, delay);
	}
	
	public void doDespawn()
	{
		stopMove();
		if(_durationCheckTask != null)
		{
			_durationCheckTask.cancel(false);
			_durationCheckTask = null;
		}
		if(_buffTask != null)
		{
			_buffTask.cancel(false);
			_buffTask = null;
		}
		Player owner;
		if((owner = getPlayer()) != null && owner.getTrainedBeast() == this)
		{
			owner.setTrainedBeast(null);
		}
		setTarget(null);
		_foodSkillId = 0;
		_remainingTime = 0;
		onDecay();
	}
	
	private static class CheckDuration extends RunnableImpl
	{
		private final TamedBeastInstance _tamedBeast;
		
		CheckDuration(TamedBeastInstance tamedBeast)
		{
			_tamedBeast = tamedBeast;
		}
		
		@Override
		public void runImpl() throws Exception
		{
			Player owner = _tamedBeast.getPlayer();
			if(owner == null || !owner.isOnline())
			{
				_tamedBeast.doDespawn();
				return;
			}
			if(_tamedBeast.getDistance(owner) > 2000.0)
			{
				_tamedBeast.doDespawn();
				return;
			}
			int foodTypeSkillId = _tamedBeast.getFoodType();
			_tamedBeast.setRemainingTime(_tamedBeast.getRemainingTime() - 60000);
			ItemInstance item = null;
			int foodItemId = _tamedBeast.getItemIdBySkillId(foodTypeSkillId);
			if(foodItemId > 0)
			{
				item = owner.getInventory().getItemByItemId(foodItemId);
			}
			if(item != null && item.getCount() >= 1)
			{
				_tamedBeast.onReceiveFood();
				owner.getInventory().destroyItem(item, 1);
			}
			else if(_tamedBeast.getRemainingTime() < 900000)
			{
				_tamedBeast.setRemainingTime(-1);
			}
			if(_tamedBeast.getRemainingTime() <= 0)
			{
				_tamedBeast.doDespawn();
			}
		}
	}
	
	public static class Buff extends RunnableImpl
	{
		private final NpcInstance _actor;
		private final Player _owner;
		private final Skill _skill;
		
		public Buff(NpcInstance actor, Player owner, Skill skill)
		{
			_actor = actor;
			_owner = owner;
			_skill = skill;
		}
		
		@Override
		public void runImpl() throws Exception
		{
			if(_actor != null)
			{
				_actor.doCast(_skill, _owner, true);
			}
		}
	}
}