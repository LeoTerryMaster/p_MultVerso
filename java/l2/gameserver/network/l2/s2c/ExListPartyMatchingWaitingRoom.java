package l2.gameserver.network.l2.s2c;

import l2.commons.lang.ArrayUtils;
import l2.gameserver.instancemanager.MatchingRoomManager;
import l2.gameserver.model.Player;
import l2.gameserver.model.entity.Reflection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExListPartyMatchingWaitingRoom extends L2GameServerPacket
{
	private final int _fullSize;
	private List<PartyMatchingWaitingInfo> _waitingList = Collections.emptyList();
	
	public ExListPartyMatchingWaitingRoom(Player searcher, int minLevel, int maxLevel, int page, int[] classes)
	{
		List<Player> temp = MatchingRoomManager.getInstance().getWaitingList(minLevel, maxLevel, classes);
		_fullSize = temp.size();
		_waitingList = new ArrayList<>(_fullSize);
		int i = 0;
		int firstNot = page * 64;
		int first = (page - 1) * 64;
		for(Player pc : temp)
		{
			if(i < first || i >= firstNot)
				continue;
			_waitingList.add(new PartyMatchingWaitingInfo(pc));
			++i;
		}
	}
	
	@Override
	protected void writeImpl()
	{
		writeEx(54);
		writeD(_fullSize);
		writeD(_waitingList.size());
		for(PartyMatchingWaitingInfo waiting_info : _waitingList)
		{
			writeS(waiting_info.name);
			writeD(waiting_info.classId);
			writeD(waiting_info.level);
		}
	}
	
	static class PartyMatchingWaitingInfo
	{
		public final int classId;
		public final int level;
		public final int currentInstance;
		public final String name;
		public final int[] instanceReuses;
		
		public PartyMatchingWaitingInfo(Player member)
		{
			name = member.getName();
			classId = member.getClassId().getId();
			level = member.getLevel();
			Reflection ref = member.getReflection();
			currentInstance = ref == null ? 0 : ref.getInstancedZoneId();
			instanceReuses = ArrayUtils.toArray(member.getInstanceReuses().keySet());
		}
	}
}