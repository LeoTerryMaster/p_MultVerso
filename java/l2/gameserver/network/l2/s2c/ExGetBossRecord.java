package l2.gameserver.network.l2.s2c;

import java.util.List;

public class ExGetBossRecord extends L2GameServerPacket
{
	private final List<BossRecordInfo> _bossRecordInfo;
	private final int _ranking;
	private final int _totalPoints;
	
	public ExGetBossRecord(int ranking, int totalScore, List<BossRecordInfo> bossRecordInfo)
	{
		_ranking = ranking;
		_totalPoints = totalScore;
		_bossRecordInfo = bossRecordInfo;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeEx(51);
		writeD(_ranking);
		writeD(_totalPoints);
		writeD(_bossRecordInfo.size());
		for(BossRecordInfo w : _bossRecordInfo)
		{
			writeD(w._bossId);
			writeD(w._points);
			writeD(w._unk1);
		}
	}
	
	public static class BossRecordInfo
	{
		public int _bossId;
		public int _points;
		public int _unk1;
		
		public BossRecordInfo(int bossId, int points, int unk1)
		{
			_bossId = bossId;
			_points = points;
			_unk1 = unk1;
		}
	}
	
}

