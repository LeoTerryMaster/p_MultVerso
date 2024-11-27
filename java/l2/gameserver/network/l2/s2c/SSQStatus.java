package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Player;
import l2.gameserver.model.entity.SevenSigns;
import l2.gameserver.model.entity.SevenSignsFestival.SevenSignsFestival;
import l2.gameserver.templates.StatsSet;

public class SSQStatus extends L2GameServerPacket
{
	private final int _page;
	private final int period;
	private Player _player;
	
	public SSQStatus(Player player, int recordPage)
	{
		_player = player;
		_page = recordPage;
		period = SevenSigns.getInstance().getCurrentPeriod();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(245);
		writeC(_page);
		writeC(period);
		switch(_page)
		{
			case 1:
			{
				writeD(SevenSigns.getInstance().getCurrentCycle());
				switch(period)
				{
					case 0:
					{
						writeD(1183);
						break;
					}
					case 1:
					{
						writeD(1176);
						break;
					}
					case 2:
					{
						writeD(1184);
						break;
					}
					case 3:
					{
						writeD(1177);
					}
				}
				switch(period)
				{
					case 0:
					case 2:
					{
						writeD(1287);
						break;
					}
					case 1:
					case 3:
					{
						writeD(1286);
					}
				}
				writeC(SevenSigns.getInstance().getPlayerCabal(_player));
				writeC(SevenSigns.getInstance().getPlayerSeal(_player));
				writeD((int) SevenSigns.getInstance().getPlayerStoneContrib(_player));
				writeD((int) SevenSigns.getInstance().getPlayerAdenaCollect(_player));
				long dawnStoneScore = SevenSigns.getInstance().getCurrentStoneScore(2);
				long dawnFestivalScore = SevenSigns.getInstance().getCurrentFestivalScore(2);
				long dawnTotalScore = SevenSigns.getInstance().getCurrentScore(2);
				long duskStoneScore = SevenSigns.getInstance().getCurrentStoneScore(1);
				long duskFestivalScore = SevenSigns.getInstance().getCurrentFestivalScore(1);
				long duskTotalScore = SevenSigns.getInstance().getCurrentScore(1);
				long totalStoneScore = duskStoneScore + dawnStoneScore;
				totalStoneScore = totalStoneScore == 0 ? 1 : totalStoneScore;
				long duskStoneScoreProp = Math.round((double) duskStoneScore * 500.0 / (double) totalStoneScore);
				long dawnStoneScoreProp = Math.round((double) dawnStoneScore * 500.0 / (double) totalStoneScore);
				long totalOverallScore = duskTotalScore + dawnTotalScore;
				totalOverallScore = totalOverallScore == 0 ? 1 : totalOverallScore;
				long dawnPercent = Math.round((double) dawnTotalScore * 110.0 / (double) totalOverallScore);
				long duskPercent = Math.round((double) duskTotalScore * 110.0 / (double) totalOverallScore);
				writeD((int) duskStoneScoreProp);
				writeD((int) duskFestivalScore);
				writeD((int) duskTotalScore);
				writeC((int) duskPercent);
				writeD((int) dawnStoneScoreProp);
				writeD((int) dawnFestivalScore);
				writeD((int) dawnTotalScore);
				writeC((int) dawnPercent);
				break;
			}
			case 2:
			{
				writeH(1);
				writeC(5);
				for(int i = 0;i < 5;++i)
				{
					writeC(i + 1);
					writeD(SevenSignsFestival.FESTIVAL_LEVEL_SCORES[i]);
					long duskScore = SevenSignsFestival.getInstance().getHighestScore(1, i);
					long dawnScore = SevenSignsFestival.getInstance().getHighestScore(2, i);
					writeQ(duskScore);
					StatsSet highScoreData = SevenSignsFestival.getInstance().getHighestScoreData(1, i);
					String[] partyMembers;
					if(duskScore > 0)
					{
						partyMembers = highScoreData.getString("names").split(",");
						writeC(partyMembers.length);
						for(String partyMember : partyMembers)
						{
							writeS(partyMember);
						}
					}
					else
					{
						writeC(0);
					}
					writeQ(dawnScore);
					highScoreData = SevenSignsFestival.getInstance().getHighestScoreData(2, i);
					if(dawnScore > 0)
					{
						partyMembers = highScoreData.getString("names").split(",");
						writeC(partyMembers.length);
						for(String partyMember : partyMembers)
						{
							writeS(partyMember);
						}
						continue;
					}
					writeC(0);
				}
				break;
			}
			case 3:
			{
				writeC(10);
				writeC(35);
				writeC(3);
				int totalDawnProportion = 1;
				int totalDuskProportion = 1;
				int i;
				for(i = 1;i <= 3;++i)
				{
					totalDawnProportion += SevenSigns.getInstance().getSealProportion(i, 2);
					totalDuskProportion += SevenSigns.getInstance().getSealProportion(i, 1);
				}
				totalDawnProportion = Math.max(1, totalDawnProportion);
				totalDuskProportion = Math.max(1, totalDuskProportion);
				for(i = 1;i <= 3;++i)
				{
					int dawnProportion = SevenSigns.getInstance().getSealProportion(i, 2);
					int duskProportion = SevenSigns.getInstance().getSealProportion(i, 1);
					writeC(i);
					writeC(SevenSigns.getInstance().getSealOwner(i));
					writeC(duskProportion * 100 / totalDuskProportion);
					writeC(dawnProportion * 100 / totalDawnProportion);
				}
				break;
			}
			case 4:
			{
				int winningCabal = SevenSigns.getInstance().getCabalHighestScore();
				writeC(winningCabal);
				writeC(3);
				int dawnTotalPlayers = SevenSigns.getInstance().getTotalMembers(2);
				int duskTotalPlayers = SevenSigns.getInstance().getTotalMembers(1);
				for(int i = 1;i < 4;++i)
				{
					writeC(i);
					int dawnSealPlayers = SevenSigns.getInstance().getSealProportion(i, 2);
					int duskSealPlayers = SevenSigns.getInstance().getSealProportion(i, 1);
					int dawnProp = dawnTotalPlayers > 0 ? dawnSealPlayers * 100 / dawnTotalPlayers : 0;
					int duskProp = duskTotalPlayers > 0 ? duskSealPlayers * 100 / duskTotalPlayers : 0;
					int curSealOwner = SevenSigns.getInstance().getSealOwner(i);
					if(Math.max(dawnProp, duskProp) < 10)
					{
						writeC(0);
						if(curSealOwner == 0)
						{
							writeD(1292);
							continue;
						}
						writeD(1291);
						continue;
					}
					if(Math.max(dawnProp, duskProp) < 35)
					{
						writeC(curSealOwner);
						if(curSealOwner == 0)
						{
							writeD(1292);
							continue;
						}
						writeD(1289);
						continue;
					}
					if(dawnProp == duskProp)
					{
						writeC(0);
						writeD(1293);
						continue;
					}
					int sealWinning = dawnProp > duskProp ? 2 : 1;
					writeC(sealWinning);
					if(sealWinning == curSealOwner)
					{
						writeD(1289);
						continue;
					}
					writeD(1290);
				}
				break;
			}
		}
		_player = null;
	}
}