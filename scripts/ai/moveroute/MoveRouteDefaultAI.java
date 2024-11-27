package ai.moveroute;

import l2.commons.collections.CollectionUtils;
import l2.commons.util.Rnd;
import l2.gameserver.Config;
import l2.gameserver.ai.CtrlIntention;
import l2.gameserver.ai.DefaultAI;
import l2.gameserver.geodata.GeoEngine;
import l2.gameserver.model.Creature;
import l2.gameserver.model.MinionList;
import l2.gameserver.model.World;
import l2.gameserver.model.instances.MinionInstance;
import l2.gameserver.model.instances.MonsterInstance;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.network.l2.s2c.SocialAction;
import l2.gameserver.scripts.Functions;
import l2.gameserver.templates.moveroute.MoveNode;
import l2.gameserver.templates.moveroute.MoveRoute;
import l2.gameserver.utils.Location;
import parsers.MoveRouteHolder;

import java.util.List;

public class MoveRouteDefaultAI extends DefaultAI
{
	private final MoveRoute _moveRoute;
	private MoveNode _targetMoveNode;
	private boolean _goBackCircle;
	private boolean _nodeDelay;
	
	public MoveRouteDefaultAI(NpcInstance actor)
	{
		super(actor);
		String moveRoute = actor.getParameter("moveroute", null);
		_moveRoute = moveRoute == null ? null : MoveRouteHolder.getInstance().getRoute(moveRoute);
	}
	
	@Override
	protected void onEvtSpawn()
	{
		super.onEvtSpawn();
		if(_moveRoute == null)
		{
			return;
		}
		_goBackCircle = false;
		if(_moveRoute.isRunning())
		{
			getActor().setRunning();
		}
		else
		{
			getActor().setWalking();
		}
	}
	
	@Override
	protected Location getPursueBaseLoc()
	{
		return _targetMoveNode != null ? _targetMoveNode : super.getPursueBaseLoc();
	}
	
	@Override
	protected boolean thinkActive()
	{
		if(_moveRoute == null)
		{
			return super.thinkActive();
		}
		NpcInstance actor = getActor();
		if(actor.isActionsDisabled())
		{
			return true;
		}
		if(_randomAnimationEnd > System.currentTimeMillis())
		{
			return true;
		}
		if(_def_think)
		{
			if(doTask())
			{
				clearTasks();
			}
			return true;
		}
		long now = System.currentTimeMillis();
		if(now - _checkAggroTimestamp > (long) Config.AGGRO_CHECK_INTERVAL)
		{
			_checkAggroTimestamp = now;
			boolean aggressive = Rnd.chance(actor.getParameter("SelfAggressive", actor.isAggressive() ? 100 : 0));
			if(!actor.getAggroList().isEmpty() || aggressive)
			{
				List<Creature> chars = World.getAroundCharacters(actor);
				CollectionUtils.eqSort(chars, _nearestTargetComparator);
				for(Creature target : chars)
				{
					if(!aggressive && actor.getAggroList().get(target) == null || !checkAggression(target))
						continue;
					actor.getAggroList().addDamageHate(target, 0, 2);
					if(target.isSummon())
					{
						actor.getAggroList().addDamageHate(target.getPlayer(), 0, 1);
					}
					startRunningTask(AI_TASK_ATTACK_DELAY);
					setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
					return true;
				}
			}
		}
		MonsterInstance leader;
		if(actor.isMinion() && (leader = ((MinionInstance) actor).getLeader()) != null)
		{
			double distance = actor.getDistance(leader.getX(), leader.getY());
			if(distance > 1000.0)
			{
				actor.teleToLocation(leader.getMinionPosition());
			}
			else if(distance > 200.0)
			{
				addTaskMove(leader.getMinionPosition(), false);
			}
			return true;
		}
		if(randomAnimation())
		{
			return true;
		}
		if(randomWalk())
		{
			return true;
		}
		if(!actor.isMoving() && !actor.isFollowing())
		{
			Location currLoc = actor.getLoc();
			int nearestMoveNodeIdx = getNearestMoveNodeIdx();
			MoveNode nearestMoveNode = _moveRoute.getNodes().get(nearestMoveNodeIdx);
			double nearestToCurrDist = nearestMoveNode.distance3D(currLoc);
			if(nearestToCurrDist > Math.max(64.0, 4.0 * actor.getCollisionRadius()))
			{
				_targetMoveNode = nearestMoveNode;
				returnHome(true, !GeoEngine.canMoveToCoord(currLoc.getX(), currLoc.getY(), currLoc.getZ(), nearestMoveNode.getX(), nearestMoveNode.getY(), nearestMoveNode.getZ(), actor.getGeoIndex()));
			}
			else
			{
				MoveNode currNode = nearestMoveNode;
				if(currNode.getSocialId() > 0)
				{
					actor.broadcastPacketToOthers(new SocialAction(actor.getObjectId(), currNode.getSocialId()));
				}
				if(currNode.getNpcMsgAddress() != null)
				{
					Functions.npcSayCustomMessage(actor, currNode.getChatType(), currNode.getNpcMsgAddress(), (Object[]) new Object[0]);
				}
				if(!_nodeDelay && currNode.getDelay() > 0)
				{
					_nodeDelay = true;
					setIsInRandomAnimation(currNode.getDelay());
					return true;
				}
				_nodeDelay = false;
				if(_moveRoute.isRunning())
				{
					actor.setRunning();
				}
				else
				{
					actor.setWalking();
				}
				int currNodeIdx = nearestMoveNodeIdx;
				int nextNodeIdx = getNextMoveNodeIdx(currNodeIdx);
				MoveNode nextNode;
				_targetMoveNode = nextNode = _moveRoute.getNodes().get(nextNodeIdx);
				addTaskMove(nextNode, true);
				if(actor.hasMinions())
				{
					attendMinions();
				}
				return doTask();
			}
		}
		return false;
	}
	
