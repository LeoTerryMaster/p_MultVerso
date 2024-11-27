package l2.gameserver.network.l2.c2s;

import l2.gameserver.instancemanager.RaidBossSpawnManager;
import l2.gameserver.model.Player;
import l2.gameserver.network.l2.s2c.ExGetBossRecord;

import java.util.ArrayList;
import java.util.Map;

public class RequestGetBossRecord extends L2GameClientPacket
{
	private int _bossID;
	
	@Override
	protected void readImpl()
	{
		_bossID = readD();
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		ArrayList<ExGetBossRecord.BossRecordInfo> list = new ArrayList<>();
		Map<Integer, Integer> points = RaidBossSpawnManager.getInstance().getPointsForOwnerId(activeChar.getObjectId());
		int ranking = 0;
		int totalPoints = 0;
		if(points != null && !points.isEmpty())
		{
			block4:
			for(Map.Entry<Integer, Integer> e : points.entrySet())
			{
				switch(e.getKey())
				{
					case -1:
					{
						ranking = e.getValue();
						continue block4;
					}
					case 0:
					{
						totalPoints = e.getValue();
						continue block4;
					}
				}
				list.add(new ExGetBossRecord.BossRecordInfo(e.getKey(), e.getValue(), 0));
			}
		}
		activeChar.sendPacket(new ExGetBossRecord(ranking, totalPoints, list));
	}
}