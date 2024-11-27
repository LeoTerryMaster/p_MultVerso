package l2.gameserver.model.entity;

import l2.gameserver.model.Player;
import l2.gameserver.model.World;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Mentoring implements Comparable<Mentoring>
{
	private final int _mentorObjId;
	private final Set<Integer> _menteesObjId = new HashSet<>();
	
	public Mentoring(int mentorObjId, int[] menteesObjId)
	{
		for(int menteeObjId : menteesObjId)
		{
			_menteesObjId.add(menteeObjId);
		}
		_mentorObjId = mentorObjId;
	}
	
	public Mentoring(int mentorObjId, Collection<Integer> menteesObjId)
	{
		for(Integer menteeObjId : menteesObjId)
		{
			if(menteeObjId == null)
				continue;
			addMenteeObjId(menteeObjId);
		}
		_mentorObjId = mentorObjId;
	}
	
	public int getMenteeCount()
	{
		return _menteesObjId.size();
	}
	
	public Collection<Integer> getMenteesIds()
	{
		return _menteesObjId;
	}
	
	public int getMentorObjId()
	{
		return _mentorObjId;
	}
	
	protected void addMenteeObjId(int objId)
	{
		_menteesObjId.add(objId);
	}
	
	protected void removeMenteeObjId(int objId)
	{
		Iterator<Integer> iter = _menteesObjId.iterator();
		while(iter.hasNext())
		{
			if(iter.next() != objId)
				continue;
			iter.remove();
		}
	}
	
	public boolean isMineMentor(Player player)
	{
		return player != null && player.getObjectId() == _mentorObjId;
	}
	
	public boolean isMineMentee(Player player)
	{
		if(player == null)
		{
			return false;
		}
		int poid = player.getObjectId();
		for(Integer objId : _menteesObjId)
		{
			if(poid != objId)
				continue;
			return true;
		}
		return false;
	}
	
	public Player getMentor()
	{
		return World.getPlayer(_mentorObjId);
	}
	
	public Collection<Player> getMentees()
	{
		ArrayList<Player> ret = new ArrayList<>(3);
		for(Integer objId : _menteesObjId)
		{
			Player p = World.getPlayer(objId);
			if(p == null)
				continue;
			ret.add(p);
		}
		return ret;
	}
	
	public boolean isMentee(Player player)
	{
		if(player == null)
		{
			return false;
		}
		return isMentee(player.getObjectId());
	}
	
	public boolean isMentee(int objid)
	{
		for(Integer oid : _menteesObjId)
		{
			if(oid == null || oid != objid)
				continue;
			return true;
		}
		return false;
	}
	
	public boolean isMentor(Player player)
	{
		return player != null && player.getObjectId() == _mentorObjId;
	}
	
	public boolean isMentor(int objid)
	{
		return objid == _mentorObjId;
	}
	
	public boolean isMenteeBuffApplicable(Player mentee)
	{
		if(mentee == null || mentee.getMentoring() != this)
		{
			return false;
		}
		Player mentor = getMentor();
		return mentor != null && mentor.isOnline() && !mentor.isLogoutStarted();
	}
	
	public boolean isMentorBuffApplicable(Player mentor)
	{
		if(mentor == null || mentor.getMentoring() != this)
		{
			return false;
		}
		for(Integer menteeObjId : _menteesObjId)
		{
			Player mentee = World.getPlayer(menteeObjId);
			if(mentee == null || !mentee.isOnline() || mentor.isLogoutStarted())
				continue;
			return true;
		}
		return false;
	}
	
	@Override
	public int compareTo(Mentoring o)
	{
		if(o == null)
		{
			return 1;
		}
		return o._mentorObjId - _mentorObjId;
	}
	
	public void onEnter(Player player)
	{
		MentorController.getInstance().onPlayerEnter(this, player);
	}
	
	public void onExit(Player player)
	{
		MentorController.getInstance().onPlayerExit(this, player);
	}
	
	public void makeReward(Player player, boolean newSub)
	{
		if(newSub && player.getClassId().getLevel() == 4)
		{
			MentorController.getInstance().onMenteeNewSubclass(player, this);
		}
		if(MentorController.isCanBeMentor(player))
		{
			MentorController.getInstance().onMentoringComplete(player, this);
		}
	}
}