	private int getNextMoveNodeIdx(int currNodeIdx)
	{
		switch(_moveRoute.getType())
		{
			case LOOP:
			{
				return currNodeIdx + 1 < _moveRoute.getNodes().size() ? currNodeIdx + 1 : 0;
			}
			case CIRCLE:
			{
				if(!_goBackCircle)
				{
					if(currNodeIdx + 1 < _moveRoute.getNodes().size())
					{
						return currNodeIdx + 1;
					}
					_goBackCircle = true;
					return currNodeIdx - 1;
				}
				if(currNodeIdx - 1 > 0)
				{
					return currNodeIdx - 1;
				}
				_goBackCircle = false;
				return currNodeIdx + 1;
			}
		}
		return -1;
	}
	
	private void attendMinions()
	{
		NpcInstance actor = getActor();
		MinionList minionList = actor.getMinionList();
		if(minionList.hasAliveMinions())
		{
			for(NpcInstance minion : minionList.getAliveMinions())
			{
				if(minion.isInCombat() || minion.isAfraid())
					continue;
				if(_moveRoute.isRunning())
				{
					minion.setRunning();
				}
				else
				{
					minion.setWalking();
				}
				minion.moveToRelative(getActor(), 400, 500, true);
			}
		}
	}
	
	private int getNearestMoveNodeIdx()
	{
		if(_moveRoute == null)
		{
			return -1;
		}
		NpcInstance actor = getActor();
		List nodes = _moveRoute.getNodes();
		int result = -1;
		long nearestNodeDistSq = Long.MAX_VALUE;
		for(int nodeIdx = 0;nodeIdx < nodes.size();++nodeIdx)
		{
			MoveNode moveNode = (MoveNode) nodes.get(nodeIdx);
			long moveNodeDistSq = actor.getXYZDeltaSq(moveNode);
			if(moveNodeDistSq >= nearestNodeDistSq)
				continue;
			nearestNodeDistSq = moveNodeDistSq;
			result = nodeIdx;
		}
		return result;
	}
	
	@Override
	public boolean isGlobalAI()
	{
		return false;
	}
}