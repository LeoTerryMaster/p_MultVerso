package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Party;
import l2.gameserver.model.Player;

import java.util.ArrayList;
import java.util.List;

public class ExMPCCShowPartyMemberInfo extends L2GameServerPacket
{
	private final List<PartyMemberInfo> members = new ArrayList<>();
	
	public ExMPCCShowPartyMemberInfo(Party party)
	{
		for(Player _member : party.getPartyMembers())
		{
			members.add(new PartyMemberInfo(_member.getName(), _member.getObjectId(), _member.getClassId().getId()));
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeEx(74);
		writeD(members.size());
		for(PartyMemberInfo member : members)
		{
			writeS(member.name);
			writeD(member.object_id);
			writeD(member.class_id);
		}
	}
	
	static class PartyMemberInfo
	{
		public String name;
		public int object_id;
		public int class_id;
		
		public PartyMemberInfo(String _name, int _object_id, int _class_id)
		{
			name = _name;
			object_id = _object_id;
			class_id = _class_id;
		}
	}
}