package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.quest.QuestNpcLogInfo;
import l2.gameserver.model.quest.QuestState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExQuestNpcLogList extends L2GameServerPacket
{
	private final int _questId;
	private List<int[]> _logList = Collections.emptyList();
	
	public ExQuestNpcLogList(QuestState state)
	{
		_questId = state.getQuest().getQuestIntId();
		int cond = state.getCond();
		List<QuestNpcLogInfo> vars = state.getQuest().getNpcLogList(cond);
		if(vars == null)
		{
			return;
		}
		_logList = new ArrayList<>(vars.size());
		for(QuestNpcLogInfo entry : vars)
		{
			int[] i = {entry.getNpcIds()[0] + 1000000, state.getInt(entry.getVarName())};
			_logList.add(i);
		}
	}
	
	@Override
	protected void writeImpl()
	{
		writeEx(197);
		writeD(_questId);
		writeC(_logList.size());
		for(int i = 0;i < _logList.size();++i)
		{
			int[] values = _logList.get(i);
			writeD(values[0]);
			writeC(0);
			writeD(values[1]);
		}
	}
}