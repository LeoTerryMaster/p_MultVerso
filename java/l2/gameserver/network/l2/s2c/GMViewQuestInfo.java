package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Player;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;

public class GMViewQuestInfo extends L2GameServerPacket
{
	private final Player _cha;
	
	public GMViewQuestInfo(Player cha)
	{
		_cha = cha;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(147);
		writeS(_cha.getName());
		Quest[] quests = _cha.getAllActiveQuests();
		if(quests.length == 0)
		{
			writeC(0);
			writeH(0);
			writeH(0);
			return;
		}
		writeH(quests.length);
		for(Quest q : quests)
		{
			writeD(q.getQuestIntId());
			QuestState qs = _cha.getQuestState(q.getName());
			writeD(qs == null ? 0 : qs.getInt("cond"));
		}
	}
}