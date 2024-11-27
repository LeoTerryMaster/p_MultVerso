package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Player;
import l2.gameserver.model.quest.QuestState;

import java.util.ArrayList;
import java.util.List;

public class QuestList extends L2GameServerPacket
{
	private final List<int[]> questlist;
	
	public QuestList(Player player)
	{
		QuestState[] allQuestStates = player.getAllQuestsStates();
		questlist = new ArrayList<>(allQuestStates.length);
		for(QuestState quest : allQuestStates)
		{
			if(!quest.getQuest().isVisible() || !quest.isStarted())
				continue;
			questlist.add(new int[] {quest.getQuest().getQuestIntId(), quest.getInt("cond")});
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x80);
		writeH(questlist.size());
		for(int[] q : questlist)
		{
			writeD(q[0]);
			writeD(q[1]);
		}
	}
}