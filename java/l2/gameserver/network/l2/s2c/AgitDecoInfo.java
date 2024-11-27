package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.entity.residence.ClanHall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AgitDecoInfo extends L2GameServerPacket
{
	private static final Logger _log = LoggerFactory.getLogger(AgitDecoInfo.class);
	private static final int[] _buff = {0, 1, 1, 1, 2, 2, 2, 2, 2, 0, 0, 1, 1, 1, 2, 2, 2, 2, 2};
	private static final int[] _itCr8 = {0, 1, 2, 2};
	private final int _id;
	private final int hp_recovery;
	private final int mp_recovery;
	private final int exp_recovery;
	private final int teleport;
	private final int curtains;
	private final int itemCreate;
	private final int support;
	private final int platform;
	
	public AgitDecoInfo(ClanHall clanHall)
	{
		_id = clanHall.getId();
		hp_recovery = getHpRecovery(clanHall.isFunctionActive(3) ? clanHall.getFunction(3).getLevel() : 0);
		mp_recovery = getMpRecovery(clanHall.isFunctionActive(4) ? clanHall.getFunction(4).getLevel() : 0);
		exp_recovery = getExpRecovery(clanHall.isFunctionActive(5) ? clanHall.getFunction(5).getLevel() : 0);
		teleport = clanHall.isFunctionActive(1) ? clanHall.getFunction(1).getLevel() : 0;
		curtains = clanHall.isFunctionActive(7) ? clanHall.getFunction(7).getLevel() : 0;
		itemCreate = clanHall.isFunctionActive(2) ? _itCr8[clanHall.getFunction(2).getLevel()] : 0;
		support = clanHall.isFunctionActive(6) ? _buff[clanHall.getFunction(6).getLevel()] : 0;
		platform = clanHall.isFunctionActive(8) ? clanHall.getFunction(8).getLevel() : 0;
	}
	
	private static int getHpRecovery(int percent)
	{
		switch(percent)
		{
			case 0:
			{
				return 0;
			}
			case 20:
			case 40:
			case 80:
			case 120:
			case 140:
			{
				return 1;
			}
			case 160:
			case 180:
			case 200:
			case 220:
			case 240:
			case 260:
			case 280:
			case 300:
			{
				return 2;
			}
		}
		_log.warn("Unsupported percent " + percent + " in hp recovery");
		return 0;
	}
	
	private static int getMpRecovery(int percent)
	{
		switch(percent)
		{
			case 0:
			{
				return 0;
			}
			case 5:
			case 10:
			case 15:
			case 20:
			{
				return 1;
			}
			case 25:
			case 30:
			case 35:
			case 40:
			case 45:
			case 50:
			{
				return 2;
			}
		}
		_log.warn("Unsupported percent " + percent + " in mp recovery");
		return 0;
	}
	
	private static int getExpRecovery(int percent)
	{
		switch(percent)
		{
			case 0:
			{
				return 0;
			}
			case 5:
			case 10:
			case 15:
			case 20:
			{
				return 1;
			}
			case 25:
			case 30:
			case 35:
			case 40:
			case 45:
			case 50:
			{
				return 2;
			}
		}
		_log.warn("Unsupported percent " + percent + " in exp recovery");
		return 0;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(247);
		writeD(_id);
		writeC(hp_recovery);
		writeC(mp_recovery);
		writeC(mp_recovery);
		writeC(exp_recovery);
		writeC(teleport);
		writeC(0);
		writeC(curtains);
		writeC(itemCreate);
		writeC(support);
		writeC(support);
		writeC(platform);
		writeC(itemCreate);
		writeD(0);
		writeD(0);
	}
}